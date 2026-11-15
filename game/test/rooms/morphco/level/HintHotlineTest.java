package rooms.morphco.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import feature.petrinet.PetriNetSystem;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.morphco.level.HintHotline.Stage;
import rooms.morphco.petrinet.MorphCoMilestone;
import rooms.morphco.petrinet.MorphCoProgressNet;

class HintHotlineTest {

  @BeforeEach
  void setUp() {
    MorphCoProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.add(new PetriNetSystem());
    MorphCoProgressNet.initialize();
  }

  @AfterEach
  void tearDown() {
    MorphCoProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  @Test
  void stagesLeadToTheSolutionSurviveLoadingAndStartAgainForTheNextStep() {
    MorphCoState state = new MorphCoState();
    for (Stage stage : List.of(Stage.DIRECTION, Stage.TIP, Stage.SOLUTION)) {
      HintHotline.Offer offer = HintHotline.offer(state);
      assertEquals(new HintHotline.Offer(ShiftLead.Step.ALARM, stage), offer);
      state.set(offer.flag());
    }
    // Once heard, the solution is repeated.
    assertEquals(Stage.SOLUTION, HintHotline.offer(state).stage());

    MorphCoState loaded = new MorphCoState();
    loaded.restore(state.flags(), Set.of(), 0, 0);
    assertEquals(Stage.SOLUTION, HintHotline.offer(loaded).stage());

    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.ALARM_OFF));
    assertEquals(
        new HintHotline.Offer(ShiftLead.Step.PC, Stage.DIRECTION), HintHotline.offer(loaded));
  }
}
