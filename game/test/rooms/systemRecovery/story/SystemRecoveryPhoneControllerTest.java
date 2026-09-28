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
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import rooms.lasthour.util.LastHourSounds;
import rooms.systemRecovery.util.SystemRecoveryAchievements;
import rooms.systemRecovery.util.SystemRecoveryQuestLogUtil;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Verifies restore and completion of ECHO's system-core telephone call. */
class SystemRecoveryPhoneControllerTest {

  @Test
  void recordsOpeningAndRequiredCallsForThePhoneAchievement() {
    Point phonePoint = new Point(4, 7);
    Entity phone = new Entity("phone");
    Entity phoneEmote = new Entity("ringing-phone");
    Entity player = new Entity("player");
    DrawSystem drawSystem = mock(DrawSystem.class);
    AtomicReference<IVoidFunction> finishDialog = new AtomicReference<>();
    AtomicInteger dataStorageOpens = new AtomicInteger();
    SystemRecoveryPhoneController controller =
        new SystemRecoveryPhoneController(dataStorageOpens::incrementAndGet, () -> {});

    try (MockedStatic<DecoFactory> decoFactory = mockStatic(DecoFactory.class);
        MockedStatic<DrawSystem> draw = mockStatic(DrawSystem.class);
        MockedStatic<Game> game = mockStatic(Game.class);
        MockedStatic<Sounds> sounds = mockStatic(Sounds.class);
        MockedStatic<EmoteFactory> emotes = mockStatic(EmoteFactory.class);
        MockedStatic<EntityUtils> entityUtils = mockStatic(EntityUtils.class);
        MockedStatic<DialogFactory> dialogs = mockStatic(DialogFactory.class);
        MockedStatic<SystemRecoveryAchievements> achievements =
            mockStatic(SystemRecoveryAchievements.class);
        MockedStatic<SystemRecoveryQuestLogUtil> questLog =
            mockStatic(SystemRecoveryQuestLogUtil.class)) {
      decoFactory.when(() -> DecoFactory.createDeco(phonePoint, Deco.Phone)).thenReturn(phone);
      draw.when(DrawSystem::getInstance).thenReturn(drawSystem);
      entityUtils.when(() -> EntityUtils.getPosition(phone)).thenReturn(phonePoint);
      emotes
          .when(() -> EmoteFactory.createEmote(phonePoint, Emote.EXCLAMATION, 60 * 60 * 1000))
          .thenReturn(phoneEmote);
      dialogs
          .when(
              () ->
                  DialogFactory.showDialogDialog(
                      anyString(), anyString(), any(IVoidFunction.class), anyInt()))
          .thenAnswer(
              invocation -> {
                finishDialog.set(invocation.getArgument(2));
                return null;
              });

      controller.setup(phonePoint);
      controller.triggerOpeningCall();
      phone.fetch(InteractionComponent.class).orElseThrow().triggerInteraction(phone, player);
      finishDialog.get().execute();
      achievements.verify(() -> SystemRecoveryAchievements.phoneCallAnswered(false));

      controller.triggerDataStorageProblemCall();
      phone.fetch(InteractionComponent.class).orElseThrow().triggerInteraction(phone, player);
      assertEquals(0, dataStorageOpens.get());
      finishDialog.get().execute();

      achievements.verify(() -> SystemRecoveryAchievements.phoneCallAnswered(true));
      assertEquals(1, dataStorageOpens.get());
    }
  }

  @Test
  void ringsAgainAfterRestoreUntilPlayerFinishesWarningCall() {
    Point phonePoint = new Point(4, 7);
    Entity phone = new Entity("phone");
    Entity phoneEmote = new Entity("ringing-phone");
    Entity player = new Entity("player");
    DrawSystem drawSystem = mock(DrawSystem.class);
    AtomicReference<IVoidFunction> finishDialog = new AtomicReference<>();
    AtomicInteger saves = new AtomicInteger();
    SystemRecoveryPhoneController controller =
        new SystemRecoveryPhoneController(() -> {}, () -> {}, saves::incrementAndGet);

    try (MockedStatic<DecoFactory> decoFactory = mockStatic(DecoFactory.class);
        MockedStatic<DrawSystem> draw = mockStatic(DrawSystem.class);
        MockedStatic<Game> game = mockStatic(Game.class);
        MockedStatic<Sounds> sounds = mockStatic(Sounds.class);
        MockedStatic<EmoteFactory> emotes = mockStatic(EmoteFactory.class);
        MockedStatic<EntityUtils> entityUtils = mockStatic(EntityUtils.class);
        MockedStatic<DialogFactory> dialogs = mockStatic(DialogFactory.class);
        MockedStatic<SystemRecoveryQuestLogUtil> questLog =
            mockStatic(SystemRecoveryQuestLogUtil.class)) {
      decoFactory.when(() -> DecoFactory.createDeco(phonePoint, Deco.Phone)).thenReturn(phone);
      draw.when(DrawSystem::getInstance).thenReturn(drawSystem);
      entityUtils.when(() -> EntityUtils.getPosition(phone)).thenReturn(phonePoint);
      entityUtils.when(() -> EntityUtils.getDistance(phone, player)).thenReturn(0.0);
      emotes
          .when(() -> EmoteFactory.createEmote(phonePoint, Emote.EXCLAMATION, 60 * 60 * 1000))
          .thenReturn(phoneEmote);
      dialogs
          .when(
              () ->
                  DialogFactory.showDialogDialog(
                      anyString(), anyString(), any(IVoidFunction.class), anyInt()))
          .thenAnswer(
              invocation -> {
                finishDialog.set(invocation.getArgument(2));
                return null;
              });

      controller.setup(phonePoint);
      controller.restoreSystemCoreCalls(false, true, false);

      assertFalse(controller.systemCoreWarningCallAnswered());
      phone.fetch(InteractionComponent.class).orElseThrow().triggerInteraction(phone, player);
      dialogs.verify(
          () ->
              DialogFactory.showDialogDialog(
                  eq(SystemRecoveryText.echoCall("system-core-warning")),
                  eq(SystemRecoveryText.echoSpeakerImage()),
                  any(IVoidFunction.class),
                  eq(player.id())));
      sounds.verify(() -> Sounds.play(LastHourSounds.PHONE_RINGING));

      finishDialog.get().execute();

      assertTrue(controller.systemCoreWarningCallAnswered());
      questLog.verify(
          () ->
              SystemRecoveryQuestLogUtil.addDialogEntry(
                  "riddle10",
                  "system-core-warning",
                  SystemRecoveryText.echoCall("system-core-warning")));
      assertEquals(1, saves.get());
      controller.triggerSystemCoreWarningCall();
      sounds.verify(() -> Sounds.play(LastHourSounds.PHONE_RINGING), times(1));
    }
  }
}
