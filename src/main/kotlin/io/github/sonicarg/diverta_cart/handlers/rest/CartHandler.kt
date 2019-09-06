package io.github.sonicarg.diverta_cart.handlers.rest

import io.github.sonicarg.diverta_cart.Product
import io.github.sonicarg.diverta_cart.ProductsTable
import io.javalin.http.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.*

object CartHandler {
    private fun initGetCart(ctx: Context): MutableMap<Product, Int> {
        if ("cart" !in ctx.sessionAttributeMap<MutableMap<Product, Int>>()) {
            ctx.sessionAttribute("cart", mutableMapOf<Product, Int>())
        }
        return ctx.sessionAttribute<MutableMap<Product, Int>>("cart")!!
    }

    private fun saveAndSend(ctx: Context, status: Int, message: String, cart: MutableMap<Product, Int>) {
        ctx.sessionAttribute("cart", cart)
        ctx.status(status).json(obj = mapOf(
            "status" to status,
            "message" to message,
            "cart" to ProcessedCart(cart)
        ))
    }

    fun list(ctx: Context) {
        saveAndSend(ctx, 200, "Listing cart contents", initGetCart(ctx))
    }

    fun add(ctx:Context) {
        val cartContents = initGetCart(ctx)
        val sku = ctx.formParam("sku")?.toLongOrNull()
        if (sku == null) {
            saveAndSend(ctx, 400, "No product SKU was given", cartContents)
            return
        }
        val product: Product? = transaction {
            ProductsTable.select { ProductsTable.sku eq sku }.firstOrNull()?.let {
                Product.fromResultRow(it)
            }
        }
        if (product == null) {
            saveAndSend(ctx, 404, "Product was not found", cartContents)
        }
        else {
            if (product in cartContents) {
                cartContents[product] = (cartContents[product] as Int) + 1
            }
            else {
                cartContents[product] = 1
            }
            saveAndSend(ctx, 200, "Product added into cart", cartContents)
        }
    }

    fun changeQty(ctx: Context) {
        val cartContents = initGetCart(ctx)
        val sku = ctx.formParam("sku")?.toLongOrNull()
        if (sku == null) {
            saveAndSend(ctx, 400, "No product SKU was given", cartContents)
            return
        }
        val newQty = ctx.formParam("qty")?.toIntOrNull()
        if (newQty == null) {
            saveAndSend(ctx, 400, "No quantity was given", cartContents)
            return
        }
        if (newQty <= 0) {
            saveAndSend(
                ctx,
                400,
                "Quantity cannot be 0 or negative. " +
                    "If you want to delete a product, send a HTTP DELETE request instead.",
                cartContents
            )
            return
        }
        val product = cartContents.keys.firstOrNull { it.sku == sku }
        if (product != null) {
            cartContents[product] = newQty
            saveAndSend(ctx, 200, "Product quantity modified sucessfully", cartContents)
        }
        else
        {
            saveAndSend(ctx, 404, "Product was not found in cart", cartContents)
        }
    }

    fun remove(ctx: Context) {
        val cartContents = initGetCart(ctx)
        // 'sku' nullity has been already tested, we can assure that 'sku' won't be null (!!)
        val sku = ctx.formParam("sku")!!.toLong()
        val product = cartContents.keys.firstOrNull { it.sku == sku }
        if (product != null) {
            cartContents.remove(product)
            saveAndSend(ctx, 200, "Successfully removed product from cart", cartContents)
        }
        else {
            saveAndSend(ctx, 404, "Product was not found in cart", cartContents)
        }
    }

    fun empty(ctx: Context) {
        val cartContents = initGetCart(ctx)
        cartContents.clear()
        saveAndSend(ctx, 200, "Cart emptied", cartContents)
    }
}

@Suppress("MemberVisibilityCanBePrivate", "unused")
data class ProcessedCart(private val _contents: MutableMap<Product, Int>) {
    val contents = _contents.map {
        mapOf(
            "sku" to it.key.sku,
            "name" to it.key.name,
            "unitPrice" to it.key.price,
            "quantity" to it.value,
            "price" to it.key.price * it.value
        )
    }
    val numElements = contents.size
    val subTotal = _contents.map { it.key.price * it.value }.sum()
    val tax = (subTotal * 0.1).toInt()
}