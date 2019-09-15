package io.github.sonicarg.diverta_cart.handlers.www

import io.javalin.http.Context
import kotlinx.html.*
import kotlinx.html.stream.createHTML

object ErrorPageHandler {
    fun show(ctx: Context) {
        val httpText = mapOf(
            400 to "Seems we are missing a piece of information; if you don't give us all, we can't continue.",
            404 to "Feeling like going nowhere? Yes, this page does not exist.",
            405 to "We can't help; you're asking things in a different way than allowed.",
            500 to "Yes, sorry, we screwed up everything from here. We'll see if we can fix it.",
            503 to "Closed for now; please return in a while."
        )

        val renderedHTML = createHTML().html {
            head {
                meta { charset = "UTF-8" }
                title("Home page | NipponTech")
                link("/favicon.ico", "shortcut icon")
                commonCSS()
            }
            body {
                navbar(ctx)
                main {
                    div("row") {
                        div("col-1")
                        div("col-10 text-center text-danger") {
                            h2 {
                                i("fas fa-exclamation-circle")
                                text(" " + ctx.status())
                            }
                            h4 {
                                text(httpText.getValue(ctx.status()))
                            }
                            a("/") {
                                text("Take me to the main page, please")
                            }
                        }
                        div("col-1")
                    }
                }
                footer()
                commonJS()
                loadJS("_diverta-cart", "product_list.js")
            }
        }
        ctx.status(200).contentType("text/html").result(renderedHTML)
    }
}