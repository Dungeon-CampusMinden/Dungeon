import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.window.GameWindow;

import java.io.IOException;

public class FontTest {

  public static void main(String[] args) {

    try {
      Font.load(Resource.load("/fonts/opensans_variable.ttf"), 24);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

}
