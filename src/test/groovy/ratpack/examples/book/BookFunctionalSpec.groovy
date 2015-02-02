package ratpack.examples.book

import geb.spock.GebReportingSpec
import groovy.sql.Sql
import ratpack.examples.book.fixture.ExampleBooksApplicationUnderTest
import ratpack.examples.book.pages.BooksPage
import ratpack.examples.book.pages.CreateBookPage
import ratpack.examples.book.pages.UpdateBookPage
import ratpack.groovy.test.embed.GroovyEmbeddedApp
import ratpack.test.ApplicationUnderTest
import ratpack.test.embed.EmbeddedApp
import ratpack.test.remote.RemoteControl
import spock.lang.Shared
import spock.lang.Stepwise

@Stepwise
class BookFunctionalSpec extends GebReportingSpec {

    @Shared
    ApplicationUnderTest aut = new ExampleBooksApplicationUnderTest()

    @Shared
    EmbeddedApp isbndb = GroovyEmbeddedApp.build {
        handlers {
            handler {
                render '{"data" : [{"title" : "Jurassic Park: A Novel", "publisher_name" : "Ballantine Books", "author_data" : [{"id" : "cm", "name" : "Crichton, Michael"}]}]}'
            }
        }
    }

    def setupSpec() {
        System.setProperty('ratpack.isbndb.host', "http://${isbndb.address.host}:${isbndb.address.port}")
        System.setProperty('ratpack.isbndb.apikey', "fakeapikey")
    }

    def setup() {
        browser.baseUrl = aut.address.toString()
    }

    def cleanupSpec() {
        RemoteControl remote = new RemoteControl(aut)
        remote.exec {
            get(Sql).execute("delete from books")
        }
        System.clearProperty('ratpack.isbndb.host')
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
        isbnField = "0345538986"
        quantityField = "10"
        priceField = "10.23"
        createButton.click()

        then:
        at BooksPage

        and:
        books.size() == 1
        books[0].isbn == "0345538986"
        books[0].title == "Jurassic Park: A Novel"
        books[0].author == "Crichton, Michael"
        books[0].publisher == "Ballantine Books"
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
        books[0].isbn == "0345538986"
        books[0].title == "Jurassic Park: A Novel"
        books[0].author == "Crichton, Michael"
        books[0].publisher == "Ballantine Books"
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

