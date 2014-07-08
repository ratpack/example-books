if (error) {
    div(class: "alert alert-danger alert-dismissable") {
    button(type: "button", class: "close", 'data-dismiss': "alert", 'aria-hidden': "true", '&times')
    yield error
    }
}
form(class: "form-horizontal", role: "form", method: method, action: action) {
    div(class: "form-group") {
        label(for: "username", class: "col-sm-2 control-label", 'Username')
        div(class: "col-sm-10") {
            input(type: "text", name: "username", class: "form-control", id: "username", value: "") {}
        }
    }
    div(class: "form-group") {
        label(for: "password", class: "col-sm-2 control-label", 'Password')
        div(class: "col-sm-10") {
            input(type: "password", name: "password", class: "form-control", id: "password", value: "") {}
        }
    }

    div(class: "form-group") {
        div(class: "col-sm-offset-2 col-sm-10") {
            button(type: "submit", class: "btn btn-primary") { yield buttonText }
            a (href: "/", class: "btn btn-default", 'Back')
        }

    }

    input(type: "hidden", name: "client_name", id: "client_name", value: "FormClient") {}
}