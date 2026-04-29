package contrib.hud.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import contrib.configuration.KeyboardConfig;
import contrib.hud.UIUtils;
import contrib.hud.elements.RichLabel;
import contrib.hud.elements.richlabel.TagParams;
import core.Game;
import core.utils.BaseContainerUI;
import core.utils.FontSpec;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

  /** Default speaker portrait used when none is supplied by the script. */
  static final String DEFAULT_SPEAKER_IMAGE = "other/unknown.png";

  /** Default speaker name used when none is supplied by the script. */
  static final String DEFAULT_SPEAKER_NAME = "";

  /** Tag used inside the dialog script to split into multiple pages. */
  private static final Pattern PAGE_BREAK_PATTERN = Pattern.compile("\\s*\\[p]\\s*");

  private DialogDialog() {}

  /**
   * Builds a DialogDialog from the given context.
   *
   * <p>On headless servers, returns a {@link HeadlessDialogGroup} placeholder containing all
   * speaker lines concatenated (one per line) so the server can still log/forward the payload.
   *
   * @param ctx The dialog context. Requires {@link DialogContextKeys#DIALOG} as a non-blank
   *     {@link String} script.
   * @return A fully configured DialogDialog or HeadlessDialogGroup.
   */
  static Group build(DialogContext ctx) {
    String script = ctx.require(DialogContextKeys.DIALOG, String.class);
    if (script.isBlank()) {
      throw new DialogCreationException("DialogDialog requires a non-blank dialog script");
    }
    List<DialogEntry> entries = parseScript(script);
    if (entries.isEmpty()) {
      throw new DialogCreationException("DialogDialog script produced no pages");
    }

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
            RichLabel.toRichText(entries.getFirst().speakerName()), NAME_FONT_SPEC);

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
        new RichLabel(RichLabel.toRichText(entries.getFirst().text()), TEXT_FONT_SPEC, true);
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

    // Wrap in an actor that clears the local texture cache on stage removal. Textures themselves
    // are owned by the TextureMap and must not be disposed here.
    dialog.pack();
    return new BaseContainerUI(dialog, Align.top, 0f, TOP_OFFSET, false, true) {
      @Override
      protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage == null) {
          textureCache.clear();
        }
      }
    };
  }

  /** Parses the script string into dialog pages with resolved speaker metadata. */
  private static List<DialogEntry> parseScript(String script) {
    String[] parts = PAGE_BREAK_PATTERN.split(script, -1);
    List<DialogEntry> out = new ArrayList<>(parts.length);
    String currentImage = DEFAULT_SPEAKER_IMAGE;
    String currentName = DEFAULT_SPEAKER_NAME;
    for (String rawPart : parts) {
      String part = rawPart;
      SpeakerTagParse parsedSpeakerTag = parseLeadingSpeakerTag(part);
      if (parsedSpeakerTag != null) {
        // A speaker tag starts a fresh speaker context for this page: omitted fields intentionally
        // fall back to defaults instead of inheriting from previous pages.
        currentImage = DEFAULT_SPEAKER_IMAGE;
        currentName = DEFAULT_SPEAKER_NAME;
        TagParams tp = TagParams.parse(parsedSpeakerTag.params());
        String img = tp.getString("img", null);
        String name = tp.getString("name", null);
        if (img != null) currentImage = img;
        if (name != null) currentName = name;
        part = parsedSpeakerTag.remainingText();
      }
      String text = part.strip();
      if (text.isEmpty()) continue;
      out.add(DialogEntry.of(currentName, currentImage, text));
    }
    return out;
  }

  private record SpeakerTagParse(String params, String remainingText) {}

  private static SpeakerTagParse parseLeadingSpeakerTag(String page) {
    int len = page.length();
    int i = 0;
    while (i < len && Character.isWhitespace(page.charAt(i))) i++;

    if (i >= len || page.charAt(i) != '[') return null;
    if (!page.regionMatches(true, i + 1, "speaker", 0, "speaker".length())) return null;

    int afterKeyword = i + 1 + "speaker".length();
    if (afterKeyword >= len) return null;
    char next = page.charAt(afterKeyword);
    if (!(Character.isWhitespace(next) || next == ']')) return null;

    boolean inQuotes = false;
    boolean escaped = false;
    for (int pos = afterKeyword; pos < len; pos++) {
      char c = page.charAt(pos);
      if (escaped) {
        escaped = false;
        continue;
      }
      if (inQuotes && c == '\\') {
        escaped = true;
        continue;
      }
      if (c == '"') {
        inQuotes = !inQuotes;
        continue;
      }
      if (c == ']' && !inQuotes) {
        int textStart = pos + 1;
        while (textStart < len && Character.isWhitespace(page.charAt(textStart))) textStart++;
        return new SpeakerTagParse(page.substring(afterKeyword, pos), page.substring(textStart));
      }
    }

    return null;
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
    Texture tex =
        cache.computeIfAbsent(
            entry.imagePath(), p -> TextureMap.instance().textureAt(new SimpleIPath(p)));
    speakerImage.setDrawable(new TextureRegionDrawable(tex));
    nameLabel.setText(RichLabel.toRichText(entry.speakerName()));
    textLabel.setText(RichLabel.toRichText(entry.text()));
  }
}
