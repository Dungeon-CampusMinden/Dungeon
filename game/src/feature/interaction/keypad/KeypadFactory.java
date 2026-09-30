package feature.interaction.keypad;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Point;
import engine.utils.components.draw.state.State;
import engine.utils.components.draw.state.StateMachine;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import engine.utils.logging.DungeonLogger;
import feature.components.CollideComponent;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.tasks.FreeTextTask;
import feature.tasks.TaskComponent;
import java.util.Arrays;
import java.util.List;

/** Factory class for creating keypad entities. */
public class KeypadFactory {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(KeypadFactory.class);

  private static final float DEFAULT_INTERACTION_RADIUS = 1.5f;
  private static final IPath TEXTURE_ON = new SimpleIPath("objects/keypad/on.png");
  private static final IPath TEXTURE_OFF = new SimpleIPath("objects/keypad/off.png");

  /**
   * Creates a keypad at the designated position.
   *
   * @param pos The position where the lever will be created.
   * @param correctDigits The correct digits that will start the action if entered
   * @param action The action to execute when the correct digits are entered
   * @param showDigitCount Whether to show the number of digits to be entered
   * @return The created keypad entity.
   */
  public static Entity createKeypad(
      Point pos, List<Integer> correctDigits, Runnable action, boolean showDigitCount) {
    return createKeypad(new Entity("keypad"), pos, correctDigits, action, showDigitCount);
  }

  /**
   * Configures a supplied entity as a keypad.
   *
   * @param entity entity to configure
   * @param pos position where the keypad is created
   * @param correctDigits correct digits that trigger the action
   * @param action action executed for a correct code
   * @param showDigitCount whether the required digit count is shown
   * @return the supplied configured entity
   */
  public static Entity createKeypad(
      Entity entity,
      Point pos,
      List<Integer> correctDigits,
      Runnable action,
      boolean showDigitCount) {
    setupBaseKeypad(entity, pos);

    KeypadComponent kc = new KeypadComponent(correctDigits, action, showDigitCount);
    entity.add(kc);

    entity.add(
        new InteractionComponent(
            new Interaction(
                (e, who) -> {
                  DialogContext context =
                      DialogContext.builder()
                          .type(DialogType.DefaultTypes.KEYPAD)
                          .put(DialogContextKeys.ENTITY, e.id())
                          .build();
                  UIComponent uic = DialogFactory.show(context, who.id());
                  uic.registerCallback(
                      DialogContextKeys.ON_CONFIRM,
                      (payload) -> {
                        if (payload instanceof DialogResponseMessage.StringValue(String value)) {
                          KeypadUI.onButtonPress(e, who, value);
                        }
                      });
                  LOGGER.info("Interacted with keypad sprite");
                },
                DEFAULT_INTERACTION_RADIUS)));
    return entity;
  }

  /**
   * Creates a text keypad at the designated position. Only Characters from A-Z + Space are allowed,
   * everything else currently not supported.
   *
   * @param pos The position where the keypad will be created.
   * @param correctTexts The correct text that will start the action if entered
   * @param action The action to execute when the correct text is entered
   * @return The created keypad entity.
   */
  public static Entity createTextKeypad(Point pos, List<String> correctTexts, Runnable action) {
    Entity entity = new Entity("keypad");
    setupBaseKeypad(entity, pos);
    boolean valid = correctTexts.stream().allMatch(s -> s != null && s.matches("[A-Za-z ]+"));
    if (!valid) {
      throw new TextKeyPadException(
          "TextKeypad only supports letter in the range A-Z, a-z and spaces.");
    }
    TaskComponent<String> taskComponent = new TaskComponent<>(new FreeTextTask(correctTexts));
    taskComponent.onCorrect((caller) -> action.run());
    TextKeyPadComponent kc = new TextKeyPadComponent(action);
    entity.add(kc);
    entity.add(taskComponent);

    entity.add(
        new InteractionComponent(
            new Interaction(
                (e, who) -> {
                  DialogContext context =
                      DialogContext.builder()
                          .type(DialogType.DefaultTypes.TEXT_KEYPAD)
                          .put(DialogContextKeys.ENTITY, e.id())
                          .build();
                  UIComponent uic = DialogFactory.show(context, who.id());
                  uic.registerCallback(
                      DialogContextKeys.ON_CONFIRM,
                      (payload) -> {
                        if (payload instanceof DialogResponseMessage.StringValue(String value)) {
                          TextKeypadUI.onButtonPress(e, taskComponent, who, value);
                        }
                      });
                  LOGGER.info("Interacted with keypad sprite");
                },
                DEFAULT_INTERACTION_RADIUS)));
    return entity;
  }

  private static void setupBaseKeypad(Entity entity, Point pos) {
    entity.add(new PositionComponent(pos));

    State stClosed = new State("closed", TEXTURE_OFF);
    State stOpen = new State("open", TEXTURE_ON);
    StateMachine sm = new StateMachine(Arrays.asList(stClosed, stOpen));
    sm.addTransition(stClosed, "open", stOpen);
    sm.addTransition(stOpen, "close", stClosed);
    DrawComponent dc = new DrawComponent(sm);
    entity.add(dc);
  }
}
