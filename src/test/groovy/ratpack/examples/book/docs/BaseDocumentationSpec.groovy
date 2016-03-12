package ratpack.examples.book.docs

import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration

import com.jayway.restassured.builder.RequestSpecBuilder
import com.jayway.restassured.specification.RequestSpecification
import org.junit.Rule
import org.springframework.restdocs.JUnitRestDocumentation
import ratpack.examples.book.fixture.ExampleBooksApplicationUnderTest
import ratpack.test.ApplicationUnderTest
import spock.lang.Shared
import spock.lang.Specification

class BaseDocumentationSpec extends Specification {

    @Shared
    ApplicationUnderTest aut = new ExampleBooksApplicationUnderTest()

    @Rule
    JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation('src/docs/generated-snippets')

    protected RequestSpecification documentationSpec

    void setup() {
        this.documentationSpec = new RequestSpecBuilder()
                .addFilter(documentationConfiguration(restDocumentation))
                .build()
    }
}
