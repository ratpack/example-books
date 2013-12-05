import com.google.inject.Module
import ratpack.example.books.Book
import ratpack.example.books.BookModule
import ratpack.example.books.BookService
import ratpack.groovy.sql.SqlModule
import ratpack.h2.H2Module
import ratpack.jackson.JacksonModule

import static ratpack.form.Forms.form
import static ratpack.groovy.Groovy.groovyTemplate
import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json
import static ratpack.jackson.Jackson.jsonNode

ratpack {
    modules {
        register new H2Module()
        register new SqlModule()
        register new JacksonModule()
        register new BookModule()

        try {
            Module remoteControlModule = getClass().classLoader.loadClass("ratpack.remote.RemoteControlModule").newInstance()
            register remoteControlModule
        } catch (ignore) {
        }

    }

    handlers { BookService bookService ->

        bookService.createTable()

        get {
            render groovyTemplate("listing.html", title: "Books", books: bookService.list(), msg: request.queryParams.msg ?: "")
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
            handler("book/:id?") {
                def id = pathTokens.asLong("id")
                background {
                    id ? bookService.find(id) : null


                } then { Book book ->
                    if (!request.method.post && (id == null || book == null)) {
                        return clientError(404)
                    }

                    byMethod {
                        post {
                            if (id != null) {
                                clientError 404
                            } else {
                                def input = parse jsonNode()
                                background {
                                    def newId = bookService.insert(input.get("title").asText(), input.get("content").asText())
                                    bookService.find(newId)
                                } then { Book createdBook ->
                                    render json(createdBook)
                                }
                            }
                        }
                        get {
                            render book
                        }
                        put {
                            def input = parse jsonNode()
                            background {
                                bookService.update(id, input.get("title").asText(), input.get("content").asText())
                            } then {
                                render json(bookService.find(it))
                            }
                        }
                        delete {
                            background {
                                bookService.delete(id)
                            } then {
                                response.send()
                            }
                        }
                    }
                }
            }
        }

        assets "public"
    }

}