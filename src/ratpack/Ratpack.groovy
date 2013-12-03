import static ratpack.groovy.Groovy.*

ratpack {
    handlers {
        get {
            render groovyTemplate("message.html", title: "Hello World!", message: "Welcome to Ratpack!")
        }

        assets "public"
    }
}