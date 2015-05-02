package ratpack.example.books

import org.pac4j.core.profile.UserProfile
import ratpack.exec.Promise
import ratpack.groovy.template.MarkupTemplate
import ratpack.handling.Context
import ratpack.render.RenderableDecoratorSupport

class MarkupTemplateRenderableDecorator extends RenderableDecoratorSupport<MarkupTemplate> {
    @Override
    Promise<MarkupTemplate> decorate(Context context, MarkupTemplate template) {
        def username = ""
        context.request.maybeGet(UserProfile).ifPresent({ userProfile ->
            username = userProfile.getAttribute('username') ?: ""
        })

        return context.promiseOf(
                new MarkupTemplate(template.name, template.contentType, template.model + ['username': username])
        )
    }
}
