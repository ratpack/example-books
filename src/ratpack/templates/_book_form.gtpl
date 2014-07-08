def column = [class: 'col-sm-10']
def columnOffset = [class: 'col-sm-offset-2 col-sm-10']
def formGroup = [class: 'form-group']
def controlLabel(id) { [for: id, class: 'col-sm-2 control-label'] }
def inputText(id, value, opts=[disabled:false]) {
    def attr = [type: 'text', name: id, class: 'form-control', id: id, value: value]
    if (opts.disabled) { attr.disabled = 'disabled' }
    attr
}

form(class:"form-horizontal", role:"form", method:method, action:action) {
    div(class: "form-group") {
        label(controlLabel('isbn'), 'ISBN')
        div(column) {
            input (inputText('isbn', isbn, [disabled: buttonText == 'Update'])) {}
        }
    }

if (buttonText == "Update") {
    div(formGroup) {
        label(controlLabel('title'), 'Title')
        div(column) {
            input (inputText('title', bookTitle, [disabled: true])) {}
        }
    }
    div(formGroup) {
        label(controlLabel('author'), 'Author')
        div(column) {
            input (inputText('author', author, [disabled: true])) {}
        }
    }
    div(formGroup) {
        label(controlLabel('publisher'), 'Publisher')
        div(column) {
            input(inputText('publisher', publisher, [disabled: true]))
        }
    }
}
    div(formGroup) {
        label(controlLabel('quantity'), 'Quantity')
        div(column) {
            input(inputText('quantity', quantity))
        }
    }
    div(formGroup) {
        label(controlLabel('price'), 'Price')
        div(column) {
            input(inputText('price', price))
        }
    }
    div(formGroup) {
        div(columnOffset) {
            button(type: "submit", class: "btn btn-primary", buttonText)
            a(href: "/", class: "btn btn-default", 'Back')
        }
    }
}