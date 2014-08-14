package ratpack.example.books

import groovy.util.logging.Slf4j
import org.codehaus.groovy.runtime.StackTraceUtils
import ratpack.error.ServerErrorHandler
import ratpack.handling.Context

import static ratpack.groovy.Groovy.groovyMarkupTemplate

@Slf4j
class ErrorHandler implements ServerErrorHandler {

    @Override
    void error(Context context, Throwable throwable) {
        log.warn "Problems yo", throwable
        context.with {
            render groovyMarkupTemplate("error.gtpl",
                    title: 'Exception',
                    exception: throwable,
                    sanitizedException: StackTraceUtils.deepSanitize(throwable))
        }
    }


}
