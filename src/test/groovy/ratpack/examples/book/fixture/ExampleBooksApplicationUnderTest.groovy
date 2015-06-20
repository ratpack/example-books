package ratpack.examples.book.fixture

import groovy.transform.CompileStatic
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.guice.Guice
import ratpack.registry.Registry
import ratpack.remote.RemoteModule

@CompileStatic
class ExampleBooksApplicationUnderTest extends GroovyRatpackMainApplicationUnderTest {
  protected Registry createOverrides(Registry serverRegistry) throws Exception {
    return Guice.registry {
        it.module RemoteModule
    }.apply(serverRegistry)
  }
}
