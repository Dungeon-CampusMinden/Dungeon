package feature.audience;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import feature.components.UIComponent;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.ChoiceOptions;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Tests how free-text and code votes are normalized before they are counted, and what the devices
 * show of a dialog.
 */
class AudienceContextTest {

  private static Optional<String> normalized(int digits, String vote) {
    return new AudienceContext.Text(null, digits)
        .normalize(AudienceContext.JSON.stringNode(vote))
        .map(JsonNode::asString);
  }

  @Test
  void freeTextIgnoresWhitespaceAndCase() {
    assertEquals(Optional.of("o(logn)"), normalized(0, " O(log n) "));
    assertEquals(normalized(0, "o(logn)"), normalized(0, "O (LOG\tN)"));
  }

  @Test
  void blankFreeTextIsNoVote() {
    assertTrue(normalized(0, "  ").isEmpty());
  }

  @Test
  void codesKeepLeadingZerosAndNeedAllDigits() {
    assertEquals(Optional.of("047"), normalized(3, " 0 47"));
    assertTrue(normalized(3, "47").isEmpty());
  }

  @Test
  void choicePromptListsTheAnswersInDisplayOrder() {
    UIComponent ui = mock(UIComponent.class);
    when(ui.dialogContext())
        .thenReturn(
            DialogContext.builder()
                .type(DialogType.DefaultTypes.MULTIPLE_CHOICE)
                .put(DialogContextKeys.TITLE, "Terminal 1")
                .put(
                    DialogContextKeys.OPTIONS,
                    new ChoiceOptions(
                        List.of(
                            ChoiceOption.of("Queue", "queue"),
                            ChoiceOption.of(" Stack ", "stack"))))
                .build());
    ObjectNode prompt = AudienceContext.prompt(ui);
    assertEquals("Terminal 1", prompt.path("title").asString());
    assertEquals(2, prompt.path("answers").size());
    assertEquals("Queue", prompt.path("answers").get(0).asString());
    assertEquals("Stack", prompt.path("answers").get(1).asString());
  }

  @Test
  void textPromptHasNoAnswers() {
    UIComponent ui = mock(UIComponent.class);
    when(ui.dialogContext())
        .thenReturn(
            DialogContext.builder()
                .type(DialogType.DefaultTypes.TEXT)
                .put(DialogContextKeys.MESSAGE, "Hallo")
                .build());
    ObjectNode prompt = AudienceContext.prompt(ui);
    assertEquals("Hallo", prompt.path("text").asString());
    assertTrue(prompt.path("answers").isMissingNode());
  }
}
