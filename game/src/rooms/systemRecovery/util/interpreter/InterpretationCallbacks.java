package rooms.systemRecovery.util.interpreter;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.level.DungeonLevel;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.draw.shader.EnergyFillShader;
import engine.utils.components.path.SimpleIPath;
import feature.hud.DialogUtils;

/** Applies room-side effects for terminal interpretation outcomes. */
public final class InterpretationCallbacks {

  private InterpretationCallbacks() {}

  /** Spawns the energy crates after successful array initialization. */
  public static void spawnEnergieCrates() {
    DungeonLevel level = (DungeonLevel) Game.currentLevel().get();
    Game.add(rooms.systemRecovery.entities.EntityFactory.cryoBox(level.getPoint("a0"), false));
    Game.add(rooms.systemRecovery.entities.EntityFactory.cryoBox(level.getPoint("a1"), false));
    Game.add(rooms.systemRecovery.entities.EntityFactory.cryoBox(level.getPoint("a2"), false));
    Game.add(rooms.systemRecovery.entities.EntityFactory.cryoBox(level.getPoint("a3"), false));
    Game.add(rooms.systemRecovery.entities.EntityFactory.cryoBox(level.getPoint("a4"), false));
    markEnergyCratesCorrect();
  }

  /** Colors every energy crate green after the complete assignment succeeds. */
  public static void markEnergyCratesCorrect() {
    System.out.println("TEst");
    DungeonLevel level = (DungeonLevel) Game.currentLevel().get();
    Game.entityAtPoint(level.getPoint("a0")).findFirst().flatMap(e -> e.fetch(DrawComponent.class)).ifPresent(dc -> {
        dc.shaders().add("energieShader", new EnergyFillShader(0.7f, Color.valueOf("0000FF77"), TextureMap.instance().textureAt(new SimpleIPath("objects/tech/CryoBox.png"))));
    });
    Game.entityAtPoint(level.getPoint("a1")).findFirst().flatMap(e -> e.fetch(DrawComponent.class)).ifPresent(dc -> {
        dc.shaders().add("energieShader", new EnergyFillShader(0.3f, Color.valueOf("0000FF77"), TextureMap.instance().textureAt(new SimpleIPath("objects/tech/CryoBox.png"))));
    });
    Game.entityAtPoint(level.getPoint("a2")).findFirst().flatMap(e -> e.fetch(DrawComponent.class)).ifPresent(dc -> {
      dc.shaders().add("energieShader", new EnergyFillShader(0.4f, Color.valueOf("FF000077"), TextureMap.instance().textureAt(new SimpleIPath("objects/tech/CryoBox.png"))));
    });
    Game.entityAtPoint(level.getPoint("a3")).findFirst().flatMap(e -> e.fetch(DrawComponent.class)).ifPresent(dc -> {
      dc.shaders().add("energieShader", new EnergyFillShader(1.0f, Color.valueOf("00FF0077"), TextureMap.instance().textureAt(new SimpleIPath("objects/tech/CryoBox.png"))));
    });
    Game.entityAtPoint(level.getPoint("a4")).findFirst().flatMap(e -> e.fetch(DrawComponent.class)).ifPresent(dc -> {
      dc.shaders().add("energieShader", new EnergyFillShader(0.0f, Color.valueOf("0000FF77"), TextureMap.instance().textureAt(new SimpleIPath("objects/tech/CryoBox.png"))));
    });
  }

  /** Shows feedback for a correct terminal input. */
  public static void showCorrectTerminalInputDialog() {
    DialogUtils.showTextPopup("War richtig", "Terminal");
  }

  /** Shows feedback for an incorrect terminal input. */
  public static void showIncorrectTerminalInputDialog() {
    DialogUtils.showTextPopup("war falsch", "Terminal");
  }
}
