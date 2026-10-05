package feature.achievements;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.TimeUtils;
import engine.Entity;
import engine.Game;
import engine.System;
import engine.game.ECSManagement;
import engine.game.PreRunConfiguration;
import engine.network.server.DialogTracker;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import testingUtils.MockNetworkHandler;

/** Exercises queued popups through the real ECS and HUD pause handling without graphics. */
class AchievementPopupSystemTest {

  private final AtomicLong now = new AtomicLong();
  private final Group stageRoot = new Group();
  private final Group firstCard = new Group();
  private final Group secondCard = new Group();
  private MockedStatic<Game> game;
  private MockedStatic<TimeUtils> clock;
  private MockedStatic<AchievementPopup> cards;
  private AchievementPopupSystem popups;

  @BeforeAll
  static void registerDialogType() {
    DialogFactory.register(TestDialogType.TEST, ignored -> new Group());
  }

  @BeforeEach
  void setUp() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    MockNetworkHandler.useLocalNetworkHandler();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);

    Stage stage = mock(Stage.class);
    when(stage.getActors()).thenReturn(stageRoot.getChildren());
    doAnswer(
            invocation -> {
              stageRoot.addActor(invocation.getArgument(0, Actor.class));
              return null;
            })
        .when(stage)
        .addActor(any(Actor.class));
    game = mockStatic(Game.class, Mockito.CALLS_REAL_METHODS);
    game.when(Game::isHeadless).thenReturn(false);
    game.when(Game::stage).thenReturn(Optional.of(stage));
    game.when(Game::windowWidth).thenReturn(0);
    game.when(Game::windowHeight).thenReturn(0);
    clock = mockStatic(TimeUtils.class, Mockito.CALLS_REAL_METHODS);
    clock.when(TimeUtils::millis).thenAnswer(ignored -> now.get());
    cards = mockStatic(AchievementPopup.class);
    cards.when(() -> AchievementPopup.buildCard("first.png", "first")).thenReturn(firstCard);
    cards.when(() -> AchievementPopup.buildCard("second.png", "second")).thenReturn(secondCard);

    Game.add(Game.hud());
    Game.hud().dialogsSuppressed(false);
    Game.hud().execute();
    popups = new AchievementPopupSystem();
    Game.add(popups);
  }

  @AfterEach
  void tearDown() {
    Game.removeAllEntities();
    Game.hud().execute();
    Game.removeAllSystems();
    DialogTracker.instance().clear();
    MockNetworkHandler.useLocalNetworkHandler();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
    cards.close();
    clock.close();
    game.close();
  }

  @Test
  void pausingUiStopsGameplayButThePopupQueueKeepsShowingCardsInOrder() {
    System gameplay = new PausableSystem();
    Game.add(gameplay);
    Entity owner = Entity.createLocalEntity("pausing-dialog");
    DialogContext context = DialogContext.builder().type(TestDialogType.TEST).build();
    context.owner(owner.id());
    UIComponent dialog = new UIComponent(context, true, false);
    owner.add(dialog);
    Game.add(owner);
    popups.enqueue("first", "first.png");
    popups.enqueue("second", "second.png");

    ECSManagement.executeOneTick(System.AuthoritativeSide.BOTH);

    assertFalse(gameplay.isRunning());
    assertTrue(popups.isRunning());
    assertTrue(stageRoot.getChildren().contains(firstCard, true));
    assertNull(secondCard.getParent());
    assertTrue(dialog.isVisible());

    now.set(AchievementPopupQueue.DISPLAY_DURATION_MS - 1);
    ECSManagement.executeOneTick(System.AuthoritativeSide.BOTH);
    assertTrue(stageRoot.getChildren().contains(firstCard, true));
    assertNull(secondCard.getParent());

    now.set(AchievementPopupQueue.DISPLAY_DURATION_MS);
    ECSManagement.executeOneTick(System.AuthoritativeSide.BOTH);
    assertNull(firstCard.getParent());
    assertTrue(stageRoot.getChildren().contains(secondCard, true));
    assertFalse(gameplay.isRunning());
    assertTrue(dialog.isVisible());

    now.set(2 * AchievementPopupQueue.DISPLAY_DURATION_MS);
    ECSManagement.executeOneTick(System.AuthoritativeSide.BOTH);
    assertNull(secondCard.getParent());
    cards.verify(() -> AchievementPopup.buildCard("first.png", "first"), times(1));
    cards.verify(() -> AchievementPopup.buildCard("second.png", "second"), times(1));
  }

  private static final class PausableSystem extends System {
    private PausableSystem() {
      super(AuthoritativeSide.BOTH);
    }

    @Override
    public void execute() {}
  }

  private enum TestDialogType implements DialogType {
    TEST;

    @Override
    public String type() {
      return "achievement-popup-pause-test";
    }
  }
}
