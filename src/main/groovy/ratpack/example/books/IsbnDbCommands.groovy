package ratpack.example.books

import com.google.inject.Inject
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixObservableCommand
import ratpack.http.client.ReceivedResponse
import ratpack.http.client.RequestSpec
import ratpack.launch.LaunchConfig
import rx.Observable

import static ratpack.rx.RxRatpack.observe
import static ratpack.http.client.HttpClients.httpClient

class IsbnDbCommands {

    private final LaunchConfig launchConfig

    @Inject
    public IsbnDbCommands(LaunchConfig launchConfig) {
        this.launchConfig = launchConfig
    }

    public Observable<String> getBookRequest(final String isbn) {
        new HystrixObservableCommand<String>(
            HystrixObservableCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("http-isbndb"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("getBookRequest"))) {

            @Override
            protected Observable<String> run() {
                observe(httpClient(launchConfig).get({ RequestSpec request ->
                    request.url.set("http://isbndb.com/api/v2/json/${launchConfig.getOther('isbndb.apikey', '')}/book/$isbn".toURI())
                })).map { ReceivedResponse resp ->
                    resp.body.text
                }
            }

            @Override
            protected Observable<String> getFallback() {
                return Observable.just('{"data" : [{"title" : "Groovy in Action", "publisher_name" : "Manning Publications", "author_data" : [{"id" : "dierk_koenig", "name" : "Dierk Koenig"}]}]}')
            }

            @Override
            protected String getCacheKey() {
                return "http-isbndb-book-$isbn"
            }
        }.toObservable()
    }

}
