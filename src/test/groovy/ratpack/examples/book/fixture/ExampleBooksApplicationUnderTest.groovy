package ratpack.examples.book.fixture

import ratpack.example.books.App
import ratpack.registry.Registries
import ratpack.test.MainClassApplicationUnderTest

class ExampleBooksApplicationUnderTest extends MainClassApplicationUnderTest {

    ExampleBooksApplicationUnderTest() {
        // super(getOverriddenProperties())
        super(App, Registries.empty())
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
