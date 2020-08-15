import cmd.CommandClient
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import commands.MiscCommands
import commands.MusicCommands
import commands.SystemCommands
import commands.UserCommands
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.hooks.AnnotatedEventManager
import org.jetbrains.exposed.sql.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pl.droidsonroids.jspoon.Jspoon
import utils.ConfigUtils

object UADAB {

    lateinit var cfg: Config
        private set

    lateinit var bot: JDA
        private set

    lateinit var commandClient: CommandClient

    val log: Logger = LoggerFactory.getLogger("UADAB")
    val parser: JsonParser = JsonParser()
    val http = HttpClient(Apache)
    val spoon = Jspoon.create()

    @JvmStatic
    fun main(args: Array<String>) {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out") //Redirect slf4j-simple to System.out from System.err
        cfg = ConfigUtils.loadConfig(Config::class.java, "config.json", JsonObject())
        Database.connect(
            "jdbc:mysql://${cfg.dbHost}:3306/${cfg.dbName}?useUnicode=yes&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=UTC&useSSL=false",
            "com.mysql.jdbc.Driver",
            cfg.dbLogin,
            cfg.dbPass
        )
        commandClient = CommandClient(cfg.prefix)
        commandClient.register(SystemCommands, MiscCommands, MusicCommands, UserCommands)

        bot = JDABuilder(cfg.token)
            .setBulkDeleteSplittingEnabled(false)
            .setGame(Game.watching("за своей загрузкой"))
            .setEventManager(AnnotatedEventManager())
            .addEventListener(EventListener, Reactions)
            .setStatus(OnlineStatus.DO_NOT_DISTURB)
            .build()
    }

}