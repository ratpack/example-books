package ratpack.examples.book.pages

import geb.Module

class BookRow extends Module {

    static content = {
        id { $("td", 0).text() }
        title { $("td", 1).text() }
        updateButton { $("a", text: "Update") }
        deleteButton { $("input", value: "Delete") }
    }
}
