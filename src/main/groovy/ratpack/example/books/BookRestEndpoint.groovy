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
            background {
                id != null ? bookService.find(id) : null
            } then { Book book ->
                if (!request.method.post && (id == null || book == null)) {
                    clientError(404)
                    return null
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
}
