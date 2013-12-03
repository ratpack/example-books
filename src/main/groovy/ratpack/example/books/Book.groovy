package ratpack.example.books

import groovy.transform.Immutable

@Immutable
class Book {
    long id
    String title
    String content
}
