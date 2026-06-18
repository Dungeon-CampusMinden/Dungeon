package modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import contrib.hud.dialogs.DialogCallbackResolver;
import core.sound.Sounds;
import core.utils.Scene2dElementFactory;
import modules.computer.ComputerFactory;
import modules.computer.ComputerStateComponent;
import util.LastHourSounds;
import util.Lore;

/**
 * Tab that exposes the room's environmental control panel. Opened by clicking the {@code
 * control-panel.key} file inside the USB drive tab.
 */
public class ControlPanelTab extends ComputerTab {

  /** Key for identifying the control panel tab in the computer dialog. */
  public static final String KEY = "control-panel";

  // ----- Constants (text + layout) -----

  private static final String TITLE = "Control Panel";

  private static final String HEADER_TEXT = "Room Control Panel";
  private static final String HEADER_SUB_TEXT =
      "Authorized personnel only. Changes apply immediately.";

  private static final String LIGHT_TITLE = "Light";
  private static final String HEATER_TITLE = "Heater";
  private static final String DOOR1_TITLE = "Door 1 (Storage)";
  private static final String DOOR2_TITLE = "Door 2 (Office)";
  private static final String AC_TITLE = "Air Conditioning";
  private static final String CAMERAS_TITLE = "Security Cameras";

  private static final String AC_VENT_PREFIX = "sv000";
  private static final String AC_VENT_HINT = "Serial";
  private static final String AC_CONNECT_BUTTON = "Connect";
  private static final String AC_STATUS_NO_CONNECTION = "No Connection";
  private static final String AC_STATUS_CONNECTED = "Connected";

  private static final String STATE_ON = "ON";
  private static final String STATE_OFF = "OFF";
  private static final String STATE_OPEN = "OPEN";
  private static final String STATE_CLOSED = "CLOSED";
  private static final String STATE_LOCKED = "LOCKED";

  private static final String HEATER_UNIT = " C";
  private static final int HEATER_MIN = 10;
  private static final int HEATER_MAX = 30;
  private static final String HEATER_UP = "+";
  private static final String HEATER_DOWN = "-";
  private static final String CLOSE_BUTTON = "Close";
  private static final String OPEN_BUTTON = "Open";
  private static final String UNLOCK_BUTTON = "Unlock";
  private static final String TURN_ON_BUTTON = "Turn ON";
  private static final String TURN_OFF_BUTTON = "Turn OFF";

  private static final String DOOR2_PASSWORD_HINT = "Password";

  private static final int HEADER_FONT_SIZE = 30;
  private static final int HEADER_SUB_FONT_SIZE = 14;
  private static final int SECTION_TITLE_FONT_SIZE = 22;
  private static final int SECTION_BODY_FONT_SIZE = 18;
  private static final int SMALL_BUTTON_FONT_SIZE = 18;

  private static final String CARD_BACKGROUND = "generic-area";
  private static final String GREEN_BUTTON_STYLE = "green";
  private static final String BLUE_BUTTON_STYLE = "blue-outline";

  private static final Color HEADER_COLOR = new Color(0.10f, 0.10f, 0.45f, 1f);
  private static final Color HEADER_SUB_COLOR = Color.GRAY;
  private static final Color SECTION_TITLE_COLOR = new Color(0.10f, 0.10f, 0.45f, 1f);
  private static final Color STATE_ON_COLOR = new Color(0f, 0.85f, 0f, 1f);
  private static final Color STATE_OFF_COLOR = new Color(0.85f, 0f, 0f, 1f);
  private static final Color WRONG_COLOR = STATE_OFF_COLOR;

  private static final float CARD_PAD = 14f;
  private static final float CARD_INNER_PAD = 10f;
  private static final float SECTION_VALUE_PAD_TOP = 4f;
  private static final float HEADER_PAD_BOTTOM = 14f;

  // Widgets
  private Label lightStateLabel;
  private TextButton lightButton;
  private Label heaterValueLabel;
  private Label door1StateLabel;
  private TextButton door1Button;
  private Label door2StateLabel;
  private TextField door2PasswordField;
  private TextButton door2UnlockButton;
  private TextButton door2OpenButton;
  private Label acStateLabel;
  private TextButton acButton;
  private TextField acVentSerialField;
  private TextButton acVentConnectButton;
  private Label acVentStatusLabel;
  private Label camerasStateLabel;
  private TextButton camerasButton;

  /**
   * Creates a new ControlPanelTab.
   *
   * @param sharedState the shared computer state component
   */
  public ControlPanelTab(ComputerStateComponent sharedState) {
    super(sharedState, KEY, TITLE, false);
    dismissAttention();
  }

  @Override
  protected void createActors() {
    Table root = new Table();
    root.top();

    // Header.
    Label header = Scene2dElementFactory.createLabel(HEADER_TEXT, HEADER_FONT_SIZE, HEADER_COLOR);
    header.setAlignment(Align.center);
    root.add(header).colspan(2).center().padBottom(2f).row();

    Label subHeader =
        Scene2dElementFactory.createLabel(HEADER_SUB_TEXT, HEADER_SUB_FONT_SIZE, HEADER_SUB_COLOR);
    subHeader.setAlignment(Align.center);
    root.add(subHeader).colspan(2).center().padBottom(HEADER_PAD_BOTTOM).row();

    root.add(Scene2dElementFactory.createHorizontalDivider())
        .colspan(2)
        .fillX()
        .height(2f)
        .padBottom(HEADER_PAD_BOTTOM)
        .row();

    // Two-column body (left | right). Cells share the same width via uniform() but each row picks
    // its own height based on the tallest card in the row, so sections that need more space (like
    // the AC card with its connection sub-section) won't waste vertical space on the others.
    root.add(buildLightSection()).top().growX().fill().uniformX().pad(CARD_PAD);
    root.add(buildCamerasSection()).top().growX().fill().uniformX().pad(CARD_PAD).row();

    root.add(buildHeaterSection()).top().growX().fill().uniformX().pad(CARD_PAD);
    root.add(buildAcSection()).top().growX().fill().uniformX().pad(CARD_PAD).row();

    root.add(buildDoor1Section()).top().growX().fill().uniformX().pad(CARD_PAD);
    root.add(buildDoor2Section()).top().growX().fill().uniformX().pad(CARD_PAD).row();

    this.add(root).grow();
  }

  // ----- Section builders -----

  private Table buildLightSection() {
    Table card = newCard();
    card.add(sectionTitle(LIGHT_TITLE)).left().row();

    lightStateLabel = Scene2dElementFactory.createLabel("", SECTION_BODY_FONT_SIZE);
    card.add(lightStateLabel).left().padTop(SECTION_VALUE_PAD_TOP).row();

    lightButton =
        Scene2dElementFactory.createButton("", GREEN_BUTTON_STYLE, SMALL_BUTTON_FONT_SIZE);
    lightButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            postUpdate(sharedState().withLightsOn(!sharedState().lightsOn()));
          }
        });
    card.add(lightButton).left().padTop(8f).row();

    refreshLight();
    return card;
  }

  private Table buildHeaterSection() {
    Table card = newCard();
    card.add(sectionTitle(HEATER_TITLE)).left().colspan(3).row();

    heaterValueLabel =
        Scene2dElementFactory.createLabel(
            sharedState().heaterCelsius() + HEATER_UNIT, SECTION_BODY_FONT_SIZE, STATE_ON_COLOR);

    TextButton down =
        Scene2dElementFactory.createButton(HEATER_DOWN, BLUE_BUTTON_STYLE, SMALL_BUTTON_FONT_SIZE);
    down.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            int next = Math.max(HEATER_MIN, sharedState().heaterCelsius() - 1);
            if (next != sharedState().heaterCelsius()) {
              postUpdate(sharedState().withHeaterCelsius(next));
            }
          }
        });
    TextButton up =
        Scene2dElementFactory.createButton(HEATER_UP, BLUE_BUTTON_STYLE, SMALL_BUTTON_FONT_SIZE);
    up.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            int next = Math.min(HEATER_MAX, sharedState().heaterCelsius() + 1);
            if (next != sharedState().heaterCelsius()) {
              postUpdate(sharedState().withHeaterCelsius(next));
            }
          }
        });

    Table row = new Table();
    row.add(down).width(40f).padRight(10f);
    row.add(heaterValueLabel).padRight(10f);
    row.add(up).width(40f);
    card.add(row).left().padTop(SECTION_VALUE_PAD_TOP).row();
    return card;
  }

  private Table buildDoor1Section() {
    Table card = newCard();
    card.add(sectionTitle(DOOR1_TITLE)).left().row();
    door1StateLabel = Scene2dElementFactory.createLabel("", SECTION_BODY_FONT_SIZE);
    card.add(door1StateLabel).left().padTop(SECTION_VALUE_PAD_TOP).row();

    door1Button =
        Scene2dElementFactory.createButton(
            CLOSE_BUTTON, GREEN_BUTTON_STYLE, SMALL_BUTTON_FONT_SIZE);
    door1Button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            postUpdate(sharedState().withDoor1Open(!sharedState().door1Open()));
          }
        });
    card.add(door1Button).left().padTop(8f).row();

    refreshDoor1();
    return card;
  }

  private Table buildDoor2Section() {
    Table card = newCard();
    card.add(sectionTitle(DOOR2_TITLE)).left().colspan(2).row();
    door2StateLabel = Scene2dElementFactory.createLabel("", SECTION_BODY_FONT_SIZE);
    card.add(door2StateLabel).left().colspan(2).padTop(SECTION_VALUE_PAD_TOP).row();

    door2PasswordField = Scene2dElementFactory.createTextField("");
    door2PasswordField.setMessageText(DOOR2_PASSWORD_HINT);
    door2PasswordField.setPasswordMode(true);
    door2PasswordField.setPasswordCharacter('*');
    door2PasswordField.setTextFieldListener(
        (textField, c) -> {
          if (c == '\r' || c == '\n') {
            tryUnlockDoor2();
          }
        });

    door2UnlockButton =
        Scene2dElementFactory.createButton(
            UNLOCK_BUTTON, BLUE_BUTTON_STYLE, SMALL_BUTTON_FONT_SIZE);
    door2UnlockButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            tryUnlockDoor2();
          }
        });

    door2OpenButton =
        Scene2dElementFactory.createButton(OPEN_BUTTON, GREEN_BUTTON_STYLE, SMALL_BUTTON_FONT_SIZE);
    door2OpenButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (!sharedState().door2Unlocked() || sharedState().door2Open()) return;
            postUpdate(sharedState().withDoor2Open(true));
          }
        });

    Table row = new Table();
    row.add(door2PasswordField).width(180f).padRight(8f);
    row.add(door2UnlockButton).padRight(8f);
    row.add(door2OpenButton);
    card.add(row).left().padTop(8f).row();

    refreshDoor2();
    return card;
  }

  private Table buildAcSection() {
    Table card = newCard();
    card.add(sectionTitle(AC_TITLE)).left().colspan(2).row();

    // Left half of the AC card: state + on/off toggle. Right half: connection sub-section that
    // floats independently so it doesn't disturb the left layout.
    Table left = new Table();
    left.top().left();
    left.defaults().left();
    acStateLabel = Scene2dElementFactory.createLabel("", SECTION_BODY_FONT_SIZE);
    left.add(acStateLabel).left().padTop(SECTION_VALUE_PAD_TOP).row();
    acButton = Scene2dElementFactory.createButton("", GREEN_BUTTON_STYLE, SMALL_BUTTON_FONT_SIZE);
    acButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (!sharedState().acVentConnected()) return;
            postUpdate(sharedState().withAcOn(!sharedState().acOn()));
          }
        });
    left.add(acButton).left().padTop(8f).row();

    card.add(left).top().left().growX();
    card.add(buildAcConnectionSubSection()).top().right().padLeft(12f).row();

    refreshAc();
    refreshAcConnection();
    return card;
  }

  private Table buildAcConnectionSubSection() {
    Table sub = new Table(skin);
    sub.setBackground(CARD_BACKGROUND);
    sub.pad(CARD_INNER_PAD);
    sub.top().right();
    sub.defaults().left();

    Label prefixLabel =
        Scene2dElementFactory.createLabel(AC_VENT_PREFIX, SECTION_BODY_FONT_SIZE, Color.DARK_GRAY);
    acVentSerialField = Scene2dElementFactory.createTextField("");
    acVentSerialField.setMessageText(AC_VENT_HINT);
    acVentSerialField.setTextFieldListener(
        (textField, c) -> {
          if (c == '\r' || c == '\n') {
            tryConnectAcVent();
          }
        });
    acVentConnectButton =
        Scene2dElementFactory.createButton(
            AC_CONNECT_BUTTON, BLUE_BUTTON_STYLE, SMALL_BUTTON_FONT_SIZE);
    acVentConnectButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            tryConnectAcVent();
          }
        });
    acVentStatusLabel =
        Scene2dElementFactory.createLabel(
            AC_STATUS_NO_CONNECTION, SECTION_BODY_FONT_SIZE, STATE_OFF_COLOR);

    Table inputRow = new Table();
    inputRow.add(prefixLabel).padRight(2f);
    inputRow.add(acVentSerialField).width(140f);
    sub.add(inputRow).left().row();
    sub.add(acVentConnectButton).left().padTop(6f).row();
    sub.add(acVentStatusLabel).left().padTop(4f).row();
    return sub;
  }

  private Table buildCamerasSection() {
    Table card = newCard();
    card.add(sectionTitle(CAMERAS_TITLE)).left().row();
    camerasStateLabel = Scene2dElementFactory.createLabel("", SECTION_BODY_FONT_SIZE);
    card.add(camerasStateLabel).left().padTop(SECTION_VALUE_PAD_TOP).row();
    camerasButton =
        Scene2dElementFactory.createButton("", GREEN_BUTTON_STYLE, SMALL_BUTTON_FONT_SIZE);
    camerasButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            postUpdate(sharedState().withCamerasOn(!sharedState().camerasOn()));
          }
        });
    card.add(camerasButton).left().padTop(8f).row();
    refreshCameras();
    return card;
  }

  // ----- Helpers -----

  private Table newCard() {
    Table card = new Table(skin);
    card.setBackground(CARD_BACKGROUND);
    card.pad(CARD_INNER_PAD);
    card.top().left();
    card.defaults().left();
    return card;
  }

  private Label sectionTitle(String text) {
    Label label =
        Scene2dElementFactory.createLabel(text, SECTION_TITLE_FONT_SIZE, SECTION_TITLE_COLOR);
    label.setAlignment(Align.left);
    return label;
  }

  private void tryUnlockDoor2() {
    if (sharedState().door2Unlocked()) {
      return;
    }

    String entered = door2PasswordField.getText();
    if (entered != null && entered.equalsIgnoreCase(Lore.ControlPanelDoor2Password)) {
      postUpdate(sharedState().withDoor2Unlocked(true));
      Sounds.play(LastHourSounds.COMPUTER_LOGIN_SUCCESS);
    } else {
      Sounds.play(LastHourSounds.COMPUTER_LOGIN_FAILED);
    }
  }

  private void tryConnectAcVent() {
    if (sharedState().acVentConnected()) {
      return;
    }

    String entered = acVentSerialField.getText();
    if (entered != null && entered.equals(Lore.VentSerialNumber)) {
      postUpdate(sharedState().withAcVentConnected(true));
      Sounds.play(LastHourSounds.COMPUTER_LOGIN_SUCCESS);
    } else {
      acVentStatusLabel.setColor(STATE_OFF_COLOR);
      acVentStatusLabel.setText(AC_STATUS_NO_CONNECTION);
      Sounds.play(LastHourSounds.COMPUTER_LOGIN_FAILED);
    }
  }

  private void postUpdate(ComputerStateComponent newState) {
    DialogCallbackResolver.createButtonCallback(
            context().dialogId(), ComputerFactory.UPDATE_STATE_KEY)
        .accept(newState);
  }

  private void refreshLight() {
    refreshLight(sharedState());
  }

  private void refreshLight(ComputerStateComponent s) {
    if (lightStateLabel == null) return;
    boolean on = s.lightsOn();
    lightStateLabel.setText(on ? STATE_ON : STATE_OFF);
    lightStateLabel.setColor(on ? STATE_ON_COLOR : STATE_OFF_COLOR);
    lightButton.setText(on ? TURN_OFF_BUTTON : TURN_ON_BUTTON);
  }

  private void refreshDoor1() {
    refreshDoor1(sharedState());
  }

  private void refreshDoor1(ComputerStateComponent s) {
    if (door1StateLabel == null) return;
    boolean open = s.door1Open();
    door1StateLabel.setText(open ? STATE_OPEN : STATE_CLOSED);
    door1StateLabel.setColor(open ? STATE_ON_COLOR : STATE_OFF_COLOR);
    door1Button.setText(open ? CLOSE_BUTTON : OPEN_BUTTON);
  }

  private void refreshDoor2() {
    refreshDoor2(sharedState());
  }

  private void refreshDoor2(ComputerStateComponent s) {
    if (door2StateLabel == null) return;
    boolean open = s.door2Open();
    boolean unlocked = s.door2Unlocked();
    if (open) {
      door2StateLabel.setText(STATE_OPEN);
      door2StateLabel.setColor(STATE_ON_COLOR);
    } else if (unlocked) {
      door2StateLabel.setText(STATE_CLOSED);
      door2StateLabel.setColor(STATE_OFF_COLOR);
    } else {
      door2StateLabel.setText(STATE_LOCKED);
      door2StateLabel.setColor(WRONG_COLOR);
    }
    door2PasswordField.setDisabled(unlocked);
    door2UnlockButton.setDisabled(unlocked);
    door2OpenButton.setDisabled(!unlocked || open);
  }

  private void refreshAc() {
    refreshAc(sharedState());
  }

  private void refreshAc(ComputerStateComponent s) {
    if (acStateLabel == null) return;
    boolean on = s.acOn();
    acStateLabel.setText(on ? STATE_ON : STATE_OFF);
    acStateLabel.setColor(on ? STATE_ON_COLOR : STATE_OFF_COLOR);
    acButton.setText(on ? TURN_OFF_BUTTON : TURN_ON_BUTTON);
    acButton.setDisabled(!s.acVentConnected());
  }

  private void refreshAcConnection() {
    refreshAcConnection(sharedState());
  }

  private void refreshAcConnection(ComputerStateComponent s) {
    if (acVentStatusLabel == null) return;
    boolean connected = s.acVentConnected();
    acVentStatusLabel.setText(connected ? AC_STATUS_CONNECTED : AC_STATUS_NO_CONNECTION);
    acVentStatusLabel.setColor(connected ? STATE_ON_COLOR : STATE_OFF_COLOR);
    acVentSerialField.setDisabled(connected);
    acVentConnectButton.setDisabled(connected);
  }

  private void refreshCameras() {
    refreshCameras(sharedState());
  }

  private void refreshCameras(ComputerStateComponent s) {
    if (camerasStateLabel == null) return;
    boolean on = s.camerasOn();
    camerasStateLabel.setText(on ? STATE_ON : STATE_OFF);
    camerasStateLabel.setColor(on ? STATE_ON_COLOR : STATE_OFF_COLOR);
    camerasButton.setText(on ? TURN_OFF_BUTTON : TURN_ON_BUTTON);
  }

  private void refreshHeater(ComputerStateComponent s) {
    if (heaterValueLabel == null) return;
    heaterValueLabel.setText(s.heaterCelsius() + HEATER_UNIT);
  }

  @Override
  protected void updateState(ComputerStateComponent newStateComp) {
    refreshLight(newStateComp);
    refreshDoor1(newStateComp);
    refreshDoor2(newStateComp);
    refreshAc(newStateComp);
    refreshCameras(newStateComp);
    refreshHeater(newStateComp);
    refreshAcConnection(newStateComp);
  }
}
