package contrib.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Disposable;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.game.PreRunConfiguration;
import core.network.handler.NettyNetworkHandler;
import core.network.server.DialogTracker;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import testingUtils.MockNetworkHandler;

/** Tests for {@link HudSystem}. */
public class HudSystemTest {

  private HudSystem hudSystem;

  @BeforeAll
  static void registerDialogType() {
    DialogFactory.register(TestDialogType.TEST, ignored -> new Group());
    DialogFactory.register(TestDialogType.DISPOSABLE, ignored -> new TestDisposableGroup());
    DialogFactory.register(
        TestDialogType.REJECTED,
        ignored -> {
          throw new AssertionError("A dialog without a present target must not be created");
        });
  }

  @BeforeEach
  void setUp() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    MockNetworkHandler.useLocalNetworkHandler();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
    hudSystem = Game.hud();
    Game.add(hudSystem);
  }

  @AfterEach
  void tearDown() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    DialogTracker.instance().clear();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
  }

  /** Replacing a player's UI keeps the HUD query synchronized with the current component. */
  @Test
  public void replacingUiComponentRunsFullLifecycle() {
    Entity player = player();
    UIComponent first = ui(player, player.id());
    player.add(first);

    assertTrue(hudSystem.hasOpenUI(player));
    assertSame(first, hudSystem.topmostCloseableUI().orElseThrow().b());

    UIComponent second = ui(player, false, player.id());
    player.add(second);

    assertTrue(hudSystem.hasOpenUI(player));
    assertFalse(hudSystem.hasOpenPausingUI(player));
    assertSame(second, player.fetch(UIComponent.class).orElseThrow());
    assertSame(second, hudSystem.topmostCloseableUI().orElseThrow().b());

    UIUtils.closeDialog(first);
    assertSame(second, player.fetch(UIComponent.class).orElseThrow());
    assertTrue(hudSystem.hasOpenUI(player));

    UIUtils.closeDialog(second);
    assertFalse(hudSystem.hasOpenUI(player));
  }

  /** Temporarily detaching the HUD keeps a dialog reusable until its component is removed. */
  @Test
  public void readdingHudSystemDoesNotDisposeDialog() {
    Entity player = player();
    UIComponent component = ui(TestDialogType.DISPOSABLE, player, true, player.id());
    player.add(component);
    TestDisposableGroup dialog = (TestDisposableGroup) component.dialog();

    Game.remove(HudSystem.class);

    assertFalse(dialog.disposed);
    assertEquals(-1, DialogTracker.instance().getEntityId(component.dialogContext().dialogId()));

    Game.add(hudSystem);

    assertFalse(dialog.disposed);
    assertTrue(hudSystem.hasOpenUI(player));
    assertEquals(
        player.id(), DialogTracker.instance().getEntityId(component.dialogContext().dialogId()));

    UIUtils.closeDialog(component);
    assertTrue(dialog.disposed);
  }

  /** Removing a UI-owning entity performs terminal dialog cleanup. */
  @Test
  public void removingUiOwnerDisposesDialog() {
    Entity player = player();
    UIComponent component = ui(TestDialogType.DISPOSABLE, player, true, player.id());
    player.add(component);
    TestDisposableGroup dialog = (TestDisposableGroup) component.dialog();

    Game.remove(player);

    assertTrue(dialog.disposed);
    assertFalse(hudSystem.hasOpenUI(player));
    assertEquals(-1, DialogTracker.instance().getEntityId(component.dialogContext().dialogId()));
  }

  /** Empty targets affect every player while explicit targets only affect matching players. */
  @Test
  public void uiTargetsDetermineWhoIsAffected() {
    Entity player = player();
    Entity otherPlayer = player();
    NettyNetworkHandler networkHandler = Mockito.mock(NettyNetworkHandler.class);
    Mockito.when(networkHandler.isServer()).thenReturn(true);
    Mockito.when(networkHandler.serverRuntime()).thenReturn(Optional.empty());
    MockNetworkHandler.useNetworkHandler(networkHandler);

    UIComponent global = show(player);
    assertTrue(hudSystem.hasOpenUI(player));
    assertTrue(hudSystem.hasOpenUI(otherPlayer));

    UIUtils.closeDialog(global);
    assertFalse(hudSystem.hasOpenUI(player));

    UIComponent targeted = show(player, player.id());
    assertTrue(hudSystem.hasOpenUI(player));
    assertFalse(hudSystem.hasOpenUI(otherPlayer));

    UIUtils.closeDialog(targeted);
    assertFalse(hudSystem.hasOpenUI(player));
  }

  /** A targeted UI is not created when none of its target players are present. */
  @Test
  public void uiWithoutPresentTargetIsNotCreated() {
    Entity player = player();
    UIComponent component = ui(TestDialogType.REJECTED, player, true, Integer.MAX_VALUE);

    player.add(component);

    assertFalse(hudSystem.hasOpenUI(player));
    player.remove(UIComponent.class);
  }

  private static Entity player() {
    Entity player = new Entity("player");
    player.add(new PlayerComponent());
    Game.add(player);
    return player;
  }

  private static UIComponent show(Entity owner, int... targetEntityIds) {
    UIComponent component = ui(owner, targetEntityIds);
    owner.add(component);
    return component;
  }

  private static UIComponent ui(Entity owner, int... targetEntityIds) {
    return ui(owner, true, targetEntityIds);
  }

  private static UIComponent ui(Entity owner, boolean willPauseGame, int... targetEntityIds) {
    return ui(TestDialogType.TEST, owner, willPauseGame, targetEntityIds);
  }

  private static UIComponent ui(
      DialogType dialogType, Entity owner, boolean willPauseGame, int... targetEntityIds) {
    DialogContext context =
        new DialogContext(
            dialogType,
            true,
            Map.of(contrib.hud.dialogs.DialogContextKeys.OWNER_ENTITY, owner.id()));
    return new UIComponent(context, willPauseGame, true, targetEntityIds);
  }

  private enum TestDialogType implements DialogType {
    TEST,
    DISPOSABLE,
    REJECTED;

    @Override
    public String type() {
      return "HUD_SYSTEM_TEST";
    }
  }

  private static final class TestDisposableGroup extends Group implements Disposable {
    private boolean disposed;

    @Override
    public void dispose() {
      disposed = true;
    }
  }
}
