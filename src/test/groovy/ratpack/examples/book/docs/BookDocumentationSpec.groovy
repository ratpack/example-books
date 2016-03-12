package ratpack.examples.book.docs

import static org.hamcrest.CoreMatchers.is
import static com.jayway.restassured.RestAssured.given
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.restdocs.restassured.operation.preprocess.RestAssuredPreprocessors.modifyUris
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document

import groovy.json.JsonOutput
import groovy.sql.Sql
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import ratpack.groovy.test.embed.GroovyEmbeddedApp
import ratpack.http.client.RequestSpec
import ratpack.test.embed.EmbeddedApp
import ratpack.test.http.TestHttpClient
import ratpack.test.remote.RemoteControl
import spock.lang.Shared

class BookDocumentationSpec extends BaseDocumentationSpec {

    @Shared
    EmbeddedApp isbndb = GroovyEmbeddedApp.of {
        handlers {
            all {
                render '{"data" : [{"title" : "Learning Ratpack", "publisher_name" : "O\'Reilly Media", "author_data" : [{"id" : "dan_woods", "name" : "Dan Woods"}]}]}'
            }
        }
    }

    @Delegate
    TestHttpClient client = aut.httpClient
    RemoteControl remote = new RemoteControl(aut)


    def setupSpec() {
        System.setProperty('ratpack.isbndb.host', "http://${isbndb.address.host}:${isbndb.address.port}")
        System.setProperty('ratpack.isbndb.apikey', "fakeapikey")
    }

    def cleanupSpec() {
        System.clearProperty('ratpack.isbndb.host')
    }

    def setupTestBook() {
        requestSpec { RequestSpec requestSpec ->
            requestSpec.body.type("application/json")
            requestSpec.body.text(JsonOutput.toJson([isbn: "1932394842", quantity: 0, price: 22.34]))
        }
        post("api/book")
    }

    def cleanup() {
        remote.exec {
            get(Sql).execute("delete from books")
        }
    }

    def "test and document create book"() {
        given:
        def setup = given(this.documentationSpec)
                .body('{"isbn": "1234567890", "quantity": 10, "price": 22.34}')
                .contentType('application/json')
                .accept('application/json')
                .port(aut.address.port)
                .filter(document('books-create-example',
                preprocessRequest(prettyPrint(),
                        modifyUris()
                                .host('books.example.com')
                                .removePort()),
                preprocessResponse(prettyPrint()),
                responseFields(bookFields),
                requestFields(
                        fieldWithPath('isbn').type(JsonFieldType.STRING).description('book ISBN id'),
                        fieldWithPath('quantity').type(JsonFieldType.NUMBER).description('quanity available'),
                        fieldWithPath('price').type(JsonFieldType.NUMBER)
                                .description('price of the item as a number without currency')
                ),))
        when:
        def result = setup
                .when()
                .post("api/book")
        then:
        result
                .then()
                .assertThat()
                .statusCode(is(200))
    }

    void 'test and document list books'() {
        setup:
        setupTestBook()

        expect:
        given(this.documentationSpec)
                .contentType('application/json')
                .accept('application/json')
                .port(aut.address.port)
                .filter(document('books-list-example',
                preprocessRequest(modifyUris()
                        .host('books.example.com')
                        .removePort()),
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath('[].isbn').description('The ISBN of the book'),
                        fieldWithPath('[].quantity').description("The quantity of the book that is available"),
                        fieldWithPath('[].price').description("The current price of the book"),
                        fieldWithPath('[].title').description("The title of the book"),
                        fieldWithPath('[].author').description('The author of the book'),
                        fieldWithPath('[].publisher').description('The publisher of the book')
                )))
                .when()
                .get('/api/book')
                .then()
                .assertThat()
                .statusCode(is(200))
    }

    void 'test and document get individual book'() {
        setup:
        setupTestBook()

        expect:
        given(this.documentationSpec)
                .contentType('application/json')
                .accept('application/json')
                .port(aut.address.port)
                .filter(document('books-get-example',
                preprocessRequest(modifyUris()
                        .host('books.example.com')
                        .removePort()),
                preprocessResponse(prettyPrint()),
                responseFields(bookFields)))
                .when()
                .get("/api/book/1932394842")
                .then()
                .assertThat()
                .statusCode(is(200))
    }

    FieldDescriptor[] getBookFields() {
        [fieldWithPath('isbn').description('The ISBN of the book'),
         fieldWithPath('quantity').description("The quantity of the book that is available"),
         fieldWithPath('price').description("The current price of the book"),
         fieldWithPath('title').description("The title of the book"),
         fieldWithPath('author').description('The author of the book'),
         fieldWithPath('publisher').description('The publisher of the book')]
    }
}
