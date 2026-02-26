package contrib.hud.dialogs;

/**
 * A utility class that defines a set of constant keys used for dialog context management.
 *
 * <p>These keys are used to store and retrieve values in a context map for dialog-related
 * operations.
 *
 * @see DialogContext
 */
public class DialogContextKeys {

  /** The key for the dialog title content. */
  public static final String TITLE = "title";

  /** The key for the secondary title content. */
  public static final String SECONDARY_TITLE = "secondaryTitle";

  /** The key for the dialog message content. */
  public static final String MESSAGE = "message";

  /** The key for the owner entity ID (the entity holding the UIComponent). */
  public static final String OWNER_ENTITY = "ownerEntity";

  /** The key for the associated entity in the dialog context. */
  public static final String ENTITY = "entity";

  /** The key for a secondary entity in the dialog context. */
  public static final String SECONDARY_ENTITY = "secondaryEntity";

  /** The key for the dialog question content. */
  public static final String QUESTION = "question";

  /** The key for the label of the confirm button. */
  public static final String CONFIRM_LABEL = "confirmLabel";

  /** The key for the label of the cancel button. */
  public static final String CANCEL_LABEL = "cancelLabel";

  /** The key for the callback function to execute on confirmation. */
  public static final String ON_CONFIRM = "onConfirm";

  /** The key for the callback function to execute on a "Yes" response. */
  public static final String ON_YES = "onYes";

  /** The key for the callback function to execute on a "No" response. */
  public static final String ON_NO = "onNo";

  /** The key for the callback function to execute on cancellation. */
  public static final String ON_CANCEL = "onCancel";

  /** The key for the callback function to execute on closure of the dialog. */
  public static final String ON_CLOSE = "onClose";

  /** The key for the callback function to execute on a "Resume" response. */
  public static final String ON_RESUME = "onResume";

  /** The key for the callback function to execute on a "Quit" response. */
  public static final String ON_QUIT = "onQuit";

  /** The key for the placeholder text in an input field. */
  public static final String INPUT_PLACEHOLDER = "inputPlaceholder";

  /** The key for the pre-filled text in an input field. */
  public static final String INPUT_PREFILL = "inputPrefill";

  /**
   * The key for specifying additional buttons in the dialog.
   *
   * <p>Use this key to provide an array of strings representing additional button labels to be
   * included in the dialog interface.
   *
   * <p>For callbacks, register functions with "on" as a prefix followed by the button label. For
   * example, for a button labeled "Retry", register a callback with the key "onRetry
   */
  public static final String ADDITIONAL_BUTTONS = "additionalButtons";

  /** The key for the image content in the dialog. */
  public static final String IMAGE = "image";

  /** The key for the image transition speed in the dialog. */
  public static final String IMAGE_TRANSITION_SPEED = "imageTransitionSpeed";

  /** The key for the progress bar component in the dialog. */
  public static final String PROGRESS_BAR = "progressBar";

  private DialogContextKeys() {}
}
