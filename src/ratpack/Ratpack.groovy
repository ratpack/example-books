import ratpack.example.books.Book

import static ratpack.groovy.Groovy.groovyTemplate
import static ratpack.groovy.Groovy.ratpack

ratpack {
    handlers {
        get {
            def books = [new Book(1, "Book 1", "Stuff!"), new Book(2, "Book 2", "More Stuff")]
            render groovyTemplate("listing.html", title: "Books", books: books, msg: request.queryParams.msg ?: "")
        }

        assets "public"
    }
}