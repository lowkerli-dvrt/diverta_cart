package io.github.sonicarg.diverta_cart.handlers.www

import io.javalin.http.Context
import kotlinx.html.*

fun BODY.navbar(ctx: Context) = header {
    nav("navbar navbar-expand-md navbar-dark fixed-top bg-primary z-1030") {
        a(classes = "navbar-brand") {
            i("fas fa-microchip") { }
            text("Nipp")
            i("fas fa-circle text-danger") { }
            text("nTech")
        }
        button(type = ButtonType.button, classes = "navbar-toggler") {
            attributes.apply {
                put("data-toggle", "collapse")
                put("data-target", "#navbarCollapse")
                put("aria-controls", "navbarCollapse")
                put("aria-expanded", "false")
                put("aria-label", "Toggle navigation")
            }
            span("navbar-toggler-icon") { }
        }

        div("collapse navbar-collapse") {
            id = "navbarCollapse"
            ul("navbar-nav mr-auto") {
                li("nav-item " + if (ctx.path() == "/") "active" else "inactive") {
                    a("/", classes = "nav-link") {
                        text("Main page")
                        Entities.nbsp
                        if (ctx.path() == "/") {
                            span("sr-only") { text("(current)") }
                        }
                    }
                }
                li("nav-item " + if (ctx.path() == "/cart") "active" else "inactive") {
                    a("/cart", classes = "nav-link") {
                        text("Cart (")
                        span {
                            id = "cartTotals"
                            text("---")
                        }
                        text(")")
                        Entities.nbsp
                        if (ctx.path() == "/cart") {
                            span("sr-only") { text("(current)") }
                        }
                    }
                }
            }
            form(action = "/", method = FormMethod.get, classes = "form-inline") {
                textInput(
                    name = "q",
                    classes = "form-control mr-sm-2"
                ) {
                    placeholder = "Search for product..."
                    disabled = ctx.path() == "/checkout"
                    attributes["aria-search"] = "Search"
                }
                submitInput(classes = "btn btn-outline-success my-2 my-sm-0") {
                    value = "Search it!"
                    disabled = ctx.path() == "/checkout"
                }
            }
        }
    }
}

fun BODY.footer() = footer("footer bg-secondary z-1030") {
    div("container") {
        div("row") {
            div("col mt-3") {
                p("mb-1") { text("\u24b8 Francisco Cáneva, 2019") }
            }
            div("col")
            div("col mt-2") {
                p("mb-1") {
                    h4("text-right") {
                        a("#top") {
                            attributes.apply {
                                put("data-toggle", "tooltip")
                                put("title", "Back to top")
                            }
                            i("fas fa-arrow-circle-up") { }
                        }
                    }
                }
            }
        }
    }
}

val PACKAGES_VERSIONS = mapOf(
    "_diverta-cart" to "SNAPSHOT",
    "accounting.js" to "0.4.2",
    "bootstrap" to "4.1.3",
    "bootstrap-select" to "1.13.9",
    "bootswatch" to "4.3.1",
    "creationix" to "gist-7435851",
    "fontawesome-free" to "5.10.2",
    "jquery" to "3.4.1",
    "jscookie" to "2.2.1",
    "js-sha256" to "0.9.0",
    "popper" to "1.15.0",
    "sweetalert2" to "8.16.3",
    "toastr" to "2.1.4",
    "tooltip" to "1.3.2",
    "waitme" to "1.19"
)

fun HEAD.loadCSS(pkg: String, file: String, version: String? = null) {
    val defVersion = version ?: PACKAGES_VERSIONS[pkg]
    link("packages/$pkg/$defVersion/css/$file", "stylesheet")
}

fun BODY.loadJS(pkg: String, file: String, version: String? = null) {
    val defVersion = version ?: PACKAGES_VERSIONS[pkg]
    script("application/javascript", "packages/$pkg/$defVersion/js/$file") { }
}

fun HEAD.commonCSS() {
    loadCSS("bootstrap", "bootstrap.min.css")
    loadCSS("bootswatch", "darkly.min.css")
    loadCSS("fontawesome-free", "all.min.css")
    loadCSS("toastr", "toastr.min.css")

    loadCSS("_diverta-cart", "main.css")
}

fun BODY.commonJS() {
    loadJS("jquery", "jquery-${PACKAGES_VERSIONS["jquery"]}.min.js")
    loadJS("tooltip", "tooltip.min.js")
    loadJS("popper", "popper.min.js")
    loadJS("bootstrap", "bootstrap.bundle.min.js")
    loadJS("fontawesome-free", "all.min.js")
    loadJS("toastr", "toastr.min.js")
    loadJS("accounting.js", "accounting.min.js")

    loadJS("_diverta-cart", "main.js")
}