layout 'layout.gtpl',
title: title,
msg: msg,
bodyContents: contents {
    h1('An Exception Occurred')
    div(class: "alert alert-danger", style: "white-space: pre") {
        yield new StringWriter().withWriter {
            sanitizedException.printStackTrace(new PrintWriter(it))
            it.toString()
        }
    }
}