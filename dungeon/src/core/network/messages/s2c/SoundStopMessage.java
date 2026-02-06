package core.network.messages.s2c;

import core.network.messages.NetworkMessage;
import java.io.Serial;

/**
 * Server-to-client message instructing the client to stop a sound.
 *
 * @param soundInstanceId the sound instance to stop
 * @see SoundPlayMessage
 */
public record SoundStopMessage(long soundInstanceId) implements NetworkMessage {
  @Serial private static final long serialVersionUID = 1L;
}
