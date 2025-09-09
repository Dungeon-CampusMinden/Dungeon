package entities;

import components.TorchComponent;
import contrib.components.InteractionComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * This class is responsible for creating torch entities in the game. It defines the default
 * interaction radius and the textures for the torch when it is on and off.
 */
public class TorchFactory {

  private static final float DEFAULT_INTERACTION_RADIUS = 2.5f;

  /**
   * Creates a torch entity at a given position, with a specified initial state (lit or not),
   * interactability, interaction behavior, and value.
   *
   * @param pos The position where the torch will be created.
   * @param lit The initial state of the torch (true if the torch is lit, false otherwise).
   * @param isInteractable Whether the torch can be interacted with.
   * @param onInteract The behavior when the torch is interacted with.
   * @param value The value of the torch.
   * @return The created torch entity.
   */
  public static Entity createTorch(
      Point pos,
      boolean lit,
      boolean isInteractable,
      BiConsumer<Entity, Entity> onInteract,
      int value) {
    Entity torch = new Entity("torch");

    torch.add(new PositionComponent(pos));

    Map<String, Animation> map =
        Animation.loadAnimationSpritesheet(new SimpleIPath("objects/torch/torch.png"));
    State stOff = State.fromMap(map, "off");
    State stOn = State.fromMap(map, "on");
    StateMachine sm = new StateMachine(Arrays.asList(stOff, stOn));
    sm.addTransition(stOff, "on", stOn);
    sm.addTransition(stOn, "off", stOff);
    DrawComponent dc = new DrawComponent(sm);
    if (lit) {
      dc.sendSignal("on");
    } else {
      dc.sendSignal("off");
    }
    torch.add(dc);

    TorchComponent tc = new TorchComponent(lit, value);
    torch.add(tc);

    if (isInteractable)
      torch.add(
          new InteractionComponent(
              DEFAULT_INTERACTION_RADIUS,
              true,
              (entity, who) -> {
                tc.toggle();
                dc.sendSignal(tc.lit() ? "on" : "off");
                onInteract.accept(entity, who);
              }));

    return torch;
  }

  /**
   * Creates an anti-torch entity at a given position, with a specified initial state (lit or not),
   *
   * <p>Anti-torches are torches that, if lit, will amplify the effects of {@link
   * systems.FogOfWarSystem FogOfWar} in the game.
   *
   * @param pos The position where the torch will be created.
   * @param lit The initial state of the torch (true if the torch is lit, false otherwise).
   * @param isInteractable Whether the torch can be interacted with.
   * @param onInteract The behavior when the torch is interacted with.
   * @param value The value of the torch.
   * @return The created torch entity.
   * @see systems.FogOfWarSystem FogOfWarSystem
   * @see level.devlevel.IllusionRiddleLevel IllusionRiddleLevel
   */
  public static Entity createAntiTorch(
      Point pos,
      boolean lit,
      boolean isInteractable,
      BiConsumer<Entity, Entity> onInteract,
      int value) {
    Entity torch = createTorch(pos, lit, isInteractable, onInteract, value);
    torch.name(torch.name().replace("torch", "anti_torch"));
    torch.fetch(DrawComponent.class).ifPresent(dc -> dc.tintColor(0x00FFFFFF));
    return torch;
  }
}
