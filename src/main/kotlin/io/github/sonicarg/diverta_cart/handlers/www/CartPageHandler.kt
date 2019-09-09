package io.github.sonicarg.diverta_cart.handlers.www

import io.javalin.http.Context
import kotlinx.html.*
import kotlinx.html.stream.createHTML

object CartPageHandler {
    fun show(ctx: Context) {
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


                }
                footer()
                commonJS()
            }
        }
        ctx.status(200).contentType("text/html").result(renderedHTML)
    }
}