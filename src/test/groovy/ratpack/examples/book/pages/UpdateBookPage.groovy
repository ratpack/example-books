package ratpack.examples.book.pages

class UpdateBookPage extends BookFormPage {

    static at = { heading == "Update Book" }

    static content = {
        updateButton { submitButton }
    }
}
