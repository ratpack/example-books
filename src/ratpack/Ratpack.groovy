import org.pac4j.core.profile.UserProfile
import org.pac4j.http.client.FormClient
import org.pac4j.http.credentials.SimpleTestUsernamePasswordAuthenticator
import ratpack.codahale.metrics.CodaHaleMetricsModule
import ratpack.codahale.metrics.HealthCheckHandler
import ratpack.codahale.metrics.MetricsWebsocketBroadcastHandler
import ratpack.example.books.*
import ratpack.form.Form
import ratpack.groovy.sql.SqlModule
import ratpack.hikari.HikariModule
import ratpack.hystrix.HystrixRatpack
import ratpack.jackson.JacksonModule
import ratpack.pac4j.Pac4jModule
import ratpack.remote.RemoteControlModule
import ratpack.rx.RxRatpack
import ratpack.session.SessionModule
import ratpack.session.store.MapSessionsModule
import ratpack.session.store.SessionStorage

import static ratpack.groovy.Groovy.groovyTemplate
import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json
import static ratpack.pac4j.internal.SessionConstants.USER_PROFILE

ratpack {
    bindings {
        bind DatabaseHealthCheck
        add new CodaHaleMetricsModule().jvmMetrics().jmx().websocket().healthChecks()
        add new HikariModule([URL: "jdbc:h2:mem:dev;INIT=CREATE SCHEMA IF NOT EXISTS DEV"], "org.h2.jdbcx.JdbcDataSource")
        add new SqlModule()
        add new JacksonModule()
        add new BookModule()
        add new RemoteControlModule()
        add new SessionModule()
        add new MapSessionsModule(10, 5)
        add new Pac4jModule<>(new FormClient("/login", new SimpleTestUsernamePasswordAuthenticator()), new AuthPathAuthorizer())

        init { BookService bookService ->
            RxRatpack.initialize()
            HystrixRatpack.initialize()
            bookService.createTable()
        }
    }

    handlers { BookService bookService ->

        get {
           bookService.all().toList().subscribe { List<Book> books ->
                SessionStorage sessionStorage = request.get(SessionStorage)
                UserProfile profile = sessionStorage.get(USER_PROFILE)
                def username = profile?.getAttribute("username")

                render groovyTemplate("listing.html",
                        username: username ?: "",
                        title: "Books",
                        books: books,
                        msg: request.queryParams.msg ?: "")
           }
        }

        handler("create") {
            byMethod {
                get {
                    render groovyTemplate("create.html", title: "Create Book")
                }
                post {
                    Form form = parse(Form)
                    bookService.insert(
                            form.isbn,
                            form.get("quantity").asType(Long),
                            form.get("price").asType(BigDecimal)
                    ).single().subscribe { String isbn ->
                        redirect "/?msg=Book+$isbn+created"
                    }
                }
            }
        }

        handler("update/:isbn") {
            def isbn = pathTokens["isbn"]

            bookService.find(isbn).single().subscribe { Book book ->
                if (book == null) {
                    clientError(404)
                } else {
                    byMethod {
                        get {
                            render groovyTemplate("update.html", title: "Update Book", book: book)
                        }
                        post {
                            Form form = parse(Form)
                            bookService.update(
                                    isbn,
                                    form.get("quantity").asType(Long),
                                    form.get("price").asType(BigDecimal)
                            ) subscribe {
                                redirect "/?msg=Book+$isbn+updated"
                            }
                        }
                    }
                }
            }
        }

        post("delete/:isbn") {
            def isbn = pathTokens["isbn"]
            bookService.delete(isbn).subscribe {
                redirect "/?msg=Book+$isbn+deleted"
            }
        }

        prefix("api") {
            get("books") {
                bookService.all().toList().subscribe { List<Book> books ->
                    render json(books)
                }
            }

            handler("book/:isbn?", registry.get(BookRestEndpoint))
        }

        prefix("admin") {
            get("health-check/:name?", new HealthCheckHandler())
            get("metrics-report", new MetricsWebsocketBroadcastHandler())

            get("metrics") {
                render groovyTemplate("metrics.html", title: "Metrics")
            }
        }

        handler("login") {
            render groovyTemplate("login.html", title: "Login", error: request.queryParams.error ?: "")
        }

        assets "public"
    }

}