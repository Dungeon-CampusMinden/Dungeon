package rooms.systemRecovery.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import engine.Entity;
import engine.Game;
import engine.sound.Sounds;
import engine.systems.DrawSystem;
import engine.utils.IVoidFunction;
import engine.utils.Point;
import feature.emote.Emote;
import feature.emote.EmoteFactory;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.InteractionComponent;
import feature.utils.EntityUtils;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import rooms.lasthour.util.LastHourSounds;
import rooms.systemRecovery.util.SystemRecoveryAchievements;
import rooms.systemRecovery.util.SystemRecoveryQuestLogUtil;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Verifies one-shot first contact, call priority, restoration and stale dialog callbacks. */
class SystemRecoveryPhoneControllerTest {

  private final Point point = new Point(4, 7);
  private final Entity phone = new Entity("phone");
  private final Entity player = new Entity("player");
  private final Entity emote = new Entity("ringing-phone");
  private final AtomicReference<IVoidFunction> finish = new AtomicReference<>();
  private final AtomicReference<String> script = new AtomicReference<>();
  private final AtomicInteger storageOpens = new AtomicInteger();
  private final AtomicInteger elevatorOpens = new AtomicInteger();
  private final AtomicInteger saves = new AtomicInteger();
  private final SystemRecoveryPhoneController controller =
      new SystemRecoveryPhoneController(
          storageOpens::incrementAndGet, elevatorOpens::incrementAndGet, saves::incrementAndGet);
  private MockedStatic<DecoFactory> decorations;
  private MockedStatic<DrawSystem> draw;
  private MockedStatic<Game> game;
  private MockedStatic<Sounds> sounds;
  private MockedStatic<EmoteFactory> emotes;
  private MockedStatic<EntityUtils> entities;
  private MockedStatic<DialogFactory> dialogs;
  private MockedStatic<SystemRecoveryAchievements> achievements;
  private MockedStatic<SystemRecoveryQuestLogUtil> questLog;

  @BeforeEach
  void setUp() {
    decorations = mockStatic(DecoFactory.class);
    draw = mockStatic(DrawSystem.class);
    game = mockStatic(Game.class);
    sounds = mockStatic(Sounds.class);
    emotes = mockStatic(EmoteFactory.class);
    entities = mockStatic(EntityUtils.class);
    dialogs = mockStatic(DialogFactory.class);
    achievements = mockStatic(SystemRecoveryAchievements.class);
    questLog = mockStatic(SystemRecoveryQuestLogUtil.class);
    decorations.when(() -> DecoFactory.createDeco(point, Deco.Phone)).thenReturn(phone);
    draw.when(DrawSystem::getInstance).thenReturn(mock(DrawSystem.class));
    entities.when(() -> EntityUtils.getPosition(phone)).thenReturn(point);
    emotes
        .when(() -> EmoteFactory.createEmote(point, Emote.EXCLAMATION, 60 * 60 * 1000))
        .thenReturn(emote);
    dialogs
        .when(
            () ->
                DialogFactory.showDialogDialog(
                    anyString(), anyString(), any(IVoidFunction.class), anyInt()))
        .thenAnswer(
            invocation -> {
              script.set(invocation.getArgument(0));
              finish.set(invocation.getArgument(2));
              return null;
            });
    controller.setup(point);
  }

  @AfterEach
  void tearDown() {
    questLog.close();
    achievements.close();
    dialogs.close();
    entities.close();
    emotes.close();
    sounds.close();
    game.close();
    draw.close();
    decorations.close();
  }

  @Test
  void reminderCannotReplaceOpeningOrRequiredCalls() {
    controller.triggerOpeningCall();
    assertFalse(controller.triggerHintReminder());
    answer();
    assertEquals(SystemRecoveryText.echoCall("opening-call"), script.get());
    game.verify(() -> Game.remove(emote));
    finish.get().execute();
    achievements.verify(() -> SystemRecoveryAchievements.phoneCallAnswered(false));
    controller.triggerDataStorageProblemCall();
    controller.cancelHintReminder();
    assertFalse(controller.triggerHintReminder());
    answer();
    assertEquals(SystemRecoveryText.echoCall("data-storage-problem"), script.get());
    finish.get().execute();
    assertEquals(1, storageOpens.get());
    achievements.verify(() -> SystemRecoveryAchievements.phoneCallAnswered(true));
  }

  @Test
  void helpBeforeFirstAttemptIntroducesEchoInsteadOfLeavingADeadLine() {
    assertTrue(controller.triggerHintReminder());
    answer();
    assertEquals(SystemRecoveryText.echoCall("opening-call-idle"), script.get());
    finish.get().execute();
    achievements.verify(() -> SystemRecoveryAchievements.phoneCallAnswered(false));
    questLog.verify(
        () ->
            SystemRecoveryQuestLogUtil.addDialogEntry(
                "riddle1", "opening-call-idle", SystemRecoveryText.echoCall("opening-call-idle")));
    assertFalse(controller.triggerIdleOpeningCall());
    assertTrue(controller.triggerHintReminder());
    answer();
    assertEquals(SystemRecoveryText.echoCall("hint-reminder"), script.get());
  }

  @ParameterizedTest
  @ValueSource(strings = {"opening-call", "opening-call-correct", "opening-call-idle"})
  void allOpeningVariantsRingOnceAndRecordTheirOwnText(String key) {
    switch (key) {
      case "opening-call" -> controller.triggerOpeningCall();
      case "opening-call-correct" -> controller.triggerCorrectOpeningCall();
      default -> assertTrue(controller.triggerIdleOpeningCall());
    }
    assertFalse(controller.triggerIdleOpeningCall());
    controller.triggerOpeningCall();
    controller.triggerCorrectOpeningCall();
    answer();
    assertEquals(SystemRecoveryText.echoCall(key), script.get());
    finish.get().execute();
    questLog.verify(
        () ->
            SystemRecoveryQuestLogUtil.addDialogEntry(
                "riddle1", key, SystemRecoveryText.echoCall(key)));
    assertFalse(controller.triggerIdleOpeningCall());
    game.verify(() -> Game.add(emote), times(1));
  }

  @Test
  void restoredIntroductionDoesNotRingAgain() {
    controller.restorePastIntroduction(true);
    assertFalse(controller.triggerIdleOpeningCall());
    controller.triggerOpeningCall();
    controller.triggerCorrectOpeningCall();
    sounds.verifyNoInteractions();
    assertTrue(controller.triggerHintReminder());
  }

  @Test
  void finalCallReplacesOptionalReminderAndStillOpensTheElevator() {
    controller.restorePastIntroduction(true);
    controller.triggerHintReminder();
    controller.triggerFinalEchoCall();
    controller.cancelHintReminder();
    assertFalse(controller.triggerHintReminder());
    answer();
    assertEquals(SystemRecoveryText.echoCall("final-call"), script.get());
    finish.get().execute();
    assertEquals(1, elevatorOpens.get());
  }

  @Test
  void answeredReminderDoesNotCountAsStoryContactOrOpenADoor() {
    controller.restorePastIntroduction(true);
    assertTrue(controller.triggerHintReminder());
    assertFalse(controller.triggerHintReminder());
    answer();
    assertEquals(SystemRecoveryText.echoCall("hint-reminder"), script.get());
    game.verify(() -> Game.remove(emote));
    finish.get().execute();
    assertEquals(0, storageOpens.get());
    achievements.verifyNoInteractions();
    questLog.verifyNoInteractions();
    assertTrue(controller.triggerHintReminder());
  }

  @Test
  void progressCancelsOptionalRingingAndItsBubble() {
    controller.restorePastIntroduction(true);
    assertTrue(controller.triggerHintReminder());
    controller.cancelHintReminder();
    game.verify(() -> Game.remove(emote));
    assertTrue(controller.triggerHintReminder());
    game.verify(() -> Game.add(emote), times(2));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void staleReminderCallbackCannotFinishANewCallEvenWithTheSameScript(boolean sameScript) {
    controller.restorePastIntroduction(true);
    controller.triggerHintReminder();
    answer();
    IVoidFunction stale = finish.get();
    if (sameScript) {
      controller.cancelHintReminder();
      assertTrue(controller.triggerHintReminder());
    } else {
      controller.triggerDataStorageProblemCall();
    }
    stale.execute();
    assertEquals(0, storageOpens.get());
    assertFalse(controller.triggerHintReminder());
    answer();
    String key = sameScript ? "hint-reminder" : "data-storage-problem";
    assertEquals(SystemRecoveryText.echoCall(key), script.get());
    finish.get().execute();
    assertEquals(sameScript ? 0 : 1, storageOpens.get());
  }

  @Test
  void restoredCoreWarningRingsUntilAnsweredAndPersistsCompletionOnce() {
    controller.restoreSystemCoreCalls(false, true, false);
    assertFalse(controller.systemCoreWarningCallAnswered());
    answer();
    dialogs.verify(
        () ->
            DialogFactory.showDialogDialog(
                eq(SystemRecoveryText.echoCall("system-core-warning")),
                eq(SystemRecoveryText.echoSpeakerImage()),
                any(IVoidFunction.class),
                eq(player.id())));
    finish.get().execute();
    assertTrue(controller.systemCoreWarningCallAnswered());
    assertEquals(1, saves.get());
    questLog.verify(
        () ->
            SystemRecoveryQuestLogUtil.addDialogEntry(
                "riddle10",
                "system-core-warning",
                SystemRecoveryText.echoCall("system-core-warning")));
    controller.triggerSystemCoreWarningCall();
    sounds.verify(() -> Sounds.play(LastHourSounds.PHONE_RINGING), times(1));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void pendingFinalCallFollowsEitherCoreWarningOrAnUnansweredOpening(boolean restored) {
    if (restored) {
      controller.restoreSystemCoreCalls(false, true, true);
    } else {
      controller.triggerOpeningCall();
      controller.triggerFinalEchoCall();
    }
    answer();
    assertEquals(
        SystemRecoveryText.echoCall(restored ? "system-core-warning" : "opening-call"),
        script.get());
    finish.get().execute();
    controller.triggerFinalEchoCall();
    answer();
    assertEquals(SystemRecoveryText.echoCall("final-call"), script.get());
    finish.get().execute();
    assertEquals(1, elevatorOpens.get());
    sounds.verify(() -> Sounds.play(LastHourSounds.PHONE_RINGING), times(2));
  }

  private void answer() {
    phone.fetch(InteractionComponent.class).orElseThrow().triggerInteraction(phone, player);
  }
}
