package core.network.codec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogType;
import contrib.utils.components.showImage.TransitionSpeed;
import core.network.codec.converters.s2c.DialogShowConverter;
import core.network.messages.s2c.DialogShowMessage;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Tests for {@link DialogShowConverter}. */
public class DialogShowConverterTest {
  private static final DialogShowConverter CONVERTER = new DialogShowConverter();

  /** Verifies dialog context conversion roundtrip. */
  @Test
  public void testDialogContextRoundTrip() {
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.TEXT)
            .center(false)
            .dialogId("dialog-42")
            .put(DialogContextKeys.TITLE, "Hello")
            .put(DialogContextKeys.MESSAGE, "World")
            .put(DialogContextKeys.OWNER_ENTITY, 10)
            .put(DialogContextKeys.ENTITY, 20)
            .put(DialogContextKeys.ADDITIONAL_BUTTONS, new String[] {"Retry", "Quit"})
            .put(DialogContextKeys.IMAGE_TRANSITION_SPEED, TransitionSpeed.SLOW)
            .build();

    DialogShowMessage message = new DialogShowMessage(context, true);
    core.network.proto.s2c.DialogShowMessage proto = CONVERTER.toProto(message);

    assertEquals("dialog-42", proto.getDialogId());
    assertEquals("TEXT", proto.getDialogType());
    assertTrue(proto.getCanBeClosed());
    assertFalse(proto.getCenter());
    assertTrue(
        proto.getAttributesList().stream()
            .anyMatch(
                attr ->
                    DialogContextKeys.OWNER_ENTITY.equals(attr.getKey())
                        && attr.getIntValue() == 10));
    assertTrue(
        proto.getAttributesList().stream()
            .anyMatch(
                attr ->
                    DialogContextKeys.ENTITY.equals(attr.getKey()) && attr.getIntValue() == 20));
    assertTrue(
        proto.getAttributesList().stream()
            .anyMatch(
                attr ->
                    DialogContextKeys.IMAGE_TRANSITION_SPEED.equals(attr.getKey())
                        && attr.getValueCase()
                            == core.network.proto.s2c.DialogAttribute.ValueCase.CUSTOM_VALUE));

    DialogShowMessage roundTripMessage = CONVERTER.fromProto(proto);
    DialogContext roundTrip = roundTripMessage.context();

    assertEquals("dialog-42", roundTrip.dialogId());
    assertEquals(context.dialogType(), roundTrip.dialogType());
    assertEquals(context.center(), roundTrip.center());
    assertEquals("Hello", roundTrip.require(DialogContextKeys.TITLE, String.class));
    assertEquals("World", roundTrip.require(DialogContextKeys.MESSAGE, String.class));
    assertEquals(10, roundTrip.require(DialogContextKeys.OWNER_ENTITY, Integer.class));
    assertEquals(20, roundTrip.require(DialogContextKeys.ENTITY, Integer.class));
    assertArrayEquals(
        new String[] {"Retry", "Quit"},
        roundTrip.require(DialogContextKeys.ADDITIONAL_BUTTONS, String[].class));
    assertEquals(
        TransitionSpeed.SLOW,
        roundTrip.require(DialogContextKeys.IMAGE_TRANSITION_SPEED, TransitionSpeed.class));
  }

  /** Verifies custom codec roundtrip via CUSTOM_VALUE. */
  @Test
  public void testCustomCodecRoundTrip() {
    DialogValueCodecRegistry registry = DialogValueCodecRegistry.global();
    if (registry.byTypeId("DialogShowConverterTestData").isEmpty()) {
      registry.register(
          new DialogValueCodec<TestData>() {
            @Override
            public String typeId() {
              return "DialogShowConverterTestData";
            }

            @Override
            public Class<TestData> type() {
              return TestData.class;
            }

            @Override
            public byte[] encode(TestData value) {
              return (value.label() + "|" + value.count()).getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public TestData decode(byte[] data) {
              String[] parts = new String(data, StandardCharsets.UTF_8).split("\\|");
              return new TestData(parts[0], Integer.parseInt(parts[1]));
            }
          });
    }

    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.TEXT)
            .center(false)
            .dialogId("custom-test")
            .put("custom_data", new TestData("hello", 42))
            .build();

    DialogShowMessage message = new DialogShowMessage(context, true);
    core.network.proto.s2c.DialogShowMessage proto = CONVERTER.toProto(message);
    assertTrue(
        proto.getAttributesList().stream()
            .anyMatch(
                attr ->
                    "custom_data".equals(attr.getKey())
                        && attr.getValueCase()
                            == core.network.proto.s2c.DialogAttribute.ValueCase.CUSTOM_VALUE));

    DialogShowMessage roundTripMessage = CONVERTER.fromProto(proto);
    DialogContext roundTrip = roundTripMessage.context();

    TestData result = roundTrip.require("custom_data", TestData.class);
    assertEquals("hello", result.label());
    assertEquals(42, result.count());
  }

  private record TestData(String label, int count) implements Serializable {}
}
