package contrib.hud.dialogs;

/**
 * A utility class that defines a set of constant keys used for dialog context management.
 *
 * <p>These keys are used to store and retrieve values in a context map for dialog-related
 * operations.
 *
 * @see DialogContext
 */
public final class DialogContextKeys {

  /** The key for the dialog message content. */
  public static final String MESSAGE = "message";

  /** The key for the dialog question content. */
  public static final String QUESTION = "question";

  /** The key for the label of the confirm button. */
  public static final String CONFIRM_LABEL = "confirmLabel";

  /** The key for the label of the cancel button. */
  public static final String CANCEL_LABEL = "cancelLabel";

  /** The key for the style of the dialog window. */
  public static final String WINDOW_STYLE = "windowStyle";

  /** The key for the callback function to execute on confirmation. */
  public static final String ON_CONFIRM = "onConfirm";

  /** The key for the callback function to execute on cancellation. */
  public static final String ON_CANCEL = "onCancel";

  /** The key for the callback function to execute on a "Yes" response. */
  public static final String ON_YES = "onYes";

  /** The key for the callback function to execute on a "No" response. */
  public static final String ON_NO = "onNo";

  /** The key for the callback function to handle input submission. */
  public static final String INPUT_CALLBACK = "inputCallback";

  /** The key for the placeholder text in an input field. */
  public static final String INPUT_PLACEHOLDER = "inputPlaceholder";

  /** The key for the pre-filled text in an input field. */
  public static final String INPUT_PREFILL = "inputPrefill";

  /** The key for the quiz data associated with the dialog. */
  public static final String QUIZ = "quiz";

  /** The key for linking a result handler to the dialog. */
  public static final String RESULT_HANDLER_LINKER = "resultHandlerLinker";

  /** The key for specifying additional buttons in the dialog. */
  public static final String ADDITIONAL_BUTTONS = "additionalButtons";

  /** The key for the result handler callback function. */
  public static final String RESULT_HANDLER = "resultHandler";

  private DialogContextKeys() {}
}
