package portal.physicsobject;

import contrib.entities.LeverFactory;
import contrib.hud.DialogUtils;
import contrib.utils.DynamicCompiler;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import starter.PortalStarter;

/**
 * Utility class for creating a lever that triggers user-defined spawn logic.
 *
 * <p>This class acts as a bridge between level design and dynamically compiled user code. When the
 * lever is activated, a user-provided implementation is loaded at runtime and executed.
 *
 * <p>The lever itself is fully configured by the engine; only the behavior executed on activation
 * is delegated to user code.
 */
public class SpawnLever {

  /** Path to the user-defined cube spawner source file. */
  private static final SimpleIPath PATH =
      new SimpleIPath("advancedDungeon/src/portal/riddles/MyCubeSpawner.java");

  /** Fully qualified class name of the user-defined cube spawner. */
  private static final String CLASSNAME = "portal.riddles.MyCubeSpawner";

  /**
   * Creates a lever entity that executes user-defined spawn logic when activated.
   *
   * <p>On activation, this lever dynamically loads the user implementation specified by {@link
   * #PATH} and {@link #CLASSNAME} and invokes its {@code spawn()} method.
   *
   * <p>If loading or execution fails, a user-facing error dialog is shown. In debug mode, the
   * underlying exception is printed to the console.
   *
   * @param position the world position where the lever should be created
   * @return a fully configured lever entity
   */
  public static Entity spawnLever(Point position, Point spawnPoint) {
    return LeverFactory.createLever(
        position,
        new ICommand() {
          private Entity cube;

          /**
           * Executes the user-defined spawn logic.
           *
           * <p>This method is called when the lever is activated.
           */
          @Override
          public void execute() {
            try {
              Object o = DynamicCompiler.loadUserInstance(PATH, CLASSNAME);
              Entity cube = ((CubeSpawner) o).spawn(spawnPoint);
              if(cube==null)throw new NullPointerException();
              if (cube.fetch(PositionComponent.class).get().position().equals(spawnPoint)) {
                Game.add(cube);
                this.cube = cube;
              } else
                throw new IllegalStateException(
                    "Da stimmt was nicht. Ich glaube die Spawnposition ist falsch.");

            } catch (Exception e) {
              if (PortalStarter.DEBUG_MODE) e.printStackTrace();
              DialogUtils.showTextPopup("Dieser Schalter funktioniert nicht richtig", "Code Error");
            }
          }

          @Override
          public void undo() {
            if (cube != null) Game.remove(cube);
          }
        });
  }
}
