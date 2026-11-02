package feature.interaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import com.badlogic.gdx.graphics.Texture;
import engine.Component;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.draw.shader.OutlineShader;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests the outlines that show without a cursor, as in audience mode. */
class InteractionFeedbackTest {
  // The name InteractionFeedback stores its outline under.
  private static final String OUTLINE = "highlight_outline";
  private static final IPath SPRITE = new SimpleIPath("test_assets/textures/mailbox.png");

  private Entity hero;

  @BeforeEach
  void setup() {
    TextureMap.instance().put(SPRITE.pathString(), mock(Texture.class));
    Game.removeAllEntities();
    hero = entity(new PlayerComponent(true));
    hero.add(new InteractionComponent());
  }

  @AfterEach
  void cleanup() {
    InteractionFeedback.followCursor();
    Game.removeAllEntities();
    TextureMap.instance().clear();
  }

  private static Entity entity(Component... components) {
    Entity entity = new Entity();
    entity.add(new PositionComponent(new Point(0, 0)));
    entity.add(new DrawComponent(SPRITE));
    for (Component component : components) entity.add(component);
    Game.add(entity);
    return entity;
  }

  private static Optional<OutlineShader> outline(Entity entity) {
    return Optional.ofNullable(
        (OutlineShader) entity.fetch(DrawComponent.class).orElseThrow().shaders().get(OUTLINE));
  }

  @Test
  void everyInteractableEntityShowsAnOutlineAndTheTargetASolidOne() {
    Entity terminal = entity(new InteractionComponent());
    Entity chest = entity(new InteractionComponent());
    Entity plant = entity();
    InteractionFeedback.showAlways(() -> Optional.of(chest));

    InteractionFeedback.update();

    assertEquals(1f, outline(chest).orElseThrow().color().a);
    assertEquals(0.4f, outline(terminal).orElseThrow().color().a);
    assertNull(outline(plant).orElse(null));
    assertNull(outline(hero).orElse(null));
  }

  @Test
  void outlinesFollowTheTarget() {
    Entity terminal = entity(new InteractionComponent());
    Entity chest = entity(new InteractionComponent());
    InteractionFeedback.showAlways(() -> Optional.of(chest));
    InteractionFeedback.update();

    InteractionFeedback.showAlways(Optional::empty);
    InteractionFeedback.update();

    assertEquals(0.4f, outline(chest).orElseThrow().color().a);
    assertEquals(0.4f, outline(terminal).orElseThrow().color().a);
  }

  @Test
  void filterLimitsTheOutlines() {
    Entity terminal = entity(new InteractionComponent());
    Entity chest = entity(new InteractionComponent());
    InteractionFeedback.showAlways(Optional::empty, entity -> entity == terminal);

    InteractionFeedback.update();

    assertEquals(0.4f, outline(terminal).orElseThrow().color().a);
    assertNull(outline(chest).orElse(null));
  }
}
