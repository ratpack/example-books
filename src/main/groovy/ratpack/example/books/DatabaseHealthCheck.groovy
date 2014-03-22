package ratpack.example.books

import com.codahale.metrics.annotation.Timed
import com.codahale.metrics.health.HealthCheck
import com.google.inject.Inject
import groovy.sql.Sql
import ratpack.codahale.metrics.NamedHealthCheck

class DatabaseHealthCheck extends NamedHealthCheck {

    @Inject
    Sql sql

    public String getName() {
        return "Database-Health-Check"
    }

    @Override
    @Timed
    protected HealthCheck.Result check() throws Exception {
        sql.rows("select count(*) from books")
        HealthCheck.Result.healthy()
    }
}
