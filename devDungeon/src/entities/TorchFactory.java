package entities;

import components.TorchComponent;
import contrib.components.InteractionComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class TorchFactory {

  private static final float DEFAULT_INTERACTION_RADIUS = 2.5f;
  private static final IPath TORCH_TEXTURE_OFF = new SimpleIPath("objects/torch/off/torch_0.png");
  private static final List<IPath> TORCH_TEXTURE_ON =
      List.of(
          new SimpleIPath("objects/torch/on/torch_0.png"),
          new SimpleIPath("objects/torch/on/torch_1.png"),
          new SimpleIPath("objects/torch/on/torch_2.png"),
          new SimpleIPath("objects/torch/on/torch_3.png"));

  public static Entity createTorch(
      Point pos, boolean lit, boolean isInteractable, BiConsumer<Entity, Entity> onInteract) {
    Entity torch = new Entity();

    torch.add(new PositionComponent(pos));
    DrawComponent dc = new DrawComponent(Animation.fromSingleImage(TORCH_TEXTURE_OFF));
    Map<String, Animation> animationMap =
        Map.of("off", dc.currentAnimation(), "on", Animation.fromCollection(TORCH_TEXTURE_ON));
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
                          if (entity
                              .fetch(TorchComponent.class)
                              .orElseThrow(
                                  () ->
                                      MissingComponentException.build(torch, TorchComponent.class))
                              .lit()) {
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
