package ratpack.examples.book.pages

import geb.Page

class BooksPage extends Page {

    static at = { heading == "Books" }

    static content = {
        heading { $("h1").text() }
        books { moduleList BookRow, $("tbody tr") }
        createBookButton { $("a", href: endsWith("/create")) }
        loginButton(required: false, cache: false) { $("a", href: endsWith("/login")) }
        logoutButton(required: false, cache: false) { $("a", href: endsWith("/logout")) }
        signedInAs(required: false, cache: false) { $("p.navbar-text").text() }
    }
}
