package music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import uadamusic.MusicData


sealed class MusicHandlerRet {

    open val path: String? = null
    open val data: MusicData? = null
    open val track: AudioTrack? = null
    open val error: Throwable? = null

}

class MHSuccess(override val path: String, override val data: MusicData, override val track: AudioTrack) : MusicHandlerRet()

class MHAlreadyInQueue(override val path: String, override val data: MusicData, override val track: AudioTrack) : MusicHandlerRet()

class MHNotFound(override val path: String, override val data: MusicData) : MusicHandlerRet()

class MHError(override val error: Throwable, override val data: MusicData) : MusicHandlerRet()

class MHUnknown(override val data: MusicData) : MusicHandlerRet()

class MHNoMoreTracks : MusicHandlerRet()

sealed class MHMultiLoad(val results: List<MusicHandlerRet>) : MusicHandlerRet()

class MHFullyLoaded(results: List<MusicHandlerRet>) : MHMultiLoad(results)

class MHAllLoaded(results: List<MusicHandlerRet>) : MHMultiLoad(results)

class MHPartiallyLoaded(results: List<MusicHandlerRet>, val loaded: Int) : MHMultiLoad(results)

class MHNotLoaded : MHMultiLoad(emptyList())