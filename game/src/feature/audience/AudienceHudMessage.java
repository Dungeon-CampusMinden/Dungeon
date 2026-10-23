package feature.audience;

import com.google.protobuf.Parser;
import engine.network.codec.ConverterRegistry;
import engine.network.codec.MessageConverter;
import engine.network.messages.NetworkMessage;
import engine.network.proto.s2c.AudienceHud;

/**
 * Overlay state the audience-mode server sends to the game-screen client several times per second.
 *
 * @param json overlay state as written by {@link AudienceSystem}
 */
public record AudienceHudMessage(String json) implements NetworkMessage {
  private static boolean registered;

  /** Registers the converter once per process; both the server and the client need it. */
  static synchronized void registerConverter() {
    if (registered) return;
    ConverterRegistry.global().register(new Converter());
    registered = true;
  }

  /** Converts the message to and from its protobuf form. */
  static final class Converter implements MessageConverter<AudienceHudMessage, AudienceHud> {
    private static final byte WIRE_TYPE_ID = (byte) 130;

    @Override
    public AudienceHud toProto(AudienceHudMessage message) {
      return AudienceHud.newBuilder().setJson(message.json()).build();
    }

    @Override
    public AudienceHudMessage fromProto(AudienceHud proto) {
      return new AudienceHudMessage(proto.getJson());
    }

    @Override
    public Class<AudienceHudMessage> domainType() {
      return AudienceHudMessage.class;
    }

    @Override
    public Class<AudienceHud> protoType() {
      return AudienceHud.class;
    }

    @Override
    public Parser<AudienceHud> parser() {
      return AudienceHud.parser();
    }

    @Override
    public byte wireTypeId() {
      return WIRE_TYPE_ID;
    }
  }
}
