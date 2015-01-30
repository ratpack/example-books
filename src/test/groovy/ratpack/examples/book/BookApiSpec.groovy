package ratpack.examples.book

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.sql.Sql
import ratpack.examples.book.fixture.ExampleBooksApplicationUnderTest
import ratpack.http.client.RequestSpec
import ratpack.test.ApplicationUnderTest
import ratpack.test.http.TestHttpClient
import ratpack.test.remote.RemoteControl
import spock.lang.Shared
import spock.lang.Specification

class BookApiSpec extends Specification {

    @Shared
    ApplicationUnderTest aut = new ExampleBooksApplicationUnderTest()

    @Delegate
    TestHttpClient client = aut.httpClient
    RemoteControl remote = new RemoteControl(aut)

    def cleanup() {
        remote.exec {
            get(Sql).execute("delete from books")
        }
    }

    def "list empty books"() {
        given:
        def json = new JsonSlurper()
        expect:
        json.parseText(getText("api/book")) == []
    }

    def "create book"() {
        given:
        def json = new JsonSlurper()

        when:
        requestSpec { RequestSpec requestSpec ->
            requestSpec.body.type("application/json")
            requestSpec.body.text(JsonOutput.toJson([isbn: "1932394842", quantity: 10, price: 22.34]))
        }
        post("api/book")

        then:
        def book = json.parseText(response.body.text)
        with(book) {
            isbn == "1932394842"
            title == "Groovy in Action"
            author == "Dierk Koenig"
            publisher == "Manning Publications"
            quantity == 10
            price == 22.34
        }

        and:
        resetRequest()
        def books = json.parseText(get("api/book").body.text)
        with(books[0]) {
            get("isbn") == "1932394842"
            get("title") == "Groovy in Action"
            get("author") == "Dierk Koenig"
            get("publisher") == "Manning Publications"
            get("quantity") == 10
            get("price") == 22.34
        }
    }

}
