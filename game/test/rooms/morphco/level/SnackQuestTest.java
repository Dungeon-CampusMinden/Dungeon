package rooms.morphco.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import feature.hud.dialogs.ChoiceOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SnackQuestTest {

  @Test
  void shiftLeadTalksAboutTheSideQuestOnlyWhileItWaitsForHer() {
    MorphCoLevel level =
        new MorphCoLevel(
            new LevelElement[][] {{LevelElement.FLOOR}}, DesignLabel.DEFAULT, Map.of(), List.of());
    MorphCoState state = level.state();
    SnackQuest quest = new SnackQuest(level);

    assertTrue(quest.shiftLeadOption().isEmpty());
    state.set(MorphCoState.SNACK_SEEN);
    assertEquals(Optional.of("morphCo.story.snack-money-option"), label(quest));
    state.set(MorphCoState.SNACK_ASKED);
    state.set(MorphCoState.STORAGE_OPEN);
    assertTrue(quest.shiftLeadOption().isEmpty());
    state.set(MorphCoState.LOCKER_KEY);
    assertEquals(Optional.of("morphCo.story.snack-key-option"), label(quest));
    state.set(MorphCoState.KITCHEN_OPEN);
    assertTrue(quest.shiftLeadOption().isEmpty());
  }

  private static Optional<String> label(SnackQuest quest) {
    return quest.shiftLeadOption().map(ChoiceOption::label);
  }
}
