package feature.audience;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import engine.utils.Point;
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
 * Tests how the game normalizes free-text and code votes before counting them, what the devices
 * show of a dialog, and how the devices and the game screen show a choice of targets.
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
  void choiceVotesShowTheLettersOfTheirOptions() {
    AudienceContext choice =
        new AudienceContext.Choice(
            null,
            List.of(
                ChoiceOption.of("a", "x"), ChoiceOption.of("b", "y"), ChoiceOption.of("c", "z")),
            true);
    JsonNode vote = choice.normalize(AudienceContext.JSON.arrayNode().add(3).add(1)).orElseThrow();
    assertEquals("A + C", choice.label(vote));
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

  private static final AudienceContext.Goto TARGETS =
      new AudienceContext.Goto(
          List.of(
              new AudienceContext.Goto.Destination(7, "Robo", new Point(3.5f, 5)),
              new AudienceContext.Goto.Destination(9, "Truhe", new Point(8.5f, 7))));

  @Test
  void targetVotesCountByName() {
    JsonNode vote = TARGETS.normalize(AudienceContext.JSON.numberNode(2)).orElseThrow();
    assertEquals("Truhe", vote.asString());
    assertEquals("Truhe", TARGETS.label(vote));
    assertTrue(TARGETS.normalize(AudienceContext.JSON.numberNode(3)).isEmpty());
  }

  @Test
  void devicesShowTargetsAsAChoiceWithTheirNames() {
    assertEquals("goto", TARGETS.kind());
    assertEquals("choice", TARGETS.controls());
    ObjectNode json = AudienceContext.JSON.objectNode();
    TARGETS.describe(json);
    assertEquals(2, json.path("options").asInt());
    assertEquals("Robo", json.path("answers").get(0).asString());
    assertEquals("Truhe", json.path("answers").get(1).asString());
  }

  @Test
  void gameScreenShowsEachLetterAboveItsTarget() {
    JsonNode truhe = TARGETS.markers().get(1);
    assertEquals("B", truhe.path("letter").asString());
    assertEquals("Truhe", truhe.path("label").asString());
    assertEquals(8.5, truhe.path("x").asDouble(), 1e-6);
    assertEquals(7, truhe.path("y").asDouble(), 1e-6);
  }
}
