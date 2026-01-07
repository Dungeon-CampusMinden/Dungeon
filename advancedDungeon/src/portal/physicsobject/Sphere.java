package portal.physicsobject;

import contrib.components.AttachmentComponent;
import contrib.components.CollideComponent;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.Map;

/** A sphere which can be moved by walking into it. */
public class Sphere {
  private static final SimpleIPath PORTAL_SPHERE = new SimpleIPath("portal/kubus");
  private static final float sphere_mass = 3f;
  private static final float sphere_maxSpeed = 10f;

  /**
   * Creates a sphere which can be moved by walking into it.
   *
   * @param position the position where the sphere will spawn.
   * @param mass Mass of the sphere
   * @return the sphere entity
   */
  public static Entity portalSphere(Point position, float mass) {
    Entity sphere = new Entity("moveableSphere");
    sphere.add(new PortalSphereComponent());
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(PORTAL_SPHERE);

    State stIdle = new State("idle", animationMap.get("idle"));
    State stMove = new State("move", animationMap.get("move"));
    StateMachine sm = new StateMachine(Arrays.asList(stIdle, stMove));

    sm.addTransition(stIdle, "move", stMove);
    sm.addTransition(stMove, "move", stMove);
    sm.addTransition(stMove, "idle", stIdle);

    DrawComponent dc = new DrawComponent(sm);
    sphere.add(dc);
    sphere.add(new PositionComponent(position));
    sphere.add(new VelocityComponent(sphere_maxSpeed, mass, entity -> {}, false));

    final boolean[] attached = {false};
    CollideComponent cc = new CollideComponent();
    cc.collideLeave(
        (self, other, dir) -> {
          if (!cc.isSolid() && !attached[0]) {
            cc.isSolid(true);
          }
        });
    sphere.add(cc);

    sphere.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    (interacted, interactor) -> {
                      PositionComponent interactorPositioncomponent =
                          interactor.fetch(PositionComponent.class).orElseThrow();
                      PositionComponent interactedPositioncomponent =
                          interacted.fetch(PositionComponent.class).orElseThrow();
                      if (!attached[0]) {
                        AttachmentComponent attachmentComponent =
                            new AttachmentComponent(
                                Vector2.ZERO,
                                interactedPositioncomponent,
                                interactorPositioncomponent);
                        sphere.add(attachmentComponent);
                        cc.isSolid(false);
                        attached[0] = true;
                      } else {
                        sphere.remove(AttachmentComponent.class);
                        Game.tileAt(interactedPositioncomponent.coordinate())
                            .ifPresent(
                                tile -> {
                                  if (tile.levelElement() == LevelElement.WALL
                                      || tile.levelElement() == LevelElement.GITTER
                                      || tile.levelElement() == LevelElement.GLASSWALL
                                      || tile.levelElement() == LevelElement.PORTAL) {
                                    interactedPositioncomponent.position(
                                        interactorPositioncomponent.position());
                                  }
                                });
                        attached[0] = false;
                      }
                    },
                    2f)));

    return sphere;
  }

  /**
   * Creates a sphere which can be moved by walking into it.
   *
   * @param position the position where the sphere will spawn.
   * @return the sphere entity
   */
  public static Entity portalSphere(Point position) {
    return portalSphere(position, sphere_mass);
  }
}
