package ratpack.example.books

import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler

import javax.inject.Inject

import static ratpack.jackson.Jackson.json
import static ratpack.jackson.Jackson.jsonNode

class BookRestEndpoint extends GroovyHandler {

    private final BookService bookService

    @Inject
    BookRestEndpoint(BookService bookService) {
        this.bookService = bookService
    }

    @Override
    protected void handle(GroovyContext context) {
        context.with {
            def isbn = pathTokens["isbn"]

            byMethod {
                post {
                    if (isbn != null) {
                        clientError 404
                    } else {
                        def input = parse jsonNode()
                        bookService.insert(
                                input.get("isbn").asText(),
                                input.get("quantity").asLong(),
                                input.get("price").asDouble()
                        ) flatMap {
                            bookService.find(it).single()
                        } subscribe { Book createdBook ->
                            render json(createdBook)
                        }
                    }
                }
                get {
                    bookService.find(isbn).single().subscribe { Book book ->
                        if (book == null) {
                            clientError 404
                        } else {
                            render book
                        }
                    }
                }
                put {
                    def input = parse jsonNode()
                    bookService.update(
                            isbn,
                            input.get("quantity").asLong(),
                            input.get("price").asDouble()
                    ) flatMap {
                        bookService.find(isbn).single()
                    } subscribe { Book book ->
                        render json(book)
                    }
                }
                delete {
                    bookService.delete(isbn).subscribe {
                        response.send()
                    }
                }
            }
        }
    }
}
