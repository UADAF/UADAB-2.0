package utils

import dao.BashQuote
import io.ktor.client.call.call
import io.ktor.client.response.readText
import io.ktor.client.response.readText

object BashUtils {
    suspend fun fetchQuote(url: String): BashQuote? {
        return try {
            val call = UADAB.http.call(url)
            if (call.request.url.toString() != url) {
                null
            } else{
                UADAB.spoon.adapter(BashQuote::class.java).fromHtml(call.response.readText())
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchQuote(id: Int): BashQuote? =
            BashUtils.fetchQuote("https://bash.im/quote/$id")
}