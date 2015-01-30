package ratpack.examples.book.fixture

import ratpack.registry.Registries
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest

class ExampleBooksApplicationUnderTest extends GroovyRatpackMainApplicationUnderTest {

    ExampleBooksApplicationUnderTest() {
        // super(getOverriddenProperties())
        super()
    }

    private static Map<String, String> getOverriddenProperties() {
        def overriddenProperties = ['other.remoteControl.enabled': 'true']
        def isbnDbApiKey = System.getenv("ISBNDB_API_KEY")
        if (isbnDbApiKey) {
            overriddenProperties['other.isbndb.apikey'] = isbnDbApiKey
        }
        overriddenProperties
    }
}
