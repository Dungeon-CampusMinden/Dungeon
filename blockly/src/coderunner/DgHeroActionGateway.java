package coderunner;

import blockly.dgir.dialect.dg.DgAttrs;
import blockly.dgir.vm.dialect.dg.DgActionGateway;
import com.badlogic.gdx.Gdx;
import components.AmmunitionComponent;
import components.BlocklyItemComponent;
import components.HeroActionComponent;
import contrib.components.CollideComponent;
import contrib.modules.interaction.InteractionComponent;
import contrib.systems.EventScheduler;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.utils.Direction;
import core.utils.MissingPlayerException;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import entities.MiscFactory;
import entities.monster.BlocklyMonster;
import org.jetbrains.annotations.NotNull;

/**
 * Game-side implementation of {@link DgActionGateway}.
 *
 * <p>Each action is scheduled on the libGDX render (game) thread via {@code
 * Gdx.app.postRunnable(Runnable)}. Component-based actions attach the appropriate {@link
 * HeroActionComponent} to the hero entity and pass {@code onComplete} as its completion callback;
 * the callback fires from inside {@link HeroActionComponent#endAction()} once the action finishes,
 * which in turn releases the {@link java.util.concurrent.CountDownLatch} that is blocking the VM
 * thread.
 *
 * <p>For instant, component-less actions (e.g. {@link #use}) the interaction logic runs directly in
 * the {@code postRunnable} and {@code onComplete} is called immediately afterwards.
 *
 * <p>If {@link Game#player()} returns empty for any action, {@code onComplete} is still called so
 * the VM thread is never left blocked indefinitely.
 */
public class DgHeroActionGateway implements DgActionGateway {
  public static float REST_DURATION = 1f;

  /**
   * All this stuff should ideally be in a config file and not hardcoded. TODO Maybe find a willing
   * intern to do this in a future PR?
   */
  private static final float FIREBALL_REST_TIME = 1f;

  private static final float FIREBALL_RANGE = Integer.MAX_VALUE;
  private static final float FIREBALL_SPEED = 15;
  private static final int FIREBALL_DMG = 1;
  private static final boolean IGNORE_FIRST_WALL = false;
  private static final FireballSkill fireballSkill =
      new FireballSkill(
          () -> {
            Entity player = Game.player().orElseThrow(MissingPlayerException::new);
            return player
                .fetch(CollideComponent.class)
                .map(cc -> cc.collider().absoluteCenter())
                .map(p -> p.translate(EntityUtils.getViewDirection(player)))
                .orElseThrow(() -> MissingComponentException.build(player, CollideComponent.class));
          },
          1,
          FIREBALL_SPEED,
          FIREBALL_RANGE,
          FIREBALL_DMG,
          IGNORE_FIRST_WALL);

  @Override
  public void move(@NotNull Runnable onComplete) {
    Gdx.app.postRunnable(
        () -> {
          var heroOpt = Game.player();
          if (heroOpt.isEmpty()) {
            onComplete.run();
            return;
          }
          heroOpt.get().add(new HeroActionComponent.Move(onComplete));
        });
  }

  @Override
  public void turn(@NotNull DgAttrs.TurnDirectionAttr.TurnDir dir, @NotNull Runnable onComplete) {
    Direction direction =
        switch (dir) {
          case LEFT -> Direction.LEFT;
          case RIGHT -> Direction.RIGHT;
        };
    Gdx.app.postRunnable(
        () -> {
          // Update player
          {
            var heroOpt = Game.player();
            if (heroOpt.isEmpty()) {
              onComplete.run();
              return;
            }
            PositionComponent pc =
                heroOpt
                    .get()
                    .fetch(PositionComponent.class)
                    .orElseThrow(
                        () ->
                            MissingComponentException.build(
                                heroOpt.get(), PositionComponent.class));
            pc.viewDirection(pc.viewDirection().applyRelative(direction));
          }
          // Update black night
          {
            var blackNightOpt =
                Game.levelEntities()
                    .filter(entity -> entity.name().equals(BlocklyMonster.BLACK_KNIGHT_NAME))
                    .findFirst();
            blackNightOpt
                .flatMap(entity -> entity.fetch(PositionComponent.class))
                .ifPresent(pc -> pc.viewDirection(pc.viewDirection().applyRelative(direction)));
          }
          onComplete.run();
        });
  }

  @Override
  public void use(@NotNull DgAttrs.UseDirectionAttr.UseDir dir, @NotNull Runnable onComplete) {
    Gdx.app.postRunnable(
        () -> {
          try {
            Game.player()
                .ifPresent(
                    hero ->
                        hero.fetch(PositionComponent.class)
                            .ifPresent(
                                pc -> {
                                  Tile tile = resolveTile(pc, dir);
                                  Game.entityAtTile(tile)
                                      .forEach(
                                          entity ->
                                              entity
                                                  .fetch(InteractionComponent.class)
                                                  .ifPresent(
                                                      ic -> ic.triggerInteraction(entity, hero)));
                                }));
          } finally {
            // Interaction is instant; always unblock the VM thread.
            onComplete.run();
          }
        });
  }

  @Override
  public void push(@NotNull Runnable onComplete) {
    Gdx.app.postRunnable(
        () -> {
          var heroOpt = Game.player();
          if (heroOpt.isEmpty()) {
            onComplete.run();
            return;
          }
          var moveC = HeroActionComponent.MovePushable.of(true, onComplete);
          if (moveC.isEmpty()) {
            onComplete.run();
            return;
          }
          heroOpt.get().add(moveC.get());
        });
  }

  @Override
  public void pull(@NotNull Runnable onComplete) {
    Gdx.app.postRunnable(
        () -> {
          var heroOpt = Game.player();
          if (heroOpt.isEmpty()) {
            onComplete.run();
            return;
          }
          var moveC = HeroActionComponent.MovePushable.of(false, onComplete);
          if (moveC.isEmpty()) {
            onComplete.run();
            return;
          }
          heroOpt.get().add(moveC.get());
        });
  }

  @Override
  public void drop(@NotNull DgAttrs.DropTypeAttr.DropType dropType, @NotNull Runnable onComplete) {
    Gdx.app.postRunnable(
        () -> {
          var heroOpt = Game.player();
          if (heroOpt.isEmpty()) {
            onComplete.run();
            return;
          }
          var hero = heroOpt.get();
          Point heroPos =
              hero.fetch(PositionComponent.class)
                  .map(PositionComponent::position)
                  .map(pos -> pos.translate(0.5f, 0.5f))
                  .orElse(null);

          switch (dropType) {
            case BREADCRUMBS -> Game.add(MiscFactory.breadcrumb(heroPos));
            case CLOVER -> Game.add(MiscFactory.clover(heroPos));
            default ->
                throw new IllegalArgumentException(
                    "Can not convert " + dropType + " to droppable Item.");
          }
        });
  }

  @Override
  public void pickup(@NotNull Runnable onComplete) {
    Gdx.app.postRunnable(
        () -> {
          var heroOpt = Game.player();
          if (heroOpt.isEmpty()) {
            onComplete.run();
            return;
          }
          var hero = heroOpt.get();
          // Get the tile at the position of the hero
          hero.fetch(PositionComponent.class)
              .map(PositionComponent::position)
              .map(pos -> pos.translate(0.5f, 0.5f))
              .flatMap(Game::tileAt)
              .map(Game::entityAtTile)
              // Filter the entities to only include entities that are items
              .ifPresent(
                  stream ->
                      stream
                          .filter(e -> e.isPresent(BlocklyItemComponent.class))
                          // Trigger the interaction of each item with the hero, which
                          // should result in the item being picked up and added to the
                          // inventory
                          .forEach(
                              item ->
                                  item.fetch(InteractionComponent.class)
                                      .ifPresent(ic -> ic.triggerInteraction(item, hero))));
          onComplete.run();
        });
  }

  @Override
  public void fireball(@NotNull Runnable onComplete) {
    Gdx.app.postRunnable(
        () -> {
          var heroOpt = Game.player();
          if (heroOpt.isEmpty()) {
            onComplete.run();
            return;
          }
          var hero = heroOpt.get();
          hero.fetch(AmmunitionComponent.class)
              .filter(AmmunitionComponent::checkAmmunition)
              .ifPresent(
                  ac -> {
                    fireballSkill.execute(hero);
                    ac.spendAmmo();
                  });

          EventScheduler.scheduleAction(onComplete, (long) (1000 * FIREBALL_REST_TIME));
        });
  }

  @Override
  public void rest(@NotNull Runnable onComplete) {
    Gdx.app.postRunnable(
        () -> {
          var heroOpt = Game.player();
          if (heroOpt.isEmpty()) {
            onComplete.run();
            return;
          }

          EventScheduler.scheduleAction(onComplete, (long) (1000 * REST_DURATION));
        });
  }

  // =========================================================================
  // Private helpers
  // =========================================================================

  /**
   * Resolves the {@link Tile} targeted by a {@code use} operation in the given direction.
   *
   * @param pc the hero's position component.
   * @param dir the use direction.
   * @return the target tile, or {@code null} if no tile exists in that direction.
   */
  private static Tile resolveTile(
      @NotNull PositionComponent pc, @NotNull DgAttrs.UseDirectionAttr.UseDir dir) {
    if (dir == DgAttrs.UseDirectionAttr.UseDir.HERE) {
      return Game.tileAt(pc.position().translate(Vector2.of(0.5f, 0.5f))).orElse(null);
    }
    Direction relative =
        switch (dir) {
          case UP -> Direction.UP;
          case DOWN -> Direction.DOWN;
          case LEFT -> Direction.LEFT;
          case RIGHT -> Direction.RIGHT;
          default -> Direction.NONE;
        };
    return Game.tileAt(
            pc.position().translate(Vector2.of(0.5f, 0.5f)),
            pc.viewDirection().applyRelative(relative))
        .orElse(null);
  }
}
