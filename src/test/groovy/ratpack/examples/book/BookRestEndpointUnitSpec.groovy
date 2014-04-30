package ratpack.examples.book

import ratpack.example.books.Book
import ratpack.example.books.BookRestEndpoint
import ratpack.example.books.BookService
import ratpack.path.PathBinding
import ratpack.path.PathTokens
import spock.lang.Specification

import static ratpack.groovy.test.GroovyUnitTest.handle

class BookRestEndpointUnitSpec extends Specification {

    def "will render book"() {
        given:
        def book = new Book("1932394842", 10, 22.22, "Groovy in Action", "Dierk Koenig", "Manning Publications")

        rx.Observable<Book> findObservable = rx.Observable.just(book)

        def bookServices = Mock(BookService)
        bookServices.find("1932394842") >> findObservable

        def pathTokens = Mock(PathTokens)
        pathTokens.get("isbn") >> "1932394842"

        def pathBinding = Mock(PathBinding)
        pathBinding.getTokens() >> pathTokens

        when:
        def result = handle(new BookRestEndpoint(bookServices)) {
            method "get"
            header "Accept", "application/json"
            registry {
                add pathBinding
            }
        }

        then:
        with(result) {
            rendered(Book) == book
        }
    }

    def "will return 404 if book not found"() {
        given:
        rx.Observable<Book> findObservable = rx.Observable.just(null)

        def bookServices = Mock(BookService)
        bookServices.find("1932394842") >> findObservable

        def pathTokens = Mock(PathTokens)
        pathTokens.get("isbn") >> "1932394842"

        def pathBinding = Mock(PathBinding)
        pathBinding.getTokens() >> pathTokens

        when:
        def result = handle(new BookRestEndpoint(bookServices)) {
            method "get"
            header "Accept", "application/json"
            registry {
                add pathBinding
            }
        }

        then:
        with(result) {
            clientError == 404
        }
    }

    def "will delete book"() {
        given:
        rx.Observable<Book> deleteObservable = rx.Observable.just(null)

        def bookServices = Mock(BookService)
        1 * bookServices.delete("1932394842") >> deleteObservable

       def pathTokens = Mock(PathTokens)
        pathTokens.get("isbn") >> "1932394842"

        def pathBinding = Mock(PathBinding)
        pathBinding.getTokens() >> pathTokens

        when:
        def result = handle(new BookRestEndpoint(bookServices)) {
            method "delete"
            header "Accept", "application/json"
            registry {
                add pathBinding
            }
        }

        then:
        with(result) {
            bodyText == ""
            status.code == 200
        }
    }

}
