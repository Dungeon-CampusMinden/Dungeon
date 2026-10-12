package rooms.lasthour.petrinet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import feature.petrinet.PetriNetSystem;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LastHourProgressNetTest {
  @BeforeEach
  void setUp() {
    LastHourProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.add(new PetriNetSystem());
    LastHourProgressNet.initialize();
  }

  @AfterEach
  void tearDown() {
    LastHourProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  @Test
  void storageCanOpenBeforePowerButBlueUsbRequiresBothBranches() {
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.STORAGE_OPENED));
    assertFalse(LastHourProgressNet.complete(LastHourMilestone.BLUE_USB_INSERTED));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.LOGIN_SUCCEEDED));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.BLUE_USB_INSERTED));
    assertEquals(4, LastHourProgressNet.completedMilestones().size());
  }

  @Test
  void wrongOrderAndDuplicateEventsCannotAdvanceTheMarking() {
    assertFalse(LastHourProgressNet.complete(LastHourMilestone.EXIT_OPENED));
    assertTrue(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    assertFalse(LastHourProgressNet.complete(LastHourMilestone.POWER_ON));
    assertEquals(Set.of(LastHourMilestone.POWER_ON), LastHourProgressNet.completedMilestones());
  }

  @Test
  void restorePreservesIndependentProgressAndRejectsInvalidMarkings() {
    Set<LastHourMilestone> marking =
        EnumSet.of(LastHourMilestone.STORAGE_OPENED, LastHourMilestone.POWER_ON);
    assertTrue(LastHourProgressNet.restore(marking));
    assertEquals(marking, LastHourProgressNet.completedMilestones());
    assertFalse(LastHourProgressNet.restore(Set.of(LastHourMilestone.BLUE_USB_INSERTED)));
    assertEquals(marking, LastHourProgressNet.completedMilestones());
  }
}
