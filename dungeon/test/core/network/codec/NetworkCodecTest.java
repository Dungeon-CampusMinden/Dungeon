package core.network.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.Message;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.*;
import core.network.messages.s2c.*;
import core.sound.SoundSpec;
import core.utils.Vector2;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class NetworkCodecTest {

  @Test
  void roundTripSelectedMessages() throws Exception {
    for (NetworkMessage message : sampleMessages()) {
      assertRoundTrip(message);
    }
  }

  private static void assertRoundTrip(NetworkMessage message) throws IOException {
    byte[] bytes = NetworkCodec.serialize(message);
    Message expectedProto = ConverterRegistry.global().toProto(message);
    assertEquals(ConverterRegistry.global().typeId(expectedProto), bytes[0]);
    NetworkMessage decoded = NetworkCodec.deserialize(Unpooled.wrappedBuffer(bytes));
    Message decodedProto = ConverterRegistry.global().toProto(decoded);
    assertEquals(expectedProto, decodedProto);
  }

  private static List<NetworkMessage> sampleMessages() {
    return List.of(
        new ConnectRequest((short) 1, "player", 42, new byte[] {1, 2, 3}),
        new InputMessage(
            42,
            100,
            (short) 7,
            InputMessage.Action.MOVE,
            new InputMessage.Move(Vector2.of(1.5f, -2.5f))),
        new InputMessage(
            42,
            101,
            (short) 8,
            InputMessage.Action.CUSTOM,
            new InputMessage.Custom("escapeRoom:hint_log.open", new byte[] {1}, 1)),
        new DialogResponseMessage("dialog-1", "onConfirm", null),
        new RegisterUdp(42, new byte[] {9, 8, 7}, (short) 4),
        new RequestEntitySpawn(99),
        new SoundFinishedMessage(123L),
        new ConnectAck((short) 5, 42, new byte[] {4, 5, 6}),
        new ConnectReject(ConnectReject.Reason.INVALID_NAME),
        new DialogCloseMessage("dialog-2"),
        new EntityDespawnEvent(77, "left"),
        new GameOverEvent("Game over"),
        new LevelChangeEvent("level-1", "data"),
        new RegisterAck(true),
        new SoundPlayMessage(
            2,
            SoundSpec.builder("sound")
                .instanceId(1L)
                .volume(0.5f)
                .pitch(1.0f)
                .pan(0.0f)
                .looping(false)
                .maxDistance(10.0f)
                .attenuation(0.1f)
                .build()),
        new SoundStopMessage(2L));
  }
}
