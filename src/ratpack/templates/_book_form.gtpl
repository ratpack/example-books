form(class:"form-horizontal", role:"form", method:method, action:action) {
    div(class: "form-group") {
        label(for:"isbn", class:"col-sm-2 control-label", 'ISBN')
        div(class: "col-sm-10") {
            def attributes = [type: 'text', name: "isbn", class: "form-control", id: "isbn", value: isbn]
            if (buttonText == 'Update') {
                attributes.disabled = 'disabled'
            }
            input (attributes) {}
        }
    }

if (buttonText == "Update") {
    div(class: 'form-group') {
        label(for: "title", class: "col-sm-2 control-label", 'Title')
        div(class: 'col-sm-10') {
            input (type: 'text', name: 'title', class: 'form-control', id: 'title', value: bookTitle, disabled: 'disabled') {}
        }
    }
    div(class: 'form-group') {
        label(for: "author", class: "col-sm-2 control-label", 'Author')
        div(class: 'col-sm-10') {
            input (type: 'text', name: 'author', class: 'form-control', id: 'author', value: author, disabled: 'disabled') {}
        }
    }
    div(class: 'form-group') {
        label(for: "publisher", class: "col-sm-2 control-label", 'Publisher')
        div(class: 'col-sm-10') {
            input (type: 'text', name: 'publisher', class: 'form-control', id: 'publisher', value: publisher, disabled: 'disabled') {}
        }
    }
}
    div(class: 'form-group') {
        label(for: "quantity", class: "col-sm-2 control-label", 'Quantity')
        div(class: 'col-sm-10') {
            input (type: "text", name: "quantity", class: "form-control", id: "quantity", value: quantity) {}
        }
    }
    div(class: 'form-group') {
        label(for: "price", class: "col-sm-2 control-label", 'Price')
        div(class: 'col-sm-10') {
            input (type: "text", name: "price", class: "form-control", id: "price", value: price) {}
        }
    }
    div(class: 'form-group') {
        div(class: "col-sm-offset-2 col-sm-10") {
            button(type: "submit", class: "btn btn-primary", buttonText)
            a(href: "/", class: "btn btn-default", 'Back')
        }
    }
}