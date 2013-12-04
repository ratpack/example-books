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

        def bookServices = Mock(BookService)
        bookServices.find(0) >> book

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
        def bookServices = Mock(BookService)
        bookServices.find(0) >> null

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
        def book = new Book(0, "foo", "bar")

        def bookServices = Mock(BookService)
        1 * bookServices.find(0) >> book
        1 * bookServices.delete(0)

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
