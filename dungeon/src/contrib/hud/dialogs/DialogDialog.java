package contrib.hud.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import contrib.configuration.KeyboardConfig;
import contrib.hud.UIUtils;
import contrib.hud.elements.RichLabel;
import core.Game;
import core.utils.BaseContainerUI;
import core.utils.FontSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Package-private builder for a sequenced speaker dialogue ("DialogDialog").
 *
 * <p>Displays a series of {@link DialogEntry} items: the current speaker's portrait and name on the
 * left, the spoken text (rendered through a {@link RichLabel} with optional typewriter mode) on the
 * right.
 *
 * <p>User interaction:
 *
 * <ul>
 *   <li>Any mouse click anywhere on the dialog or pressing the configured interact key (see {@link
 *       contrib.configuration.KeyboardConfig#INTERACT_WORLD}) advances the dialog.
 *   <li>If the typewriter is still revealing text, advancing skips to the end of the current
 *       entry's text.
 *   <li>Otherwise, the next {@link DialogEntry} is shown.
 *   <li>After the last entry has been confirmed, the {@link DialogContextKeys#ON_CONFIRM} callback
 *       is fired.
 * </ul>
 *
 * <p>Use {@link DialogFactory#showDialogDialog} instead of accessing this class directly.
 */
final class DialogDialog {

  private static final FontSpec NAME_FONT_SPEC =
      FontSpec.of("fonts/Roboto-SemiBold.ttf", 18, Color.BLACK);
  private static final FontSpec TEXT_FONT_SPEC = DialogDesign.DIALOG_FONT_SPEC_NORMAL;

  private static final float IMAGE_SIZE = 128f;
  private static final float COLUMN_GAP = 16f;
  private static final float TEXT_WIDTH = 380f;
  private static final float MIN_CONTENT_HEIGHT = 160f;
  private static final float NAME_PAD_TOP = 6f;

  /** Distance in pixels from the top edge of the stage to the top of the dialog. */
  private static final float TOP_OFFSET = 100;

  private DialogDialog() {}

  /**
   * Builds a DialogDialog from the given context.
   *
   * <p>On headless servers, returns a {@link HeadlessDialogGroup} placeholder containing all
   * speaker lines concatenated (one per line) so the server can still log/forward the payload.
   *
   * @param ctx The dialog context. Requires {@link DialogContextKeys#ENTRIES} as a non-empty {@code
   *     ArrayList<DialogEntry>}.
   * @return A fully configured DialogDialog or HeadlessDialogGroup.
   */
  @SuppressWarnings("unchecked")
  static Group build(DialogContext ctx) {
    List<DialogEntry> rawEntries =
        (List<DialogEntry>) ctx.require(DialogContextKeys.ENTRIES, ArrayList.class);
    if (rawEntries.isEmpty()) {
      throw new DialogCreationException("DialogDialog requires at least one DialogEntry");
    }
    List<DialogEntry> entries = expandPageBreaks(rawEntries);

    if (Game.isHeadless()) {
      StringBuilder combined = new StringBuilder();
      for (DialogEntry e : entries) {
        if (!combined.isEmpty()) combined.append('\n');
        combined.append(e.speakerName()).append(": ").append(e.text());
      }
      return new HeadlessDialogGroup("", combined.toString());
    }

    return create(ctx, entries);
  }

  private static Group create(DialogContext ctx, List<DialogEntry> entries) {
    Skin skin = UIUtils.defaultSkin();

    HandledDialog dialog =
        new HandledDialog("", skin, (d, id) -> true); // no buttons; advance via input listeners
    DialogDesign.setDialogDefaults(dialog, "");

    Table content = dialog.getContentTable();

    // -- Left column: image + name label --
    Image speakerImage = new Image();
    speakerImage.setScaling(Scaling.fit);
    speakerImage.setAlign(Align.center);

    RichLabel nameLabel =
        new RichLabel(
            RichLabel.toRichText(entries.getFirst().speakerName()), NAME_FONT_SPEC, false);

    Table leftColumn = new Table();
    leftColumn.add(speakerImage).size(IMAGE_SIZE).row();
    leftColumn.add(nameLabel).center().padTop(NAME_PAD_TOP);
    leftColumn.pack();

    // Anchor the left column at the top with a padding such that, when the row is exactly at
    // MIN_CONTENT_HEIGHT, the image block is centered vertically. Once the right column grows
    // larger than MIN_CONTENT_HEIGHT, the image stays at this same Y offset (does not move).
    float leftBlockHeight = leftColumn.getPrefHeight();
    float anchorPadTop = Math.max(0f, (MIN_CONTENT_HEIGHT - leftBlockHeight) / 2f);

    // -- Right column: the spoken text (RichLabel, with potential typewriter) --
    RichLabel textLabel =
        new RichLabel(RichLabel.toRichText(entries.getFirst().text()), TEXT_FONT_SPEC);
    textLabel.setWrap(true);

    content.add(leftColumn).top().padTop(anchorPadTop).padRight(COLUMN_GAP);
    content.add(textLabel).width(TEXT_WIDTH).minHeight(MIN_CONTENT_HEIGHT).center();
    content.row();

    // -- Texture cache (avoid recreating Texture every entry change) --
    Map<String, Texture> textureCache = new HashMap<>();
    applyEntry(entries.getFirst(), speakerImage, nameLabel, textLabel, textureCache);

    final int[] currentIndex = {0};

    Runnable advance =
        () -> {
          if (!textLabel.isTypewriterFinished()) {
            textLabel.skipTypewriter();
            return;
          }
          int next = currentIndex[0] + 1;
          if (next >= entries.size()) {
            // Sequence complete - fire the final callback.
            DialogCallbackResolver.createButtonCallback(
                    ctx.dialogId(), DialogContextKeys.ON_CONFIRM)
                .accept(null);
            return;
          }
          currentIndex[0] = next;
          applyEntry(entries.get(next), speakerImage, nameLabel, textLabel, textureCache);
          // Recompute anchor in case the new image / name layout changes the left block height.
          leftColumn.pack();
          float newAnchor = Math.max(0f, (MIN_CONTENT_HEIGHT - leftColumn.getPrefHeight()) / 2f);
          content.getCells().get(0).padTop(newAnchor);
          dialog.pack();
        };

    dialog.setTouchable(Touchable.enabled);
    dialog.addCaptureListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            advance.run();
            event.stop();
            return true;
          }
        });

    // Key listener on the dialog itself, only the configured interact key advances.
    dialog.addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            if (keycode != KeyboardConfig.INTERACT_WORLD.value()) {
              return false;
            }
            advance.run();
            return true;
          }
        });

    // Continuously claim keyboard focus so key input keeps reaching us even after mouse activity.
    dialog.addAction(
        new Action() {
          @Override
          public boolean act(float delta) {
            Stage stage = dialog.getStage();
            if (stage != null) {
              stage.setKeyboardFocus(dialog);
            }
            return false; // run forever
          }
        });

    // Wrap in an actor that disposes textures on stage removal.
    dialog.pack();
    return new BaseContainerUI(dialog, Align.top, 0f, TOP_OFFSET, false, true) {
      @Override
      protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage == null) {
          for (Texture t : textureCache.values()) {
            t.dispose();
          }
          textureCache.clear();
        }
      }
    };
  }

  /** Tag used inside {@link DialogEntry#text()} to split a single entry into multiple pages. */
  private static final java.util.regex.Pattern PAGE_BREAK_PATTERN =
      java.util.regex.Pattern.compile("\\s*\\[p]\\s*");

  /**
   * Expands the {@code [p]} page-break tag: any entry whose text contains {@code [p]} is replaced
   * by N entries, one per segment, all sharing the original speaker name and image. Empty segments
   * are skipped. Entries without {@code [p]} are returned unchanged.
   */
  private static List<DialogEntry> expandPageBreaks(List<DialogEntry> entries) {
    List<DialogEntry> out = new ArrayList<>(entries.size());
    for (DialogEntry e : entries) {
      String text = e.text();
      if (text == null || !text.contains("[p]")) {
        out.add(e);
        continue;
      }
      String[] parts = PAGE_BREAK_PATTERN.split(text, -1);
      for (String part : parts) {
        if (part.isEmpty()) continue;
        out.add(DialogEntry.of(e.speakerName(), e.imagePath(), part));
      }
    }
    return out;
  }

  /**
   * Updates the visible UI to reflect the given entry: swaps the speaker image, updates the speaker
   * name label, and resets the text label (which restarts any typewriter effect).
   */
  private static void applyEntry(
      DialogEntry entry,
      Image speakerImage,
      RichLabel nameLabel,
      RichLabel textLabel,
      Map<String, Texture> cache) {
    Texture tex = cache.computeIfAbsent(entry.imagePath(), p -> new Texture(Gdx.files.internal(p)));
    speakerImage.setDrawable(new TextureRegionDrawable(tex));
    nameLabel.setText(RichLabel.toRichText(entry.speakerName()));
    textLabel.setText(RichLabel.toRichText(entry.text()));
  }
}
