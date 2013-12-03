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
                    def id = bookService.insert(form.title, form.content)
                    redirect "/?msg=Book+$id+created"
                }
            }
        }

        handler("update/:id") {
            def id = pathTokens.asLong("id")
            def book = bookService.find(id)
            if (book == null) {
                clientError(404)
            } else {
                byMethod {
                    get {
                        render groovyTemplate("update.html", title: "Update Book", book: book)
                    }
                    post {
                        def form = parse form()
                        bookService.update(id, form.title, form.content)
                        redirect "/?msg=Book+$id+updated"
                    }
                }
            }
        }

        post("delete/:id") {
            def id = pathTokens.asLong("id")
            def book = bookService.find(id)
            if (book == null) {
                clientError(404)
            } else {
                bookService.delete(id)
                redirect "/?msg=Book+$id+deleted"
            }
        }

        prefix("api") {
            get("books") {
                render json(bookService.list())
            }
            handler("book/:id?") {
                def id = pathTokens.asLong("id")
                def book = id ? bookService.find(id) : null

                if (!request.method.post && (id == null || book == null)) {
                    return clientError(404)
                }

                byMethod {
                    post {
                        if (id != null) {
                            clientError 404
                        } else {
                            def input = parse jsonNode()
                            def newId = bookService.insert(input.get("title").asText(), input.get("content").asText())
                            render json(bookService.find(newId))
                        }
                    }
                    get {
                        render json(book)
                    }
                    put {
                        def input = parse jsonNode()
                        bookService.update(id, input.get("title").asText(), input.get("content").asText())
                        render json(bookService.find(id))
                    }
                    delete {
                        bookService.delete(id)
                        response.send()
                    }
                }
            }
        }

        assets "public"
    }

}