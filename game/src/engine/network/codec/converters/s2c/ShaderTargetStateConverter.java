package engine.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import engine.network.codec.MessageConverter;
import engine.network.codec.ShaderComponentCodec;
import engine.network.messages.s2c.ShaderTargetStateMessage;

/** Converter for server-to-client managed shader target state messages. */
public final class ShaderTargetStateConverter
    implements MessageConverter<
        ShaderTargetStateMessage, engine.network.proto.s2c.ShaderTargetStateMessage> {
  private static final byte WIRE_TYPE_ID = 29;

  @Override
  public engine.network.proto.s2c.ShaderTargetStateMessage toProto(
      ShaderTargetStateMessage message) {
    return engine.network.proto.s2c.ShaderTargetStateMessage.newBuilder()
        .setTarget(toProtoTarget(message.target()))
        .setDepth(message.depth())
        .setShaderComponent(ShaderComponentCodec.toProto(message.shaderComponent()))
        .build();
  }

  @Override
  public ShaderTargetStateMessage fromProto(
      engine.network.proto.s2c.ShaderTargetStateMessage proto) {
    return new ShaderTargetStateMessage(
        fromProtoTarget(proto.getTarget()),
        proto.getDepth(),
        ShaderComponentCodec.fromProto(proto.getShaderComponent()));
  }

  @Override
  public Class<ShaderTargetStateMessage> domainType() {
    return ShaderTargetStateMessage.class;
  }

  @Override
  public Class<engine.network.proto.s2c.ShaderTargetStateMessage> protoType() {
    return engine.network.proto.s2c.ShaderTargetStateMessage.class;
  }

  @Override
  public Parser<engine.network.proto.s2c.ShaderTargetStateMessage> parser() {
    return engine.network.proto.s2c.ShaderTargetStateMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }

  private static engine.network.proto.s2c.ShaderTargetStateMessage.Target toProtoTarget(
      ShaderTargetStateMessage.Target target) {
    return switch (target) {
      case SCENE -> engine.network.proto.s2c.ShaderTargetStateMessage.Target.SCENE;
      case LEVEL -> engine.network.proto.s2c.ShaderTargetStateMessage.Target.LEVEL;
      case DEPTH_LAYER -> engine.network.proto.s2c.ShaderTargetStateMessage.Target.DEPTH_LAYER;
    };
  }

  private static ShaderTargetStateMessage.Target fromProtoTarget(
      engine.network.proto.s2c.ShaderTargetStateMessage.Target target) {
    return switch (target) {
      case SCENE -> ShaderTargetStateMessage.Target.SCENE;
      case LEVEL -> ShaderTargetStateMessage.Target.LEVEL;
      case DEPTH_LAYER -> ShaderTargetStateMessage.Target.DEPTH_LAYER;
      case TARGET_UNSPECIFIED, UNRECOGNIZED ->
          throw new IllegalArgumentException("Unknown shader target: " + target);
    };
  }
}
