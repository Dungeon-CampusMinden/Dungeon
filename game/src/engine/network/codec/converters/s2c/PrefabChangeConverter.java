package engine.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import engine.network.codec.MessageConverter;
import engine.network.messages.s2c.PrefabChangeMessage;

/** Converter for server-to-client runtime prefab change messages. */
public final class PrefabChangeConverter
    implements MessageConverter<PrefabChangeMessage, engine.network.proto.s2c.PrefabChangeMessage> {
  private static final byte WIRE_TYPE_ID = 31;

  @Override
  public engine.network.proto.s2c.PrefabChangeMessage toProto(PrefabChangeMessage message) {
    return engine.network.proto.s2c.PrefabChangeMessage.newBuilder()
        .setAction(
            switch (message.action()) {
              case PUT -> engine.network.proto.s2c.PrefabChangeMessage.Action.PUT;
              case REMOVE -> engine.network.proto.s2c.PrefabChangeMessage.Action.REMOVE;
              case RESET -> engine.network.proto.s2c.PrefabChangeMessage.Action.RESET;
            })
        .setName(message.name())
        .setType(message.type())
        .setPropertiesJson(message.propertiesJson())
        .build();
  }

  @Override
  public PrefabChangeMessage fromProto(engine.network.proto.s2c.PrefabChangeMessage proto) {
    PrefabChangeMessage.Action action =
        switch (proto.getAction()) {
          case PUT -> PrefabChangeMessage.Action.PUT;
          case REMOVE -> PrefabChangeMessage.Action.REMOVE;
          case RESET -> PrefabChangeMessage.Action.RESET;
          case UNRECOGNIZED ->
              throw new IllegalArgumentException("Unknown prefab change action: " + proto);
        };
    return new PrefabChangeMessage(
        action, proto.getName(), proto.getType(), proto.getPropertiesJson());
  }

  @Override
  public Class<PrefabChangeMessage> domainType() {
    return PrefabChangeMessage.class;
  }

  @Override
  public Class<engine.network.proto.s2c.PrefabChangeMessage> protoType() {
    return engine.network.proto.s2c.PrefabChangeMessage.class;
  }

  @Override
  public Parser<engine.network.proto.s2c.PrefabChangeMessage> parser() {
    return engine.network.proto.s2c.PrefabChangeMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
