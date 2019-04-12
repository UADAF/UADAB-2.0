package sources

import uadamusic.MusicContext
import java.nio.file.Paths


object MusicSource : BasicExternalSource<MusicContext>() {

    override suspend fun load(): MusicContext {
        return MusicContext(Paths.get(UADAB.cfg.musicDir))
    }

}