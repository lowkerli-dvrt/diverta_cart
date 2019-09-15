package io.github.sonicarg.diverta_cart.handlers.rest

import io.javalin.http.Context

object MethodNotAllowedErrorHandler {
    fun showError(ctx: Context) {
        ctx.status(405).json(mapOf(
            "code" to "405",
            "message" to "Method not allowed for this request."
        ))
    }
}