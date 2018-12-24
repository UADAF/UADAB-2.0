package utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.ktor.client.request.get
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.dv8tion.jda.core.exceptions.HttpException

object HttpCodes {

    private lateinit var codesData: Map<Int, HTTPStatusCode>
    private const val codesURL = "https://raw.githubusercontent.com/for-GET/know-your-http-well/master/json/status-codes.json"

    fun getDataSet(): Deferred<Map<Int, HTTPStatusCode>> {
        return if (::codesData.isInitialized) {
            CompletableDeferred(codesData)
        } else {
            GlobalScope.async { loadCodes() }
        }
    }

    private suspend fun loadCodes(): Map<Int, HTTPStatusCode> {
        try {
            val result = UADAB.http.get<String>(codesURL)
            codesData = UADAB.parser.parse(result).arr
                .map(JsonElement::obj)
                .filter { "x" !in it["code"].str }
                .map(::HTTPStatusCode)
                .associateBy(HTTPStatusCode::code)
            return codesData
        } catch (e: Exception) {
            UADAB.log.error("An exception occurred while http codes load", e)
            throw HttpException(e.message ?: e::class.java.simpleName)
        }
    }
}

data class HTTPStatusCode(val code: Int, val phrase: String, val description: String) {
    constructor(data: JsonObject): this(data["code"].int, data["phrase"].str,
        data["description"].str.removeSurrounding("\"").capitalize())
}