package portal.physicsobject;

import contrib.components.AttachmentComponent;
import contrib.components.CollideComponent;
import contrib.components.TransportableComponent;
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
  private static final String idle = "idle";
  private static final String move = "move";

  /**
   * Creates a sphere which can be moved by walking into it.
   *
   * @param position the position where the sphere will spawn.
   * @param mass Mass of the sphere
   * @param isPickupable should this entity be pickupable.
   * @param texture Path to the texture
   * @return the sphere entity
   */
  public static Entity portalSphere(
      Point position, float mass, boolean isPickupable, String texture) {
    Entity sphere = new Entity("moveableSphere");
    sphere.add(new PortalSphereComponent());
    try {
      sphere.add(getDrawComponent(texture));
    } catch (Exception e) {
      sphere.add(new DrawComponent(new SimpleIPath(texture)));
    }

    sphere.add(new PositionComponent(position));
    sphere.add(new VelocityComponent(sphere_maxSpeed, mass, entity -> {}, false));
    TransportableComponent tComp = new TransportableComponent();
    sphere.add(tComp);

    final boolean[] attached = {false};

    CollideComponent cc = createCollideComponent(attached);

    sphere.add(cc);

    if (isPickupable) {
      sphere.add(createInteractionComponent(attached, sphere, cc));
    }
    return sphere;
  }

  /**
   * Creates a sphere which can be moved by walking into it.
   *
   * @param position the position where the sphere will spawn.
   * @return the sphere entity
   */
  public static Entity portalSphere(Point position) {
    return portalSphere(position, sphere_mass, true, PORTAL_SPHERE.pathString());
  }

  private static DrawComponent getDrawComponent(String texture) {
    Map<String, Animation> animationMap =
        Animation.loadAnimationSpritesheet(new SimpleIPath(texture));
    State stIdle = new State(idle, animationMap.get(idle));
    State stMove = new State(move, animationMap.get(move));
    StateMachine sm = new StateMachine(Arrays.asList(stIdle, stMove));

    sm.addTransition(stIdle, move, stMove);
    sm.addTransition(stMove, move, stMove);
    sm.addTransition(stMove, idle, stIdle);

    return new DrawComponent(sm);
  }

  private static CollideComponent createCollideComponent(boolean[] attached) {
    CollideComponent cc = new CollideComponent();
    cc.collideLeave(
        (self, other, dir) -> {
          if (!cc.isSolid() && !attached[0]) {
            cc.isSolid(true);
          }
        });
    return cc;
  }

  private static InteractionComponent createInteractionComponent(
      boolean[] attached, Entity sphere, CollideComponent cc) {
    return new InteractionComponent(
        () ->
            new Interaction(
                (interacted, interactor) ->
                    handlePickup(sphere, cc, attached, interacted, interactor),
                2f));
  }

  private static void handlePickup(
      Entity sphere,
      CollideComponent cc,
      boolean[] attached,
      Entity interacted,
      Entity interactor) {

    PositionComponent interactorPositioncomponent =
        interactor.fetch(PositionComponent.class).orElseThrow();
    PositionComponent interactedPositioncomponent =
        interacted.fetch(PositionComponent.class).orElseThrow();
    if (!attached[0]) {
      AttachmentComponent attachmentComponent =
          new AttachmentComponent(
              Vector2.ZERO, interactedPositioncomponent, interactorPositioncomponent);
      sphere.add(attachmentComponent);
      cc.isSolid(false);
      attached[0] = true;
    } else {
      detachSphere(sphere, interactedPositioncomponent, interactorPositioncomponent, attached);
    }
  }

  private static void detachSphere(
      Entity sphere,
      PositionComponent interactedPos,
      PositionComponent interactorPos,
      boolean[] attached) {
    sphere.remove(AttachmentComponent.class);
    Game.tileAt(interactedPos.coordinate())
        .ifPresent(
            tile -> {
              if (tile.levelElement() == LevelElement.WALL
                  || tile.levelElement() == LevelElement.GITTER
                  || tile.levelElement() == LevelElement.GLASSWALL
                  || tile.levelElement() == LevelElement.PORTAL) {
                interactedPos.position(interactorPos.position());
              }
            });
    attached[0] = false;
  }
}
