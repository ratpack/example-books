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
            def id = pathTokens.asLong("id")

            byMethod {
                post {
                    if (id != null) {
                        clientError 404
                    } else {
                        def input = parse jsonNode()
                        bookService.insert(input.get("title").asText(), input.get("content").asText())
                        .flatMap { Long newId ->
                            bookService.find(newId)
                            .single()
                        }
                        .subscribe { Book createdBook ->
                            render json(createdBook)
                        }
                    }
                }
                get {
                    bookService.find(id)
                    .single()
                    .subscribe { Book book ->
                        if (book == null) {
                            clientError 404
                        } else {
                            render book
                        }
                    }
                }
                put {
                    def input = parse jsonNode()
                    bookService.update(id, input.get("title").asText(), input.get("content").asText())
                    .flatMap {
                        bookService.find(id)
                        .single()
                    }
                    .subscribe { Book book ->
                        render json(book)
                    }
                }
                delete {
                    bookService.delete(id)
                    .subscribe {
                        response.send()
                    }
                }
            }
        }
    }
}
