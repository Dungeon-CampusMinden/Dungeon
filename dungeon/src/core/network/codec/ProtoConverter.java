package core.network.codec;

import core.sound.SoundSpec;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;

/**
 * Converts between protobuf messages and domain objects for common network types.
 */
public final class ProtoConverter {

  private ProtoConverter() {}

  /**
   * Converts a {@link Point} into its protobuf representation.
   *
   * @param point the domain point to convert
   * @return the protobuf point message
   */
  public static core.network.proto.common.Point toProto(Point point) {
    return core.network.proto.common.Point.newBuilder()
        .setX(point.x())
        .setY(point.y())
        .build();
  }

  /**
   * Converts a protobuf point into a {@link Point}.
   *
   * @param proto the protobuf point message
   * @return the domain point
   */
  public static Point fromProto(core.network.proto.common.Point proto) {
    return new Point(proto.getX(), proto.getY());
  }

  /**
   * Converts a {@link Vector2} into its protobuf representation.
   *
   * @param vector the domain vector to convert
   * @return the protobuf vector message
   */
  public static core.network.proto.common.Vector2 toProto(Vector2 vector) {
    return core.network.proto.common.Vector2.newBuilder()
        .setX(vector.x())
        .setY(vector.y())
        .build();
  }

  /**
   * Converts a protobuf vector into a {@link Vector2}.
   *
   * @param proto the protobuf vector message
   * @return the domain vector
   */
  public static Vector2 fromProto(core.network.proto.common.Vector2 proto) {
    return Vector2.of(proto.getX(), proto.getY());
  }

  /**
   * Converts a {@link Direction} into its protobuf representation.
   *
   * @param direction the domain direction to convert
   * @return the protobuf direction enum
   */
  public static core.network.proto.common.Direction toProto(Direction direction) {
    return switch (direction) {
      case UP -> core.network.proto.common.Direction.DIRECTION_UP;
      case DOWN -> core.network.proto.common.Direction.DIRECTION_DOWN;
      case LEFT -> core.network.proto.common.Direction.DIRECTION_LEFT;
      case RIGHT -> core.network.proto.common.Direction.DIRECTION_RIGHT;
      case NONE -> core.network.proto.common.Direction.DIRECTION_NONE;
    };
  }

  /**
   * Converts a protobuf direction into a {@link Direction}.
   *
   * @param proto the protobuf direction enum
   * @return the domain direction
   */
  public static Direction fromProto(core.network.proto.common.Direction proto) {
    return switch (proto) {
      case DIRECTION_UP -> Direction.UP;
      case DIRECTION_DOWN -> Direction.DOWN;
      case DIRECTION_LEFT -> Direction.LEFT;
      case DIRECTION_RIGHT -> Direction.RIGHT;
      case DIRECTION_NONE, DIRECTION_UNSPECIFIED, UNRECOGNIZED -> Direction.NONE;
    };
  }

  /**
   * Converts a {@link SoundSpec} into its protobuf representation.
   *
   * <p>Target entity IDs are not included in the protobuf definition and are not serialized.
   *
   * @param spec the domain sound specification to convert
   * @return the protobuf sound specification
   */
  public static core.network.proto.common.SoundSpec toProto(SoundSpec spec) {
    return core.network.proto.common.SoundSpec.newBuilder()
        .setInstanceId(spec.instanceId())
        .setSoundName(spec.soundName())
        .setBaseVolume(spec.baseVolume())
        .setLooping(spec.looping())
        .setPitch(spec.pitch())
        .setPan(spec.pan())
        .setMaxDistance(spec.maxDistance())
        .setAttenuationFactor(spec.attenuationFactor())
        .build();
  }

  /**
   * Converts a protobuf sound specification into a {@link SoundSpec}.
   *
   * <p>Target entity IDs are not included in the protobuf definition and default to an empty array.
   *
   * @param proto the protobuf sound specification
   * @return the domain sound specification
   */
  public static SoundSpec fromProto(core.network.proto.common.SoundSpec proto) {
    return SoundSpec.builder(proto.getSoundName())
        .instanceId(proto.getInstanceId())
        .volume(proto.getBaseVolume())
        .looping(proto.getLooping())
        .pitch(proto.getPitch())
        .pan(proto.getPan())
        .maxDistance(proto.getMaxDistance())
        .attenuation(proto.getAttenuationFactor())
        .build();
  }
}
