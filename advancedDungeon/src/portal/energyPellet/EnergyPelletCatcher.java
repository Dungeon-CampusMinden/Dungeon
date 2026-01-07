package portal.energyPellet;

import contrib.components.CollideComponent;
import contrib.hud.DialogUtils;
import contrib.utils.DynamicCompiler;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.path.SimpleIPath;
import portal.energyPellet.abstraction.EnergyPelletCatcherBehavior;
import portal.util.ToggleableComponent;

/** An EnergyPelletCatcher is like a switch that activates when it is hit by an Energy Pellet. */
public class EnergyPelletCatcher {
  private static final SimpleIPath PELLET_CATCHER = new SimpleIPath("portal/pellet_catcher");

  private static final SimpleIPath PATH =
      new SimpleIPath("advancedDungeon/src/portal/riddles/MyEnergyPelletCatcherBehavior.java");
  private static final String CLASSNAME = "portal.riddles.MyEnergyPelletCatcherBehavior";

  /**
   * Creates a new entity that can catch energy pellets.
   *
   * @param position the position of the pellet catcher.
   * @param catchDirection the direction the pellet catcher is facing.
   * @return a new energyPelletCatcher entity.
   */
  public static Entity energyPelletCatcher(Point position, Direction catchDirection) {
    Entity catcher = new Entity("energyPelletCatcher");
    catcher.add(new PositionComponent(position));
    DrawComponent dc = EnergyPelletLauncher.chooseTexture(catchDirection, PELLET_CATCHER);
    catcher.add(dc);
    catcher.add(new ToggleableComponent(false));

    TriConsumer<Entity, Entity, Direction> action =
        (self, pellet, direction) -> {
          if (pellet.name().matches("energyPelletLauncher_\\d+_skill_projectile")) {
            Object o = null;
            try {
              o = DynamicCompiler.loadUserInstance(PATH, CLASSNAME);
              ((EnergyPelletCatcherBehavior) (o)).catchPellet(self, pellet);

            } catch (Exception e) {
              DialogUtils.showTextPopup(
                  "Der Energie Fänger funktioniert nicht richtig.", "Code Error");
            }
          }
        };

    CollideComponent colComp = new CollideComponent(action, CollideComponent.DEFAULT_COLLIDER);
    catcher.add(colComp);

    return catcher;
  }
}
