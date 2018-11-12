import com.google.gson.JsonObject
import utils.ConfigUtils
import utils.str

class Config {

    lateinit var tokenType: String
        private set

    @ConfigUtils.ManualConfigProperty
    lateinit var token: String
        private set


    fun loadManual(json: JsonObject) {
        token = json["${tokenType}_token"].str
    }

}