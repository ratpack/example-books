package ratpack.example.books

import groovy.sql.Sql
import ratpack.exec.Promise
import ratpack.launch.LaunchConfig
import rx.Observable

import javax.inject.Inject

import static ratpack.rx.RxRatpack.observe
import static ratpack.rx.RxRatpack.observeEach

class BookService {

    private final Sql sql
    private final LaunchConfig launchConfig

    @Inject
    BookService(Sql sql, LaunchConfig launchConfig) {
        this.sql = sql
        this.launchConfig = launchConfig
    }

    void createTable() {
        sql.executeInsert("drop table if exists books")
        sql.executeInsert("create table books (id int primary key auto_increment, title varchar(255), content varchar(255))")
    }

    private <T> Promise<T> blocking(GroovyCallable<T> blockingOperation) {
        return launchConfig.execController.blocking(blockingOperation)
    }

    Observable<Book> all() {
        observeEach(blocking {
            sql.rows("select id, title, content from books order by id")
        }) map {
            new Book(it.id, it.title, it.content)
        }
    }

    Observable<Long> insert(String title, String content) {
        observeEach(blocking {
            sql.executeInsert("insert into books (title, content) values ($title, $content)")
        }).first().map {
            it[0] as long
        }
    }

    Observable<Book> find(long id) {
        observe(blocking {
            sql.firstRow("select title, content from books where id = $id order by id")
        }) map {
            it == null ? null : new Book(id, it.title, it.content)
        }
    }

    Observable<Void> update(long id, String title, String content) {
        observe(blocking {
            sql.executeUpdate("update books set title = $title, content = $content where id = $id")
        })
    }

    Observable<Void> delete(long id) {
        observe(blocking {
            sql.executeUpdate("delete from books where id = $id")
        })
    }

}
