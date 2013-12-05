package ratpack.examples.book.pages

class CreateBookPage extends BookFormPage {

    static at = { heading == "Create Book" }

    static content = {
        createButton { submitButton }
    }

}
