import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ratpack.example.books.Book
import ratpack.groovy.sql.SqlModule
import ratpack.h2.H2Module

import static ratpack.groovy.Groovy.groovyTemplate
import static ratpack.groovy.Groovy.ratpack

ratpack {
    modules {
        register new H2Module()
        register new SqlModule()
    }

    handlers { Sql sql ->

        sql.executeInsert("create table if not exists books (id int primary key auto_increment, title varchar(255), content varchar(255))")
        sql.executeUpdate("merge into books (id, title, content) key (id) values (0, 'Book 1', 'Stuff')")

        get {
            def books = sql.rows("select id, title, content from books order by id").collect { GroovyRowResult result ->
                new Book(result.id, result.title, result.content)
            }
            render groovyTemplate("listing.html", title: "Books", books: books, msg: request.queryParams.msg ?: "")
        }

        assets "public"
    }

}