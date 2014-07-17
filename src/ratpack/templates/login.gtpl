layout 'layout.gtpl',
title: title,
error: error,
bodyContents: contents {
    h1('Login')
    div(class: "alert alert-info") {
        yield 'A matching username and password will pass authentication'
    }
    includeGroovy '_login_form.gtpl'
}