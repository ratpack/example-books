package ratpack.examples.book.pages

import geb.Page

class LoginPage extends Page {

    static at = { heading == "Login" }

    static content = {
        heading { $("h1").text() }
        loginForm { $("form") }
    }
}
