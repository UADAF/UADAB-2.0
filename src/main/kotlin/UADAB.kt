import com.google.gson.JsonObject
import io.ktor.client.HttpClient
import io.ktor.client.engine.jetty.Jetty
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.hooks.AnnotatedEventManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.ConfigUtils

object UADAB {

    lateinit var cfg: Config
        private set

    lateinit var bot: JDA
        private set

    val log: Logger = LoggerFactory.getLogger("UADAB")

    val http = HttpClient(Jetty)

    @JvmStatic
    fun main(args: Array<String>) {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out") //Redirect slf4j-simple to System.out from System.err
        cfg = ConfigUtils.loadConfig(Config::class.java, "config.json", JsonObject())
        bot = JDABuilder(cfg.token)
            .setBulkDeleteSplittingEnabled(false)
            .setGame(Game.watching("за своей загрузкой"))
            .setEventManager(AnnotatedEventManager())
            .addEventListener(EventListener, Reactions)
            .setStatus(OnlineStatus.DO_NOT_DISTURB)
            .build()
    }

}