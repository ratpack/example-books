package ratpack.examples.book.pages

import geb.Page

class BookFormPage extends Page {

    static content = {
        heading { $("h1").text() }
        submitButton { $("button[type=submit]") }
        titleField { $("#title") }
        contentField { $("#content") }
    }
}
