package core.network.messages.c2s;

import core.network.messages.NetworkMessage;

/**
 * Client → server message reporting that a sound has finished playing.
 *
 * <p>Sent by clients when a sound instance completes playback (for non-looping audio) or when
 * explicitly stopped. The server uses this to trigger any registered {@code onFinished} callbacks
 * that were associated with the sound instance.
 *
 * <p>The {@code soundInstanceId} uniquely identifies a specific playback instance, allowing the
 * server to distinguish between multiple simultaneous plays of the same sound.
 *
 * @param soundInstanceId unique identifier for the sound instance that finished
 * @see core.sound.player.IPlayHandle#onFinished(Runnable)
 */
public record SoundFinishedMessage(long soundInstanceId) implements NetworkMessage {}
