layout 'layout.gtpl',
title: title,
msg: msg,
bodyContents: contents {
    div(class: 'alert alert-info') {
        p {
            yield 'In order to use this application you will need to create a free account and api key with '
            a(href: 'http://isbndb.com/account/logincreate', 'ISBNdb')
        }
        p {
            yield 'Once you have done this you need to add your api key to the property '
            code('other.isbndb.apikey')
            yield ' in the '
            code('ratpack.properties')
            yield ' file and restart the application'
        }
        p {
            code('other.isbndb.apikey')
            yield ' is currently set to '
            code(isbndbApikey)
        }
    }

    if (username) {
        p(class: "navbar-text navbar-right") {
            span(class: "glyphicon glyphicon-user") {}
            yield 'Signed in as, ' strong(username)
        }
    } else {
        ul(class: "nav nav-pills pull-right") { li { a(href: "/login", 'Sign in') } }
    }

    h1('Books')
    nav(class: 'navbar navbar-default') {
        ul(class: "nav navbar-nav nav-pills") {
            li { a(href: "/create", 'Create Book') }
            li(class: !username ? "disabled" : '') {
                a(href: "/admin/metrics", 'Metrics Dashboard')
            }
            li(class: !username ? "disabled" : '') {
                a(href: "/admin/health-check", 'Run Health Checks')
            }
        }
        table(class: "table table-striped table-bordered") {
            thead {
                tr {
                    th('ISBN')
                    th('Title')
                    th('Author')
                    th('Publisher')
                    th('Quantity')
                    th('Price')
                    th('Actions')
                }
            }
            tbody { books.each { book ->
                tr {
                    td(book.isbn)
                    td(book.title)
                    td(book.author)
                    td(book.publisher)
                    td(book.quantity)
                    td(book.price)
                    td {
                        form(action: "/delete/$book.isbn", method: 'post', style: 'display: inline') {
                            input(type: "submit", name:"delete", value: "Delete", class: "btn btn-danger ", '')
                        }
                        a(href: "/update/$book.isbn", class:"btn btn-default", 'Update')
                    }
                }
            } }
        }
    }
}
