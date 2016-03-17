package ratpack.example.books

import groovy.transform.CompileStatic
import org.pac4j.core.profile.UserProfile
import ratpack.exec.Promise
import ratpack.groovy.template.MarkupTemplate
import ratpack.handling.Context
import ratpack.pac4j.RatpackPac4j
import ratpack.render.RenderableDecoratorSupport

@CompileStatic
class MarkupTemplateRenderableDecorator extends RenderableDecoratorSupport<MarkupTemplate> {
	@Override
	Promise<MarkupTemplate> decorate(Context context, MarkupTemplate template) {
		return RatpackPac4j
            .userProfile(context)
            .map { Optional<UserProfile> u -> u.orElse(null) }
            .map { UserProfile userProfile ->
			    template.model.putAll([username: userProfile?.attributes?.username] as Map)

                new MarkupTemplate(template.name,
                        template.contentType,
                        template.model)
		}
	}
}
