package feature.interaction.combinationlock;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.BaseContainerUI;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The dialog of a combination lock, with one wheel per digit in a steel frame. With its controls,
 * arrows turn the selected wheel and select another one, and a button opens the lock. They send
 * their actions to the server, see {@link CombinationLock}. The wheels show the {@link
 * CombinationLockComponent} of the lock entity and roll digit by digit to each new digit.
 */
public final class CombinationLockUI extends Table {

  private static final String TEXT_FONT = "fonts/Lexend-Regular.ttf";
  private static final String BOLD_FONT = "fonts/Lexend-Bold.ttf";
  private static final Color STEEL = Color.valueOf("2b313bf5");
  private static final Color RIM = Color.valueOf("4a5262ff");
  private static final Color FACE = Color.valueOf("ece6d6ff");
  private static final Color INK = Color.valueOf("1b1f27ff");
  private static final Color TEXT = Color.valueOf("e8eaefff");
  private static final Color MUTED = Color.valueOf("a3aab8ff");
  private static final Color ACCENT = Color.valueOf("f0b43cff");
  private static final float TEXT_WIDTH = 500;
  private static final float WHEEL_WIDTH = 116;
  private static final float ROW = 88;
  private static final float RIM_WIDTH = 6;
  private static final float ARROW = 40;
  // Time per digit. A wheel rolls at most five digits, which takes less than the 0.8 seconds the
  // lock waits per dialed wheel.
  private static final float ROLL_SECONDS = 0.13f;
  private static final Map<Arrow, Texture> ARROWS = new EnumMap<>(Arrow.class);

  private final Entity lock;
  private final String dialogId;
  private final boolean controls;
  private final Table wheelRow = new Table();
  private final List<Column> columns = new ArrayList<>();

  private CombinationLockUI(
      Entity lock, String dialogId, String title, String question, String hint, boolean controls) {
    this.lock = lock;
    this.dialogId = dialogId;
    this.controls = controls;
    setBackground(tinted(STEEL));
    pad(28, 40, 28, 40);
    // Text beside the dial, so the dialog fits a screen 720 pixels high.
    Table text = new Table();
    text.defaults().left();
    text.add(label(title, BOLD_FONT, 34, TEXT)).row();
    if (!question.isBlank()) {
      text.add(wrapped(question, 24, TEXT)).width(TEXT_WIDTH).padTop(12).row();
    }
    if (!hint.isBlank()) {
      text.add(wrapped(hint, 20, MUTED)).width(TEXT_WIDTH).padTop(16).row();
    }
    if (controls) {
      TextButton open = Scene2dElementFactory.createButton("Öffnen", "green", 28);
      open.addListener(sends(CombinationLock.OPEN));
      text.add(open).padTop(24);
    }
    add(text).width(TEXT_WIDTH).padRight(48);

    Table dial = new Table();
    if (controls) dial.add(arrow(Arrow.LEFT, CombinationLock.LEFT)).size(ARROW).padRight(20);
    dial.add(wheelRow);
    if (controls) dial.add(arrow(Arrow.RIGHT, CombinationLock.RIGHT)).size(ARROW).padLeft(20);
    add(dial);
  }

  /**
   * Builds the dialog of a combination lock.
   *
   * @param context the dialog context with the lock entity, the title, the question, the hint and
   *     whether to show the controls, see {@link CombinationLock#show}
   * @return the dialog, or a placeholder on a headless server
   */
  public static Group build(DialogContext context) {
    String title = context.find(DialogContextKeys.TITLE, String.class).orElse("");
    String question = context.find(DialogContextKeys.QUESTION, String.class).orElse("");
    if (Game.isHeadless()) return new HeadlessDialogGroup(title, question);
    String hint = context.find(DialogContextKeys.DESCRIPTION, String.class).orElse("");
    boolean controls = context.find(CombinationLock.CONTROLS, Boolean.class).orElse(true);
    return new BaseContainerUI(
        new CombinationLockUI(
            context.requireEntity(DialogContextKeys.ENTITY),
            context.dialogId(),
            title,
            question,
            hint,
            controls));
  }

  @Override
  public void act(float delta) {
    // Before the first synchronized state arrives, the dial shows no wheels.
    Optional<CombinationLockComponent> state = lock.fetch(CombinationLockComponent.class);
    if (state.isPresent()) {
      CombinationLockComponent component = state.get();
      if (columns.size() != component.wheels()) columns(component.wheels());
      String digits = component.digits();
      for (int i = 0; i < columns.size(); i++) {
        columns.get(i).show(digits.charAt(i) - '0', i == component.selected());
      }
    }
    super.act(delta);
  }

  // Builds one column per wheel: the wheel in its rim, with the controls between arrows.
  private void columns(int count) {
    wheelRow.clearChildren();
    columns.clear();
    for (int i = 0; i < count; i++) {
      Column column = new Column();
      wheelRow.add(column.table).padLeft(i == 0 ? 0 : 18);
      columns.add(column);
    }
    invalidateHierarchy();
  }

  private Image arrow(Arrow direction, String action) {
    Image image = new Image(arrowTexture(direction));
    image.setColor(ACCENT);
    image.addListener(sends(action));
    return image;
  }

  private ClickListener sends(String action) {
    return new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CONFIRM)
            .accept(new DialogResponseMessage.StringValue(action));
      }
    };
  }

  // White triangles, tinted when drawn; the fonts lack arrow glyphs.
  private static Texture arrowTexture(Arrow direction) {
    return ARROWS.computeIfAbsent(
        direction,
        key -> {
          int size = 64;
          Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
          pixmap.setColor(Color.WHITE);
          int near = 8;
          int far = size - 8;
          int middle = size / 2;
          // Pixmaps count rows from the top.
          switch (key) {
            case UP -> pixmap.fillTriangle(near, far, far, far, middle, near);
            case DOWN -> pixmap.fillTriangle(near, near, far, near, middle, far);
            case LEFT -> pixmap.fillTriangle(far, near, far, far, near, middle);
            case RIGHT -> pixmap.fillTriangle(near, near, near, far, far, middle);
          }
          Texture texture = new Texture(pixmap);
          texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
          pixmap.dispose();
          return texture;
        });
  }

  private static Drawable tinted(Color color) {
    return UIUtils.defaultSkin().newDrawable("white", color);
  }

  private static Label label(String text, String font, int size, Color color) {
    return Scene2dElementFactory.createLabel(text, FontSpec.of(font, size, color));
  }

  private static Label wrapped(String text, int size, Color color) {
    Label label = label(text, TEXT_FONT, size, color);
    label.setWrap(true);
    label.setAlignment(Align.left);
    return label;
  }

  /** Directions of the arrows. */
  private enum Arrow {
    UP,
    DOWN,
    LEFT,
    RIGHT
  }

  /** A wheel with its rim and, with the controls, the arrows that turn it while it is selected. */
  private final class Column {
    private final Table table = new Table();
    private final Table rim = new Table();
    private final Image up = arrow(Arrow.UP, CombinationLock.UP);
    private final Image down = arrow(Arrow.DOWN, CombinationLock.DOWN);
    private final Wheel wheel = new Wheel();
    private Boolean selected;

    private Column() {
      rim.pad(RIM_WIDTH);
      rim.add(wheel).size(WHEEL_WIDTH, 3 * ROW);
      if (controls) table.add(up).size(ARROW).padBottom(10).row();
      table.add(rim).row();
      if (controls) table.add(down).size(ARROW).padTop(10);
    }

    private void show(int digit, boolean selected) {
      wheel.show(digit);
      if (Boolean.valueOf(selected).equals(this.selected)) return;
      this.selected = selected;
      rim.setBackground(tinted(selected ? ACCENT : RIM));
      // Hidden arrows keep their cells, so the wheels stay in place.
      up.setVisible(selected);
      down.setVisible(selected);
    }
  }

  /**
   * The face of a wheel: its digit in the middle, the neighbours fading above and below. A new
   * digit rolls in from below when the wheel turns up and from above when it turns down; a digit
   * several steps away rolls in step by step, the short way round.
   */
  private static final class Wheel extends Table {
    // Digits from two below to two above the shown one; the outer ones only show while rolling.
    private final Label[] slots = new Label[5];
    private final Group strip = new Group();
    private int digit = -1;
    private int target = -1;
    private float rollFrom;
    private float rolled = ROLL_SECONDS;

    private Wheel() {
      setBackground(tinted(FACE));
      setClip(true);
      add(strip).size(WHEEL_WIDTH, 3 * ROW);
      for (int i = 0; i < slots.length; i++) {
        slots[i] = label("", BOLD_FONT, 76, INK);
        slots[i].setAlignment(Align.center);
        slots[i].setSize(WHEEL_WIDTH, ROW);
        strip.addActor(slots[i]);
      }
    }

    private void show(int next) {
      target = next;
      // The first digit just appears.
      if (digit < 0) roll(next, 0);
    }

    // Shows the next digit, rolling in from where it stood on the wheel.
    private void roll(int next, int step) {
      digit = next;
      for (int i = 0; i < slots.length; i++) {
        slots[i].setText(Integer.toString(Math.floorMod(digit + i - 2, 10)));
      }
      // The new digit starts where it stood before the turn: one row lower after a turn up.
      rollFrom = -step * ROW;
      rolled = step == 0 ? ROLL_SECONDS : 0;
    }

    @Override
    public void act(float delta) {
      super.act(delta);
      rolled = Math.min(ROLL_SECONDS, rolled + delta);
      if (rolled >= ROLL_SECONDS && digit != target) {
        int up = Math.floorMod(target - digit, 10);
        int step = up <= 5 ? 1 : -1;
        roll(Math.floorMod(digit + step, 10), step);
      }
      float offset = rollFrom * (1 - Interpolation.pow2Out.apply(rolled / ROLL_SECONDS));
      float middle = 1.5f * ROW;
      for (int i = 0; i < slots.length; i++) {
        // Higher digits stand lower on the wheel.
        float center = middle - (i - 2) * ROW + offset;
        slots[i].setPosition(0, center - ROW / 2);
        float distance = Math.abs(center - middle) / ROW;
        float alpha = distance <= 1 ? 1 - 0.7f * distance : Math.max(0, 0.3f * (2 - distance));
        slots[i].getColor().a = alpha;
      }
    }
  }
}
