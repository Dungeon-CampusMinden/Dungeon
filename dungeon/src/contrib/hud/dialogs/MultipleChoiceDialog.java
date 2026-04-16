package contrib.hud.dialogs;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import core.Game;
import core.network.messages.c2s.DialogResponseMessage;
import core.utils.BaseContainerUI;
import core.utils.FontHelper;
import core.utils.FontSpec;
import core.utils.Scene2dElementFactory;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.List;

/**
 * Package-private builder for multiple choice dialogs.
 *
 * <p>Creates a dialog with a header box (title, question, optional description) and a vertical list
 * of individually styled selection boxes below it. Supports keyboard navigation (W/Up, S/Down) and
 * mouse hover for selection, and Enter/click to confirm.
 */
final class MultipleChoiceDialog {

  private static final String TITLE_DEFAULT = "";
  private static final String CANCEL_LABEL = "Abbrechen";
  private static final float MAIN_BOX_PAD = 20;
  private static final float OPTION_WIDTH = 420;
  private static final float OPTION_PAD = 16;
  private static final float ICON_SIZE = 24;
  private static final float GAP = 4;

  private static final FontSpec OPTION_FONT_NORMAL =
      FontSpec.of("fonts/Roboto-SemiBold.ttf", 18, Color.BLACK);
  private static final FontSpec DESCRIPTION_FONT =
      OPTION_FONT_NORMAL.withColor(Color.GRAY).withSize(16);
  private static final FontSpec OPTION_FONT_SELECTED = OPTION_FONT_NORMAL.withColor(Color.WHITE);

  private static final float SLIDE_DISTANCE = 60;
  private static final float SLIDE_DURATION = 0.3f;
  private static final float SLIDE_STAGGER = 0.05f;

  private MultipleChoiceDialog() {}

  /**
   * Builds a multiple choice dialog from the given context.
   *
   * <p>On headless servers, returns a {@link HeadlessDialogGroup} placeholder.
   *
   * @param ctx The dialog context.
   * @return The created dialog Group or HeadlessDialogGroup.
   */
  @SuppressWarnings("unchecked")
  static Group build(DialogContext ctx) {
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse(TITLE_DEFAULT);
    String message = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String description = ctx.find(DialogContextKeys.DESCRIPTION, String.class).orElse(null);
    List<ChoiceOption> options =
        (List<ChoiceOption>) ctx.require(DialogContextKeys.OPTIONS, ArrayList.class);
    boolean canCancel = ctx.find(DialogContextKeys.CAN_CANCEL, Boolean.class).orElse(false);

    if (Game.isHeadless()) {
      List<String> allButtons = new ArrayList<>();
      for (ChoiceOption opt : options) allButtons.add(opt.label());
      if (canCancel) allButtons.add(CANCEL_LABEL);
      return new HeadlessDialogGroup(title, message, allButtons.toArray(new String[0]));
    }

    return buildDialog(title, message, description, options, canCancel, ctx);
  }

  /**
   * Builds the Scene2D UI with a header box and individual option rows.
   *
   * @param title The dialog window title.
   * @param message The question/prompt text.
   * @param description Optional description text (may be null).
   * @param options The list of selectable options.
   * @param canCancel Whether to append a cancel option.
   * @param ctx The dialog context.
   * @return The configured UI wrapped in a BaseContainerUI.
   */
  private static Group buildDialog(
      String title,
      String message,
      String description,
      List<ChoiceOption> options,
      boolean canCancel,
      DialogContext ctx) {

    Skin skin = UIUtils.defaultSkin();

    // Build the full list of entries (options + optional cancel)
    List<ChoiceOption> allEntries = new ArrayList<>(options);
    if (canCancel) {
      allEntries.add(ChoiceOption.of(CANCEL_LABEL));
    }
    int cancelIndex = canCancel ? allEntries.size() - 1 : -1;

    Drawable bgNormal = paddedDrawable(skin.getDrawable("generic-area"), OPTION_PAD);
    Drawable bgSelected = paddedDrawable(skin.getDrawable("blue_square_depth_flat"), OPTION_PAD);
    String headerDrawableName =
        title.isBlank() ? "window_background_big" : "window_background_big_blue";
    Drawable headerBg = paddedDrawable(skin.getDrawable(headerDrawableName), MAIN_BOX_PAD);

    // Root vertical table
    Table root = new Table();
    root.defaults().maxWidth(OPTION_WIDTH);

    // Header box
    Table header = new Table();
    header.setBackground(headerBg);

    if (!title.isBlank()) {
      headerBg.setTopHeight(12);
      Label titleLabel =
          Scene2dElementFactory.createLabel(title, DialogDesign.DIALOG_FONT_SPEC_TITLE);
      titleLabel.setAlignment(Align.center);
      header.add(titleLabel).growX().padBottom(18).row();
    }

    Label questionLabel =
        Scene2dElementFactory.createLabel(message, DialogDesign.DIALOG_FONT_SPEC_NORMAL);
    questionLabel.setWrap(true);
    questionLabel.setAlignment(Align.left);
    header.add(questionLabel).growX().padBottom(description != null ? 4 : 0).row();

    if (description != null && !description.isBlank()) {
      Label descLabel = Scene2dElementFactory.createLabel(description, DESCRIPTION_FONT);
      descLabel.setWrap(true);
      descLabel.setAlignment(Align.left);
      header.add(descLabel).growX().row();
    }

    root.add(header).width(OPTION_WIDTH).padBottom(GAP).row();

    // Option rows
    final int[] selectedIndex = {-1};
    final List<Table> rowTables = new ArrayList<>();
    final List<Label> rowLabels = new ArrayList<>();

    for (int i = 0; i < allEntries.size(); i++) {
      ChoiceOption entry = allEntries.get(i);

      Table row = new SlideTable();
      row.setBackground(bgNormal);
      row.setTouchable(Touchable.enabled);

      // Optional icon (resolve as skin drawable first, then as texture file path)
      if (entry.iconPath() != null) {
        Image icon = resolveIcon(entry.iconPath(), skin);
        row.add(icon).size(ICON_SIZE).padRight(8);
      }

      Label label = Scene2dElementFactory.createLabel(entry.label(), OPTION_FONT_NORMAL);
      label.setAlignment(Align.left);

      // Compute the icon width contribution for max label width calculation
      float iconSpace = (entry.iconPath() != null) ? ICON_SIZE + 8 : 0;
      float maxLabelWidth = OPTION_WIDTH - OPTION_PAD * 2 - iconSpace;
      float naturalLabelWidth = label.getPrefWidth();

      if (naturalLabelWidth > maxLabelWidth) {
        // Text exceeds max width - enable wrapping and constrain
        label.setWrap(true);
        row.add(label).width(maxLabelWidth).left();
      } else {
        row.add(label).left();
      }

      rowTables.add(row);
      rowLabels.add(label);

      final int idx = i;

      // Click listener - confirm immediately; hover to select; exit to clear
      row.addListener(
          new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
              confirmSelection(idx, allEntries, cancelIndex, ctx);
              return true;
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
              if (pointer == -1) {
                updateSelection(idx, selectedIndex, rowTables, rowLabels, bgNormal, bgSelected);
              }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
              if (pointer == -1 && (toActor == null || !toActor.isDescendantOf(row))) {
                updateSelection(-1, selectedIndex, rowTables, rowLabels, bgNormal, bgSelected);
              }
            }
          });

      root.add(row)
          .maxWidth(OPTION_WIDTH)
          .left()
          .padBottom(i < allEntries.size() - 1 ? GAP : 0)
          .row();
    }

    // Keyboard listener on root
    root.addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            int total = allEntries.size();
            if (total == 0) return false;

            int current = selectedIndex[0];
            int next = current;
            if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
              next = current <= 0 ? total - 1 : current - 1;
            } else if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
              next = current >= total - 1 ? 0 : current + 1;
            } else if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER) {
              if (current >= 0) {
                confirmSelection(current, allEntries, cancelIndex, ctx);
              }
              return true;
            } else {
              return false;
            }

            updateSelection(next, selectedIndex, rowTables, rowLabels, bgNormal, bgSelected);
            return true;
          }
        });

    root.setTouchable(Touchable.enabled);

    root.pack();

    // Slide-in animation for option rows using draw-time offset to avoid Table layout conflicts
    for (int i = 0; i < rowTables.size(); i++) {
      SlideTable row = (SlideTable) rowTables.get(i);
      float delay = i * SLIDE_STAGGER;
      row.slideOffsetX = SLIDE_DISTANCE;
      row.getColor().a = 0f;
      row.addAction(
          Actions.sequence(
              Actions.delay(delay),
              Actions.parallel(
                  Actions.fadeIn(SLIDE_DURATION, Interpolation.fastSlow),
                  new TemporalAction(SLIDE_DURATION, Interpolation.fastSlow) {
                    @Override
                    protected void update(float percent) {
                      row.slideOffsetX = SLIDE_DISTANCE * (1f - percent);
                    }
                  })));
    }

    // Grab keyboard focus on every frame the stage exists (ensures it works after mouse
    // interaction)
    root.addAction(
        new Action() {
          @Override
          public boolean act(float delta) {
            Stage stage = root.getStage();
            if (stage != null) {
              stage.setKeyboardFocus(root);
            }
            return false; // keep running every frame
          }
        });

    return new BaseContainerUI(root);
  }

  /**
   * Updates the visual selection state to highlight the given index. Pass -1 to clear selection.
   *
   * @param newIndex The index to select, or -1 to clear.
   * @param selectedIndex Mutable array holding current selection.
   * @param rowTables All row Table actors.
   * @param rowLabels All row Label actors.
   * @param bgNormal Normal background drawable.
   * @param bgSelected Selected background drawable.
   */
  private static void updateSelection(
      int newIndex,
      int[] selectedIndex,
      List<Table> rowTables,
      List<Label> rowLabels,
      Drawable bgNormal,
      Drawable bgSelected) {

    // Deselect old
    if (selectedIndex[0] >= 0 && selectedIndex[0] < rowTables.size()) {
      int old = selectedIndex[0];
      rowTables.get(old).setBackground(bgNormal);
      applyFont(rowLabels.get(old), OPTION_FONT_NORMAL);
    }

    // Select new
    selectedIndex[0] = newIndex;
    if (newIndex >= 0 && newIndex < rowTables.size()) {
      rowTables.get(newIndex).setBackground(bgSelected);
      applyFont(rowLabels.get(newIndex), OPTION_FONT_SELECTED);
    }
  }

  /**
   * Resolves an icon path to an {@link Image}. Tries the skin first; if the drawable is not found,
   * attempts to load it as a texture file path.
   *
   * @param iconPath The skin drawable name or texture file path.
   * @param skin The UI skin.
   * @return An Image actor, or {@code null} if the icon could not be resolved.
   */
  private static Image resolveIcon(String iconPath, Skin skin) {
    try {
      return new Image(skin.getDrawable(iconPath));
    } catch (com.badlogic.gdx.utils.GdxRuntimeException ignored) {
      // Not a skin drawable — try as texture file path via TextureMap
    }
    Texture tex = TextureMap.instance().textureAt(new SimpleIPath(iconPath));
    return new Image(tex);
  }

  /**
   * Creates a copy of a drawable with custom uniform padding. This ensures that when the drawable
   * is used as a {@link Table} background, the table's padding comes from our override rather than
   * the original drawable's built-in nine-patch padding.
   *
   * @param source The original drawable.
   * @param pad The uniform padding to apply on all sides.
   * @return A new drawable with the specified padding.
   */
  private static Drawable paddedDrawable(Drawable source, float pad) {
    Drawable copy;
    if (source instanceof NinePatchDrawable npd) {
      copy = new NinePatchDrawable(npd);
    } else if (source instanceof TextureRegionDrawable trd) {
      copy = new TextureRegionDrawable(trd);
    } else {
      // Fallback: mutate the source directly (safe if skin drawables aren't reused elsewhere)
      copy = source;
    }
    copy.setLeftWidth(pad);
    copy.setRightWidth(pad);
    copy.setTopHeight(pad);
    copy.setBottomHeight(pad);
    return copy;
  }

  /**
   * Applies a FontSpec to a Label by replacing its style.
   *
   * @param label The label to update.
   * @param spec The font specification to apply.
   */
  private static void applyFont(Label label, FontSpec spec) {
    Label.LabelStyle style = new Label.LabelStyle(label.getStyle());
    style.font = FontHelper.getFont(spec);
    style.fontColor = spec.color();
    label.setStyle(style);
  }

  /**
   * Confirms the selection at the given index, invoking the appropriate callback.
   *
   * @param index The selected index.
   * @param entries All choice entries.
   * @param cancelIndex The cancel entry index, or -1.
   * @param ctx The dialog context.
   */
  private static void confirmSelection(
      int index, List<ChoiceOption> entries, int cancelIndex, DialogContext ctx) {
    if (index == cancelIndex) {
      DialogCallbackResolver.createButtonCallback(ctx.dialogId(), DialogContextKeys.ON_CANCEL)
          .accept(null);
    } else {
      DialogCallbackResolver.createButtonCallback(
              ctx.dialogId(), DialogContextKeys.ON_OPTION_SELECTED)
          .accept(new DialogResponseMessage.StringValue(entries.get(index).label()));
    }
  }

  /**
   * A Table subclass that supports a horizontal draw-time offset for slide-in animations. This
   * avoids conflicts with the parent Table's layout, which resets child positions each frame.
   */
  private static class SlideTable extends Table {
    /** Horizontal draw offset in pixels. Positive values shift the row to the right. */
    float slideOffsetX;

    @Override
    public void draw(Batch batch, float parentAlpha) {
      float origX = getX();
      setX(origX + slideOffsetX);
      super.draw(batch, parentAlpha);
      setX(origX);
    }
  }
}
