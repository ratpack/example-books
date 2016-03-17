import com.zaxxer.hikari.HikariConfig
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.dropwizard.metrics.DropwizardMetricsConfig
import ratpack.dropwizard.metrics.DropwizardMetricsModule
import ratpack.dropwizard.metrics.MetricsWebsocketBroadcastHandler
import ratpack.error.ServerErrorHandler
import ratpack.example.books.*
import ratpack.form.Form
import ratpack.groovy.sql.SqlModule
import ratpack.groovy.template.MarkupTemplateModule
import ratpack.handling.RequestLogger
import ratpack.health.HealthCheckHandler
import ratpack.hikari.HikariModule
import ratpack.hystrix.HystrixMetricsEventStreamHandler
import ratpack.hystrix.HystrixModule
import ratpack.pac4j.RatpackPac4j
import ratpack.rx.RxRatpack
import ratpack.server.Service
import ratpack.server.StartEvent
import ratpack.session.SessionModule

import static ratpack.groovy.Groovy.groovyMarkupTemplate
import static ratpack.groovy.Groovy.ratpack

final Logger logger = LoggerFactory.getLogger(ratpack.class);

ratpack {
    serverConfig {
        props("application.properties")
        sysProps("eb.")
        env("EB_")
        require("/isbndb", IsbndbConfig)
        require("/metrics", DropwizardMetricsConfig)
    }
    bindings {
        moduleConfig(DropwizardMetricsModule, DropwizardMetricsConfig)
        bind DatabaseHealthCheck
        module HikariModule, { HikariConfig c ->
            c.addDataSourceProperty("URL", "jdbc:h2:mem:dev;INIT=CREATE SCHEMA IF NOT EXISTS DEV")
            c.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource")
        }
        module SqlModule
        module BookModule
        module SessionModule
        module MarkupTemplateModule
        module new HystrixModule().sse()
        bind MarkupTemplateRenderableDecorator

        bindInstance Service, new Service() {
            @Override
            void onStart(StartEvent event) throws Exception {
                logger.info "Initializing RX"
                RxRatpack.initialize()
                event.registry.get(BookService).createTable()
            }
        }

        bind ServerErrorHandler, ErrorHandler
    }

    handlers { BookService bookService ->
        all RequestLogger.ncsa(logger) // log all requests

        get {
            bookService.all().
                toList().
                subscribe { List<Book> books ->
                    def isbndbApikey = context.get(IsbndbConfig).apikey

                    render groovyMarkupTemplate("listing.gtpl",
                        isbndbApikey: isbndbApikey,
                        title: "Books",
                        books: books,
                        msg: request.queryParams.msg ?: "")
                }
        }

        path("create") {
            byMethod {
                get {
                    render groovyMarkupTemplate("create.gtpl",
                        title: "Create Book",
                        isbn: '',
                        quantity: '',
                        price: '',
                        method: 'post',
                        action: '',
                        buttonText: 'Create'
                    )
                }
                post {
                    parse(Form).
                        observe().
                        flatMap { Form form ->
                            bookService.insert(
                                form.isbn,
                                form.get("quantity").asType(Long),
                                form.get("price").asType(BigDecimal)
                            )
                        }.
                        single().
                        subscribe() { String isbn ->
                            redirect "/?msg=Book+$isbn+created"
                        }
                }
            }
        }

        path("update/:isbn") {
            def isbn = pathTokens["isbn"]

            bookService.find(isbn).
                single().
                subscribe { Book book ->
                    if (book == null) {
                        clientError(404)
                    } else {
                        byMethod {
                            get {
                                render groovyMarkupTemplate("update.gtpl",
                                    title: "Update Book",
                                    method: 'post',
                                    action: '',
                                    buttonText: 'Update',
                                    isbn: book.isbn,
                                    bookTitle: book.title,
                                    author: book.author,
                                    publisher: book.publisher,
                                    quantity: book.quantity,
                                    price: book.price)
                            }
                            post {
                                parse(Form).
                                    observe().
                                    flatMap { Form form ->
                                        bookService.update(
                                            isbn,
                                            form.get("quantity").asType(Long),
                                            form.get("price").asType(BigDecimal)
                                        )
                                    }.
                                    subscribe {
                                        redirect "/?msg=Book+$isbn+updated"
                                    }
                            }
                        }
                    }
                }
        }

        post("delete/:isbn") {
            def isbn = pathTokens["isbn"]
            bookService.delete(isbn).
                subscribe {
                    redirect "/?msg=Book+$isbn+deleted"
                }
        }

        prefix("api/book") {
            all chain(registry.get(BookRestEndpoint))
        }

        def pac4jCallbackPath = "pac4j-callback"
        all(RatpackPac4j.authenticator(
            pac4jCallbackPath,
            new FormClient("/login", new SimpleTestUsernamePasswordAuthenticator())))

        prefix("admin") {
            all(RatpackPac4j.requireAuth(FormClient.class))

            get("health-check/:name?", new HealthCheckHandler())
            get("metrics-report", new MetricsWebsocketBroadcastHandler())

            get("metrics") {
                render groovyMarkupTemplate("metrics.gtpl", title: "Metrics")
            }
        }
        get("hystrix.stream", new HystrixMetricsEventStreamHandler())

        get("login") { ctx ->
            render groovyMarkupTemplate("login.gtpl",
                title: "Login",
                action: "/$pac4jCallbackPath",
                method: 'get',
                buttonText: 'Login',
                error: request.queryParams.error ?: "")
        }

        get("logout") { ctx ->
            RatpackPac4j.logout(ctx).then {
                redirect("/")
            }
        }

        files { it.dir("public") }
    }

}
