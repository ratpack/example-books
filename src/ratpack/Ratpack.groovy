import ratpack.example.books.Book
import ratpack.example.books.BookModule
import ratpack.example.books.BookRestEndpoint
import ratpack.example.books.BookService
import ratpack.groovy.sql.SqlModule
import ratpack.h2.H2Module
import ratpack.jackson.JacksonModule
import ratpack.remote.RemoteControlModule

import static ratpack.form.Forms.form
import static ratpack.groovy.Groovy.groovyTemplate
import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json

ratpack {
    modules {
        register new H2Module()
        register new SqlModule()
        register new JacksonModule()
        register new BookModule()
        register new RemoteControlModule()

        init { BookService bookService ->
            bookService.createTable()
        }
    }

    handlers { BookService bookService ->

        get {
            background {
                bookService.list()
            } then {
                render groovyTemplate("listing.html", title: "Books", books: it, msg: request.queryParams.msg ?: "")
            }
        }

        handler("create") {
            byMethod {
                get {
                    render groovyTemplate("create.html", title: "Create Book")
                }
                post {
                    def form = parse form()
                    background {
                        bookService.insert(form.title, form.content)
                    } then {
                        redirect "/?msg=Book+$it+created"
                    }
                }
            }
        }

        handler("update/:id") {
            def id = pathTokens.asLong("id")
            background {
                bookService.find(id)
            } then { Book book ->
                if (book == null) {
                    clientError(404)
                } else {
                    byMethod {
                        get {
                            render groovyTemplate("update.html", title: "Update Book", book: book)
                        }
                        post {
                            def form = parse form()
                            background {
                                bookService.update(id, form.title, form.content)
                            } then {
                                redirect "/?msg=Book+$id+updated"
                            }
                        }
                    }
                }
            }
        }

        post("delete/:id") {
            def id = pathTokens.asLong("id")
            background {
                bookService.find(id)
            } then { Book book ->
                if (book == null) {
                    clientError(404)
                } else {
                    background {
                        bookService.delete(id)
                    } then {
                        redirect "/?msg=Book+$id+deleted"
                    }
                }
            }
        }

        prefix("api") {
            get("books") {
                background {
                    bookService.list()
                } then { List<Book> books ->
                    render json(books)
                }
            }
            handler("book/:id?", registry.get(BookRestEndpoint))
        }

        assets "public"
    }

}