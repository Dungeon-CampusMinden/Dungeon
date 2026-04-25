package core.platform.client.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.input.Keys;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Regression tests for AWT-to-input key mapping. */
class ClientInputBridgeTest {

  @Test
  void mapsLeftShiftToLeftShiftKey() throws Exception {
    assertEquals(
      Keys.SHIFT_LEFT,
      mapAwtKey(KeyEvent.VK_SHIFT, KeyEvent.KEY_LOCATION_LEFT));
  }

  @Test
  void mapsRightShiftToRightShiftKey() throws Exception {
    assertEquals(
      Keys.SHIFT_RIGHT,
      mapAwtKey(KeyEvent.VK_SHIFT, KeyEvent.KEY_LOCATION_RIGHT));
  }

  @Test
  void mapsEveryDefinedKeyboardKeyFromAwt() throws Exception {
    Set<Integer> mappedKeys = new HashSet<>();

    mappedKeys.add(mapAwtKey(KeyEvent.VK_0, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_1, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_2, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_3, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_4, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_5, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_6, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_7, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_8, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_9, KeyEvent.KEY_LOCATION_STANDARD));

    mappedKeys.add(mapAwtKey(KeyEvent.VK_UP, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_DOWN, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_LEFT, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_RIGHT, KeyEvent.KEY_LOCATION_STANDARD));

    mappedKeys.add(mapAwtKey(KeyEvent.VK_A, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_B, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_C, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_D, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_E, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_F, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_G, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_H, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_I, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_J, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_K, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_L, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_M, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_N, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_O, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_P, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_Q, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_R, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_S, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_T, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_U, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_V, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_W, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_X, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_Y, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_Z, KeyEvent.KEY_LOCATION_STANDARD));

    mappedKeys.add(mapAwtKey(KeyEvent.VK_COMMA, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_PERIOD, KeyEvent.KEY_LOCATION_STANDARD));

    mappedKeys.add(mapAwtKey(KeyEvent.VK_SHIFT, KeyEvent.KEY_LOCATION_LEFT));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_SHIFT, KeyEvent.KEY_LOCATION_RIGHT));

    mappedKeys.add(mapAwtKey(KeyEvent.VK_TAB, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_SPACE, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_ENTER, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_BACK_SPACE, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_ESCAPE, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_DELETE, KeyEvent.KEY_LOCATION_STANDARD));

    mappedKeys.add(mapAwtKey(KeyEvent.VK_F1, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_F2, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_F3, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_F4, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_F5, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_F6, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_F7, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_F8, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_F9, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_F10, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_F11, KeyEvent.KEY_LOCATION_STANDARD));
    mappedKeys.add(mapAwtKey(KeyEvent.VK_F12, KeyEvent.KEY_LOCATION_STANDARD));

    assertEquals(expectedMappedKeys(), mappedKeys);
    assertTrue(!mappedKeys.contains(-1), "known AWT keys must not map to the internal sentinel");
  }

  private static Set<Integer> expectedMappedKeys() {
    return Arrays.stream(Keys.class.getFields())
      .filter(field -> field.getType() == int.class)
      .filter(field -> Modifier.isStatic(field.getModifiers()))
      .filter(field -> Modifier.isFinal(field.getModifiers()))
      .filter(field -> !field.getName().equals("ANY_KEY"))
      .filter(field -> !field.getName().equals("UNKNOWN"))
      .map(ClientInputBridgeTest::readIntField)
      .collect(Collectors.toSet());
  }

  private static int mapAwtKey(int awtKey, int keyLocation) throws Exception {
    Method method =
      ClientInputBridge.class.getDeclaredMethod(
        "mapAwtKeyToInputCode", int.class, int.class);
    method.setAccessible(true);
    return (int) method.invoke(null, awtKey, keyLocation);
  }

  private static int readIntField(Field field) {
    try {
      return field.getInt(null);
    } catch (IllegalAccessException exception) {
      throw new AssertionError(exception);
    }
  }
}
