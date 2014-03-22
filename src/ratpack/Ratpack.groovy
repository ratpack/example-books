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
import ratpack.jackson.JacksonModule
import ratpack.pac4j.Pac4jModule
import ratpack.remote.RemoteControlModule
import ratpack.rx.RxModule
import ratpack.session.SessionModule
import ratpack.session.store.MapSessionsModule
import ratpack.session.store.SessionStorage

import static ratpack.groovy.Groovy.groovyTemplate
import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json
import static ratpack.pac4j.internal.SessionConstants.USER_PROFILE

ratpack {
    modules {
        bind DatabaseHealthCheck
        register new CodaHaleMetricsModule().jvmMetrics().jmx().websocket().healthChecks()
        register new HikariModule([URL: "jdbc:h2:mem:dev;INIT=CREATE SCHEMA IF NOT EXISTS DEV"], "org.h2.jdbcx.JdbcDataSource")
        register new SqlModule()
        register new JacksonModule()
        register new BookModule()
        register new RemoteControlModule()
        register new RxModule()
        register new SessionModule()
        register new MapSessionsModule(10, 5)
        register new Pac4jModule<>(new FormClient("/login", new SimpleTestUsernamePasswordAuthenticator()), new AuthPathAuthorizer())

        init { BookService bookService ->
            bookService.createTable()
        }
    }

    handlers { BookService bookService ->

        get {
           bookService.all()
           .toList()
           .subscribe { List<Book> books ->
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
                    def form = parse(Form.class)
                    bookService.insert(form.title, form.content)
                    .single()
                    .subscribe { Long id ->
                        redirect "/?msg=Book+$id+created"
                    }
                }
            }
        }

        handler("update/:id") {
            def id = pathTokens.asLong("id")

            bookService.find(id)
            .single()
            .subscribe { Book book ->
                if (book == null) {
                    clientError(404)
                } else {
                    byMethod {
                        get {
                            render groovyTemplate("update.html", title: "Update Book", book: book)
                        }
                        post {
                            def form = parse(Form.class)
                            bookService.update(id, form.title, form.content)
                            .subscribe {
                                redirect "/?msg=Book+$id+updated"
                            }
                        }
                    }
                }
            }
        }

        post("delete/:id") {
            def id = pathTokens.asLong("id")
            bookService.delete(id)
            .subscribe {
                redirect "/?msg=Book+$id+deleted"
            }
        }

        prefix("api") {
            get("books") {
                bookService.all()
                .toList()
                .subscribe { List<Book> books ->
                    render json(books)
                }
            }
            handler("book/:id?", registry.get(BookRestEndpoint))
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