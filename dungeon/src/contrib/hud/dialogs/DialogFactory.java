package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.components.InventoryComponent;
import contrib.components.ShowImageComponent;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.crafting.CraftingGUI;
import contrib.hud.elements.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import contrib.modules.keypad.KeypadUI;
import contrib.utils.components.showImage.ShowImageUI;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.utils.IVoidFunction;
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

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(DialogFactory.class);
  private static final Map<DialogType, Function<DialogContext, Group>> registry = new HashMap<>();

  static {
    register(DialogType.DefaultTypes.OK, OkDialog::build);
    register(DialogType.DefaultTypes.YES_NO, YesNoDialog::build);
    register(DialogType.DefaultTypes.TEXT, TextDialogBuilders::buildTextDialog);
    register(DialogType.DefaultTypes.IMAGE, ImageDialog::buildSingleImage);
    register(DialogType.DefaultTypes.FREE_INPUT, FreeInputDialog::build);
    register(DialogType.DefaultTypes.QUIZ, QuizDialogBuilders::buildQuizDialog);
    register(DialogType.DefaultTypes.INVENTORY, InventoryDialogBuilders::buildInventoryDialog);
    register(
        DialogType.DefaultTypes.DUAL_INVENTORY, InventoryDialogBuilders::buildDualInventoryDialog);
    register(DialogType.DefaultTypes.CRAFTING_GUI, InventoryDialogBuilders::buildCraftingGuiDialog);
    register(DialogType.DefaultTypes.KEYPAD, KeypadDialog::build);
    register(DialogType.DefaultTypes.PROGRESS_BAR, ProgressBarDialog::build);
    LOGGER.debug("Registered built-in dialog types");
  }

  /**
   * Registers a custom dialog dialogType with the factory.
   *
   * <p>This allows extending the dialog system with new dialog types without modifying existing
   * code. The creator function receives a {@link DialogContext} and must return a fully configured
   * {@link Dialog}.
   *
   * @param type The unique dialogType of the dialog
   * @param creator Function that creates a dialog from a context
   * @throws DialogCreationException if a dialog dialogType with the given name is already
   *     registered
   */
  public static void register(DialogType type, Function<DialogContext, Group> creator) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(creator, "creator");
    if (registry.containsKey(type)) {
      throw new DialogCreationException("Dialog dialogType '" + type + "' is already registered");
    }
    registry.put(type, creator);
  }

  public static Group create(DialogContext context) {
    Objects.requireNonNull(context, "context");
    Function<DialogContext, Group> creator = registry.get(context.dialogType());
    if (creator == null) {
      throw new DialogCreationException(
          "Unknown dialog dialogType: " + context.dialogType().type());
    }
    var dialog = creator.apply(context);
    if (context.center()) {
      UIUtils.center(dialog);
    }
    return dialog;
  }

  /**
   * Creates and displays a dialog of the specified dialogType.
   *
   * <p>This method creates the dialog, centers it on screen, wraps it in a UI entity, and adds it
   * to the game. The entity lifecycle is automatically managed.
   *
   * @param context The context containing all necessary data for dialog creation
   * @return The entity containing the dialog UI component
   * @throws DialogCreationException if the dialog dialogType is not registered
   */
  public static Entity show(final DialogContext context) {
    Objects.requireNonNull(context, "context");

    Entity entity =
        context
            .find(DialogContextKeys.ENTITY, Entity.class)
            .orElseGet(() -> new Entity("dialog-" + context.dialogType()));
    DialogContext effectiveContext =
        context.toBuilder().put(DialogContextKeys.ENTITY, entity).build();
    showDialog(effectiveContext, entity, true, new int[0]);
    Game.add(entity);
    return entity;
  }

  /**
   * Show the given dialog on the screen for the specified target entities.
   *
   * @param dialogContext the context that defines the dialog to be shown
   * @param entity the entity on which the dialog is being stored
   * @param willPause whether the dialog should pause the game or not
   * @param targetEntityIds the target entity ids this UI should be shown for (e.g. for inventory
   *     UIs). Empty array for all entities.
   */
  private static void showDialog(
      DialogContext dialogContext, Entity entity, boolean willPause, int[] targetEntityIds) {
    // displays this dialog, caches the dialog callback, and increments and decrements the dialog
    Game.player()
        .flatMap(player -> player.fetch(PlayerComponent.class))
        .ifPresentOrElse(
            playerPC -> {
              // counter so that the inventory is not opened while the dialog is displayed
              playerPC.incrementOpenDialogs();

              UIComponent ui = new UIComponent(dialogContext, willPause, targetEntityIds);
              IVoidFunction oldOnClose = ui.onClose();

              ui.onClose(
                  () -> {
                    playerPC.decrementOpenDialogs();
                    oldOnClose.execute();
                  });

              entity.add(ui);
            },
            () -> LOGGER.warn("No player entity found to show dialog."));
  }

  /**
   * Shows a simple OK dialog with a message and a single confirmation button.
   *
   * @param text The message to display in the dialog body
   * @param title The dialog window title
   * @param onConfirm Callback executed when the OK button is pressed
   * @return A tuple containing the entity (with lifecycle management) and the dialog instance
   */
  public static Entity showOkDialog(String text, String title, IVoidFunction onConfirm) {
    return show(
        DialogContext.builder()
            .type(DialogType.DefaultTypes.OK)
            .put(DialogContextKeys.TITLE, title)
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
  public static Entity showYesNoDialog(
      String text, String title, IVoidFunction onYes, IVoidFunction onNo) {
    return show(
        DialogContext.builder()
            .type(DialogType.DefaultTypes.YES_NO)
            .put(DialogContextKeys.TITLE, title)
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
  public static Entity showQuizDialog(
      Quiz quiz, Function<Entity, BiFunction<Dialog, String, Boolean>> handlerLinker) {
    return show(
        DialogContext.builder()
            .type(DialogType.DefaultTypes.QUIZ)
            .put(DialogContextKeys.TITLE, quiz.taskName())
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
  public static Entity showTextDialog(
      String text,
      String title,
      IVoidFunction onConfirm,
      String confirmLabel,
      String cancelLabel,
      List<String> additionalButtons,
      BiFunction<Dialog, String, Boolean> customHandler) {
    DialogContext.Builder builder =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.TEXT)
            .put(DialogContextKeys.TITLE, title)
            .put(DialogContextKeys.MESSAGE, text);
    if (onConfirm != null) builder.put(DialogContextKeys.ON_CONFIRM, onConfirm);
    if (confirmLabel != null) builder.put(DialogContextKeys.CONFIRM_LABEL, confirmLabel);
    if (cancelLabel != null) builder.put(DialogContextKeys.CANCEL_LABEL, cancelLabel);
    if (additionalButtons != null)
      builder.put(DialogContextKeys.ADDITIONAL_BUTTONS, additionalButtons);
    if (customHandler != null) builder.put(DialogContextKeys.RESULT_HANDLER, customHandler);
    return show(builder.build());
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
    return showYesNoDialog(text, title, gradeOn(task), () -> {});
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
  static Group buildTextDialog(DialogContext context) {
    String title = context.require(DialogContextKeys.TITLE, String.class);
    String text = context.require(DialogContextKeys.MESSAGE, String.class);
    String button =
        context
            .find(DialogContextKeys.CONFIRM_LABEL, String.class)
            .orElse(OkDialog.DEFAULT_OK_BUTTON);
    String cancelButton = context.find(DialogContextKeys.CANCEL_LABEL, String.class).orElse(null);
    @SuppressWarnings("unchecked")
    List<String> extraButtons =
        context.find(DialogContextKeys.ADDITIONAL_BUTTONS, List.class).orElse(List.of());
    Entity entity = context.require(DialogContextKeys.ENTITY, Entity.class);
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
  static Group buildQuizDialog(DialogContext context) {
    String title = context.require(DialogContextKeys.TITLE, String.class);
    Quiz quiz = context.require(DialogContextKeys.QUIZ, Quiz.class);
    @SuppressWarnings("unchecked")
    Function<Entity, BiFunction<Dialog, String, Boolean>> resultHandlerLinker =
        (Function<Entity, BiFunction<Dialog, String, Boolean>>)
            context.require(DialogContextKeys.RESULT_HANDLER_LINKER, Function.class);
    String message =
        context
            .find(DialogContextKeys.MESSAGE, String.class)
            .orElse(UIUtils.formatString(quiz.taskText()));
    String confirmLabel =
        context.find(DialogContextKeys.CONFIRM_LABEL, String.class).orElse("Bestätigen");
    String cancelLabel =
        context.find(DialogContextKeys.CANCEL_LABEL, String.class).orElse("Abbrechen");
    String windowStyle =
        context.find(DialogContextKeys.WINDOW_STYLE, String.class).orElse("Letter");
    Entity entity = context.require(DialogContextKeys.ENTITY, Entity.class);
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

final class InventoryDialogBuilders {
  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(InventoryDialogBuilders.class);

  private InventoryDialogBuilders() {}

  static Group buildInventoryDialog(DialogContext context) {
    Entity entity = context.require(DialogContextKeys.ENTITY, Entity.class);
    InventoryComponent inventory = entity.fetch(InventoryComponent.class).orElse(null);
    if (inventory == null) {
      LOGGER.warn("Entity {} has no InventoryComponent for InventoryDialog", entity);
      throw new DialogCreationException("Missing InventoryComponent for InventoryDialog");
    }
    InventoryGUI inventoryGUI = new InventoryGUI(inventory);
    return new GUICombination(inventoryGUI);
  }

  static Group buildDualInventoryDialog(DialogContext context) {
    Entity entity = context.require(DialogContextKeys.ENTITY, Entity.class);
    Entity otherEntity = context.require(DialogContextKeys.SECONDARY_ENTITY, Entity.class);
    InventoryComponent inventory = entity.fetch(InventoryComponent.class).orElse(null);
    InventoryComponent otherInventory = otherEntity.fetch(InventoryComponent.class).orElse(null);
    if (inventory == null || otherInventory == null) {
      Entity missingEntity = (inventory == null) ? entity : otherEntity;
      LOGGER.error("Entity {} has no InventoryComponent for DualInventoryDialog", missingEntity);
      throw new DialogCreationException("Missing InventoryComponent for DualInventoryDialog");
    }
    String title = context.find(DialogContextKeys.TITLE, String.class).orElse(entity.name());
    InventoryGUI inventoryGUI = new InventoryGUI(title, inventory);
    String otherTitle =
        context.find(DialogContextKeys.SECONDARY_TITLE, String.class).orElse(otherEntity.name());
    InventoryGUI otherInventoryGUI = new InventoryGUI(otherTitle, otherInventory);
    return new GUICombination(inventoryGUI, otherInventoryGUI);
  }

  static Group buildCraftingGuiDialog(DialogContext context) {
    Entity entity = context.require(DialogContextKeys.ENTITY, Entity.class);
    Entity otherEntity = context.require(DialogContextKeys.SECONDARY_ENTITY, Entity.class);
    InventoryComponent inventory = entity.fetch(InventoryComponent.class).orElse(null);
    InventoryComponent otherInventory = otherEntity.fetch(InventoryComponent.class).orElse(null);
    if (inventory == null || otherInventory == null) {
      Entity missingEntity = (inventory == null) ? entity : otherEntity;
      LOGGER.error("Entity {} has no InventoryComponent for CraftingGuiDialog", missingEntity);
      throw new DialogCreationException("Missing InventoryComponent for CraftingGuiDialog");
    }
    InventoryGUI inventoryGUI = new InventoryGUI(inventory);
    CraftingGUI craftingGUI = new CraftingGUI(inventory, otherInventory);
    return new GUICombination(inventoryGUI, craftingGUI);
  }
}

final class ImageDialog {
  private ImageDialog() {}

  static Group buildSingleImage(DialogContext context) {
    String img_path = context.require(DialogContextKeys.IMAGE, String.class);
    return new ShowImageUI(new ShowImageComponent(img_path));
  }
}

final class KeypadDialog {
  private KeypadDialog() {}

  static Group build(DialogContext context) {
    Entity entity = context.require(DialogContextKeys.ENTITY, Entity.class);
    return new KeypadUI(entity);
  }
}

final class ProgressBarDialog {
  private ProgressBarDialog() {}

  static Group build(DialogContext context) {
    ProgressBar bar = context.require(DialogContextKeys.PROGRESS_BAR, ProgressBar.class);
    Container<ProgressBar> container = new Container<>(bar);
    container.setLayoutEnabled(false);
    return container;
  }
}
