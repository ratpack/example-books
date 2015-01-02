package ratpack.example.books

import com.google.inject.Inject
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixObservableCommand
import ratpack.http.client.HttpClient
import ratpack.http.client.ReceivedResponse
import ratpack.launch.ServerConfig
import rx.Observable

import static ratpack.rx.RxRatpack.observe

class IsbnDbCommands {

    private final ServerConfig serverConfig
    private final HttpClient httpClient

    @Inject
    public IsbnDbCommands(ServerConfig serverConfig, HttpClient httpClient) {
        this.serverConfig = serverConfig
        this.httpClient = httpClient
    }

    public Observable<String> getBookRequest(final String isbn) {
        new HystrixObservableCommand<String>(
            HystrixObservableCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("http-isbndb"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("getBookRequest"))) {

            @Override
            protected Observable<String> run() {
                def uri = "http://isbndb.com/api/v2/json/${serverConfig.getOther('isbndb.apikey', '')}/book/$isbn".toURI()
                observe(httpClient.get(uri)).map { ReceivedResponse resp ->
                    if (resp.body.text.contains("Daily request limit exceeded")) {
                        throw new RuntimeException("ISBNDB daily request limit exceeded.")
                    }
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
