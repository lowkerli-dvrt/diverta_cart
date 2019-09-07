package io.github.sonicarg.diverta_cart.handlers.rest

import io.javalin.http.Context
import org.joda.time.format.DateTimeFormat
import kotlin.random.Random

object CheckoutHandler {
    private val DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM")

    private val MESSAGES = arrayOf(
        "Approved",
        "Authorization required",
        "Wrong CVV2 code",
        "Unexpected error",
        "Not enough funds",
        "Expired",
        "Input data error"
    )
    private val CARD_CODES = arrayOf(0, 13, 82, 6, 51, 54, 14)
    private val HTTP_CODES = arrayOf(200, 401, 401, 500, 403, 403, 400)

    /*
    This is the Luhn algorithm, which is used by many credit and debit card brands and
    it is used to check whether the inputted number is correct.
    Luhn algorithm works the following way:
        - Starting from the rightmost number, sum all numbers skipping one in between
        - Again, starting from the second rightmost number, sum all numbers multiplied
            by 2, skipping one in between. If any of the results of multiplying per 2
            gives a number greater or equal than 10, sum the two digits of the result.
        - Take the sums of the previous two points and sum them together.
        - Take the modulo 10 of the previous result (a.k.a. the last digit)
        - Luhn test passes only if this digit is zero.
     */
    private fun luhnTest(s: String): Boolean {
        fun sumDigits(n: Int) = n / 10 + n % 10
        val t = s.reversed()
        val s1 = t.filterIndexed { i, _ -> i % 2 == 0 }.sumBy { it - '0' }
        val s2 = t.filterIndexed { i, _ -> i % 2 == 1 }.map { sumDigits((it - '0') * 2) }.sum()
        return (s1 + s2) % 10 == 0
    }

    private fun sendResponse(ctx: Context, status: Int, message: String, result_data: PaymentResultData) {
        ctx.status(status).json(
            obj = mapOf(
                "status" to status,
                "message" to message,
                "result_data" to result_data
            )
        )
    }

    fun doPayment(ctx: Context) {
        val amount = ctx.formParam("amount")?.toLongOrNull()
        val cardHolder = ctx.formParam("cardHolder")
        val cardNumber = ctx.formParam("cardNumber")
        val cardDueDate = ctx.formParam("cardDueDate")?.let { DATE_FORMATTER.parseDateTime(it) }
        val cardControlCode = ctx.formParam("cardControlCode")?.toIntOrNull()
        val email = ctx.formParam("email")
        //val currency = "JPY"

        // INPUT TEST ERRORS
        if (
            listOfNotNull(amount, cardHolder, cardNumber, cardDueDate, cardControlCode, email)
                .size < 6
        ) {
            //If there are any null values among these, we must return a 400
            sendResponse(
                ctx,
                HTTP_CODES[6],
                "Input data incomplete; one of the input values is missing",
                PaymentResultData(CARD_CODES[6], MESSAGES[6])
            )
            return
        }
        /*
        This 'if' condition will fail whether:
        - The input contain any non-digit character; or
        - The Luhn check algorithm fails (see above)
         */
        if (!(cardNumber!!.all { it.isDigit() }) && luhnTest(cardNumber)) {
            sendResponse(
                ctx,
                HTTP_CODES[6],
                "Wrong input value for card number",
                PaymentResultData(CARD_CODES[6], MESSAGES[6])
            )
            return
        }
        if (cardControlCode!! < 0 || cardControlCode > 9999) {
            sendResponse(
                ctx,
                HTTP_CODES[6],
                "Wrong input value for card control code",
                PaymentResultData(CARD_CODES[6], MESSAGES[6])
            )
            return
        }

        // EXPIRED CARD
        if (cardDueDate!!.isBeforeNow) {
            sendResponse(
                ctx,
                HTTP_CODES[5],
                "Purchase failed, see additional data for extra info",
                PaymentResultData(CARD_CODES[5], MESSAGES[5])
            )
            return
        }

        // NOT ENOUGH FUNDS (who would have more than 1 billion JPY in credit?)
        if (amount!! > 1e9) {
            sendResponse(
                ctx,
                HTTP_CODES[4],
                "Purchase failed, see additional data for extra info",
                PaymentResultData(CARD_CODES[4], MESSAGES[4])
            )
            return
        }

        /*
        The other errors are random, with a uniform distribution between 0 and 1 and the
        following probabilities:
            - Approved = 80%
            - Authorization required = 10%
            - Wrong CVV2 code = 5%
            - Unexpected error = 5%
         */
        val result = when (Random(System.currentTimeMillis()).nextFloat()) {
            in 0.00..0.80 -> 0
            in 0.80..0.90 -> 1
            in 0.90..0.95 -> 2
            else -> 3
        }
        sendResponse(
            ctx,
            HTTP_CODES[result],
            "Purchase ${if (result == 0) "succeeded" else "failed"}, see additional data for extra info",
            PaymentResultData(CARD_CODES[result], MESSAGES[result])
        )
        if (result == 0) {
            ctx.req.session.invalidate()  // Reset session to a new customer
        }
    }
}

data class PaymentResultData(
    val code: Int,
    val message: String
)