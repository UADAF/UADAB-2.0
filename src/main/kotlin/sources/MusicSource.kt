package sources

import music.MusicHandler
import uadamusic.MusicContext
import java.nio.file.Paths


object MusicSource : BasicExternalSource<MusicContext>() {

    override suspend fun load(): MusicContext {
        return MusicContext(Paths.get(UADAB.cfg.musicDir))
    }

    override suspend fun reload() {
        super.reload()
        MusicHandler.loadContext()
    }

}