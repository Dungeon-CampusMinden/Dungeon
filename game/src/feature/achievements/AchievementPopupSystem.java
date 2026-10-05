package feature.achievements;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.TimeUtils;
import engine.Game;
import engine.System;

/** Displays locally received achievement notifications one at a time. */
public final class AchievementPopupSystem extends System {

  private final AchievementPopupQueue queue = new AchievementPopupQueue();
  private Group visibleCard;

  /** Creates the client-side achievement popup system. */
  public AchievementPopupSystem() {
    super(AuthoritativeSide.CLIENT);
  }

  /** Achievement popups keep running while a pausing dialog is open. */
  @Override
  public void stop() {}

  void enqueue(String id, String imagePath) {
    queue.enqueue(id, imagePath);
  }

  @Override
  public void execute() {
    if (Game.isHeadless()) {
      return;
    }
    Stage stage = Game.stage().orElse(null);
    if (stage == null) {
      return;
    }
    AchievementPopupQueue.Transition transition = queue.advance(TimeUtils.millis());
    transition.finished().ifPresent(ignored -> removeCard());
    transition.started().ifPresent(entry -> showCard(entry, stage));
    if (visibleCard != null) {
      visibleCard.setVisible(!Game.hud().dialogsSuppressed());
    }
  }

  private void showCard(AchievementPopupQueue.Entry entry, Stage stage) {
    visibleCard = AchievementPopup.buildCard(entry.imagePath(), entry.id());
    stage.addActor(visibleCard);
  }

  private void removeCard() {
    if (visibleCard != null) {
      visibleCard.remove();
      visibleCard = null;
    }
  }
}
