package music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

/**
 * Holder for both the player and a track scheduler for one guild.
 */
class GuildMusicManager(manager: AudioPlayerManager) {
    /**
     * Audio player for the guild.
     */
    val player: AudioPlayer = manager.createPlayer().apply { volume = 10 }
    /**
     * Track scheduler for the player.
     */
    val scheduler: TrackScheduler = TrackScheduler(player)

    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    val sendHandler: AudioPlayerSendHandler by lazy { AudioPlayerSendHandler(player) }

    init {
        player.addListener(scheduler)
    }

    val playingTrack: AudioTrack?
        get() = player.playingTrack
}