package engine.game;

import engine.System;
import engine.systems.CameraSystem;
import engine.systems.DrawSystem;
import engine.systems.FrictionSystem;
import engine.systems.LevelSystem;
import engine.systems.MoveSystem;
import engine.systems.PositionSystem;
import engine.systems.SnapshotInterpolationSystem;
import engine.systems.SoundSystem;
import engine.systems.VelocitySystem;
import feature.achievements.AchievementPopupSystem;
import feature.audience.AudienceHud;
import feature.audience.AudienceSystem;
import feature.audience.RoomCamera;
import feature.components.Debugger;
import feature.emote.EmoteSystem;
import feature.hints.HintSystem;
import feature.hud.PauseMenuHudSystem;
import feature.input.systems.ControlsDialogSystem;
import feature.input.systems.GameplayInputSystem;
import feature.interaction.InteractionFeedbackSystem;
import feature.level.visibility.LevelHideSystem;
import feature.observer.ObserverSystem;
import feature.petrinet.PetriNetSystem;
import feature.questlog.QuestLogHudSystem;
import feature.questlog.QuestLogSyncSystem;
import feature.shader.ShaderSyncSystem;
import feature.shader.ShaderSystem;
import feature.systems.AISystem;
import feature.systems.AttachmentSystem;
import feature.systems.AttributeBarSystem;
import feature.systems.BlockSystem;
import feature.systems.CollisionSystem;
import feature.systems.DebugDrawSystem;
import feature.systems.EventScheduler;
import feature.systems.FogSystem;
import feature.systems.HealthSystem;
import feature.systems.HudSystem;
import feature.systems.IdleSoundSystem;
import feature.systems.LevelEditorSystem;
import feature.systems.LevelTickSystem;
import feature.systems.LeverSystem;
import feature.systems.ManaRestoreSystem;
import feature.systems.PathSystem;
import feature.systems.PressurePlateSystem;
import feature.systems.ProjectileSystem;
import feature.systems.SpikeSystem;
import feature.systems.StaminaRestoreSystem;
import feature.tasks.AssignmentSystem;
import feature.timer.WorldTimerSystem;
import java.util.List;

/**
 * The order in which the systems of the engine and its features execute, in every runtime.
 *
 * <p>{@link ECSManagement#add(System)} sorts each system to its place here, so the order does not
 * depend on who registers a system or when. A system the lists do not name, such as one of a room,
 * runs at {@link #OTHERS}: after the simulation of its pass and before the world is drawn. A level
 * that has to act before the simulation does so in {@link feature.level.ITickable#onTick}.
 *
 * <p>The world tick and the rendered frame are separate passes, so the order of execution only
 * matters inside one list. {@link System#render(float)} follows the tick list, then the frame list.
 */
public final class SystemOrder {
  /** Stands for the systems the lists do not name. */
  private static final Class<? extends System> OTHERS = System.class;

  /** The systems of a world tick. */
  private static final List<Class<? extends System>> TICK =
      List.of(
          // A level change comes first, so a tick runs in one level from start to end.
          LevelSystem.class,
          // Due actions and the logic of the level prepare the tick: they spawn and steer.
          EventScheduler.class,
          LevelTickSystem.class,
          // Places what was just spawned before the simulation reads positions.
          PositionSystem.class,
          AudienceSystem.class,
          // A dialog opened up to here pauses the simulation below in the same tick.
          HudSystem.class,
          // Forces: decisions first, friction last from the velocity they act on.
          AISystem.class,
          PathSystem.class,
          FrictionSystem.class,
          // Turns the forces into velocity, the velocity into a position clear of walls.
          VelocitySystem.class,
          MoveSystem.class,
          // Removes a projectile past its goal before it can hit anything there. The force that
          // launches a new one acts in the next tick.
          ProjectileSystem.class,
          // Contacts at the moved positions; solid entities are pushed apart.
          SpikeSystem.class,
          CollisionSystem.class,
          // Followers take the final position of their target.
          AttachmentSystem.class,
          EmoteSystem.class,
          // What the contacts cause: a plate toggles its lever, the lever runs its command.
          PressurePlateSystem.class,
          LeverSystem.class,
          HealthSystem.class,
          // Systems that read the result of the tick.
          ManaRestoreSystem.class,
          StaminaRestoreSystem.class,
          IdleSoundSystem.class,
          AssignmentSystem.class,
          BlockSystem.class,
          ObserverSystem.class,
          PetriNetSystem.class,
          HintSystem.class,
          OTHERS,
          // Animations advance after every signal of the tick was sent.
          FogSystem.class,
          LevelHideSystem.class,
          DrawSystem.class,
          // State that leaves with the end of the tick.
          QuestLogSyncSystem.class,
          ShaderSystem.class);

  /** The systems of a rendered frame. */
  private static final List<Class<? extends System>> FRAME =
      List.of(
          // Input refers to the frame the player saw, so it is read before the camera moves.
          GameplayInputSystem.class,
          ControlsDialogSystem.class,
          Debugger.class,
          // Only renders; the overlay of the editor is drawn over it.
          DebugDrawSystem.class,
          LevelEditorSystem.class,
          // The camera follows where the entities are drawn, so the render positions come first.
          SnapshotInterpolationSystem.class,
          RoomCamera.class,
          CameraSystem.class,
          // Presentation of the frame the camera now shows.
          ShaderSyncSystem.class,
          InteractionFeedbackSystem.class,
          SoundSystem.class,
          AttributeBarSystem.class,
          WorldTimerSystem.class,
          AudienceHud.class,
          QuestLogHudSystem.class,
          PauseMenuHudSystem.class,
          AchievementPopupSystem.class,
          OTHERS);

  private SystemOrder() {}

  /**
   * Gets the place of a system in the order.
   *
   * @param system the system to place
   * @return the rank of the system; a lower rank executes earlier
   */
  static int rank(System system) {
    int tick = TICK.indexOf(system.getClass());
    if (tick >= 0) return tick;
    int frame = FRAME.indexOf(system.getClass());
    if (frame >= 0) return TICK.size() + frame;
    return system.runsPerFrame() ? TICK.size() + FRAME.indexOf(OTHERS) : TICK.indexOf(OTHERS);
  }
}
