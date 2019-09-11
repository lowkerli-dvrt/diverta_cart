package io.github.sonicarg.diverta_cart.handlers.www

import io.github.sonicarg.diverta_cart.Product
import io.github.sonicarg.diverta_cart.ProductsTable
import io.github.sonicarg.diverta_cart.ResourceLoader
import io.javalin.http.Context
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object MainPageHandler {
    private fun productImagePath(sku: String) =
        ResourceLoader.glob("var_www/products", "glob:$sku.*").first().toString().substring(8)

    fun show(ctx: Context) {
        val searchText = ctx.queryParam("q")
        val vat = ctx.sessionAttribute<Double>("vat")!!

        val matchingProducts = transaction {
            (if (searchText != null) {
                ProductsTable.select { ProductsTable.name like "%$searchText%" }
            } else {
                ProductsTable.selectAll()
            }).map {
                Product.fromResultRow(it)
            }
        }.toList()

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
                    a { id = "top" }

                    // Content of page begins here

                    h2 { text("Our products") }

                    div("row") {
                        for (p: Product in matchingProducts) {
                            val priceWithoutVAT = p.price
                            val productVAT = (priceWithoutVAT * vat).toLong()
                            val priceWithVAT = priceWithoutVAT + productVAT
                            div("col-3 mb-3") {
                                div("card") {
                                    img(src = productImagePath(p.sku), alt = p.name, classes = "card-img-top")
                                    div("card-body") {
                                        h5("card-title text-truncate") { text(p.name) }
                                        //DB = Price without the tax - WWW = Price with tax
                                        p("card-text text-right") {
                                            span("tooltip-price") {
                                                attributes.apply {
                                                    put("data-toggle", "tooltip")
                                                    put(
                                                        "title",
                                                        "Price: <span class='price'>$priceWithoutVAT</span><br>" +
                                                                "VAT: <span class='price'>$productVAT</span>"
                                                    )
                                                    put("data-html", true.toString())
                                                }
                                                span("price") { text(priceWithVAT) }
                                                Entities.nbsp
                                                Entities.nbsp
                                                small("price_tooltip") { text(" - VAT incl.") }
                                            }
                                        }
                                        div("text-right") {
                                            a(classes = "btn btn-sm btn-success") {
                                                attributes.apply {
                                                    put("data-toggle", "tooltip")
                                                    put("title", "Add to cart")
                                                }
                                                id = "btn_addProduct_${p.sku}"
                                                i("fas fa-plus")
                                            }
                                        }
                                    }
                                }
                            }
                        }
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