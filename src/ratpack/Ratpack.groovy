import ratpack.example.books.BookService
import ratpack.groovy.sql.SqlModule
import ratpack.h2.H2Module

import static ratpack.form.Forms.form
import static ratpack.groovy.Groovy.groovyTemplate
import static ratpack.groovy.Groovy.ratpack

ratpack {
    modules {
        register new H2Module()
        register new SqlModule()
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

        assets "public"
    }

}