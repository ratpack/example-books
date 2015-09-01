package ratpack.example.books

import com.google.inject.Inject
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ratpack.exec.Blocking

import static ratpack.rx.RxRatpack.observe
import static ratpack.rx.RxRatpack.observeEach

class BookDbCommands {

    private final Sql sql
    private static final HystrixCommandGroupKey hystrixCommandGroupKey = HystrixCommandGroupKey.Factory.asKey("sql-bookdb")

    @Inject
    public BookDbCommands(Sql sql) {
        this.sql = sql
    }

    void createTables() {
        sql.executeInsert("drop table if exists books")
        sql.executeInsert("create table books (isbn varchar(13) primary key, quantity int, price numeric(15, 2))")
    }

    rx.Observable<GroovyRowResult> getAll() {
        return new HystrixObservableCommand<GroovyRowResult>(
            HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("getAll"))) {

            @Override
            protected rx.Observable<GroovyRowResult> construct() {
                observeEach(Blocking.get {
                    sql.rows("select isbn, quantity, price from books order by isbn")
                })
            }

            @Override
            protected String getCacheKey() {
                return "db-bookdb-all"
            }
        }.toObservable()
    }

    rx.Observable<String> insert(final String isbn, final long quantity, final BigDecimal price) {
        return new HystrixObservableCommand<String>(
            HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("insert"))) {

            @Override
            protected rx.Observable<String> construct() {
                observe(Blocking.get {
                    sql.executeInsert("insert into books (isbn, quantity, price) values ($isbn, $quantity, $price)")
                })
            }
        }.toObservable()
    }

    rx.Observable<GroovyRowResult> find(final String isbn) {
        return new HystrixObservableCommand<GroovyRowResult>(
            HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("find"))) {

            @Override
            protected rx.Observable<GroovyRowResult> construct() {
                observe(Blocking.get {
                    sql.firstRow("select quantity, price from books where isbn = $isbn")
                })
            }

            @Override
            protected String getCacheKey() {
                return "db-bookdb-find-$isbn"
            }
        }.toObservable()
    }

    rx.Observable<Void> update(final String isbn, final long quantity, final BigDecimal price) {
        return new HystrixObservableCommand<Void>(
            HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("update"))) {

            @Override
            protected rx.Observable<Void> construct() {
                observe(Blocking.get {
                    sql.executeUpdate("update books set quantity = $quantity, price = $price where isbn = $isbn")
                })
            }
        }.toObservable()
    }

    rx.Observable<Void> delete(final String isbn) {
        return new HystrixObservableCommand<Void>(
            HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("delete"))) {

            @Override
            protected rx.Observable<Void> construct() {
                observe(Blocking.get {
                    sql.executeUpdate("delete from books where isbn = $isbn")
                })
            }
        }.toObservable()
    }

}
