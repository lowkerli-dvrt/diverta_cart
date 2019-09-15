package io.github.sonicarg.diverta_cart.handlers.rest

import io.github.sonicarg.diverta_cart.Prefecture
import io.github.sonicarg.diverta_cart.PrefectureTable
import io.github.sonicarg.diverta_cart.ShippingPriceTable
import io.javalin.http.Context
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ShippingHandler {
    fun list(ctx: Context) {
        val shippingPrices = transaction {
            ShippingPriceTable.slice(
                ShippingPriceTable.id,
                ShippingPriceTable.price,
                ShippingPriceTable.region,
                ShippingPriceTable.delay
            ).selectAll().orderBy(ShippingPriceTable.id).map {
                mapOf(
                    "id" to it[ShippingPriceTable.id],
                    "price" to it[ShippingPriceTable.price],
                    "region" to it[ShippingPriceTable.region],
                    "delay" to it[ShippingPriceTable.delay]
                )
            }.toList()
        }
        val prefectures = transaction {
            PrefectureTable.selectAll().orderBy(PrefectureTable.name).map {
                Prefecture.fromResultRow(it)
            }.toList()
        }
        ctx.status(200).json(mapOf(
            "code" to 200,
            "status" to "Listing all items",
            "data" to mapOf(
                "prices" to shippingPrices,
                "prefectures" to prefectures
            )
        ))
    }
}
