package ratpack.example.books;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class BookModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(BookService.class).in(Scopes.SINGLETON);
        bind(BookRenderer.class).in(Scopes.SINGLETON);
    }
}
