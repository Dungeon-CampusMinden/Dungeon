package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.hud.UIUtils;
import core.Entity;
import core.Game;
import core.utils.IVoidFunction;
import core.utils.Tuple;
import core.utils.logging.DungeonLogger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import task.Task;
import task.game.hud.QuizDialogDesign;
import task.tasktype.Quiz;

/**
 * Central factory for creating and displaying dialogs in a unified manner.
 *
 * <p>All dialog creation should go through this factory to ensure consistent behavior and to enable
 * future extensions via the registry system. Dialog classes are package-private to enforce the use
 * of this factory.
 *
 * <p>Usage:
 *
 * <pre>
 * // Show an OK dialog
 * DialogFactory.showOkDialog("Hello World", "Greeting", () -> System.out.println("OK pressed"));
 *
 * // Show a Yes/No dialog
 * DialogFactory.showYesNoDialog("Continue?", "Confirm", () -> continueAction(), () -> cancelAction());
 *
 * // Show a task dialog with automatic grading
 * DialogFactory.showTaskYesNoDialog(myTask);
 * </pre>
 *
 * @see DialogContext
 * @see DialogDesign
 */
public class DialogFactory {
  public static final String TYPE_OK = "OK";
  public static final String TYPE_YES_NO = "YES_NO";
  public static final String TYPE_TEXT = "TEXT";
  public static final String TYPE_FREE_INPUT = "FREE_INPUT";
  public static final String TYPE_QUIZ = "QUIZ";

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(DialogFactory.class);
  private static final Map<String, Function<DialogContext, Dialog>> registry = new HashMap<>();

  static {
    register(TYPE_OK, OkDialog::build);
    register(TYPE_YES_NO, YesNoDialog::build);
    register(TYPE_TEXT, TextDialogBuilders::buildTextDialog);
    register(TYPE_FREE_INPUT, FreeInputDialog::build);
    register(TYPE_QUIZ, QuizDialogBuilders::buildQuizDialog);
    LOGGER.debug("Registered built-in dialog types: OK, YES_NO, TEXT, FREE_INPUT, QUIZ");
  }

  /**
   * Registers a custom dialog type with the factory.
   *
   * <p>This allows extending the dialog system with new dialog types without modifying existing
   * code. The creator function receives a {@link DialogContext} and must return a fully configured
   * {@link Dialog}.
   *
   * @param name Unique identifier for the dialog type (e.g., "CUSTOM_DIALOG")
   * @param creator Function that creates a dialog from a context
   * @throws DialogCreationException if a dialog type with the given name is already registered
   */
  public static void register(String name, Function<DialogContext, Dialog> creator) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(creator, "creator");
    if (registry.containsKey(name)) {
      throw new DialogCreationException("Dialog type '" + name + "' is already registered");
    }
    registry.put(name, creator);
  }

  /**
   * Creates a dialog of the specified type using the given context.
   *
   * <p>The dialog is created but not shown. Use {@link #show(String, DialogContext)} to create and
   * display a dialog in one step.
   *
   * @param type The dialog type (must be registered)
   * @param context The context containing all necessary data for dialog creation
   * @return A fully configured dialog ready to be displayed
   * @throws DialogCreationException if the dialog type is not registered
   */
  public static Dialog create(String type, DialogContext context) {
    Objects.requireNonNull(context, "context");
    Function<DialogContext, Dialog> creator = registry.get(type);
    if (creator == null) {
      throw new DialogCreationException("Unknown dialog type: " + type);
    }
    return creator.apply(context);
  }

  /**
   * Creates and displays a dialog of the specified type.
   *
   * <p>This method creates the dialog, centers it on screen, wraps it in a UI entity, and adds it
   * to the game. The entity lifecycle is automatically managed.
   *
   * @param type The dialog type (must be registered)
   * @param context The context containing all necessary data for dialog creation
   * @return A tuple containing the entity (with lifecycle management) and the dialog instance
   * @throws DialogCreationException if the dialog type is not registered
   */
  public static Tuple<Entity, Dialog> show(String type, DialogContext context) {
    Objects.requireNonNull(context, "context");
    boolean hasEntity = context.entity().isPresent();
    Entity entity =
        hasEntity
            ? context.requireEntity()
            : new Entity(context.entityName().orElse(type + "_" + System.nanoTime()));
    DialogContext effectiveContext = hasEntity ? context : context.withEntity(entity);
    Dialog dialog = create(type, effectiveContext);
    UIUtils.center(dialog);
    UIUtils.show(() -> dialog, entity);
    Game.add(entity);
    return Tuple.of(entity, dialog);
  }

  /**
   * Shows a simple OK dialog with a message and a single confirmation button.
   *
   * @param text The message to display in the dialog body
   * @param title The dialog window title
   * @param onConfirm Callback executed when the OK button is pressed
   * @return A tuple containing the entity (with lifecycle management) and the dialog instance
   */
  public static Tuple<Entity, Dialog> showOkDialog(
      String text, String title, IVoidFunction onConfirm) {
    return show(
        TYPE_OK,
        DialogContext.builder()
            .title(title)
            .put(DialogContextKeys.MESSAGE, text)
            .put(DialogContextKeys.ON_CONFIRM, onConfirm)
            .build());
  }

  /**
   * Shows a Yes/No confirmation dialog with separate callbacks for each option.
   *
   * @param text The message to display in the dialog body
   * @param title The dialog window title
   * @param onYes Callback executed when the Yes button is pressed
   * @param onNo Callback executed when the No button is pressed
   * @return A tuple containing the entity (with lifecycle management) and the dialog instance
   */
  public static Tuple<Entity, Dialog> showYesNoDialog(
      String text, String title, IVoidFunction onYes, IVoidFunction onNo) {
    return show(
        TYPE_YES_NO,
        DialogContext.builder()
            .title(title)
            .put(DialogContextKeys.MESSAGE, text)
            .put(DialogContextKeys.ON_YES, onYes)
            .put(DialogContextKeys.ON_NO, onNo)
            .build());
  }

  /**
   * Shows a quiz dialog with custom result handling.
   *
   * <p>The result handler linker receives the dialog entity and returns a handler that processes
   * button clicks and quiz answer validation.
   *
   * @param quiz The quiz to display
   * @param handlerLinker Function that creates a result handler given the dialog entity
   * @return A tuple containing the entity (with lifecycle management) and the dialog instance
   */
  public static Tuple<Entity, Dialog> showQuizDialog(
      Quiz quiz, Function<Entity, BiFunction<Dialog, String, Boolean>> handlerLinker) {
    return show(
        TYPE_QUIZ,
        DialogContext.builder()
            .title(quiz.taskName())
            .put(DialogContextKeys.MESSAGE, UIUtils.formatString(quiz.taskText()))
            .put(DialogContextKeys.QUIZ, quiz)
            .put(DialogContextKeys.RESULT_HANDLER_LINKER, handlerLinker)
            .build());
  }

  /**
   * Shows a customizable text dialog with optional multiple buttons and custom result handling.
   *
   * @param text The message to display in the dialog body
   * @param title The dialog window title
   * @param onConfirm Callback executed when the confirm button is pressed (can be null)
   * @param confirmLabel Label for the confirm button (uses default if null)
   * @param cancelLabel Label for the cancel button (no cancel button if null)
   * @param additionalButtons List of additional button labels (can be null)
   * @param customHandler Custom result handler for button clicks (can be null)
   * @return A tuple containing the entity (with lifecycle management) and the dialog instance
   */
  public static Tuple<Entity, Dialog> showTextDialog(
      String text,
      String title,
      IVoidFunction onConfirm,
      String confirmLabel,
      String cancelLabel,
      List<String> additionalButtons,
      BiFunction<Dialog, String, Boolean> customHandler) {
    DialogContext.Builder builder =
        DialogContext.builder().title(title).put(DialogContextKeys.MESSAGE, text);
    if (onConfirm != null) builder.put(DialogContextKeys.ON_CONFIRM, onConfirm);
    if (confirmLabel != null) builder.put(DialogContextKeys.CONFIRM_LABEL, confirmLabel);
    if (cancelLabel != null) builder.put(DialogContextKeys.CANCEL_LABEL, cancelLabel);
    if (additionalButtons != null)
      builder.put(DialogContextKeys.ADDITIONAL_BUTTONS, additionalButtons);
    if (customHandler != null) builder.put(DialogContextKeys.RESULT_HANDLER, customHandler);
    return show(TYPE_TEXT, builder.build());
  }

  /**
   * Shows a Yes/No dialog for a task with automatic grading and result display.
   *
   * <p>When the user confirms ("Yes"), the task is graded and a series of result dialogs are shown
   * displaying the score, correctness, and optionally the correct answers and explanation.
   *
   * @param task The task to present and grade
   * @return The entity containing the dialog UI component
   */
  public static Entity showTaskYesNoDialog(Task task) {
    String text =
        task.taskText()
            + System.lineSeparator()
            + System.lineSeparator()
            + task.scenarioText()
            + System.lineSeparator()
            + System.lineSeparator()
            + "Bist du fertig?";
    String title = task.taskName();
    return showYesNoDialog(text, title, gradeOn(task), () -> {}).a();
  }

  /**
   * Creates a callback that grades a task and displays the results in a sequence of dialogs.
   *
   * <p>Shows: 1) Score and correctness 2) Explanation (if task was wrong and explanation exists) 3)
   * Correct answers (if task was wrong)
   *
   * @param t The task to grade
   * @return A callback that performs grading and displays results
   */
  private static IVoidFunction gradeOn(final Task t) {
    return () -> {
      float score = t.gradeTask();
      StringBuilder output = new StringBuilder();
      output
          .append("Du hast ")
          .append(score)
          .append("/")
          .append(t.points())
          .append(" Punkte erreicht")
          .append(System.lineSeparator())
          .append("Die Aufgabe ist damit ");
      if (t.state() == Task.TaskState.FINISHED_CORRECT) output.append("korrekt ");
      else output.append("falsch ");
      output.append("gelöst");

      IVoidFunction showCorrectAnswer =
          () -> showOkDialog(t.correctAnswersAsString(), "Korrekte Antwort", () -> {});

      showOkDialog(
          output.toString(),
          "Ergebnis",
          () -> {
            if (score < t.points()) {
              if (!t.explanation().isBlank() && !t.explanation().equals(Task.DEFAULT_EXPLANATION)) {
                showOkDialog(t.explanation(), "Erklärung", showCorrectAnswer);
              } else {
                showCorrectAnswer.execute();
              }
            }
          });
    };
  }
}

/**
 * Internal builder for text dialogs with support for multiple buttons and custom handlers.
 *
 * <p>This builder is used by the factory to create flexible text dialogs that can have confirm,
 * cancel, and additional custom buttons.
 */
final class TextDialogBuilders {
  private TextDialogBuilders() {}

  /**
   * Builds a text dialog from the given context.
   *
   * @param context The dialog context containing message, buttons, and handlers
   * @return A fully configured text dialog
   */
  static Dialog buildTextDialog(DialogContext context) {
    String text = context.require(DialogContextKeys.MESSAGE, String.class);
    String button =
        context
            .find(DialogContextKeys.CONFIRM_LABEL, String.class)
            .orElse(OkDialog.DEFAULT_OK_BUTTON);
    String cancelButton = context.find(DialogContextKeys.CANCEL_LABEL, String.class).orElse(null);
    @SuppressWarnings("unchecked")
    List<String> extraButtons =
        context.find(DialogContextKeys.ADDITIONAL_BUTTONS, List.class).orElse(List.of());
    String title = context.title().orElse("Text");
    Entity entity = context.requireEntity();
    Skin skin = context.skin();
    Dialog dialog =
        TextDialog.createTextDialog(
            skin,
            text,
            button,
            title,
            (d, id) -> {
              @SuppressWarnings("unchecked")
              BiFunction<Dialog, String, Boolean> customHandler =
                  context.find(DialogContextKeys.RESULT_HANDLER, BiFunction.class).orElse(null);
              if (customHandler != null && customHandler.apply(d, id)) {
                return true;
              }
              if (Objects.equals(id, button)) {
                context
                    .find(DialogContextKeys.ON_CONFIRM, IVoidFunction.class)
                    .ifPresent(IVoidFunction::execute);
                Game.remove(entity);
                return true;
              }
              if (cancelButton != null && Objects.equals(id, cancelButton)) {
                context
                    .find(DialogContextKeys.ON_CANCEL, IVoidFunction.class)
                    .ifPresent(IVoidFunction::execute);
                Game.remove(entity);
                return true;
              }
              return false;
            });
    dialog.button(button, button);
    if (cancelButton != null) {
      dialog.button(cancelButton, cancelButton);
    }
    extraButtons.forEach(additional -> dialog.button(additional, additional));
    dialog.pack();
    return dialog;
  }
}

/**
 * Internal builder for quiz dialogs.
 *
 * <p>This builder creates dialogs that display quiz questions with answer options and handle answer
 * validation through a custom result handler.
 */
final class QuizDialogBuilders {
  private QuizDialogBuilders() {}

  /**
   * Builds a quiz dialog from the given context.
   *
   * @param context The dialog context containing the quiz and result handler linker
   * @return A fully configured quiz dialog
   */
  static Dialog buildQuizDialog(DialogContext context) {
    Quiz quiz = context.require(DialogContextKeys.QUIZ, Quiz.class);
    @SuppressWarnings("unchecked")
    Function<Entity, BiFunction<Dialog, String, Boolean>> resultHandlerLinker =
        (Function<Entity, BiFunction<Dialog, String, Boolean>>)
            context.require(DialogContextKeys.RESULT_HANDLER_LINKER, Function.class);
    String message =
        context
            .find(DialogContextKeys.MESSAGE, String.class)
            .orElse(UIUtils.formatString(quiz.taskText()));
    String title = context.title().orElse(quiz.taskName());
    String confirmLabel =
        context.find(DialogContextKeys.CONFIRM_LABEL, String.class).orElse("Bestätigen");
    String cancelLabel =
        context.find(DialogContextKeys.CANCEL_LABEL, String.class).orElse("Abbrechen");
    String windowStyle =
        context.find(DialogContextKeys.WINDOW_STYLE, String.class).orElse("Letter");
    Entity entity = context.requireEntity();
    BiFunction<Dialog, String, Boolean> handler = resultHandlerLinker.apply(entity);
    Dialog dialog = new TextDialog(title, context.skin(), windowStyle, handler);
    dialog
        .getContentTable()
        .add(QuizDialogDesign.createQuizQuestion(quiz, context.skin(), message))
        .grow()
        .fill();
    dialog.button(cancelLabel, cancelLabel);
    dialog.button(confirmLabel, confirmLabel);
    dialog.pack();

    return dialog;
  }
}
