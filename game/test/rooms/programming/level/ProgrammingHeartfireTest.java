package rooms.programming.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.utils.components.path.SimpleIPath;
import feature.achievements.AchievementManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProgrammingHeartfireTest {
  @Test
  void onlyExtinguishingTheHeartfireAwardsItsAchievementToTheActingPlayer() {
    Entity player = new Entity("player");
    Entity fire = new Entity("programming-prop-torch-decisions-heart");
    Entity torch = new Entity("programming-prop-torch-decisions-gallery-0");
    var manager = mock(AchievementManager.class);
    try (var game = mockStatic(Game.class);
        var achievements = mockStatic(AchievementManager.class);
        var progress = mockStatic(ProgrammingProgress.class)) {
      game.when(Game::isHeadless).thenReturn(true);
      achievements.when(AchievementManager::instance).thenReturn(manager);
      fire.add(new DrawComponent(new SimpleIPath("objects/torch"), "on"));
      torch.add(new DrawComponent(new SimpleIPath("objects/torch"), "on"));
      ProgrammingProps.toggleTorch(torch, player);
      verify(manager, never()).popFor(player, "programming_heartfire_out");
      ProgrammingProps.toggleTorch(fire, player);
      assertEquals(
          "off",
          fire.fetch(DrawComponent.class).orElseThrow().stateMachine().getCurrentStateName());
      verify(manager).popFor(player, "programming_heartfire_out");
      ProgrammingProps.toggleTorch(fire, player);
      assertEquals(
          "on", fire.fetch(DrawComponent.class).orElseThrow().stateMachine().getCurrentStateName());
      verify(manager, times(1)).popFor(player, "programming_heartfire_out");
      game.when(Game::isMultiplayerClient).thenReturn(true);
      ProgrammingProps.toggleTorch(fire, player);
      assertEquals(
          "on", fire.fetch(DrawComponent.class).orElseThrow().stateMachine().getCurrentStateName());
      verify(manager, times(1)).popFor(player, "programming_heartfire_out");
    }
  }

  @Test
  void heartfireIsCenteredOnItsPlinth() {
    List<Entity> spawned = new ArrayList<>();
    try (var game = mockStatic(Game.class)) {
      game.when(Game::isHeadless).thenReturn(true);
      game.when(() -> Game.add(any(Entity.class)))
          .thenAnswer(
              invocation -> {
                spawned.add(invocation.getArgument(0));
                return null;
              });
      ProgrammingDecisionWorld.spawn(mock(DungeonLevel.class));
      Entity fire = named(spawned, "programming-prop-torch-decisions-heart");
      Entity plinth = named(spawned, "programming-decisions-heart-plinth");
      assertEquals(centerX(plinth), centerX(fire), .001f);
    }
  }

  private static Entity named(List<Entity> entities, String name) {
    return entities.stream().filter(entity -> entity.name().equals(name)).findFirst().orElseThrow();
  }

  private static float centerX(Entity entity) {
    var position = entity.fetch(PositionComponent.class).orElseThrow();
    return position.position().x()
        + position.scale().x() * entity.fetch(DrawComponent.class).orElseThrow().getWidth() / 2;
  }
}
