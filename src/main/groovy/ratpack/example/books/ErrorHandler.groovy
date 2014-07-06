package ratpack.example.books

import groovy.util.logging.Slf4j
import org.codehaus.groovy.runtime.StackTraceUtils
import ratpack.error.ServerErrorHandler
import ratpack.handling.Context

import static ratpack.groovy.Groovy.groovyTemplate

@Slf4j
class ErrorHandler implements ServerErrorHandler {

    @Override
    void error(Context context, Exception exception) {
        log.warn "Problems yo"
        context.with {
            render groovyTemplate("error.html",
                    title: 'Exception',
                    exception: exception,
                    sanitizedException: StackTraceUtils.deepSanitize(exception))
        }
    }
}
