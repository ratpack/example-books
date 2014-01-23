package ratpack.examples.book

import ratpack.example.books.Book
import ratpack.example.books.BookRestEndpoint
import ratpack.example.books.BookService
import ratpack.path.PathBinding
import ratpack.path.PathTokens
import spock.lang.Specification

import static ratpack.groovy.test.GroovyUnitTest.invoke

class BookRestEndpointUnitSpec extends Specification {

    def "will render book"() {
        given:
        def book = new Book(0, "foo", "bar")

        rx.Observable<Book> findObservable = rx.Observable.create { rx.Observer observer ->
            observer.onNext(book)
            observer.onCompleted()
        }

        def bookServices = Mock(BookService)
        bookServices.find(0) >> findObservable

        def pathTokens = Mock(PathTokens)
        pathTokens.asLong("id") >> 0

        def pathBinding = Mock(PathBinding)
        pathBinding.getTokens() >> pathTokens

        when:
        def invocation = invoke(new BookRestEndpoint(bookServices)) {
            method "get"
            header "Accept", "application/json"
            register pathBinding
        }

        then:
        with(invocation) {
            rendered(Book) == book
        }
    }

    def "will return 404 if book not found"() {
        given:
        rx.Observable<Book> findObservable = rx.Observable.create { rx.Observer observer ->
            observer.onNext(null)
            observer.onCompleted()
        }

        def bookServices = Mock(BookService)
        bookServices.find(0) >> findObservable

        def pathTokens = Mock(PathTokens)
        pathTokens.asLong("id") >> 0

        def pathBinding = Mock(PathBinding)
        pathBinding.getTokens() >> pathTokens

        when:
        def invocation = invoke(new BookRestEndpoint(bookServices)) {
            method "get"
            header "Accept", "application/json"
            register pathBinding
        }

        then:
        with(invocation) {
            clientError == 404
        }
    }

    def "will delete book"() {
        given:
        rx.Observable<Book> deleteObservable = rx.Observable.create { rx.Observer observer ->
            observer.onNext(null)
            observer.onCompleted()
        }

        def bookServices = Mock(BookService)
        1 * bookServices.delete(0) >> deleteObservable

       def pathTokens = Mock(PathTokens)
        pathTokens.asLong("id") >> 0

        def pathBinding = Mock(PathBinding)
        pathBinding.getTokens() >> pathTokens

        when:
        def invocation = invoke(new BookRestEndpoint(bookServices)) {
            method "delete"
            header "Accept", "application/json"
            register pathBinding
        }

        then:
        with(invocation) {
            bodyText == ""
            status.code == 200
        }
    }

}
