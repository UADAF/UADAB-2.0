package sources

import UADAB
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.ktor.client.request.get
import utils.arr
import utils.int
import utils.obj
import utils.str


typealias HttpCodeData = Map<Int, HttpCodeSource.HTTPStatusCode>

object HttpCodeSource : BasicExternalSource<HttpCodeData>() {

    private const val codesURL = "https://raw.githubusercontent.com/for-GET/know-your-http-well/master/json/status-codes.json"

    data class HTTPStatusCode(val code: Int, val phrase: String, val description: String) {
        constructor(data: JsonObject): this(data["code"].int, data["phrase"].str,
            data["description"].str.removeSurrounding("\"").capitalize())
    }

    override suspend fun load() = UADAB.parser.parse(UADAB.http.get<String>(codesURL)).arr
            .map(JsonElement::obj)
            .filter { "x" !in it["code"].str }
            .map(::HTTPStatusCode)
            .associateBy(HTTPStatusCode::code)

}