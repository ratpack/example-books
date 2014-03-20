package ratpack.example.books

import org.pac4j.http.profile.HttpProfile
import ratpack.handling.Context
import ratpack.pac4j.AbstractAuthorizer

class AuthPathAuthorizer extends AbstractAuthorizer<HttpProfile> {

    @Override
    boolean isAuthenticationRequired(Context context) {
        return context.request.path.startsWith("admin")
    }

}
