example-books
=============

An example Groovy &amp; Gradle based Ratpack app.

This app demonstrates
* Metrics
* Authetication
* Blocking I/O
* Async external HTTP requests 
* RxJava integration
* Hystrix integration

Setup
-----

This application integrates with [ISBNdb](http://isbndb.com/account/logincreate) and as such you will need to create a free 
account and api key in order to run the application successfully.  And at the moment to run the integration tests too.

When you have done this simply add your api key to the property `other.isbndb.apikey` in the `ratpack.properties` file.
