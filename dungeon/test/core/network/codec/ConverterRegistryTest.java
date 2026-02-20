package core.network.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import core.network.messages.NetworkMessage;
import core.network.messages.s2c.RegisterAck;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConverterRegistryTest {

  @Test
  void registryParsesKnownTypes() throws Exception {
    ConverterRegistry registry = ConverterRegistry.global();
    for (Message message : protoDefaults()) {
      byte typeId = registry.typeId(message);
      assertTrue(typeId != 0);
      Message parsed = registry.parse(typeId, message.toByteArray());
      assertEquals(message, parsed);
    }
  }

  @Test
  void duplicateRegistrationThrows() {
    ConverterRegistry registry = new ConverterRegistry();
    CustomPingConverter converter = new CustomPingConverter();

    registry.register(converter);
    assertThrows(IllegalStateException.class, () -> registry.register(converter));
  }

  @Test
  void unknownTypesThrow() {
    ConverterRegistry registry = new ConverterRegistry();

    assertThrows(IllegalArgumentException.class, () -> registry.toProto(new RegisterAck(true)));
    assertThrows(
        IllegalArgumentException.class,
        () -> registry.fromProto(core.network.proto.s2c.RegisterAck.getDefaultInstance()));
    assertThrows(IllegalArgumentException.class, () -> registry.parse((byte) 1, new byte[0]));
  }

  @Test
  void customConverterRoundTripWorks() throws Exception {
    ConverterRegistry registry = new ConverterRegistry();
    registry.register(new CustomPingConverter());

    CustomPing message = new CustomPing(123L);
    Message proto = registry.toProto(message);
    byte typeId = registry.typeId(proto);
    Message parsedProto = registry.parse(typeId, proto.toByteArray());
    CustomPing roundTrip = (CustomPing) registry.fromProto(parsedProto);

    assertEquals(message.value(), roundTrip.value());
    assertEquals(64, Byte.toUnsignedInt(typeId));
  }

  @Test
  void globalRegistryDispatchesCoreConverters() {
    ConverterRegistry registry = ConverterRegistry.global();

    RegisterAck message = new RegisterAck(true);
    Message proto = registry.toProto(message);
    NetworkMessage decoded = registry.fromProto(proto);

    assertTrue(decoded instanceof RegisterAck);
    assertTrue(((RegisterAck) decoded).ok());
    assertEquals(17, Byte.toUnsignedInt(registry.typeId(proto)));
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

  private record CustomPing(long value) implements NetworkMessage {}

  private static final class CustomPingConverter
      implements MessageConverter<CustomPing, core.network.proto.common.SoundSpec> {

    @Override
    public core.network.proto.common.SoundSpec toProto(CustomPing message) {
      return core.network.proto.common.SoundSpec.newBuilder()
          .setInstanceId(message.value())
          .build();
    }

    @Override
    public CustomPing fromProto(core.network.proto.common.SoundSpec proto) {
      return new CustomPing(proto.getInstanceId());
    }

    @Override
    public Class<CustomPing> domainType() {
      return CustomPing.class;
    }

    @Override
    public Class<core.network.proto.common.SoundSpec> protoType() {
      return core.network.proto.common.SoundSpec.class;
    }

    @Override
    public Parser<core.network.proto.common.SoundSpec> parser() {
      return core.network.proto.common.SoundSpec.parser();
    }

    @Override
    public byte wireTypeId() {
      return 64;
    }
  }
}
