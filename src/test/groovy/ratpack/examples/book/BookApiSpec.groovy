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
        request.contentType("application/json").body(title: "t", content: "c")
        post("api/book")

        then:
        with(response.jsonPath()) {
            get("title") == "t"
            get("content") == "c"
        }

        and:
        resetRequest()
        with(get("api/books")) {
            body.jsonPath().getMap("[0]") == [content:"c", id:1, title:"t"]
        }
    }

}
