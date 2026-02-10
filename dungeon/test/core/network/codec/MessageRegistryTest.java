package core.network.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.Message;
import java.util.List;
import org.junit.jupiter.api.Test;

class MessageRegistryTest {

  @Test
  void registryParsesKnownTypes() throws Exception {
    for (Message message : protoDefaults()) {
      byte typeId = MessageRegistry.typeId(message);
      assertTrue(typeId > 0);
      Message parsed = MessageRegistry.parse(typeId, message.toByteArray());
      assertEquals(message, parsed);
    }
  }

  private static List<Message> protoDefaults() {
    return List.of(
        core.network.proto.c2s.ConnectRequest.getDefaultInstance(),
        core.network.proto.c2s.InputMessage.getDefaultInstance(),
        core.network.proto.c2s.DialogResponseMessage.getDefaultInstance(),
        core.network.proto.c2s.RegisterUdp.getDefaultInstance(),
        core.network.proto.c2s.RequestEntitySpawn.getDefaultInstance(),
        core.network.proto.c2s.SoundFinishedMessage.getDefaultInstance(),
        core.network.proto.s2c.ConnectAck.getDefaultInstance(),
        core.network.proto.s2c.ConnectReject.getDefaultInstance(),
        core.network.proto.s2c.DialogShowMessage.getDefaultInstance(),
        core.network.proto.s2c.DialogCloseMessage.getDefaultInstance(),
        core.network.proto.s2c.EntitySpawnEvent.getDefaultInstance(),
        core.network.proto.s2c.EntitySpawnBatch.getDefaultInstance(),
        core.network.proto.s2c.EntityDespawnEvent.getDefaultInstance(),
        core.network.proto.s2c.EntityState.getDefaultInstance(),
        core.network.proto.s2c.GameOverEvent.getDefaultInstance(),
        core.network.proto.s2c.LevelChangeEvent.getDefaultInstance(),
        core.network.proto.s2c.RegisterAck.getDefaultInstance(),
        core.network.proto.s2c.SnapshotMessage.getDefaultInstance(),
        core.network.proto.s2c.SoundPlayMessage.getDefaultInstance(),
        core.network.proto.s2c.SoundStopMessage.getDefaultInstance());
  }
}
