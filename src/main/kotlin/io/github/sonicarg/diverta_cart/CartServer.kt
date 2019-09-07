package io.github.sonicarg.diverta_cart

import io.github.sonicarg.diverta_cart.handlers.rest.CartHandler
import io.github.sonicarg.diverta_cart.handlers.rest.CheckoutHandler
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import org.eclipse.jetty.server.session.DefaultSessionCache
import org.eclipse.jetty.server.session.FileSessionDataStore
import org.eclipse.jetty.server.session.SessionHandler
import org.jetbrains.exposed.sql.Database
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServlet
import kotlin.system.exitProcess

object CartServer: HttpServlet() {
    var config = JSONObject()

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val server = Javalin.create { config ->

        config.enableCorsForAllOrigins()

        config.sessionHandler(CartServer::serverSessionHandler)
        config.requestLogger { ctx, timeMs ->
            val message = if (ctx.queryString().isNullOrEmpty()) {
                "${ctx.method()} ${ctx.path()} = ${ctx.status()} ($timeMs ms)"
            }
            else {
                "${ctx.method()} ${ctx.path()}?${ctx.queryString()} = ${ctx.status()} ($timeMs ms)"
            }
            when (ctx.status()) {
                in 100..199 -> logger.debug(message)
                in 200..399 -> logger.info(message)
                in 400..499 -> logger.warn(message)
                else -> logger.error(message)
            }
        }
    }!!.routes {
        before { ctx ->
            ctx.res.characterEncoding = StandardCharsets.UTF_8.toString()
        }


        // === AJAX handlers ===
        path("/ajax") {
            path("cart") {
                //List all elements on cart
                get { ctx -> CartHandler.list(ctx) }
                //Put an element in the cart given its SKU
                post { ctx -> CartHandler.add(ctx) }
                put { ctx -> CartHandler.add(ctx) }
                //Change the number of elements in the cart of a given SKU
                patch { ctx -> CartHandler.changeQty(ctx) }
                //Remove all instances of a given element in the cart (if a SKU is given)
                //Empty the cart (if no SKU is given)
                delete { ctx ->
                    if ("sku" in ctx.formParamMap()) CartHandler.remove(ctx)
                    else CartHandler.empty(ctx)
                }
            }
            path("checkout") {
                post { ctx -> CheckoutHandler.doPayment(ctx) }
            }
        }
    }!!.events { events ->
        events.serverStarting {
            val driver = config.getJSONObject("database").getString("driver")
            val urlPrefix = config.getJSONObject("database").getString("url_prefix")
            val database = config.getJSONObject("database").getString("database")
            val server = config.getJSONObject("database").getString("server")
            val user = config.getJSONObject("database").getString("user")
            val password = config.getJSONObject("database").getString("password")
            val serverTimeZome = config.getJSONObject("database").getString("serverTimezone")
            val url = "$urlPrefix://$server/$database?" +
                      "user=$user&password=$password&" +
                      "useLegacyDatetimeCode=false&serverTimezone=$serverTimeZome&" +
                      "useUnicode=true&characterEncoding=utf-8"
            Database.connect(url, driver, user, password)
            databaseInitIfEmpty()
        }
        events.serverStartFailed {
            logger.error(
                "There was an error initializing the server. " +
                "Refer to the log file to detect the error that caused it and retry again. " +
                "Application will exit now"
            )
            stop()
            exitProcess(1)
        }
    }

    fun start(port: Int = config.getJSONObject("server").getInt("port")) {
        server.start(port)
    }

    fun stop() {
        server.stop()
    }

/*
    This function acts as a provider for Javalin/Jetty in order to make the session manager
    persistent on disk. This is stored at either "%TEMP%\javalin-session-store" (on Windows)
    or at "/tmp/javalin-session-store" (on Unix-like)
*/
    private fun serverSessionHandler() = SessionHandler().apply {
        sessionCache = DefaultSessionCache(this).apply {
            sessionDataStore = FileSessionDataStore().apply {
                val baseDir = File(System.getProperty("java.io.tmpdir"))
                this.storeDir = File(baseDir, "javalin-session-store").apply { mkdir() }
            }
        }
        httpOnly = true
    }
}
