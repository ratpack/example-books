package ratpack.example.books

import ratpack.server.RatpackServer
import ratpack.groovy.Groovy

class App {

  public static void main(String[] args) {
    RatpackServer.of(Groovy.Script.app()).start()
  }
}
