package ratpack.example.books

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.annotation.Timed
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

import javax.inject.Inject

class BookService {

    private final Sql sql
    private final MetricRegistry metricRegistry

    @Inject
    BookService(Sql sql, MetricRegistry metricRegistry) {
        this.sql = sql
        this.metricRegistry = metricRegistry
    }

    @Timed
    void createTable() {
        sql.executeInsert("create table if not exists books (id int primary key auto_increment, title varchar(255), content varchar(255))")
    }

    @Timed
    List<Book> list() {
        sql.rows("select id, title, content from books order by id").collect { GroovyRowResult result ->
            new Book(result.id, result.title, result.content)
        }
    }

    @Timed
    long insert(String title, String content) {
        if (title.toLowerCase().contains('foo')) {
            metricRegistry.meter("Foo Book Created").mark()
        }

        sql.executeInsert("insert into books (title, content) values ($title, $content)")[0][0] as long
    }

    @Timed
    Book find(long id) {
        def row = sql.firstRow("select title, content from books where id = $id order by id")
        row ? new Book(id, row.title, row.content) : null
    }

    @Timed
    void update(long id, String title, String content) {
        sql.executeUpdate("update books set title = $title, content = $content where id = $id")
    }

    @Timed
    void delete(long id) {
        sql.executeUpdate("delete from books where id = $id")
    }

}
