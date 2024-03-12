package entities;

import components.TorchComponent;
import contrib.components.InteractionComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.draw.Animation;
import core.utils.components.path.SimpleIPath;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class TorchFactory {

  private static final float DEFAULT_INTERACTION_RADIUS = 2.5f;
  private static final Animation TORCH_TEXTURE_OFF =
      Animation.fromSingleImage(new SimpleIPath("objects/torch/off/torch_0.png"));
  private static final Animation TORCH_TEXTURE_ON =
      Animation.fromCollection(
          List.of(
              new SimpleIPath("objects/torch/on/torch_0.png"),
              new SimpleIPath("objects/torch/on/torch_1.png"),
              new SimpleIPath("objects/torch/on/torch_2.png"),
              new SimpleIPath("objects/torch/on/torch_3.png")));

  public static Entity createTorch(
      Point pos, boolean lit, boolean isInteractable, BiConsumer<Entity, Entity> onInteract) {
    Entity torch = new Entity();

    torch.add(new PositionComponent(pos));
    DrawComponent dc = new DrawComponent(TORCH_TEXTURE_OFF);
    Map<String, Animation> animationMap = Map.of("off", TORCH_TEXTURE_OFF, "on", TORCH_TEXTURE_ON);
    dc.animationMap(animationMap);
    dc.currentAnimation(lit ? "on" : "off");
    torch.add(dc);
    torch.add(new TorchComponent(lit));

    if (isInteractable)
      torch.add(
          new InteractionComponent(
              DEFAULT_INTERACTION_RADIUS,
              true,
              (entity, who) -> {
                entity.fetch(TorchComponent.class).ifPresent(TorchComponent::toggle);
                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(
                        drawComponent -> {
                          if (entity.fetch(TorchComponent.class).get().lit()) {
                            drawComponent.currentAnimation("on");
                          } else {
                            drawComponent.currentAnimation("off");
                          }
                        });
                onInteract.accept(entity, who);
              }));

    return torch;
  }
}
