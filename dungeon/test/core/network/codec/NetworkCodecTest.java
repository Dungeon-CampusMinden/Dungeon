package core.network.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.Message;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.ConnectRequest;
import core.network.messages.c2s.DialogResponseMessage;
import core.network.messages.c2s.InputMessage;
import core.network.messages.c2s.RegisterUdp;
import core.network.messages.c2s.RequestEntitySpawn;
import core.network.messages.c2s.SoundFinishedMessage;
import core.network.messages.s2c.ConnectAck;
import core.network.messages.s2c.ConnectReject;
import core.network.messages.s2c.DialogCloseMessage;
import core.network.messages.s2c.EntityDespawnEvent;
import core.network.messages.s2c.GameOverEvent;
import core.network.messages.s2c.LevelChangeEvent;
import core.network.messages.s2c.RegisterAck;
import core.network.messages.s2c.SoundPlayMessage;
import core.network.messages.s2c.SoundStopMessage;
import core.utils.Point;
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
    Message expectedProto = ProtoConverter.toProto(message);
    assertEquals(MessageRegistry.typeId(expectedProto), bytes[0]);
    NetworkMessage decoded = NetworkCodec.deserialize(Unpooled.wrappedBuffer(bytes));
    Message decodedProto = ProtoConverter.toProto(decoded);
    assertEquals(expectedProto, decodedProto);
  }

  private static List<NetworkMessage> sampleMessages() {
    return List.of(
        new ConnectRequest((short) 1, "player", 42, new byte[] {1, 2, 3}),
        new InputMessage(42, 100, (short) 7, InputMessage.Action.MOVE, new Point(1.5f, -2.5f)),
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
        new SoundPlayMessage(1L, 2, "sound", 0.5f, 1.0f, 0.0f, false, 10.0f, 0.1f),
        new SoundStopMessage(2L));
  }
}
