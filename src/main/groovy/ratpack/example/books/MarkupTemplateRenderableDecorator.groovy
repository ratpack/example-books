package ratpack.example.books

import org.pac4j.core.profile.UserProfile
import ratpack.groovy.template.MarkupTemplate
import ratpack.handling.Context
import ratpack.render.RenderableDecoratorSupport
import ratpack.session.store.SessionStorage

import static ratpack.pac4j.internal.SessionConstants.USER_PROFILE

class MarkupTemplateRenderableDecorator extends RenderableDecoratorSupport<MarkupTemplate> {
    @Override
    MarkupTemplate decorate(Context context, MarkupTemplate template) {
        UserProfile profile = context.request.get(SessionStorage).get(USER_PROFILE)
        def username = profile?.getAttribute("username")

        new MarkupTemplate(template.name, template.contentType, template.model + ['username': username ?: ""])
    }
}
