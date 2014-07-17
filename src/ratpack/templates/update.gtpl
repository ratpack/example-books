layout 'layout.gtpl',
title: title,
msg: msg,
bodyContents: contents {
    h1('Update Book')
    includeGroovy '_book_form.gtpl'
}