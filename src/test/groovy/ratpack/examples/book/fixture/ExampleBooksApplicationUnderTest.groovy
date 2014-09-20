package ratpack.examples.book.fixture

import ratpack.groovy.test.LocalScriptApplicationUnderTest

class ExampleBooksApplicationUnderTest extends LocalScriptApplicationUnderTest {

    ExampleBooksApplicationUnderTest() {
        super(getOverriddenProperties())
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
