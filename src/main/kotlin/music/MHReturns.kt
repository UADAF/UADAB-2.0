package music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import uadamusic.MusicData


sealed class MusicHandlerRet

class MHSuccess(val playable: Playable) : MusicHandlerRet()

class MHAlreadyInQueue(val playable: Playable) : MusicHandlerRet()

class MHNotFound(val playable: Playable) : MusicHandlerRet()

class MHError(val error: Throwable, val playable: Playable) : MusicHandlerRet()

class MHUnknown(val playable: Playable?) : MusicHandlerRet()

class MHNoMoreTracks : MusicHandlerRet()

sealed class MHMultiLoad(val results: List<MusicHandlerRet>) : MusicHandlerRet()

class MHFullyLoaded(results: List<MusicHandlerRet>) : MHMultiLoad(results)

class MHAllLoaded(results: List<MusicHandlerRet>) : MHMultiLoad(results)

class MHPartiallyLoaded(results: List<MusicHandlerRet>, val loaded: Int) : MHMultiLoad(results)

class MHNotLoaded : MHMultiLoad(emptyList())