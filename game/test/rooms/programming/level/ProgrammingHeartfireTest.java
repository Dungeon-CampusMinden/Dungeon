package rooms.programming.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import feature.achievements.AchievementManager;
import feature.components.LeverComponent;
import feature.entities.LeverFactory;
import feature.prefabs.PrefabSide;
import feature.prefabs.PrefabSpawner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProgrammingHeartfireTest {
  @Test
  void onlyExtinguishingTheHeartfireAwardsItsAchievementToTheActingPlayer() {
    Entity player = new Entity("player");
    Entity fire = new Entity("torch-decisions-heart");
    Entity torch = new Entity("torch-decisions-gallery-0");
    var manager = mock(AchievementManager.class);
    try (var game = mockStatic(Game.class);
        var achievements = mockStatic(AchievementManager.class);
        var progress = mockStatic(ProgrammingProgress.class)) {
      game.when(Game::isHeadless).thenReturn(true);
      achievements.when(AchievementManager::instance).thenReturn(manager);
      LeverFactory.createTorch(fire, new Point(0, 0), true, true, false);
      LeverFactory.createTorch(torch, new Point(1, 0), true, true, false);
      ProgrammingProps.toggleTorch(torch, player);
      verify(manager, never()).popFor(player, "programming_heartfire_out");
      ProgrammingProps.toggleTorch(fire, player);
      assertEquals(
          "off",
          fire.fetch(DrawComponent.class).orElseThrow().stateMachine().getCurrentStateName());
      assertFalse(fire.fetch(LeverComponent.class).orElseThrow().isOn());
      verify(manager).popFor(player, "programming_heartfire_out");
      ProgrammingProps.toggleTorch(fire, player);
      assertEquals(
          "on", fire.fetch(DrawComponent.class).orElseThrow().stateMachine().getCurrentStateName());
      assertTrue(fire.fetch(LeverComponent.class).orElseThrow().isOn());
      verify(manager, times(1)).popFor(player, "programming_heartfire_out");
      game.when(Game::isMultiplayerClient).thenReturn(true);
      ProgrammingProps.toggleTorch(fire, player);
      assertEquals(
          "on", fire.fetch(DrawComponent.class).orElseThrow().stateMachine().getCurrentStateName());
      assertTrue(fire.fetch(LeverComponent.class).orElseThrow().isOn());
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
      DungeonLevel level =
          new DungeonLevel(
              new LevelElement[][] {{LevelElement.FLOOR}},
              DesignLabel.DEFAULT,
              new HashMap<>(),
              new ArrayList<>()) {
            @Override
            protected void onFirstTick() {
              ProgrammingDecisionWorld.spawn(this);
            }
          };
      game.when(Game::currentLevel).thenReturn(Optional.of(level));
      game.when(() -> Game.findEntityById(org.mockito.Mockito.anyInt()))
          .thenAnswer(
              invocation ->
                  spawned.stream()
                      .filter(entity -> entity.id() == (int) invocation.getArgument(0))
                      .findFirst());
      level.onTick(true);
      Entity fire = named(spawned, "torch-decisions-heart");
      Entity plinth = named(spawned, "programming-decisions-heart-plinth");
      assertEquals(centerX(plinth), centerX(fire), .001f);
      PrefabSpawner.clear(PrefabSide.SERVER);
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
