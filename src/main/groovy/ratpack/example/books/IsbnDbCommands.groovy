package ratpack.example.books

import com.google.inject.Inject
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import ratpack.func.Action
import ratpack.http.HttpUrlSpec
import ratpack.http.client.HttpClients
import ratpack.http.client.ReceivedResponse
import ratpack.http.client.RequestSpec
import ratpack.launch.LaunchConfig

import static ratpack.rx.RxRatpack.observe

class IsbnDbCommands {

    private final LaunchConfig launchConfig

    @Inject
    public IsbnDbCommands(LaunchConfig launchConfig) {
        this.launchConfig = launchConfig
    }

    public rx.Observable<ReceivedResponse> getBookRequest(final String isbn) {
        return new HystrixObservableCommand<ReceivedResponse>(HystrixCommandGroupKey.Factory.asKey("http-isbndb")) {
            @Override
            protected rx.Observable<ReceivedResponse> run() {
                return observe(HttpClients.httpClient(launchConfig)
                    .get(new Action<RequestSpec>() {
                        @Override
                        void execute(RequestSpec request) throws Exception {
                            request.url(new Action<HttpUrlSpec>() {
                                @Override
                                void execute(HttpUrlSpec httpUrlSpec) throws Exception {
                                    httpUrlSpec.set("http://isbndb.com/api/v2/json/${launchConfig.getOther('isbndb.apikey', '')}/book/$isbn".toURI())
                                }
                            })
                        }
                    }))
            }

            @Override
            protected String getCacheKey() {
                return "http-isbndb-book-$isbn"
            }
        }.toObservable()
    }

}
