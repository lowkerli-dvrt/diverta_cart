package io.github.sonicarg.diverta_cart

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.json.JSONObject
import java.awt.GraphicsEnvironment
import java.sql.SQLException
import javax.swing.JOptionPane
import kotlin.system.exitProcess

/*
    This file contains the definitions of the database's tables for being used along
    with Exposed library. It'll also contain some objects to convert between database
    rows to objects Kotlin can handle and vice-versa. Notice these objects can also
    be transformed into JSON objects for easily send to the client through AJAX.
 */

object ProductsTable : Table("products") {
    val sku = varchar("sku", 20).primaryKey()
    val name = varchar("name", 64)
    val price = long("price")
}

data class Product(
    var sku: String,
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
            json.getString("sku"),
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
    companion object {
        fun fromResultRow(rr: ResultRow) = ShippingRegion(
            rr[ShippingRegionTable.id],
            rr[ShippingRegionTable.name]
        )

        fun fromJSON(json: JSONObject) = ShippingRegion(
            json.getInt("id"),
            json.getString("name")
        )
    }
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
    companion object {
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
}

object PrefectureTable: Table("prefecture") {
    val id = integer("id").primaryKey()
    val name = varchar("name", 96)
    val region = reference("region", ShippingPriceTable.id, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)
}

data class Prefecture(
    var id: Int,
    var name: String,
    var region: Int
) {
    companion object {
        fun fromResultRow(rr: ResultRow) = Prefecture(
            rr[PrefectureTable.id],
            rr[PrefectureTable.name],
            rr[PrefectureTable.region]
        )

        fun fromJSON(json: JSONObject) = Prefecture(
            json.getInt("id"),
            json.getString("name"),
            json.getInt("region")
        )
    }
}

// --- Routine to initialize the database ---
// NB: This does not include products at all
fun databaseInitIfEmpty(cartServer: CartServer) {
    transaction {
        try {
            SchemaUtils.create(
                ProductsTable,
                ShippingRegionTable,
                ShippingPriceTable,
                PrefectureTable
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

            PrefectureTable.batchInsert(
                listOf(
                    Prefecture(1, "Hokkaido", 6),
                    Prefecture(2, "Aomori", 7),
                    Prefecture(3, "Iwate", 7),
                    Prefecture(4, "Miyagi", 5),
                    Prefecture(5, "Akita", 7),
                    Prefecture(6, "Yamagata", 5),
                    Prefecture(7, "Fukushima", 5),
                    Prefecture(8, "Ibaraki", 4),
                    Prefecture(9, "Tochigi", 4),
                    Prefecture(10, "Gunma", 4),
                    Prefecture(11, "Saitama", 3),
                    Prefecture(12, "Chiba", 3),
                    Prefecture(13, "Tōkyō", 2),
                    Prefecture(14, "Kanagawa", 3),
                    Prefecture(15, "Niigata", 7),
                    Prefecture(16, "Toyama", 7),
                    Prefecture(17, "Ishikawa", 7),
                    Prefecture(18, "Fukui", 7),
                    Prefecture(19, "Yamanashi", 8),
                    Prefecture(20, "Nagano", 7),
                    Prefecture(21, "Gifu", 8),
                    Prefecture(22, "Shizuoka", 8),
                    Prefecture(23, "Aichi", 8),
                    Prefecture(24, "Mie", 11),
                    Prefecture(25, "Shiga", 11),
                    Prefecture(26, "Kyōto", 10),
                    Prefecture(27, "Ōsaka", 10),
                    Prefecture(28, "Hyōgo", 11),
                    Prefecture(29, "Nara", 10),
                    Prefecture(30, "Wakayama", 11),
                    Prefecture(31, "Tottori", 12),
                    Prefecture(32, "Shimane", 13),
                    Prefecture(33, "Okayama", 12),
                    Prefecture(34, "Hiroshima", 13),
                    Prefecture(35, "Yamaguchi", 13),
                    Prefecture(36, "Tokushima", 14),
                    Prefecture(37, "Kagawa", 14),
                    Prefecture(38, "Ehime", 14),
                    Prefecture(39, "Kōchi", 14),
                    Prefecture(40, "Fukuoka", 15),
                    Prefecture(41, "Saga", 15),
                    Prefecture(42, "Nagasaki", 15),
                    Prefecture(43, "Kumamoto", 15),
                    Prefecture(44, "Ōita", 15),
                    Prefecture(45, "Miyazaki", 15),
                    Prefecture(46, "Kagoshima", 15),
                    Prefecture(47, "Okinawa", 16)
                )
            ) {
                this[PrefectureTable.id] = it.id
                this[PrefectureTable.name] = it.name
                this[PrefectureTable.region] = it.region
            }
        }
        catch (e: SQLException) {
/*
    Skip it, as this is caused by duplicate keys, which means our data was previously inside
    the database
*/
        } catch (e: ExceptionInInitializerError) {
            APP_LOGGER.error(
                "Error while connecting to the database; please check if it is running and try again."
            )
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(
                    null,
                    "Error while connecting to the database;\n" +
                            "please check if it is running and try again.",
                    "Error on starting server",
                    JOptionPane.ERROR_MESSAGE
                )
            } else {
                readLine()
            }
            cartServer.stop()
            exitProcess(0)
        }
    }
}