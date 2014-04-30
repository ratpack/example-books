package ratpack.example.books

import groovy.transform.Immutable

@Immutable
class Book {
    String isbn
    long quantity
    BigDecimal price
    String title
    String author
    String publisher
}
