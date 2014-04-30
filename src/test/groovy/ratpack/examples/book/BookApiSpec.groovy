package ratpack.examples.book

import groovy.sql.Sql
import ratpack.groovy.test.LocalScriptApplicationUnderTest
import ratpack.groovy.test.TestHttpClient
import ratpack.groovy.test.TestHttpClients
import ratpack.test.ApplicationUnderTest
import ratpack.test.remote.RemoteControl
import spock.lang.Specification

class BookApiSpec extends Specification {

    ApplicationUnderTest aut = new LocalScriptApplicationUnderTest('other.remoteControl.enabled': 'true')
    @Delegate TestHttpClient client = TestHttpClients.testHttpClient(aut)
    RemoteControl remote = new RemoteControl(aut)

    def cleanup() {
        remote.exec {
            get(Sql).execute("delete from books")
        }
    }

    def "list empty books"() {
        expect:
        with(get("api/books")) {
            body.jsonPath().getList("").empty
        }
    }

    def "create book"() {
        when:
        request.contentType("application/json").body(isbn: "1932394842", quantity: 10, price: 22.34)
        post("api/book")

        then:
        println response.jsonPath()
        with(response.jsonPath()) {
            get("isbn") == "1932394842"
            get("title") == "Groovy in Action"
            get("author") == "Dierk Koenig"
            get("publisher") == "Manning Publications"
            getInt("quantity") == 10
            getObject("price", BigDecimal) == 22.34
        }

        and:
        resetRequest()
        with(get("api/books").body.jsonPath().getMap("[0]")) {
            get("isbn") == "1932394842"
            get("title") == "Groovy in Action"
            get("author") == "Dierk Koenig"
            get("publisher") == "Manning Publications"
            get("quantity") == 10
            get("price").asType(BigDecimal) == 22.34
        }
    }

}
