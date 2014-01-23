package ratpack.example.books

import groovy.sql.Sql
import ratpack.rx.RxBackground

import javax.inject.Inject

class BookService {

    private final Sql sql
    private final RxBackground rxBackground

    @Inject
    BookService(Sql sql, RxBackground rxBackground) {
        this.sql = sql
        this.rxBackground = rxBackground
    }

    void createTable() {
        sql.executeInsert("create table if not exists books (id int primary key auto_increment, title varchar(255), content varchar(255))")
    }

    rx.Observable<Book> all() {
        rxBackground.observeEach() {
            sql.rows("select id, title, content from books order by id")
        } map{
            new Book(it.id, it.title, it.content)
        }
    }

    rx.Observable<Long> insert(String title, String content) {
        rxBackground.observeEach() {
            sql.executeInsert("insert into books (title, content) values ($title, $content)")
        }
        .first()
        .map {
            it[0] as long
        }
    }

    rx.Observable<Book> find(long id) {
        rxBackground.observe {
            sql.firstRow("select title, content from books where id = $id order by id")
        } map{
            it == null ? null : new Book(id, it.title, it.content)
        }
    }

    rx.Observable<Void> update(long id, String title, String content) {
        rxBackground.observe {
            sql.executeUpdate("update books set title = $title, content = $content where id = $id")
        }
    }

    rx.Observable<Void> delete(long id) {
        rxBackground.observe {
            sql.executeUpdate("delete from books where id = $id")
        }
    }

}
