package ratpack.examples.book

import geb.spock.GebReportingSpec
import ratpack.examples.book.fixture.ExampleBooksApplicationUnderTest
import ratpack.examples.book.pages.BooksPage
import ratpack.examples.book.pages.LoginPage
import ratpack.test.ApplicationUnderTest
import spock.lang.Shared
import spock.lang.Stepwise

@Stepwise
class LoginFunctionalSpec extends GebReportingSpec {

    @Shared
    ApplicationUnderTest aut = new ExampleBooksApplicationUnderTest()

    def setup() {
        browser.baseUrl = aut.address.toString()
    }

    def "first load"() {
        when:
        to BooksPage

        then:
        loginButton
        !signedInAs
        !logoutButton
    }

    def "go to login page"() {
        when:
        loginButton.click()

        then:
        at LoginPage
    }

    def "login with proper credentails"() {
        when:
        loginForm.with {
            username = "ratpack"
            password = "ratpack"
        }
        loginForm.find('button', type: 'submit').click()

        then:
        at BooksPage

        and:
        !loginButton
        logoutButton
        signedInAs == "Signed in as, ratpack"
    }

    def "logout"() {
        when:
        logoutButton.click()

        then:
        at BooksPage

        and:
        loginButton
        !logoutButton
        !signedInAs
    }

}

