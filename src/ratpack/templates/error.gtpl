layout 'layout.gtpl',
title: title,
msg: msg,
bodyContents: contents {

    h1('An Exception Occurred')
    div('<script>alert("sup")</script>')
    div {
        p(class: "alert alert-danger", sanitizedException?.cause)
        br()
        samp(sanitizedException?.stackTrace?.join('<br/>'))
    }

}