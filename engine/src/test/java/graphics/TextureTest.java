package graphics;

import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.graphics.texture.TextureManager;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.window.GameWindow;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class TextureTest extends GameWindow implements EventListener {

  public static void main(String[] args){
    new TextureTest().start();
  }

  private final ArrayList<byte[]> list = new ArrayList<>();

  public TextureTest() {
    super("Texture Test", new Vector2i(1280, 720), true, false);
  }

  @Override
  public void init() {
    for(int i = 0; i < 20; i ++) {
      String fileName = String.format("/textures/blocks/block_%04d.png", i);
      TextureManager.load(Resource.load(fileName));
    }
    for(int i = 0; i < 20; i ++) {
      String fileName = String.format("/textures/blocks/block_%04d.png", i);
      boolean[] cached = new boolean[1];
      TextureManager.load(Resource.load(fileName), cached);
      if(!cached[0]) {
        System.out.println("Texture not cached: " + fileName);
      }
    }

    EventManager.getInstance().registerListener(this);
  }

  @Override
  public void cleanup() {

  }

  @EventHandler
  public void onKeyboard(KeyboardEvent event) {
    if(event.action == KeyboardEvent.KeyAction.PRESS) {
      if(event.key == GLFW.GLFW_KEY_G) {
        System.out.println("Started GC");
        System.gc();
      } else if(event.key == GLFW.GLFW_KEY_F) {
        this.list.add(new byte[1024 * 1024 * 100]); //Allocate 100MB
        System.out.println("Allocated 100MB (total: " + this.list.size() * 100 + "MB)");
      }
    }
  }

}
