package ratpack.example.books

import com.codahale.metrics.annotation.Timed
import com.codahale.metrics.health.HealthCheck
import com.google.inject.Inject
import ratpack.codahale.metrics.NamedHealthCheck

class DatabaseHealthCheck extends NamedHealthCheck {

    @Inject
    BookService bookService

    public String getName() {
        return "Database Health Check"
    }

    @Override
    @Timed
    protected HealthCheck.Result check() throws Exception {
        bookService.list()
        HealthCheck.Result.healthy()
    }
}
