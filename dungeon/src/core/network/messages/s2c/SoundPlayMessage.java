package core.network.messages.s2c;

import core.network.messages.NetworkMessage;
import java.io.Serial;

/**
 * Server-to-client message instructing the client to play a sound.
 *
 * @param soundInstanceId unique identifier for this sound instance
 * @param entityId the entity emitting the sound (for position tracking)
 * @param soundName the sound asset name
 * @param volume base volume [0.0, 1.0]
 * @param pitch playback pitch multiplier
 * @param pan stereo pan [-1.0, 1.0]
 * @param looping whether the sound loops
 * @param maxDistance max audible distance (-1 for global)
 * @param attenuationFactor distance attenuation factor
 * @see SoundStopMessage
 */
public record SoundPlayMessage(
    long soundInstanceId,
    int entityId,
    String soundName,
    float volume,
    float pitch,
    float pan,
    boolean looping,
    float maxDistance,
    float attenuationFactor)
    implements NetworkMessage {
  @Serial private static final long serialVersionUID = 1L;
}
