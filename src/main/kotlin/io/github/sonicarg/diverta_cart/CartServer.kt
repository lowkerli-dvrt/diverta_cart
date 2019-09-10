package io.github.sonicarg.diverta_cart

import io.github.sonicarg.diverta_cart.handlers.rest.CartHandler
import io.github.sonicarg.diverta_cart.handlers.rest.CheckoutHandler
import io.github.sonicarg.diverta_cart.handlers.www.CartPageHandler
import io.github.sonicarg.diverta_cart.handlers.www.CheckoutPageHandler
import io.github.sonicarg.diverta_cart.handlers.www.MainPageHandler
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.staticfiles.Location
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

class CartServer(configJSON: JSONObject = JSONObject()) : HttpServlet() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val driver = configJSON.getJSONObject("database").getString("driver")
    private val port = configJSON.getJSONObject("server").getInt("port")
    private val url: String
    private val server: Javalin
    private val vat: Double

    init {
        val urlPrefix = configJSON.getJSONObject("database").getString("url_prefix")
        val database = configJSON.getJSONObject("database").getString("database")
        val server = configJSON.getJSONObject("database").getString("server")
        val user = configJSON.getJSONObject("database").getString("user")
        val password = configJSON.getJSONObject("database").getString("password")
        val serverTimeZome = configJSON.getJSONObject("database").getString("serverTimezone")

        url = "$urlPrefix://$server/$database?" +
                "user=$user&password=$password&" +
                "useLegacyDatetimeCode=false&serverTimezone=$serverTimeZome&" +
                "useUnicode=true&characterEncoding=utf-8"

        vat = configJSON.getJSONObject("ui").getDouble("vat")

        this.server = Javalin.create { config ->

            config.showJavalinBanner = false
            config.enableCorsForAllOrigins()
            config.addStaticFiles("static_www", Location.CLASSPATH)
            config.addStaticFiles("var_www", Location.EXTERNAL)

            config.sessionHandler(this::serverSessionHandler)
            config.requestLogger { ctx, timeMs ->
                val message = if (ctx.queryString().isNullOrEmpty()) {
                    "${ctx.method()} ${ctx.path()} = ${ctx.status()} ($timeMs ms)"
                } else {
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

                //Update VAT value directly in the context
                val contextVAT = ctx.sessionAttribute<Double?>("vat")
                if ( contextVAT == null || contextVAT != vat) {
                    ctx.sessionAttribute("vat", vat)
                }
            }

            // === Web handlers ===
            path("/") {
                // Homepage showing all elements for buy
                get { ctx -> MainPageHandler.show(ctx) }
            }
            path("/cart") {
                get { ctx -> CartPageHandler.show(ctx) } // CartPageHandler.show(ctx) }
            }
            path("/checkout") {
                get { ctx -> CheckoutPageHandler.show(ctx) } // CheckoutPageHandler.show(ctx) }
                post { ctx -> CheckoutPageHandler.performPayment(ctx) } // CheckoutPageHandler.doPayment(ctx) }
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
    }

    fun start(port: Int = this.port) {
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
                this.storeDir = File(baseDir, "diverta-cart.javalin-session-store").apply { mkdir() }
            }
        }
        httpOnly = true
    }
}
