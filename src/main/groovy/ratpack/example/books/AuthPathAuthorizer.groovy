package ratpack.example.books

import ratpack.handling.Context
import ratpack.pac4j.AbstractAuthorizer

class AuthPathAuthorizer extends AbstractAuthorizer {

    @Override
    boolean isAuthenticationRequired(Context context) {
        return context.request.path.startsWith("admin")
    }

}
