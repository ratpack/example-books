import static ratpack.groovy.Groovy.*

ratpack {
  handlers {
    get {
      render "Hello World!"
    }
  }
}