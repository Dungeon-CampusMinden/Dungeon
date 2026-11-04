package feature.input.systems;

import engine.Entity;
import engine.Game;
import engine.System;
import engine.input.CoreActions;
import engine.input.CursorUtils;
import engine.input.GameInput;
import engine.input.InputContext;
import engine.network.messages.c2s.InputMessage;
import engine.utils.Vector2;
import feature.entities.HeroController;
import feature.hud.dialogs.PauseDialog;
import feature.questlog.QuestLogUI;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Turns the gameplay actions of the local player into network input.
 *
 * <p>Owns the {@code gameplay} context, which exists while a local hero exists, and mirrors the
 * server's control lock as a blocking context above it. Held actions are read every frame, so a
 * blocking context above stops them at once. Movement goes out once per frame while the hero moves;
 * the stop goes out over UDP for low latency and over TCP so it cannot get lost, and the server
 * applies whichever copy arrives first.
 */
public final class GameplayInputSystem extends System {
  private final InputContext gameplay = new InputContext(CoreActions.GAMEPLAY);
  private final InputContext serverLock = new InputContext(CoreActions.SERVER_LOCK);
  private boolean moving;

  /** Creates the system and the reactions of the {@code gameplay} context. */
  public GameplayInputSystem() {
    super(AuthoritativeSide.CLIENT);
    gameplay
        .onPress(CoreActions.SKILL_MAIN_NEXT, () -> send(InputMessage.nextSkill(true)))
        .onPress(CoreActions.SKILL_MAIN_PREV, () -> send(InputMessage.prevSkill(true)))
        .onPress(CoreActions.SKILL_SECOND_NEXT, () -> send(InputMessage.nextSkill(false)))
        .onPress(CoreActions.SKILL_SECOND_PREV, () -> send(InputMessage.prevSkill(false)))
        .onPress(CoreActions.INVENTORY, () -> send(InputMessage.toggleInventory()))
        .onPress(CoreActions.INTERACT, () -> withHero(GameplayInputSystem::interact))
        .onPress(CoreActions.PAUSE_MENU, () -> withHero(PauseDialog::showPauseDialog))
        .onPress(CoreActions.QUESTLOG, () -> withHero(QuestLogUI::requestQuestLog));
  }

  private static void send(InputMessage message) {
    Game.network().sendInput(message);
  }

  private static void withHero(Consumer<Entity> action) {
    Game.player().ifPresent(action);
  }

  private static void interact(Entity hero) {
    HeroController.requestInteraction(hero, CursorUtils.positionInWorld());
  }

  @Override
  public void execute() {
    Optional<Entity> hero = Game.player();
    if (hero.isEmpty()) {
      GameInput.remove(gameplay);
      GameInput.remove(serverLock);
      moving = false;
      return;
    }
    GameInput.push(gameplay);
    if (HeroController.controlsLocked(hero.get())) GameInput.push(serverLock);
    else GameInput.remove(serverLock);
    sendMovement();
    if (GameInput.pressed(CoreActions.SKILL_MAIN)) {
      send(InputMessage.castSkill(CursorUtils.positionInWorld(), true));
    }
    if (GameInput.pressed(CoreActions.SKILL_SECOND)) {
      send(InputMessage.castSkill(CursorUtils.positionInWorld(), false));
    }
  }

  private void sendMovement() {
    Vector2 direction = CoreActions.move();
    if (!direction.isZero()) {
      moving = true;
      send(InputMessage.move(direction));
    } else if (moving) {
      moving = false;
      InputMessage stop = InputMessage.move(Vector2.ZERO);
      send(stop);
      Game.network().send((short) 0, stop, true);
    }
  }

  /** Input must keep flowing while the game is paused, so a release is never lost. */
  @Override
  public void stop() {
    this.run = true;
  }
}
