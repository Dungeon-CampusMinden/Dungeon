package core.network.codec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import core.components.PositionComponent;
import core.sound.SoundSpec;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import org.junit.jupiter.api.Test;

/** Tests for {@link ProtoConverter}. */
public class ProtoConverterTest {

  private static final float DELTA = 1e-6f;

  /** Verifies point conversion roundtrip. */
  @Test
  public void testPointRoundTrip() {
    Point point = new Point(1.25f, -2.5f);
    core.network.proto.common.Point protoPoint = ProtoConverter.toProto(point);
    Point roundTrip = ProtoConverter.fromProto(protoPoint);

    assertEquals(point.x(), protoPoint.getX(), DELTA);
    assertEquals(point.y(), protoPoint.getY(), DELTA);
    assertEquals(point.x(), roundTrip.x(), DELTA);
    assertEquals(point.y(), roundTrip.y(), DELTA);
  }

  /** Verifies vector conversion roundtrip. */
  @Test
  public void testVector2RoundTrip() {
    Vector2 vector = Vector2.of(3.5f, -4.75f);
    core.network.proto.common.Vector2 protoVector = ProtoConverter.toProto(vector);
    Vector2 roundTrip = ProtoConverter.fromProto(protoVector);

    assertEquals(vector.x(), protoVector.getX(), DELTA);
    assertEquals(vector.y(), protoVector.getY(), DELTA);
    assertEquals(vector.x(), roundTrip.x(), DELTA);
    assertEquals(vector.y(), roundTrip.y(), DELTA);
  }

  /** Verifies direction conversion to protobuf. */
  @Test
  public void testDirectionToProto() {
    assertEquals(
        core.network.proto.common.Direction.DIRECTION_UP, ProtoConverter.toProto(Direction.UP));
    assertEquals(
        core.network.proto.common.Direction.DIRECTION_DOWN, ProtoConverter.toProto(Direction.DOWN));
    assertEquals(
        core.network.proto.common.Direction.DIRECTION_LEFT, ProtoConverter.toProto(Direction.LEFT));
    assertEquals(
        core.network.proto.common.Direction.DIRECTION_RIGHT,
        ProtoConverter.toProto(Direction.RIGHT));
    assertEquals(
        core.network.proto.common.Direction.DIRECTION_NONE, ProtoConverter.toProto(Direction.NONE));
  }

  /** Verifies direction conversion from protobuf. */
  @Test
  public void testDirectionFromProto() {
    assertEquals(
        Direction.UP, ProtoConverter.fromProto(core.network.proto.common.Direction.DIRECTION_UP));
    assertEquals(
        Direction.DOWN,
        ProtoConverter.fromProto(core.network.proto.common.Direction.DIRECTION_DOWN));
    assertEquals(
        Direction.LEFT,
        ProtoConverter.fromProto(core.network.proto.common.Direction.DIRECTION_LEFT));
    assertEquals(
        Direction.RIGHT,
        ProtoConverter.fromProto(core.network.proto.common.Direction.DIRECTION_RIGHT));
    assertEquals(
        Direction.NONE,
        ProtoConverter.fromProto(core.network.proto.common.Direction.DIRECTION_NONE));
    assertEquals(
        Direction.NONE,
        ProtoConverter.fromProto(core.network.proto.common.Direction.DIRECTION_UNSPECIFIED));
    assertEquals(
        Direction.NONE, ProtoConverter.fromProto(core.network.proto.common.Direction.UNRECOGNIZED));
  }

  /** Verifies position component conversion roundtrip. */
  @Test
  public void testPositionComponentRoundTrip() {
    PositionComponent component = new PositionComponent(new Point(1.5f, -2.0f), Direction.RIGHT);
    component.rotation(30.0f);
    component.scale(Vector2.of(1.25f, 0.75f));

    core.network.proto.common.PositionInfo proto = ProtoConverter.toProto(component);
    assertEquals(1.5f, proto.getPosition().getX(), DELTA);
    assertEquals(-2.0f, proto.getPosition().getY(), DELTA);
    assertEquals(core.network.proto.common.Direction.DIRECTION_RIGHT, proto.getViewDirection());
    assertEquals(30.0f, proto.getRotation(), DELTA);
    assertEquals(1.25f, proto.getScale().getX(), DELTA);
    assertEquals(0.75f, proto.getScale().getY(), DELTA);

    PositionComponent roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(1.5f, roundTrip.position().x(), DELTA);
    assertEquals(-2.0f, roundTrip.position().y(), DELTA);
    assertEquals(Direction.RIGHT, roundTrip.viewDirection());
    assertEquals(30.0f, roundTrip.rotation(), DELTA);
    assertEquals(1.25f, roundTrip.scale().x(), DELTA);
    assertEquals(0.75f, roundTrip.scale().y(), DELTA);
  }

  /** Verifies sound specification conversion roundtrip. */
  @Test
  public void testSoundSpecRoundTrip() {
    SoundSpec spec =
        SoundSpec.builder("torch")
            .instanceId(42L)
            .volume(0.75f)
            .looping(true)
            .pitch(1.2f)
            .pan(-0.3f)
            .maxDistance(12.5f)
            .attenuation(0.8f)
            .targets(1, 2, 3)
            .build();

    core.network.proto.common.SoundSpec protoSpec = ProtoConverter.toProto(spec);
    SoundSpec roundTrip = ProtoConverter.fromProto(protoSpec);

    assertEquals(spec.instanceId(), protoSpec.getInstanceId());
    assertEquals(spec.soundName(), protoSpec.getSoundName());
    assertEquals(spec.baseVolume(), protoSpec.getBaseVolume(), DELTA);
    assertEquals(spec.looping(), protoSpec.getLooping());
    assertEquals(spec.pitch(), protoSpec.getPitch(), DELTA);
    assertEquals(spec.pan(), protoSpec.getPan(), DELTA);
    assertEquals(spec.maxDistance(), protoSpec.getMaxDistance(), DELTA);
    assertEquals(spec.attenuationFactor(), protoSpec.getAttenuationFactor(), DELTA);

    assertEquals(spec.instanceId(), roundTrip.instanceId());
    assertEquals(spec.soundName(), roundTrip.soundName());
    assertEquals(spec.baseVolume(), roundTrip.baseVolume(), DELTA);
    assertEquals(spec.looping(), roundTrip.looping());
    assertEquals(spec.pitch(), roundTrip.pitch(), DELTA);
    assertEquals(spec.pan(), roundTrip.pan(), DELTA);
    assertEquals(spec.maxDistance(), roundTrip.maxDistance(), DELTA);
    assertEquals(spec.attenuationFactor(), roundTrip.attenuationFactor(), DELTA);
    assertArrayEquals(new int[0], roundTrip.targetEntityIds());
  }
}
