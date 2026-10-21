package rooms.soulweaver.level;

import com.badlogic.gdx.math.Vector3;
import engine.Game;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.components.draw.shader.AbstractShader;
import feature.systems.LevelEditorSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Steam plumes rising from the leaking maze pipes and the sluice, the same on every client. */
final class SoulweaverSteamShader extends AbstractShader {

  /** Creates the steam shader; its sources are the named points of the current level. */
  SoulweaverSteamShader() {
    super("shaders/passthrough.vert", "shaders/soulweaver-steam.frag");
  }

  @Override
  protected void writeProperties(Map<String, String> properties) {
    // All inputs are named points of the level.
  }

  @Override
  protected void readProperties(Map<String, String> properties) {
    // This local shader has no stored properties to restore.
  }

  @Override
  public boolean enabled() {
    return super.enabled() && !LevelEditorSystem.active();
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of(
        new Vector3ArrayUniform(
            "u_steamSources",
            Game.currentLevel()
                .map(
                    level -> {
                      List<Vector3> steam = new ArrayList<>();
                      // Center of the two-pixel rupture (4, 12) in an 8x24 sprite, 0.65 tiles wide.
                      for (Point at :
                          SoulweaverMazeWorld.steamOutlets(level.namedPoints().get("maze-origin")))
                        steam.add(new Vector3(at.x() + .325f, at.y() + .975f, 1));
                      Point sluice = level.namedPoints().get("loop-departure");
                      steam.add(new Vector3(sluice.x() - 1, sluice.y(), 1.6f));
                      return steam;
                    })
                .orElse(List.of())));
  }

  @Override
  public int padding() {
    return 0;
  }

  @Override
  public Rectangle worldBounds() {
    return null;
  }
}
