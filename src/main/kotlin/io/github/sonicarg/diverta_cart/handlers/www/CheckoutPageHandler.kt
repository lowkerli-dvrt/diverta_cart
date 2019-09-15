package io.github.sonicarg.diverta_cart.handlers.www

import io.github.sonicarg.diverta_cart.Prefecture
import io.github.sonicarg.diverta_cart.PrefectureTable
import io.javalin.http.Context
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object CheckoutPageHandler {
    fun show(ctx: Context) {
        val total = ctx.formParam("tt")?.toLongOrNull()
        val region = ctx.formParam("region")?.toIntOrNull()
        if (total == null || region == null) {
            ctx.status(500)
        }

        val prefectures = transaction {
            PrefectureTable.select {
                PrefectureTable.region eq region!!
            }.map {
                Prefecture.fromResultRow(it)
            }.toList()
        }

        val renderedHTML = createHTML().html {
            head {
                meta { charset = "UTF-8" }
                title("NipponTech | Home page")
                link("/favicon.ico", "icon")
                commonCSS()
                loadCSS("waitme", "waitme.min.css")
            }
            body {
                navbar(ctx)
                main {
                    a { id = "top" }

                    // Content of page begins here
                    p("d-none") {
                        id = "region"
                        text(region!!)
                    }

                    h2 { text("Shipping details & checkout") }
                    form() {
                        id = "shippingPurchaseForm"
                        div("row") {
                            div("col-1")
                            div("col-5") {
                                id = "shippingWrapper"
                                h4 { text("Shipping information") }
                                div("d-none") {
                                    id = "deliveryInPlace"
                                    text("You have chosen to pick your products from our local. If this is not your intention, please ")
                                    a("javascript:") {
                                        onClick = "history.go(-1)"
                                        text("go back to the cart and choose the correct region.")
                                    }
                                    hr { }
                                }
                                div("d-none") {
                                    id = "deliveryTokyo"
                                    div("form-group") {
                                        label {
                                            htmlFor = "dropdownPrefecture_1_2"
                                            text("Prefecture (都)")
                                        }
                                        select("form-control select-picker") {
                                            id = "dropdownPrefecture_1_2"
                                            disabled = true
                                            option {
                                                value = "13"
                                                text("Tōkyō Metropolitan Prefecture")
                                            }
                                        }
                                    }
                                    div("form-group") {
                                        label {
                                            htmlFor = "ward_1_2"
                                            text("Ward (区)")
                                        }
                                        textInput(classes = "form-control") {
                                            id = "ward_1_2"
                                            placeholder = "e.g. Shinjuku"
                                        }
                                    }
                                    div("form-group") {
                                        label {
                                            htmlFor = "section_1_2"
                                            text("Section (町)")
                                        }
                                        textInput(classes = "form-control") {
                                            id = "section_1_2"
                                            placeholder = "e.g. Nishishinjuku"
                                        }
                                    }
                                    div("form-group") {
                                        label {
                                            htmlFor = "choumeBanGo_1_2"
                                            text("Sector-Block-House (丁目-番号)")
                                        }
                                        textInput(classes = "form-control") {
                                            id = "choumeBanGo_1_2"
                                            placeholder = "e.g. 5-3-1"
                                        }
                                    }
                                    div("form-group") {
                                        label {
                                            htmlFor = "postCode_1_2"
                                            text("Postal Code")
                                        }
                                        textInput(classes = "textinput-numeric form-control") {
                                            id = "postCode_1_2"
                                            minLength = "7"
                                            maxLength = "7"
                                            required = true
                                        }
                                        small("form-text text-muted") {
                                            id = "postCode_1_2Helper"
                                            text("Must be 7 digits")
                                        }
                                    }
                                }
                                div("d-block") {
                                    id = "deliveryNational"
                                    div("form-group") {
                                        label {
                                            htmlFor = "dropdownPrefecture"
                                            text("Prefecture (県/道/府)")
                                        }
                                        select("form-control select-picker") {
                                            id = "dropdownPrefecture"
                                            option {
                                                selected = true
                                                disabled = true
                                                text("Select a prefecture...")
                                            }
                                            for (pr in prefectures) {
                                                option {
                                                    value = "${pr.id}"
                                                    text(pr.name)
                                                }
                                            }
                                        }
                                    }
                                    div("form-group") {
                                        label {
                                            htmlFor = "city"
                                            text("City (市)")
                                        }
                                        textInput(classes = "form-control") {
                                            id = "city"
                                            placeholder = "e.g. Kyoto"
                                        }
                                    }
                                    div("form-group") {
                                        label {
                                            htmlFor = "ward"
                                            text("Ward (区)")
                                        }
                                        textInput(classes = "form-control") {
                                            id = "ward"
                                            placeholder = "e.g. Higashiyama"
                                            attributes["aria-describedby"] = "wardHelp"
                                        }
                                        small(classes = "form-text text-muted") {
                                            id = "wardHelp"
                                            text("Only if applies")
                                        }
                                    }
                                    div("form-group") {
                                        label {
                                            htmlFor = "section"
                                            text("Section (町/字)")
                                        }
                                        textInput(classes = "form-control") {
                                            id = "section"
                                            placeholder = "e.g. Kiyomizu"
                                        }
                                    }
                                    div("form-group") {
                                        label {
                                            htmlFor = "choumeBanGo"
                                            text("Sector-Block-House (丁目-番号)")
                                        }
                                        textInput(classes = "form-control") {
                                            id = "choumeBanGo"
                                            placeholder = "e.g. 1-294"
                                        }
                                    }
                                    div("form-group") {
                                        label {
                                            htmlFor = "postCode"
                                            text("Postal Code")
                                        }
                                        textInput(classes = "textinput-numeric form-control") {
                                            id = "postCode"
                                            minLength = "7"
                                            maxLength = "7"
                                            required = true
                                        }
                                        small("form-text text-muted") {
                                            id = "cardNumberHelper"
                                            text("Must be 7 digits")
                                        }
                                    }
                                }
                            }
                            div("col-5") {
                                h4 { text("Payment information") }
                                div("row") {
                                    div("col-12 form-group") {
                                        label {
                                            htmlFor = "cardHolder"
                                            text("Card holder")
                                        }
                                        textInput(classes = "form-control") {
                                            name = "cardHolder"
                                            id = "cardHolder"
                                            minLength = "3"
                                            required = true
                                        }
                                        small("form-text text-muted") {
                                            id = "cardHolderHelper"
                                            text("Write it as is on the card")
                                        }
                                        div("invalid-feedback") {
                                            id = "cardHolderInvalid"
                                            text("Something is wrong; there should be three characters at least")
                                        }
                                    }
                                }
                                div("row") {
                                    div("col-12 form-group") {
                                        label {
                                            htmlFor = "cardNumber"
                                            text("Card number")
                                        }
                                        textInput(classes = "textinput-numeric form-control") {
                                            name = "cardNumber"
                                            id = "cardNumber"
                                            minLength = "12"
                                            maxLength = "19"
                                            required = true
                                        }
                                        small("form-text text-muted") {
                                            id = "cardNumberHelper"
                                            text("Must be between 12 and 19 digits")
                                        }
                                        div("invalid-feedback") {
                                            id = "cardNumberInvalid"
                                            text("Something is wrong; check if card number is written properly")
                                        }
                                    }
                                }
                                div("row") {
                                    div("col-6 form-group") {
                                        label {
                                            htmlFor = "cardDueDate"
                                            text("Card due date")
                                        }
                                        monthInput(classes = "form-control") {
                                            name = "cardDueDate"
                                            id = "cardDueDate"
                                        }
                                    }
                                    div("col-6 form-group") {
                                        label {
                                            htmlFor = "cardControlCode"
                                            text("CVV2")
                                        }
                                        passwordInput(classes = "textinput-numeric form-control") {
                                            name = "cardControlCode"
                                            id = "cardControlCode"
                                            minLength = "3"
                                            maxLength = "4"
                                            required = true
                                        }
                                        small("form-text text-muted") {
                                            id = "cardControlCodeHelper"
                                            text("Three or four digits on the reverse of the card")
                                        }
                                    }
                                }
                                div("row") {
                                    div("col-12 form-group") {
                                        label {
                                            htmlFor = "email"
                                            text("E-Mail address")
                                        }
                                        textInput(classes = "form-control") {
                                            name = "email"
                                            id = "email"
                                            required = true
                                        }
                                        small("form-text text-muted") {
                                            id = "emailHelper"
                                            text("Here you will receive both the invoice as well as the tracking code")
                                        }
                                        div("invalid-feedback") {
                                            id = "emailInvalid"
                                            text("Something is wrong; check if email is written properly")
                                        }
                                    }
                                }
                                div("row") {
                                    div("col-6")
                                    div("col-6") {
                                        hiddenInput {
                                            name = "amount"
                                            value = total!!.toString()
                                        }
                                        button(classes = "btn btn-lg btn-block btn-success") {
                                            id = "confirmCheck"
                                            text("Finish and pay ")
                                            i("far fa-credit-card")
                                        }
                                    }
                                }
                            }
                            div("col-1")
                        }
                    }
                }
                div("modal fade") {
                    id = "preConfirmModal"
                    attributes.apply {
                        put("tabIndex", "-1")
                        put("role", "dialog")
                        put("aria-labelledby", "preConfirmModal")
                        put("aria-hidden", true.toString())
                    }
                    div("modal-dialog") {
                        attributes["role"] = "document"
                        div("modal-content") {
                            div("modal-header") {
                                h5("modal-title") {
                                    id = "preConfirmModalLabel"
                                    text("Ready to confirm purchase?")
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
                                span("price") {
                                    text(total!!)
                                }
                                text(" will be charged to the credit card ")
                                span {
                                    id = "modalPrePurchaseCreditCard"
                                    text("<#>")
                                }
                                text(" owned by ")
                                span {
                                    id = "modalPrePurchaseName"
                                    text("<a>")
                                }
                                text(". Proceed?")
                            }
                            div("modal-footer") {
                                button(classes = "btn btn-danger") {
                                    attributes["data-dismiss"] = "modal"
                                    text("Cancel")
                                }
                                button(classes = "btn btn-success") {
                                    id = "btnModal_purchaseConfirm"
                                    text("Confirm")
                                }
                            }
                        }
                    }
                }
                div("modal fade") {
                    id = "postConfirmModal"
                    attributes.apply {
                        put("tabIndex", "-1")
                        put("role", "dialog")
                        put("aria-labelledby", "postConfirmModal")
                        put("aria-hidden", true.toString())
                    }
                    div("modal-dialog") {
                        attributes["role"] = "document"
                        div("modal-content") {
                            div("modal-header") {
                                h5("modal-title") {
                                    id = "postConfirmModalLabel"
                                    text("Purhcase completed successfully!")
                                }
                            }
                            div("modal-body") {
                                text(
                                    "Thank you for your purchase! In a few minutes, you will receive the invoice and " +
                                    "the tracking code in the mail address given ("
                                )
                                span("text-monospace") {
                                    id = "postConfirmEmail"
                                    text("<test@email>")
                                }
                                text(").")
                                br { }
                                span("font-italic") {
                                    text(
                                        "Please, check your SPAM folder in case it did not arrive into your inbox " +
                                            "directly."
                                    )
                                }
                            }
                            div("modal-footer") {
                                button(classes = "btn btn-success") {
                                    id = "btnModal_purchaseFinish"
                                    text("Return to home")
                                }
                            }
                        }
                    }
                }
                footer()
                commonJS()
                loadJS("waitme", "waitme.min.js")
                loadJS("_diverta-cart", "checkout.js")
            }
        }
        ctx.status(200).contentType("text/html").result(renderedHTML)
    }

    fun performPayment(ctx: Context) {
        ctx.status(418)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}