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
        def book = new Book(0, "foo", "bar")

        rx.Observable<Book> findObservable = rx.Observable.just(book)

        def bookServices = Mock(BookService)
        bookServices.find(0) >> findObservable

        def pathTokens = Mock(PathTokens)
        pathTokens.asLong("id") >> 0

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
        bookServices.find(0) >> findObservable

        def pathTokens = Mock(PathTokens)
        pathTokens.asLong("id") >> 0

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
        1 * bookServices.delete(0) >> deleteObservable

       def pathTokens = Mock(PathTokens)
        pathTokens.asLong("id") >> 0

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
