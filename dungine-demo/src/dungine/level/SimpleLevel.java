package dungine.level;

import de.fwatermann.dungine.graphics.BillboardMode;
import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.simple.Sprite;
import de.fwatermann.dungine.graphics.texture.Texture;
import de.fwatermann.dungine.graphics.texture.TextureManager;
import de.fwatermann.dungine.graphics.texture.animation.ArrayAnimation;
import de.fwatermann.dungine.resource.Resource;
import org.joml.SimplexNoise;

public class SimpleLevel extends Renderable<SimpleLevel> {

  public static final int LEVEL_SIZE_X = 32;
  public static final int LEVEL_SIZE_Y = 32;

  private Sprite[][] tiles = new Sprite[LEVEL_SIZE_X][LEVEL_SIZE_Y];

  public SimpleLevel() {
    this.init();
  }

  public void init() {
    this.order = 0;
    Texture floor = TextureManager.load(Resource.load("/textures/floor_1.png"));
    Texture floor_dmg = TextureManager.load(Resource.load("/textures/floor_damaged.png"));
    Texture floor_hole = TextureManager.load(Resource.load("/textures/floor_hole.png"));

    float seed = (float) Math.random();

    for (int x = 0; x < LEVEL_SIZE_X; x++) {
      for (int y = 0; y < LEVEL_SIZE_Y; y++) {
        float sx = x * 0.1f;
        float sy = y * 0.1f;

        int variant = (int) (Math.floor(Math.abs(SimplexNoise.noise(sx, sy, seed)) * 3));
        Texture tex =
            switch (variant) {
              case 1 -> floor_dmg;
              case 2 -> floor_hole;
              default -> floor;
            };
        this.tiles[x][y] = new Sprite(ArrayAnimation.of(tex), BillboardMode.NONE);
        this.tiles[x][y].position(this.position().x + x, this.position().y, this.position().z + y);
        this.tiles[x][y].rotate(1, 0, 0, -90);
      }
    }
  }

  @Override
  public void render(Camera<?> camera) {
    for (int x = 0; x < LEVEL_SIZE_X; x++) {
      for (int y = 0; y < LEVEL_SIZE_Y; y++) {
        this.tiles[x][y].render(camera);
      }
    }
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    for (int x = 0; x < LEVEL_SIZE_X; x++) {
      for (int y = 0; y < LEVEL_SIZE_Y; y++) {
        this.tiles[x][y].render(camera, shader);
      }
    }
  }

  @Override
  protected void transformationChanged() {
    if (this.tiles == null) return;
    for (int x = 0; x < LEVEL_SIZE_X; x++) {
      for (int y = 0; y < LEVEL_SIZE_Y; y++) {
        if (this.tiles[x][y] == null) {
          continue;
        }
        this.tiles[x][y].position(this.position().x + x, this.position().y, this.position().z + y);
      }
    }
  }
}
