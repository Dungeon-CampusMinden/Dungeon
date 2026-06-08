package core.network.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.Message;
import contrib.entities.CharacterClass;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.ConnectRequest;
import core.network.messages.c2s.DebugPing;
import core.network.messages.c2s.DebugTelemetryRequest;
import core.network.messages.c2s.DialogResponseMessage;
import core.network.messages.c2s.InputMessage;
import core.network.messages.c2s.RegisterUdp;
import core.network.messages.c2s.RequestEntitySpawn;
import core.network.messages.c2s.SnapshotAck;
import core.network.messages.c2s.SoundFinishedMessage;
import core.network.messages.s2c.ConnectAck;
import core.network.messages.s2c.ConnectReject;
import core.network.messages.s2c.DebugPong;
import core.network.messages.s2c.DebugTelemetrySnapshot;
import core.network.messages.s2c.DeltaSnapshotMessage;
import core.network.messages.s2c.DialogCloseMessage;
import core.network.messages.s2c.EntityDelta;
import core.network.messages.s2c.EntityDespawnEvent;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.EntityStateField;
import core.network.messages.s2c.GameOverEvent;
import core.network.messages.s2c.LevelChangeEvent;
import core.network.messages.s2c.RegisterAck;
import core.network.messages.s2c.SoundPlayMessage;
import core.network.messages.s2c.SoundStopMessage;
import core.sound.SoundSpec;
import core.utils.Vector2;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        new ConnectRequest(
            (short) 1, "player", 42, new byte[] {1, 2, 3}, Optional.of(CharacterClass.HUNTER)),
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
        new SnapshotAck(123),
        new DebugTelemetryRequest(1L, DebugTelemetryRequest.Mode.ONCE, 1_000),
        new DebugPing(2L, 123_456L),
        new ConnectAck((short) 5, 42, new byte[] {4, 5, 6}),
        new ConnectReject(ConnectReject.Reason.INVALID_NAME),
        new DialogCloseMessage("dialog-2"),
        new EntityDespawnEvent(77, "left"),
        new DeltaSnapshotMessage(
            10,
            12,
            List.of(
                new EntityDelta(
                    5,
                    EntityState.builder().entityId(5).rotation(90.0f).build(),
                    Set.of(EntityStateField.POSITION))),
            List.of(8),
            null),
        new GameOverEvent("Game over"),
        new LevelChangeEvent("level-1", "data"),
        new RegisterAck(true),
        new DebugTelemetrySnapshot(
            3L,
            1_000L,
            new DebugTelemetrySnapshot.Transport(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L),
            new DebugTelemetrySnapshot.Transport(31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L),
            new DebugTelemetrySnapshot.Udp(9L, 10L, 11L, 12L, "fallback", "drop", "failure"),
            new DebugTelemetrySnapshot.Snapshots(13L, 14L, 15, 16, 17, 18, 19, 20, 21, 22L),
            new DebugTelemetrySnapshot.Windows(23L, 24L, 25L, 26L),
            List.of(new DebugTelemetrySnapshot.Client((short) 1, true, 100.0f, 50L, 44))),
        new DebugPong(4L, 5L, 6L, 7L),
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
