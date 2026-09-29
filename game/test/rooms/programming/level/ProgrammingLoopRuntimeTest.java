package rooms.programming.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Group;
import engine.Entity;
import engine.Game;
import engine.components.InputComponent;
import engine.components.NetworkPositionComponent;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import engine.level.DungeonLevel;
import engine.network.messages.c2s.InputMessage;
import engine.network.server.ClientState;
import engine.systems.LevelSystem;
import engine.systems.NetworkPositionSmoothingSystem;
import engine.systems.input.InputManager;
import engine.systems.input.InputSystem;
import engine.utils.Direction;
import engine.utils.Point;
import feature.canvas.CanvasUI;
import feature.components.CharacterClassComponent;
import feature.components.UIComponent;
import feature.entities.CharacterClass;
import feature.entities.HeroController;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import rooms.programming.modules.loops.TerminalState;
import rooms.programming.modules.methods.MethodsWorkshop;
import rooms.programming.modules.variables.BindingState;
import rooms.programming.state.VariablePuzzleStage;
import testingUtils.MockNetworkHandler;

/** Checks the live workbench controls at their actual dialog entry points. */
class ProgrammingLoopRuntimeTest {
  private Entity player;
  private ProgrammingGolemRuntime runtime;

  @BeforeEach
  void setup() throws Exception {
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.add(new LevelSystem());
    MockNetworkHandler.useLocalNetworkHandler();
    DialogFactory.register(ProgrammingTerminal.Type.TERMINAL, ignored -> new Group());
    DialogFactory.register(ProgrammingTerminal.Type.OBSERVATION, ignored -> new Group());
    ProgrammingBinding.register();
    DialogFactory.register(
        DialogFactory.registeredTypes().stream()
            .filter(type -> type.type().equals(ProgrammingBinding.ID))
            .findFirst()
            .orElseThrow(),
        ignored -> new Group());
    ProgrammingMethods.register();
    DialogFactory.register(
        DialogFactory.registeredTypes().stream()
            .filter(type -> type.type().equals(ProgrammingMethods.ID))
            .findFirst()
            .orElseThrow(),
        ignored -> new Group());
    Game.add(Game.hud());
    player = new Entity();
    player.add(new PositionComponent(new Point(0, 0)));
    player.add(new PlayerComponent());
    player.add(new VelocityComponent(3f));
    player.add(new CharacterClassComponent(CharacterClass.WIZARD));
    Game.add(player);
    var register = HeroController.class.getDeclaredMethod("registerDefaultInputHandlers");
    register.setAccessible(true);
    register.invoke(null);
    runtime = mock(ProgrammingGolemRuntime.class);
    when(runtime.bindingState())
        .thenReturn(
            new BindingState(VariablePuzzleStage.VESSELS, true, true, Map.of(), Map.of(), ""));
    when(runtime.terminalState())
        .thenReturn(
            new TerminalState(List.of(), 0, 42, 0, 0, true, true, "archive-long", "schritt();"));
  }

  @AfterEach
  void cleanup() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.currentLevel(null);
  }

  @ParameterizedTest
  @ValueSource(strings = {"binding", "terminal", "methods"})
  void workbenchLocksInputWithoutPausingSimulation(String workbench) {
    Runnable open =
        switch (workbench) {
          case "binding" -> () -> ProgrammingBinding.open(player, runtime);
          case "methods" ->
              () -> ProgrammingMethods.open(player, new MethodsWorkshop().state(), ignored -> {});
          default -> () -> ProgrammingTerminal.open(player, runtime);
        };
    open.run();
    assertFalse(
        dialog().willPauseGame(), "The terminal must not stop movement while executing code");
    verifyInputLock();
    open.run();
    open.run();
    assertEquals(
        1,
        Game.levelEntities().filter(e -> e.isPresent(UIComponent.class)).count(),
        "Repeated interaction must not open another workbench");
  }

  @Test
  void observationKeepsPhysicalSimulationRunning() {
    ProgrammingObservation.open(player, runtime);
    assertFalse(dialog().willPauseGame(), "Watching Nox must not stop his physical movement");
    ProgrammingObservation.open(player, runtime);
    assertEquals(1, Game.levelEntities().filter(e -> e.isPresent(UIComponent.class)).count());
    verifyInputLock();
  }

  @Test
  void mapFollowsVisibleNetworkPositionInsteadOfRunningAheadToSnapshotTarget() {
    var level = mock(DungeonLevel.class);
    var origin = new Point(20, 20);
    when(level.namedPoints()).thenReturn(Map.of("maze-origin", origin));
    Game.currentLevel(level);
    Entity golem = new Entity();
    var position = new PositionComponent(origin);
    golem.add(position);
    golem.add(new NetworkPositionComponent(origin.translate(1, 0)));
    Game.add(golem);
    var state =
        new TerminalState(
            List.of(), 0, golem.id(), .2f, 0, true, true, "archive-long", "schritt()");
    var smoothing = new NetworkPositionSmoothingSystem();
    Game.add(smoothing);
    for (int i = 0; i < 10; i++) {
      smoothing.execute();
      assertEquals(
          (position.position().x() - origin.x()) / 5,
          ProgrammingTerminal.mapPosition(state).x(),
          .0001f);
    }
    assertEquals(state, ProgrammingTerminal.decode(ProgrammingTerminal.encode(state)));
  }

  private UIComponent dialog() {
    return Game.levelEntities()
        .flatMap(e -> e.fetch(UIComponent.class).stream())
        .findFirst()
        .orElseThrow();
  }

  private void verifyInputLock() {
    Point target = new Point(1, 0);
    var interactions = new AtomicInteger();
    Entity object = new Entity();
    object.add(new PositionComponent(target));
    object.add(
        new InteractionComponent(new Interaction((entity, who) -> interactions.incrementAndGet())));
    Game.add(object);
    sendInput(
        player, new InputMessage(InputMessage.Action.INTERACT, new InputMessage.Interact(target)));
    assertEquals(0, interactions.get(), "World interaction must not pass through the open canvas");
    var velocity = player.fetch(VelocityComponent.class).orElseThrow();
    sendMove(player);
    assertTrue(
        velocity.force(HeroController.MOVEMENT_ID).isEmpty(),
        "The server must reject movement while the live dialog is open");
    Entity other = new Entity();
    other.add(new PlayerComponent(false));
    other.add(new VelocityComponent(3f));
    other.add(new CharacterClassComponent(CharacterClass.WIZARD));
    Game.add(other);
    sendMove(other);
    assertTrue(
        other
            .fetch(VelocityComponent.class)
            .orElseThrow()
            .force(HeroController.MOVEMENT_ID)
            .isPresent(),
        "Another player's controls must remain available");

    var input = new InputComponent();
    var movementCalls = new AtomicInteger();
    input.registerCallback(Input.Keys.W, ignored -> movementCalls.incrementAndGet());
    input.registerCallback(Input.Buttons.LEFT, who -> HeroController.interact(who, target));
    UIComponent opened = dialog();
    input.registerCallback(
        Input.Keys.ESCAPE,
        ignored -> opened.callbacks().get(CanvasUI.EVENT_CLOSE).accept(null),
        false,
        true);
    player.add(input);
    var inputs = new InputSystem();
    Game.add(inputs);
    try (var keys = mockStatic(InputManager.class)) {
      keys.when(() -> InputManager.isKeyPressed(Input.Keys.W)).thenReturn(true);
      keys.when(() -> InputManager.isButtonPressed(Input.Buttons.LEFT)).thenReturn(true);
      keys.when(() -> InputManager.isButtonJustPressed(Input.Buttons.LEFT)).thenReturn(true);
      inputs.execute();
      assertEquals(0, movementCalls.get(), "Local gameplay callbacks must be blocked");
      assertEquals(0, interactions.get(), "Local mouse input must not interact through the canvas");
      keys.when(() -> InputManager.isKeyJustPressed(Input.Keys.ESCAPE)).thenReturn(true);
      inputs.execute();
      assertFalse(Game.hud().hasOpenUI(player), "Closing must remain possible");
      keys.when(() -> InputManager.isKeyJustPressed(Input.Keys.ESCAPE)).thenReturn(false);
      int before = movementCalls.get();
      int beforeInteractions = interactions.get();
      inputs.execute();
      assertEquals(before + 1, movementCalls.get(), "Closing must restore gameplay controls");
      assertEquals(
          beforeInteractions + 1, interactions.get(), "Closing must restore mouse interactions");
    }
    player.remove(InputComponent.class);
    int beforeInteractions = interactions.get();
    sendInput(
        player, new InputMessage(InputMessage.Action.INTERACT, new InputMessage.Interact(target)));
    assertEquals(
        beforeInteractions + 1, interactions.get(), "Closing must restore server interactions");
    sendMove(player);
    assertTrue(
        velocity.force(HeroController.MOVEMENT_ID).isPresent(),
        "Closing must also release the server-side input block");
  }

  private static void sendMove(Entity target) {
    sendInput(target, InputMessage.move(Direction.RIGHT));
  }

  private static void sendInput(Entity target, InputMessage message) {
    var client =
        new ClientState((short) 1, "tester", 1, new byte[] {1, 2, 3}, CharacterClass.WIZARD);
    client.playerEntity(target);
    HeroController.enqueueInput(client, message);
    HeroController.drainAndApplyInputs();
  }
}
