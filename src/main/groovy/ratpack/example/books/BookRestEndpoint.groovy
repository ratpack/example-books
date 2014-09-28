package ratpack.example.books

import ratpack.func.Action
import ratpack.groovy.Groovy
import ratpack.handling.Chain

import javax.inject.Inject

import static ratpack.jackson.Jackson.json
import static ratpack.jackson.Jackson.jsonNode

class BookRestEndpoint implements Action<Chain> {

    private final BookService bookService

    @Inject
    BookRestEndpoint(BookService bookService) {
        this.bookService = bookService
    }

    @Override
    void execute(Chain chain) throws Exception {
        Groovy.chain(chain) {
            handler(":isbn") {
                def isbn = pathTokens["isbn"]

                byMethod {
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

            handler {
                byMethod {
                    get {
                        bookService.all().toList().subscribe { List<Book> books ->
                            render json(books)
                        }
                    }
                    post {
                        def input = parse jsonNode()
                        bookService.insert(
                                input.get("isbn").asText(),
                                input.get("quantity").asLong(),
                                input.get("price").asDouble()
                        ).single().flatMap {
                            bookService.find(it).single()
                        } subscribe { Book createdBook ->
                            render json(createdBook)
                        }
                    }
                }
            }
        }
    }
}
