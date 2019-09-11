package io.github.sonicarg.diverta_cart.handlers.www

import io.javalin.http.Context
import kotlinx.html.*
import kotlinx.html.stream.createHTML

object CheckoutPageHandler {
    fun show(ctx: Context) {
        val total = ctx.formParam("tt")?.toLongOrNull()
        val region = ctx.formParam("region")?.toIntOrNull()
        if (total == null || region == null) {
            ctx.status(500)
        }

        val renderedHTML = createHTML().html {
            head {
                meta { charset = "UTF-8" }
                title("NipponTech | Home page")
                link("/favicon.ico", "icon")
                commonCSS()
            }
            body {
                navbar(ctx)
                main {
                    a { id = "top" }

                    // Content of page begins here
                    p { text("Total: $total")}
                    p { text("Region: $region")}

                }
                footer()
                commonJS()
            }
        }
        ctx.status(200).contentType("text/html").result(renderedHTML)
    }

    fun performPayment(ctx: Context) {
        ctx.status(418)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}