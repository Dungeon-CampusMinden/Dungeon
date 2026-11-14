package feature.interaction.phone;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import engine.utils.Point;
import feature.components.DecoComponent;
import feature.components.UIComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Factory class for creating phone entities, and the server side of their dialog.
 *
 * <p>A phone entity has a {@link PhoneComponent} with the lines it reaches. Using the phone shows
 * its dialog {@link DialogType.DefaultTypes#PHONE}, see {@link #use(Entity, Entity)}. The dialog
 * tells right away whether a line answers under the dialed number and sends the number to the
 * server, which looks it up again: for a line, it closes the dialog and lets the line answer;
 * otherwise the player hears an error sound and {@link PhoneComponent#onNoConnection} runs.
 */
public final class PhoneFactory {

  /** Most digits a phone number can have. */
  public static final int MAX_DIGITS = 8;

  /** Callback key of a dialed number; the payload is a string value with the digits. */
  public static final String DIAL = "phoneDial";

  // Dialog attributes read by Dialer and PhoneUI.
  static final String DIGITS = "phoneMaxDigits";
  static final String NUMBERS = "phoneNumbers";
  static final String NOTE_NAMES = "phoneNoteNames";
  static final String NOTE_NUMBERS = "phoneNoteNumbers";
  static final String NO_CONNECTION = "phoneNoConnection";

  private static final float DEFAULT_INTERACTION_RADIUS = 1.5f;

  private PhoneFactory() {}

  /**
   * Configures a supplied entity as a phone with the engine's dialog.
   *
   * @param entity entity to configure
   * @param pos position where the phone is created
   * @param maxDigits digits a number can have, at most {@link #MAX_DIGITS}
   * @return the supplied configured entity; the lines and the callbacks are set on its {@link
   *     PhoneComponent}
   */
  public static Entity createPhone(Entity entity, Point pos, int maxDigits) {
    DecoFactory.createDeco(entity, pos, Deco.Phone);
    // A phone, not a decoration the level editor picks up.
    entity.remove(DecoComponent.class);
    entity.add(new PhoneComponent(maxDigits));
    entity.add(
        new InteractionComponent(new Interaction(PhoneFactory::use, DEFAULT_INTERACTION_RADIUS)));
    return entity;
  }

  /**
   * Lets a player use a phone on the server: shows the dialog and looks up every number the player
   * dials in it.
   *
   * @param phone entity with a {@link PhoneComponent}
   * @param user the player using the phone
   */
  public static void use(Entity phone, Entity user) {
    use(phone, user, Map.of());
  }

  /**
   * Lets a player use a phone on the server, with further dialog attributes for a room's own look
   * of the dialog, e.g. whether a lamp on the phone is lit.
   *
   * @param phone entity with a {@link PhoneComponent}
   * @param user the player using the phone
   * @param attributes further attributes of the dialog context
   */
  public static void use(Entity phone, Entity user, Map<String, Object> attributes) {
    PhoneComponent component = phone.fetch(PhoneComponent.class).orElseThrow();
    List<PhoneLine> note = component.lines().stream().filter(PhoneLine::listed).toList();
    DialogContext.Builder context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.PHONE)
            .put(DIGITS, component.maxDigits())
            .put(NUMBERS, component.lines().stream().map(PhoneLine::number).toArray(String[]::new))
            .put(NOTE_NAMES, note.stream().map(PhoneLine::name).toArray(String[]::new))
            .put(NOTE_NUMBERS, note.stream().map(PhoneLine::number).toArray(String[]::new))
            .put(NO_CONNECTION, component.noConnection());
    attributes.forEach(context::put);
    UIComponent ui = DialogFactory.show(context.build(), user.id());
    ui.registerCallback(
        DIAL,
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue(String number)) {
            dial(component, user, number, ui);
          }
        });
  }

  private static void dial(PhoneComponent phone, Entity user, String number, UIComponent ui) {
    Optional<Consumer<Entity>> line = phone.call(number);
    if (line.isEmpty()) {
      Game.audio().playGlobal(SoundSpec.builder("kenney_ui_error_001").targets(user.id()));
      phone.noConnectionListener().accept(user);
      return;
    }
    // The line answers in its own dialog.
    UIUtils.closeDialog(ui, true);
    line.get().accept(user);
  }
}
