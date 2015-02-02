layout 'layout.gtpl',
title: title,
msg: msg,
bodyContents: contents {
    if (username) {
        p(class: "navbar-text navbar-right") {
            span(class: "glyphicon glyphicon-user") {}
            yield 'Signed in as, ' strong(username)
        }
    }

    h1('Create Book')
    div(class: 'alert alert-info') {
        p {
          yield 'In order to add a new book you need to use an ISBN that is available on '
          a(href: "http://isbndb.com", target: '_blank', 'ISBNdb')
          yield ' e.g. 1932394842'
        }
        p { strong('n.b. all fields are mandatory') }
    }
    includeGroovy '_book_form.gtpl'
}