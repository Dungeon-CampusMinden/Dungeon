package feature.input.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.input.Binding;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests the controller prompts against the spritesheet atlases. */
class InputPromptHelperTest {
  @Test
  void everyPadInputHasARegionOnBothControllerSheets() throws IOException {
    String xbox = Files.readString(Path.of("assets/hud/input/xbox.xml"));
    String playstation = Files.readString(Path.of("assets/hud/input/playstation.xml"));

    List<Binding> inputs = new ArrayList<>();
    for (Binding.Pad pad : Binding.Pad.values()) inputs.add(Binding.pad(pad));
    for (Binding.PadAxisId axis : Binding.PadAxisId.values()) inputs.add(Binding.pad(axis));

    for (Binding input : inputs) {
      assertTrue(hasRegion(xbox, InputMethod.XBOX, input), input.format());
      assertTrue(hasRegion(playstation, InputMethod.PLAYSTATION, input), input.format());
    }
  }

  private static boolean hasRegion(String atlas, InputMethod method, Binding input) {
    return atlas.contains("name=\"" + InputPromptHelper.padRegionName(method, input) + "\"");
  }

  @Test
  void sonyControllersGetPlayStationPrompts() {
    assertEquals(InputMethod.PLAYSTATION, InputMethod.ofController("PS4 Controller"));
    assertEquals(
        InputMethod.PLAYSTATION, InputMethod.ofController("DualSense Wireless Controller"));
    assertEquals(InputMethod.XBOX, InputMethod.ofController("Xbox Wireless Controller"));
    assertEquals(InputMethod.XBOX, InputMethod.ofController(""));
  }
}
