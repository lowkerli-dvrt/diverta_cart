package io.github.sonicarg.diverta_cart.handlers.www

import io.github.sonicarg.diverta_cart.ShippingPrice
import io.github.sonicarg.diverta_cart.ShippingPriceTable
import io.github.sonicarg.diverta_cart.ShippingRegion
import io.github.sonicarg.diverta_cart.ShippingRegionTable
import io.javalin.http.Context
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object CartPageHandler {
    fun show(ctx: Context) {
        val vat = ctx.sessionAttribute<Double>("vat")!!

        val shipRegions = transaction {
            ShippingRegionTable.selectAll().orderBy(ShippingRegionTable.id).map {
                ShippingRegion.fromResultRow(it)
            }.toList()
        }
        val shippingPrices = transaction {
            ShippingPriceTable.selectAll().orderBy(ShippingPriceTable.id).map {
                ShippingPrice.fromResultRow(it)
            }.toList()
        }

        val renderedHTML = createHTML().html {
            head {
                meta { charset = "UTF-8" }
                title("Your cart | NipponTech")
                link("/favicon.ico", "icon")
                commonCSS()
                loadCSS("bootstrap-select", "bootstrap-select.min.css")
                loadCSS("waitme", "waitme.min.css")
            }
            body {
                a { id = "top" }
                navbar(ctx)
                main {
                    // Content of page begins here
                    h2 { text("Your cart") }
                    div("row") {
                        div("col") // Ignored for alignment
                        div("col-10") {
                            id = "cart_wrapper"
                            div("row d-table-responsive") {
                                id = "cart_empty"
                                div("col") {
                                    h4 { text("Cart contents") }
                                    div("table-responsive") {
                                        table("table table-hover table-striped") {
                                            thead {
                                                tr {
                                                    th(classes = "text-center") { text("Product") }
                                                    th(classes = "text-center") { text("Unit price") }
                                                    th(classes = "text-center") { text("Quantity") }
                                                    th(classes = "text-center") { text("Total") }
                                                    th(classes = "text-center")
                                                }
                                            }
                                            tbody {
                                                tr {
                                                    td("text-center") {
                                                        colSpan = "5"
                                                        h1 { i("fas fa-shopping-cart") }
                                                        br { }
                                                        h5 {
                                                            text("The cart looks a little lonely here... Why don't you try ")
                                                            a("/") { text("adding stuff in it?") }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            div("row d-none") {
                                id = "cart_not_empty"
                                div("col-9") {
                                    h4 { text("Cart contents") }
                                    div("table-responsive") {
                                        table("table table-hover table-striped") {
                                            thead {
                                                tr {
                                                    th(classes = "text-center") { text("Product") }
                                                    th(classes = "text-center") { text("Unit price") }
                                                    th(classes = "text-center") { text("Quantity") }
                                                    th(classes = "text-center") { text("Total") }
                                                    th(classes = "text-center")
                                                }
                                            }
                                            tbody {
                                                id = "cart_contents_table"
                                            }
                                        }
                                        button(classes = "btn-sm btn-danger mb-3") {
                                            id = "btn_emptyCart"
                                            text("Empty cart")
                                        }
                                    }
                                    h4 { text("Shipping information") }
                                    p {
                                        text("Deliver to: ")
                                        select("select-picker ml-2") {
                                            for (sr in shipRegions) {
                                                optGroup(sr.name) {
                                                    for (sp in shippingPrices.filter { it.region == sr.id }) {
                                                        option {
                                                            value = "${sp.id}"
                                                            text(sp.name)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    p {
                                        text("Your delivery will arrive ")
                                        span {
                                            id = "delay"
                                            text("on the day")
                                        }
                                    }
                                }
                                div("col-3 align-center") {
                                    h4 { text("Summary") }
                                    table("table table-hover table-striped") {
                                        id = "totalsTable"
                                        tbody {
                                            tr {
                                                td { text("Subtotal") }
                                                td("text-right") {
                                                    id = "subtotal"
                                                    span("price") { text(0) }
                                                }
                                            }
                                            tr {
                                                td { text(String.format("VAT (%.2f%%)", vat * 100.0)) }
                                                td("text-right") {
                                                    id = "tax"
                                                    span("price") { text(0) }
                                                }
                                            }
                                            tr {
                                                td { text("Shipping") }
                                                td("text-right") {
                                                    id = "shipping"
                                                    span("price") { text(0) }
                                                }
                                            }
                                            tr {
                                                td { text("Total") }
                                                td("text-right") {
                                                    id = "total"
                                                    span("price") { text(0) }
                                                }
                                            }
                                        }
                                    }
                                    form("checkout", method = FormMethod.post) {
                                        id = "checkoutForm"
                                        hiddenInput {
                                            name = "tt"
                                            id = "checkout_tt"
                                            value = "0"
                                        }
                                        hiddenInput {
                                            name = "region"
                                            id = "checkout_region"
                                            value = "1"
                                        }
                                        button(type = ButtonType.submit, classes = "btn btn-success float-right") {
                                            formAction = "/checkout"
                                            formMethod = ButtonFormMethod.post
                                            id = "checkoutButton"
                                            text("Proceed to checkout ")
                                            i("fas fa-arrow-right")
                                        }
                                    }
                                }
                            }
                        }
                        div("col") // Ignored for alignment
                    }

                    // Modal for product deletion
                    div("modal fade") {
                        id = "removeProductModal"
                        attributes.apply {
                            put("tabIndex", "-1")
                            put("role", "dialog")
                            put("aria-labelledby", "removeProductModal")
                            put("aria-hidden", true.toString())
                        }
                        div("modal-dialog") {
                            attributes["role"] = "document"
                            div("modal-content") {
                                div("modal-header") {
                                    h5("modal-title") {
                                        id = "removeProductModalLabel"
                                        text("Remove product?")
                                    }
                                    button(classes = "close") {
                                        attributes.apply {
                                            put("data-dismiss", "modal")
                                            put("aria-label", "Close")
                                        }
                                        span {
                                            attributes["aria-hidden"] = true.toString()
                                            Entities.times
                                        }
                                    }
                                }
                                div("modal-body") {
                                    text("Are you sure you want to remove the product ")
                                    span {
                                        id = "modalProductName"
                                        text("Product")
                                    }
                                }
                                div("d-none") {
                                    span {
                                        id = "modalProductSKU"
                                        text("0")
                                    }
                                }
                                div("modal-footer") {
                                    button(classes = "btn btn-secondary") {
                                        attributes["data-dismiss"] = "modal"
                                        text("Keep in cart")
                                    }
                                    button(classes = "btn btn-danger") {
                                        id = "btnModal_removeConfirm"
                                        text("Remove it")
                                    }
                                }
                            }
                        }
                    }

                    // Modal for cart emptying
                    div("modal fade") {
                        id = "emptyCartModal"
                        attributes.apply {
                            put("tabIndex", "-1")
                            put("role", "dialog")
                            put("aria-labelledby", "emptyCartModal")
                            put("aria-hidden", true.toString())
                        }
                        div("modal-dialog") {
                            attributes["role"] = "document"
                            div("modal-content") {
                                div("modal-header") {
                                    h5("modal-title") {
                                        id = "emptyCartModalLabel"
                                        text("Empty cart?")
                                    }
                                    button(classes = "close") {
                                        attributes.apply {
                                            put("data-dismiss", "modal")
                                            put("aria-label", "Close")
                                        }
                                        span {
                                            attributes["aria-hidden"] = true.toString()
                                            Entities.times
                                        }
                                    }
                                }
                                div("modal-body") {
                                    text("Are you sure you want to empty the cart? There's no way back...!")
                                }
                                div("modal-footer") {
                                    button(classes = "btn btn-secondary") {
                                        attributes["data-dismiss"] = "modal"
                                        text("Cancel")
                                    }
                                    button(classes = "btn btn-danger") {
                                        id = "btnModal_emptyConfirm"
                                        text("Empty the cart")
                                    }
                                }
                            }
                        }
                    }
                }
                footer()
                commonJS()
                loadJS("bootstrap-select", "bootstrap-select.min.js")
                loadJS("waitme", "waitme.min.js")
                loadJS("_diverta-cart", "cart.js")
            }
        }
        ctx.status(200).contentType("text/html").result(renderedHTML)
    }
}