package io.github.sonicarg.diverta_cart

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.json.JSONObject
import java.sql.SQLException
import kotlin.system.exitProcess

/*
    This file contains the definitions of the database's tables for being used along
    with Exposed library. It'll also contain some objects to convert between database
    rows to objects Kotlin can handle and vice-versa. Notice these objects can also
    be transformed into JSON objects for easily send to the client through AJAX.
 */

object ProductsTable : Table("products") {
    val sku = long("sku").primaryKey()
    val name = varchar("name", 64)
    val price = long("price")
}

data class Product(
    var sku: Long,
    var name: String,
    var price: Long
) {
    companion object {
        fun fromResultRow(rr: ResultRow) = Product(
            rr[ProductsTable.sku],
            rr[ProductsTable.name],
            rr[ProductsTable.price]
        )

        fun fromJSON(json: JSONObject) = Product(
            json.getLong("sku"),
            json.getString("name"),
            json.getLong("price")
        )
    }
}

object ShippingRegionTable: Table("shipping_region") {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", 64)
}

data class ShippingRegion(
    var id: Int,
    var name: String
) {
    fun fromResultRow(rr: ResultRow) = ShippingRegion(
        rr[ShippingRegionTable.id],
        rr[ShippingRegionTable.name]
    )

    fun fromJSON(json: JSONObject) = ShippingRegion(
        json.getInt("id"),
        json.getString("name")
    )
}

object ShippingPriceTable : Table("shipping") {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", 96)
    //Foreign key: Column name, Referenced Column, On Delete, On Update
    val region = reference("region", ShippingRegionTable.id, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)
    val price = long("price")
    val delay = integer("delay")
}

data class ShippingPrice(
    var id: Int,
    var name: String,
    var region: Int,
    var price: Long,
    var delay: Int
) {
    fun fromResultRow(rr: ResultRow) = ShippingPrice(
        rr[ShippingPriceTable.id],
        rr[ShippingPriceTable.name],
        rr[ShippingPriceTable.region],
        rr[ShippingPriceTable.price],
        rr[ShippingPriceTable.delay]
    )

    fun fromJSON(json: JSONObject) = ShippingPrice(
        json.getInt("id"),
        json.getString("name"),
        json.getInt("region"),
        json.getLong("price"),
        json.getInt("delay")
    )
}

fun databaseInitIfEmpty() {
    transaction {
        try {
            SchemaUtils.create(
                ProductsTable,
                ShippingRegionTable,
                ShippingPriceTable
            )

            ShippingRegionTable.batchInsert(
                listOf(
                    ShippingRegion(1, "Tōkyō Metropolitan Area"),
                    ShippingRegion(2, "North bound, servicing from Tōkyō"),
                    ShippingRegion(3, "North bound, servicing from Hokkaidō"),
                    ShippingRegion(4, "South-west bound, servicing from Tōkyō"),
                    ShippingRegion(5, "South-west bound, servicing from Central Kansai"),
                    ShippingRegion(6, "South-west bound, servicing from Kyūshū")
                )
            ) {
                this[ShippingRegionTable.id] = it.id
                this[ShippingRegionTable.name] = it.name
            }

            ShippingPriceTable.batchInsert(
                listOf(
                    //Reminder: ID, Name, Price (JPY), Business days for delivery
                    ShippingPrice( 1, "In-place take away", 1, 0, 0),
                    ShippingPrice( 2, "Tōkyō Metropolitan Area", 1, 750, 1),
                    ShippingPrice( 3, "Southern Kantō (Chiba, Kanagawa, Saitama)", 2, 1000, 1),
                    ShippingPrice( 4, "Northern Kantō (Gunma, Ibaraki, Tochigi)", 2, 1250, 1),
                    ShippingPrice( 5, "Southern Tōhoku (Fukushima, Miyagi, Yamagata)", 2, 1500, 1),
                    ShippingPrice( 6, "Hokkaidō", 3, 1750, 2),
                    ShippingPrice( 7, "Northern Tōhoku (Akita, Aomori, Iwate)", 3, 2000, 2),
                    ShippingPrice( 9, "South-Eastern Chūbu (Fukui, Ishikawa, Nagano, Niigata, Toyama)", 4, 1250, 1),
                    ShippingPrice( 8, "North-Western Chūbu (Aichi, Gifu, Shizuoka, Yamanashi)", 4, 1500, 1),
                    ShippingPrice(10, "Central Kansai (Kyōto, Nara, Ōsaka)", 5, 1500, 2),
                    ShippingPrice(11, "Outer Kansai (Hyōgo, Mie, Shiga, Wakayama)", 5, 1750, 2),
                    ShippingPrice(12, "Eastern Chūgoku (Okayama, Tottori)", 5, 1750, 2),
                    ShippingPrice(13, "Western Chūgoku (Hiroshima, Shimane, Yamaguchi)", 5, 2000, 2),
                    ShippingPrice(14, "Shikoku island (Ehime, Kagawa, Kōchi, Tokushima)", 5, 2000, 2),
                    ShippingPrice(15, "Mainland Kyūshū (Fukuoka, Kagoshima, Kumamoto, Miyazaki, Ōita, Nagasaki, Saga)", 6, 1750, 2),
                    ShippingPrice(16, "Insular Kyūshū and minor islands (Okinawa, Ogasawara)", 6, 2500, 3)
                )
            ) {
                this[ShippingPriceTable.id] = it.id
                this[ShippingPriceTable.name] = it.name
                this[ShippingPriceTable.region] = it.region
                this[ShippingPriceTable.price] = it.price
                this[ShippingPriceTable.delay] = it.delay
            }
        }
        catch (e: SQLException) {
/*
    Skip it, as this is caused by duplicate keys, which means our data was previously inside
    the database
*/
        }
        catch (e: Exception) {
            //The rest of exceptions should be captured and force the app to close
            APP_LOGGER.error("An exception has been caught while intializing. Exception dump follows:", e)
            exitProcess(0)
        }
    }
}