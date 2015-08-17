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

    h1('Update Book')
    includeGroovy '_book_form.gtpl'
}