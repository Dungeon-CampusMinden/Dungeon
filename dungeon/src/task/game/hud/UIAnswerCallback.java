package task.game.hud;

import static task.game.hud.QuizUI.DEFAULT_DIALOG_ABORT;
import static task.game.hud.QuizUI.DEFAULT_DIALOG_CONFIRM;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.SnapshotArray;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.TextDialog;
import core.Entity;
import core.Game;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import task.Task;
import task.TaskContent;
import task.tasktype.Quiz;
import task.tasktype.quizquestion.FreeText;

/**
 * Contains functions to build an easy-to-use Consumer as a callback for the {@link
 * contrib.components.InteractionComponent}.
 *
 * <p>The Consumer will show the given {@link Quiz} on the HUD and will call the (as parameter)
 * given Callback with the given answers as a {@link Quiz.Content}-Set.
 *
 * <p>Use to get the consumer that you can use as an interaction callback.
 */
public final class UIAnswerCallback {

  /**
   * Get a Consumer that can be used as a callback for the {@link
   * contrib.components.InteractionComponent} and will show the given {@link Quiz} on the UI.
   *
   * @param quiz The {@link Quiz} to show on the HUD on interaction.
   * @param dslCallback Callback function that will be called after the player confirms their
   *     answers.
   * @return Consumer to use as a callback for the interaction component.
   */
  public static BiConsumer<Entity, Entity> askOnInteraction(
      Quiz quiz, BiConsumer<Task, Set<TaskContent>> dslCallback) {
    return (questGiver, player) ->
        QuizUI.showQuizDialog(quiz, (Entity hudEntity) -> uiCallback(quiz, hudEntity, dslCallback));
  }

  /**
   * Callback after the answers were confirmed.
   *
   * @param quest foo
   * @param hudEntity foo
   * @param dslCallback foo
   * @return foo
   * @see UIUtils
   */
  public static BiFunction<TextDialog, String, Boolean> uiCallback(
      Quiz quest, Entity hudEntity, BiConsumer<Task, Set<TaskContent>> dslCallback) {
    return (textDialog, id) -> {
      if (Objects.equals(id, DEFAULT_DIALOG_CONFIRM)) {
        dslCallback.accept(quest, getAnswer(quest, answerSection(textDialog)));
        Game.remove(hudEntity);
        return true;
      } else if (Objects.equals(id, DEFAULT_DIALOG_ABORT)) {
        Game.remove(hudEntity);
        return true;
      }
      return false;
    };
  }

  private static VerticalGroup answerSection(TextDialog textDialog) {
    SnapshotArray<Actor> children =
        ((VerticalGroup) textDialog.getContentTable().getChildren().get(0)).getChildren();
    // find the answer section
    return (VerticalGroup)
        children
            .select((actor) -> Objects.equals(actor.getName(), QuizDialogDesign.ANSWERS_GROUP_NAME))
            .iterator()
            .next();
  }

  private static Set<TaskContent> getAnswer(Quiz quiz, VerticalGroup answerSection) {
    if (quiz instanceof FreeText) {
      Quiz.Content content = new Quiz.Content(freeTextAnswer(answerSection));
      quiz.addAnswer(content);
      return Set.of(content);
    } else return stringToContent(quiz, checkboxAnswers(answerSection));
  }

  private static Set<TaskContent> stringToContent(Quiz quiz, Set<String> answers) {
    Set<TaskContent> contentSet = new HashSet<>();
    quiz.contentStream()
        .filter(answer -> answer instanceof Quiz.Content)
        .map(answer -> (Quiz.Content) answer)
        .filter(answer -> answers.contains(answer.content()))
        .forEach(contentSet::add);
    return contentSet;
  }

  private static Set<String> checkboxAnswers(VerticalGroup answerSection) {
    Set<String> answers = new HashSet<>();

    for (Actor actor :
        ((VerticalGroup) ((ScrollPane) answerSection.getChildren().get(0)).getChildren().get(0))
            .getChildren()
            .select((x) -> x instanceof CheckBox checkbox && checkbox.isChecked()))
      if (actor instanceof CheckBox checked) answers.add(checked.getText().toString());
    if (answers.size() == 0) answers.add("No Selection");
    return answers;
  }

  private static String freeTextAnswer(VerticalGroup answerSection) {
    return ((TextArea) ((ScrollPane) answerSection.getChildren().get(0)).getChildren().get(0))
        .getText();
  }
}
