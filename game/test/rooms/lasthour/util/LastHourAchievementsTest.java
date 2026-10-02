package rooms.lasthour.util;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import engine.Entity;
import feature.achievements.AchievementManager;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;

class LastHourAchievementsTest {

  @Test
  void openingTrashcanTriggersAllThreePlayerAchievementsInOrder() {
    Entity player = new Entity("trashcan-player");
    AchievementManager manager = mock(AchievementManager.class);

    try (MockedStatic<AchievementManager> managerAccess = mockStatic(AchievementManager.class)) {
      managerAccess.when(AchievementManager::instance).thenReturn(manager);

      LastHourAchievements.onTrashcanOpened(player);

      InOrder order = inOrder(manager);
      order.verify(manager).popFor(player, LastHourAchievements.TRASH_DIVER);
      order.verify(manager).popFor(player, LastHourAchievements.TRASH_INSPECTOR);
      order.verify(manager).popFor(player, LastHourAchievements.TRASH_DETECTIVE);
      order.verifyNoMoreInteractions();
    }
  }
}
