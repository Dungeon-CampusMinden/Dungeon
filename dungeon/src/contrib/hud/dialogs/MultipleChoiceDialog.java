package contrib.hud.dialogs;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.configuration.KeyboardConfig;
import contrib.hud.UIUtils;
import contrib.hud.elements.RichLabel;
import core.Game;
import core.network.messages.c2s.DialogResponseMessage;
import core.utils.BaseContainerUI;
import core.utils.FontSpec;
import java.util.ArrayList;
import java.util.List;

/**
 * Package-private builder for multiple choice dialogs.
 *
 * <p>Creates a dialog with a header box (title, question, optional description) and a vertical list
 * of individually styled selection boxes below it. Supports keyboard navigation (W/Up, S/Down) and
 * mouse hover for selection, and the configured interact key (see {@link
 * contrib.configuration.KeyboardConfig#INTERACT_WORLD})/click to confirm.
 */
final class MultipleChoiceDialog {

  private static final String TITLE_DEFAULT = "";
  private static final String CANCEL_LABEL = "Abbrechen";
  private static final float MAIN_BOX_PAD = 20;
  private static final float OPTION_WIDTH = 420;
  private static final float OPTION_PAD = 16;
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
      RichLabel titleLabel = new RichLabel(title, DialogDesign.DIALOG_FONT_SPEC_TITLE);
      header.add(titleLabel).center().padBottom(18).row();
    }

    float headerContentWidth = OPTION_WIDTH - MAIN_BOX_PAD * 2;

    RichLabel questionLabel = new RichLabel(message, DialogDesign.DIALOG_FONT_SPEC_NORMAL);
    questionLabel.setWrap(true);
    header
        .add(questionLabel)
        .width(headerContentWidth)
        .padBottom(description != null ? 4 : 0)
        .row();

    RichLabel descLabel = null;
    if (description != null && !description.isBlank()) {
      descLabel = new RichLabel(description, DESCRIPTION_FONT);
      descLabel.setWrap(true);
      // Pause the description's typewriter immediately so it doesn't advance while the question
      // is still being revealed (and the descLabel is hidden). Resumed in the chain callback.
      descLabel.pauseTypewriter();
      header.add(descLabel).width(headerContentWidth).row();
    }

    root.add(header).width(OPTION_WIDTH).padBottom(GAP).row();

    // Option rows
    final int[] selectedIndex = {-1};
    final List<Table> rowTables = new ArrayList<>();
    final List<RichLabel> rowLabels = new ArrayList<>();

    for (int i = 0; i < allEntries.size(); i++) {
      ChoiceOption entry = allEntries.get(i);

      Table row = new SlideTable();
      row.setBackground(bgNormal);
      row.setTouchable(Touchable.enabled);

      RichLabel label = new RichLabel(entry.label(), OPTION_FONT_NORMAL, false);

      float maxLabelWidth = OPTION_WIDTH - OPTION_PAD * 2;
      float naturalLabelWidth = label.getPrefWidth();

      if (naturalLabelWidth > maxLabelWidth) {
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
            } else if (keycode == KeyboardConfig.INTERACT_WORLD.value()) {
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

    // Determine whether the question uses typewriter mode (contains a [tr] tag)
    boolean questionHasTypewriter = !questionLabel.isTypewriterFinished();

    // If typewriter is active, hide description and options until the chain completes.
    // The chain is: question typewriter -> description typewriter -> option slide-in.
    if (questionHasTypewriter) {
      // Hide description initially
      if (descLabel != null) {
        descLabel.setVisible(false);
      }
      // Hide all option rows initially
      for (Table row : rowTables) {
        row.getColor().a = 0f;
      }

      final RichLabel finalDescLabel = descLabel;
      questionLabel.onTypewriterFinished(
          () -> {
            if (finalDescLabel != null) {
              finalDescLabel.setVisible(true);
              finalDescLabel.resumeTypewriter();
              if (!finalDescLabel.isTypewriterFinished()) {
                finalDescLabel.onTypewriterFinished(() -> startOptionSlideIn(rowTables));
              } else {
                startOptionSlideIn(rowTables);
              }
            } else {
              startOptionSlideIn(rowTables);
            }
          });
    } else {
      // No typewriter on question: slide in options immediately
      startOptionSlideIn(rowTables);
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
   * @param rowLabels All row RichLabel actors.
   * @param bgNormal Normal background drawable.
   * @param bgSelected Selected background drawable.
   */
  private static void updateSelection(
      int newIndex,
      int[] selectedIndex,
      List<Table> rowTables,
      List<RichLabel> rowLabels,
      Drawable bgNormal,
      Drawable bgSelected) {

    // Deselect old
    if (selectedIndex[0] >= 0 && selectedIndex[0] < rowTables.size()) {
      int old = selectedIndex[0];
      rowTables.get(old).setBackground(bgNormal);
      rowLabels.get(old).setFontSpec(OPTION_FONT_NORMAL);
    }

    // Select new
    selectedIndex[0] = newIndex;
    if (newIndex >= 0 && newIndex < rowTables.size()) {
      rowTables.get(newIndex).setBackground(bgSelected);
      rowLabels.get(newIndex).setFontSpec(OPTION_FONT_SELECTED);
    }
  }

  /**
   * Starts the staggered slide-in animation for all option rows.
   *
   * @param rowTables the option row tables to animate
   */
  private static void startOptionSlideIn(List<Table> rowTables) {
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
          .accept(new DialogResponseMessage.CustomPayload(entries.get(index).value()));
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
