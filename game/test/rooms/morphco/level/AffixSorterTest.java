package rooms.morphco.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;

import engine.Entity;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import rooms.morphco.ui.MorphCoDialogs;

class AffixSorterTest {

  @Test
  void hatchOpensAgainWithTheCutsAndBinsItWasClosedWith() {
    MorphCoLevel level =
        new MorphCoLevel(
            new LevelElement[][] {{LevelElement.FLOOR}}, DesignLabel.DEFAULT, Map.of(), List.of());
    AffixSorter sorter = new AffixSorter(level);
    List<UIComponent> opened = new ArrayList<>();
    String draft = "w0:2,4:P,-,-;w3:4:-,S";
    // A cut outside the word, the bin of its part and an unknown word do not come back.
    String sent = "w0:2,4,99:P,-,-,-;w3:4:-,S;w99:1:P";
    try (MockedStatic<DialogFactory> dialogs = mockStatic(DialogFactory.class)) {
      dialogs
          .when(() -> DialogFactory.show(any(DialogContext.class), anyInt()))
          .thenAnswer(call -> {
            UIComponent ui = new UIComponent(call.getArgument(0), false);
            opened.add(ui);
            return ui;
          });
      Entity player = new Entity("player");

      sorter.affixes(player);
      opened
          .getFirst()
          .callbacks()
          .get(MorphCoDialogs.DRAFT)
          .accept(new DialogResponseMessage.StringValue(sent));
      sorter.affixes(player);
    }

    assertEquals(
        "", opened.getFirst().dialogContext().find(MorphCoDialogs.AFFIX_DRAFT, String.class).get());
    assertEquals(
        draft, opened.get(1).dialogContext().find(MorphCoDialogs.AFFIX_DRAFT, String.class).get());
  }
}
