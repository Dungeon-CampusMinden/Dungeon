package contrib.hud.dialogs;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.configuration.KeyboardConfig;
import contrib.hud.UIUtils;
import contrib.hud.elements.RichLabel;
import core.Game;
import core.language.Translation;
import core.network.messages.c2s.DialogResponseMessage;
import core.utils.BaseContainerUI;
import core.utils.FontSpec;
import java.util.ArrayList;
import java.util.List;

/**
 * Package-private builder for multiple choice dialogs.
 *
 * <p>Renders a {@link DialogScriptView} (parsed from a single dialog script string via {@link
 * DialogScript}) as the question content, with an optional title above and a vertical list of
 * individually styled selection boxes that appear once the script has finished playing.
 *
 * <p>Behaviour:
 *
 * <ul>
 *   <li>While the script is still revealing pages, clicks anywhere (and the configured interact
 *       key, see {@link contrib.configuration.KeyboardConfig#INTERACT_WORLD}) advance the script
 *       view, exactly like a {@link DialogDialog}.
 *   <li>Once the last page's text has been fully revealed, the option rows slide in. After that,
 *       clicks outside the option rows are ignored (they do not confirm anything); option rows are
 *       hover-/click-selectable and confirm immediately on click.
 *   <li>Keyboard W/S (or Up/Down) navigates between options; INTERACT_WORLD confirms while choices
 *       are active, otherwise it advances the script.
 * </ul>
 */
final class MultipleChoiceDialog {

  private static final String TITLE_DEFAULT = "";
  private static final String T_CANCEL = "cancel";
  private static final Translation trans = new Translation("dialog.multiple_choice_dialog");
  private static final float MAIN_BOX_PAD = 20;
  private static final float OPTION_WIDTH = DialogScriptView.FULL_TEXT_WIDTH + MAIN_BOX_PAD * 2;
  private static final float OPTION_PAD = 16;
  private static final float GAP = 4;

  private static final FontSpec OPTION_FONT_NORMAL =
      FontSpec.of("fonts/Roboto-SemiBold.ttf", 18, Color.BLACK);
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
  static Group build(DialogContext ctx) {
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse(TITLE_DEFAULT);
    String script = ctx.require(DialogContextKeys.DIALOG, String.class);
    if (script.isBlank()) {
      throw new DialogCreationException("MultipleChoiceDialog requires a non-blank dialog script");
    }
    ChoiceOptions choiceOptions = ctx.require(DialogContextKeys.OPTIONS, ChoiceOptions.class);
    List<ChoiceOption> options = choiceOptions.values();
    boolean canCancel = ctx.find(DialogContextKeys.CAN_CANCEL, Boolean.class).orElse(false);
    String cancelLabel = trans.text(T_CANCEL);

    if (Game.isHeadless()) {
      List<DialogEntry> entries =
          DialogScript.parseNonEmpty(script, () -> "MultipleChoiceDialog script produced no pages");
      List<String> allButtons = new ArrayList<>();
      for (ChoiceOption opt : options) allButtons.add(opt.label());
      if (canCancel) allButtons.add(cancelLabel);
      return new HeadlessDialogGroup(
          title, DialogScript.toHeadlessText(entries), allButtons.toArray(new String[0]));
    }

    return buildDialog(title, script, options, canCancel, cancelLabel, ctx);
  }

  /**
   * Builds the Scene2D UI: header (optional title + script view) + option rows.
   *
   * @param title The dialog window title (may be blank for no title).
   * @param script Dialog script string consumed by {@link DialogScriptView}.
   * @param options The list of selectable options.
   * @param canCancel Whether to append a cancel option.
   * @param cancelLabel The label for the cancel option (ignored if canCancel is false).
   * @param ctx The dialog context.
   * @return The configured UI wrapped in a BaseContainerUI.
   */
  private static Group buildDialog(
      String title,
      String script,
      List<ChoiceOption> options,
      boolean canCancel,
      String cancelLabel,
      DialogContext ctx) {

    Skin skin = UIUtils.defaultSkin();

    // Build the full list of entries (options + optional cancel)
    List<ChoiceOption> allEntries = new ArrayList<>(options);
    if (canCancel) {
      allEntries.add(ChoiceOption.of(cancelLabel));
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

    // Header box: optional title + the script view
    Table header = new Table();
    header.setBackground(headerBg);

    if (!title.isBlank()) {
      headerBg.setTopHeight(12);
      RichLabel titleLabel = new RichLabel(title, DialogDesign.DIALOG_FONT_SPEC_TITLE, true);
      header.add(titleLabel).center().padBottom(18).row();
    }

    DialogScriptView scriptView = new DialogScriptView(script);
    // Keep MCD's header content slightly inset when a speaker column is present.
    scriptView.setSpeakerColumnPadLeft(10f);
    header.add(scriptView).row();

    root.add(header).padBottom(GAP).row();

    // Option rows
    final int[] selectedIndex = {-1};
    final List<Table> rowTables = new ArrayList<>();
    final List<RichLabel> rowLabels = new ArrayList<>();
    final boolean[] choicesActive = {false};

    for (int i = 0; i < allEntries.size(); i++) {
      ChoiceOption entry = allEntries.get(i);

      Table row = new SlideTable();
      row.setBackground(bgNormal);
      // Disabled until choices become active so clicks during script playback don't confirm.
      row.setTouchable(Touchable.disabled);

      RichLabel label = new RichLabel(entry.label(), OPTION_FONT_NORMAL);

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

      // Click listener: confirm immediately; hover to select; exit to clear
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

    // Click on the script view (or anywhere on the root that isn't an option row) advances the
    // script while it is still playing. Once choices are active, these listeners become no-ops;
    // option rows handle their own clicks via their per-row listeners.
    InputListener advanceListener =
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (choicesActive[0]) return false;
            scriptView.advance();
            return true;
          }
        };
    scriptView.addListener(advanceListener);
    root.addListener(advanceListener);

    // Keyboard listener on root
    root.addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            int total = allEntries.size();

            if (keycode == KeyboardConfig.INTERACT_WORLD.value()) {
              if (!choicesActive[0]) {
                scriptView.advance();
                return true;
              }
              int current = selectedIndex[0];
              if (current >= 0) {
                confirmSelection(current, allEntries, cancelIndex, ctx);
              }
              return true;
            }

            if (!choicesActive[0] || total == 0) return false;

            int current = selectedIndex[0];
            int next = current;
            if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
              next = current <= 0 ? total - 1 : current - 1;
            } else if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
              next = current >= total - 1 ? 0 : current + 1;
            } else {
              return false;
            }

            updateSelection(next, selectedIndex, rowTables, rowLabels, bgNormal, bgSelected);
            return true;
          }
        });

    root.setTouchable(Touchable.enabled);

    // Hide all option rows initially; they slide in once the script's last page is revealed.
    for (Table row : rowTables) {
      row.getColor().a = 0f;
    }

    scriptView.setOnLastPageRevealed(
        () -> {
          choicesActive[0] = true;
          for (Table row : rowTables) {
            row.setTouchable(Touchable.enabled);
          }
          startOptionSlideIn(rowTables);
        });

    root.pack();

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

    return new BaseContainerUI(root) {
      @Override
      protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage == null) {
          scriptView.disposeCache();
        }
      }
    };
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
          .accept(new DialogResponseMessage.StringValue(entries.get(index).value()));
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
