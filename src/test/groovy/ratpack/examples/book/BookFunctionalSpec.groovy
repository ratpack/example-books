package ratpack.examples.book

import geb.spock.GebReportingSpec
import groovy.sql.Sql
import ratpack.examples.book.fixture.ExampleBooksApplicationUnderTest
import ratpack.examples.book.pages.BooksPage
import ratpack.examples.book.pages.CreateBookPage
import ratpack.examples.book.pages.UpdateBookPage
import ratpack.test.ApplicationUnderTest
import ratpack.test.remote.RemoteControl
import spock.lang.Shared
import spock.lang.Stepwise

@Stepwise
class BookFunctionalSpec extends GebReportingSpec {

    @Shared
    ApplicationUnderTest aut = new ExampleBooksApplicationUnderTest()

    def setup() {
        browser.baseUrl = aut.address.toString()
    }

    def cleanupSpec() {
        RemoteControl remote = new RemoteControl(aut)
        remote.exec {
            get(Sql).execute("delete from books")
        }
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
        isbnField = "1932394842"
        quantityField = "10"
        priceField = "10.23"
        createButton.click()

        then:
        at BooksPage

        and:
        books.size() == 1
        books[0].isbn == "1932394842"
        books[0].title == "Groovy in Action"
        books[0].author == "Dierk Koenig"
        books[0].publisher == "Manning Publications"
        books[0].price == "10.23"
        books[0].quantity == "10"
    }

    def "update book"() {
        when:
        books[0].updateButton.click()

        then:
        at UpdateBookPage

        when:
        quantityField = "2"
        priceField = "5.34"
        updateButton.click()

        then:
        at BooksPage

        and:
        books.size() == 1
        books[0].isbn == "1932394842"
        books[0].title == "Groovy in Action"
        books[0].author == "Dierk Koenig"
        books[0].publisher == "Manning Publications"
        books[0].price == "5.34"
        books[0].quantity == "2"
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

