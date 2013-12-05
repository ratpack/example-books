package ratpack.examples.book

import geb.spock.GebReportingSpec
import ratpack.examples.book.pages.BooksPage
import ratpack.examples.book.pages.CreateBookPage
import ratpack.examples.book.pages.UpdateBookPage
import ratpack.groovy.test.LocalScriptApplicationUnderTest
import ratpack.test.ApplicationUnderTest
import spock.lang.Stepwise

@Stepwise
class BookFunctionalSpec extends GebReportingSpec {

    ApplicationUnderTest aut = new LocalScriptApplicationUnderTest()

    def setup() {
        browser.baseUrl = aut.address.toString()
    }

    def "no books are listed"() {
        when:
        to BooksPage

        then:
        books.size() == 0
    }

    def "go to create book page"() {
        when:
        createBookButton.click()

        then:
        at CreateBookPage
    }

    def "create book"() {
        when:
        titleField = "some book"
        contentField = "some content"
        createButton.click()

        then:
        at BooksPage

        and:
        books.size() == 1
        books[0].title == "some book"
    }

    def "update book"() {
        when:
        books[0].updateButton.click()

        then:
        at UpdateBookPage

        when:
        titleField = "changed title"
        contentField = "changed content"
        updateButton.click()

        then:
        at BooksPage

        and:
        books.size() == 1
        books[0].title == "changed title"
    }

    def "delete book"() {
        when:
        books[0].deleteButton.click()

        then:
        at BooksPage

        and:
        books.size() == 0
    }

}

