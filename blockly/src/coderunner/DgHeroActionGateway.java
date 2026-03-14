package coderunner;

import blockly.dgir.dialect.dg.DgAttrs;
import blockly.dgir.vm.dialect.dg.DgActionGateway;
import com.badlogic.gdx.Gdx;
import components.HeroActionComponent;
import contrib.modules.interaction.InteractionComponent;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.utils.Direction;
import org.jetbrains.annotations.NotNull;

import static coderunner.BlocklyCommands.MAGIC_OFFSET;

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
          var heroOpt = Game.player();
          if (heroOpt.isEmpty()) {
            onComplete.run();
            return;
          }
          heroOpt.get().add(new HeroActionComponent.Rotate(direction, onComplete));
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
          heroOpt.get().add(new HeroActionComponent.MovePushable(true, onComplete));
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
          heroOpt.get().add(new HeroActionComponent.MovePushable(false, onComplete));
        });
  }

  @Override
  public void drop(@NotNull DgAttrs.DropTypeAttr.DropType dropType, @NotNull Runnable onComplete) {
    String item =
        switch (dropType) {
          case CLOVER -> HeroActionComponent.CLOVER;
          case BREADCRUMBS -> HeroActionComponent.BREADCRUMB;
        };
    Gdx.app.postRunnable(
        () -> {
          var heroOpt = Game.player();
          if (heroOpt.isEmpty()) {
            onComplete.run();
            return;
          }
          heroOpt.get().add(new HeroActionComponent.Drop(item, onComplete));
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
          heroOpt.get().add(new HeroActionComponent.Pickup(onComplete));
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
          heroOpt.get().add(new HeroActionComponent.ShootFireball(onComplete));
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
          // Use a 1-second rest duration (RestOp carries no duration attribute).
          heroOpt.get().add(new HeroActionComponent.Rest(1.0f, onComplete));
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
      return Game.tileAt(pc.position().translate(MAGIC_OFFSET)).orElse(null);
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
            pc.position().translate(MAGIC_OFFSET), pc.viewDirection().applyRelative(relative))
        .orElse(null);
  }
}
