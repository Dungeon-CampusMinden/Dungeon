package contrib.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import contrib.components.InventoryComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import core.Entity;
import core.components.PlayerComponent;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class OpenInventoryTest {

  @Test
  void openInventory_WithInventoryAndPlayerComponentsAndNoOtherDialogs_OpensInventoryDialog() {
    Entity hero = mock(Entity.class);
    InventoryComponent inventoryComponent = new InventoryComponent();
    PlayerComponent playerComponent = new PlayerComponent();

    when(hero.id()).thenReturn(42);
    when(hero.fetch(InventoryComponent.class)).thenReturn(Optional.of(inventoryComponent));
    when(hero.fetch(PlayerComponent.class)).thenReturn(Optional.of(playerComponent));

    try (MockedStatic<UIUtils> uiUtilsMock = mockStatic(UIUtils.class);
        MockedStatic<DialogFactory> dialogFactoryMock = mockStatic(DialogFactory.class)) {
      uiUtilsMock.when(() -> UIUtils.getPlayerInventoryGUI(hero)).thenReturn(Optional.empty());

      DialogContext[] capturedContext = new DialogContext[1];
      dialogFactoryMock
          .when(() -> DialogFactory.show(any(DialogContext.class), anyBoolean(), anyBoolean(), anyInt()))
          .thenAnswer(invocation -> {
            capturedContext[0] = invocation.getArgument(0);
            return null;
          });

      HeroController.openInventory(hero);

      assertNotNull(capturedContext[0], "A dialog context should be created");
      assertEquals(DialogType.DefaultTypes.INVENTORY, capturedContext[0].dialogType());
      dialogFactoryMock.verify(
          () -> DialogFactory.show(any(DialogContext.class), eq(false), eq(true), eq(42)));
    }
  }

  @Test
  void openInventory_WhenInventoryAlreadyOpen_StillOpensInventoryDialog() {
    Entity hero = mock(Entity.class);
    InventoryComponent inventoryComponent = new InventoryComponent();
    PlayerComponent playerComponent = new PlayerComponent();
    playerComponent.incrementOpenDialogs();

    when(hero.id()).thenReturn(42);
    when(hero.fetch(InventoryComponent.class)).thenReturn(Optional.of(inventoryComponent));
    when(hero.fetch(PlayerComponent.class)).thenReturn(Optional.of(playerComponent));

    try (MockedStatic<UIUtils> uiUtilsMock = mockStatic(UIUtils.class);
        MockedStatic<DialogFactory> dialogFactoryMock = mockStatic(DialogFactory.class)) {
      uiUtilsMock.when(() -> UIUtils.getPlayerInventoryGUI(hero)).thenReturn(Optional.of(mock()));

      HeroController.openInventory(hero);

      dialogFactoryMock.verify(
          () -> DialogFactory.show(any(DialogContext.class), eq(false), eq(true), eq(42)));
    }
  }

  @Test
  void openInventory_WithNullHero_ThrowsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () -> HeroController.openInventory(null),
        "Null hero should result in a NullPointerException");
  }

  @Test
  void openInventory_WhenOtherDialogsAreOpenAndInventoryIsNotOpen_DoesNothing() {
    Entity hero = mock(Entity.class);
    InventoryComponent inventoryComponent = new InventoryComponent();
    PlayerComponent playerComponent = new PlayerComponent();
    playerComponent.incrementOpenDialogs();

    when(hero.id()).thenReturn(42);
    when(hero.fetch(InventoryComponent.class)).thenReturn(Optional.of(inventoryComponent));
    when(hero.fetch(PlayerComponent.class)).thenReturn(Optional.of(playerComponent));

    try (MockedStatic<UIUtils> uiUtilsMock = mockStatic(UIUtils.class);
        MockedStatic<DialogFactory> dialogFactoryMock = mockStatic(DialogFactory.class)) {
      uiUtilsMock.when(() -> UIUtils.getPlayerInventoryGUI(hero)).thenReturn(Optional.empty());

      HeroController.openInventory(hero);

      dialogFactoryMock.verifyNoInteractions();
    }
  }

  @Test
  void openInventory_WithoutInventoryComponent_DoesNothing() {
    Entity hero = mock(Entity.class);
    PlayerComponent playerComponent = new PlayerComponent();

    when(hero.id()).thenReturn(42);
    when(hero.fetch(InventoryComponent.class)).thenReturn(Optional.empty());
    when(hero.fetch(PlayerComponent.class)).thenReturn(Optional.of(playerComponent));

    try (MockedStatic<DialogFactory> dialogFactoryMock = mockStatic(DialogFactory.class)) {
      HeroController.openInventory(hero);

      dialogFactoryMock.verifyNoInteractions();
    }
  }

  @Test
  void openInventory_WithoutPlayerComponent_DoesNothing() {
    Entity hero = mock(Entity.class);
    InventoryComponent inventoryComponent = new InventoryComponent();

    when(hero.id()).thenReturn(42);
    when(hero.fetch(InventoryComponent.class)).thenReturn(Optional.of(inventoryComponent));
    when(hero.fetch(PlayerComponent.class)).thenReturn(Optional.empty());

    try (MockedStatic<DialogFactory> dialogFactoryMock = mockStatic(DialogFactory.class)) {
      HeroController.openInventory(hero);

      dialogFactoryMock.verifyNoInteractions();
    }
  }
}
