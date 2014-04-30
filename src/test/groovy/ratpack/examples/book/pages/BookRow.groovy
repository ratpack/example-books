package ratpack.examples.book.pages

import geb.Module

class BookRow extends Module {

    static content = {
        isbn { $("td", 0).text() }
        title { $("td", 1).text() }
        author { $("td", 2).text() }
        publisher { $("td", 3).text() }
        quantity { $("td", 4).text() }
        price { $("td", 5).text() }
        updateButton { $("a", text: "Update") }
        deleteButton { $("input", value: "Delete") }
    }
}
