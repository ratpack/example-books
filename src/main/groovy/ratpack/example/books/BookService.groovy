package ratpack.example.books

import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ratpack.exec.ExecControl
import ratpack.exec.Promise
import ratpack.http.client.HttpClient
import ratpack.http.client.ReceivedResponse
import ratpack.http.client.internal.DefaultHttpClient
import ratpack.launch.LaunchConfig
import rx.Observable

import javax.inject.Inject

import static ratpack.rx.RxRatpack.observe
import static ratpack.rx.RxRatpack.observeEach

class BookService {

    private final Sql sql
    private final HttpClient httpClient
    private final ExecControl execControl
    private final String isbndbBaseUrl

    @Inject
    BookService(Sql sql, LaunchConfig launchConfig, ExecControl execControl) {
        this.sql = sql
        this.execControl = execControl
        this.httpClient = new DefaultHttpClient(launchConfig)
        this.isbndbBaseUrl = "http://isbndb.com/api/v2/json/${launchConfig.getOther('isbndb.apikey', '')}"
    }

    void createTable() {
        sql.executeInsert("drop table if exists books")
        sql.executeInsert("create table books (isbn varchar(13) primary key, quantity int, price numeric(15, 2))")
    }

    private <T> Promise<T> blocking(GroovyCallable<T> blockingOperation) {
        return execControl.blocking(blockingOperation)
    }

    Observable<Book> all() {
        observeEach(blocking {
            sql.rows("select isbn, quantity, price from books order by isbn")
        }) flatMap {
            observe(httpClient.get("$isbndbBaseUrl/book/$it.isbn")) map { ReceivedResponse resp ->
                def result = new JsonSlurper().parseText(resp.body.text)
                return new Book(
                        it.isbn,
                        it.quantity,
                        it.price,
                        result.data[0].title,
                        result.data[0].author_data[0].name,
                        result.data[0].publisher_name
                )
            }
        }
    }

    Observable<String> insert(String isbn, long quantity, BigDecimal price) {
        observe(blocking {
            sql.executeInsert("insert into books (isbn, quantity, price) values ($isbn, $quantity, $price)")
        }).map {
            isbn
        }
    }

    Observable<Book> find(String isbn) {
        def bookStockData = observe(blocking {
            sql.firstRow("select quantity, price from books where isbn = $isbn")
        })

        def bookMetaData = observe(httpClient.get("$isbndbBaseUrl/book/$isbn"))

        Observable.zip(bookStockData, bookMetaData) {GroovyRowResult dbRow, ReceivedResponse resp ->
            def result = new JsonSlurper().parseText(resp.body.text)
            return new Book(
                    isbn,
                    dbRow.quantity,
                    dbRow.price,
                    result.data[0].title,
                    result.data[0].author_data[0].name,
                    result.data[0].publisher_name
            )
        }
    }

    Observable<Void> update(String isbn, long quantity, BigDecimal price) {
        observe(blocking {
            sql.executeUpdate("update books set quantity = $quantity, price = $price where isbn = $isbn")
        })
    }

    Observable<Void> delete(String isbn) {
        observe(blocking {
            sql.executeUpdate("delete from books where isbn = $isbn")
        })
    }

}
