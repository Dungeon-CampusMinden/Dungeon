package engine.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import engine.language.Translation;
import engine.network.messages.s2c.CharacterOptions;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import engine.utils.components.draw.animation.Animation;
import engine.utils.logging.DungeonLogger;
import feature.entities.CharacterClass;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** A localized character carousel shared by the main menu and the standalone single-player flow. */
final class CharacterCarousel extends Table {

  static final float AVAILABILITY_REFRESH_SECONDS = 10f;

  private static final String TITLE_FONT = "fonts/Roboto-Bold.ttf";
  private static final Map<CharacterClass, TextureRegion> PREVIEWS = new HashMap<>();
  private static final Translation trans = new Translation("main_menu");
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(CharacterCarousel.class);

  private final Image previousPreview = new Image();
  private final Image selectedPreview = new Image();
  private final Image nextPreview = new Image();
  private final Label characterName;
  private final Label availability;
  private final ImageButton previousButton;
  private final ImageButton nextButton;

  private List<CharacterOptions.Option> options = List.of();
  private int selectedIndex;
  private Runnable selectionChanged = () -> {};

  CharacterCarousel(Skin skin) {
    super(skin);
    characterName = label("", 28, Color.BLACK);
    characterName.setAlignment(Align.center);
    availability = label("", 18, Color.GRAY);
    availability.setWrap(true);
    availability.setAlignment(Align.center);
    previousButton = navigationButton(skin, true);
    nextButton = navigationButton(skin, false);
    previousButton.addListener(
        new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
          @Override
          public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
            move(-1);
          }
        });
    nextButton.addListener(
        new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
          @Override
          public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
            move(1);
          }
        });

    Table previews = new Table();
    previews.add(previousButton).size(48, 48).padRight(10);
    previews.add(previousPreview).size(54, 80).padRight(16);
    previews.add(selectedPreview).size(96, 144).padRight(16);
    previews.add(nextPreview).size(54, 80).padRight(10);
    previews.add(nextButton).size(48, 48);

    add(characterName).width(440).padBottom(6).row();
    add(previews).padBottom(8).row();
    add(availability).width(440).minHeight(24);
    refresh();
  }

  void setOptions(List<CharacterOptions.Option> newOptions, CharacterClass preferred) {
    options = List.copyOf(newOptions);
    selectedIndex = findPreferredIndex(preferred);
    refresh();
  }

  void onSelectionChanged(Runnable listener) {
    selectionChanged = listener == null ? () -> {} : listener;
  }

  Optional<CharacterOptions.Option> selectedOption() {
    if (options.isEmpty() || selectedIndex < 0 || selectedIndex >= options.size()) {
      return Optional.empty();
    }
    return Optional.of(options.get(selectedIndex));
  }

  private int findPreferredIndex(CharacterClass preferred) {
    for (int index = 0; index < options.size(); index++) {
      if (options.get(index).assigned()) {
        return index;
      }
    }
    if (preferred != null) {
      for (int index = 0; index < options.size(); index++) {
        CharacterOptions.Option option = options.get(index);
        if (option.characterClass() == preferred) {
          return index;
        }
      }
    }
    for (int index = 0; index < options.size(); index++) {
      if (options.get(index).selectable()) {
        return index;
      }
    }
    return options.isEmpty() ? -1 : 0;
  }

  private void move(int amount) {
    if (options.size() < 2) {
      return;
    }
    selectedIndex = Math.floorMod(selectedIndex + amount, options.size());
    refresh();
    selectionChanged.run();
  }

  private void refresh() {
    if (options.isEmpty()) {
      characterName.setText(trans.text("no_characters"));
      availability.setText(trans.text("no_characters"));
      previousPreview.setDrawable(null);
      selectedPreview.setDrawable(null);
      nextPreview.setDrawable(null);
      previousButton.setDisabled(true);
      nextButton.setDisabled(true);
      return;
    }

    previousButton.setDisabled(options.size() < 2);
    nextButton.setDisabled(options.size() < 2);
    int previousIndex = Math.floorMod(selectedIndex - 1, options.size());
    int nextIndex = Math.floorMod(selectedIndex + 1, options.size());
    CharacterOptions.Option selected = options.get(selectedIndex);
    if (options.size() > 1) {
      updatePreview(previousPreview, options.get(previousIndex), false, 54, 80);
      updatePreview(nextPreview, options.get(nextIndex), false, 54, 80);
    } else {
      previousPreview.setDrawable(null);
      nextPreview.setDrawable(null);
    }
    updatePreview(selectedPreview, selected, true, 96, 144);

    characterName.setText(trans.text("characters." + key(selected.characterClass())));
    availability.setText(
        selected.selectable()
            ? ""
            : selected.maxCount() >= 0 && selected.playerCount() >= selected.maxCount()
                ? trans.text("character_count_limited", selected.playerCount(), selected.maxCount())
                : trans.text("character_forbidden"));
  }

  private static ImageButton navigationButton(Skin skin, boolean left) {
    TextureRegion arrow = new TextureRegion(skin.getRegion("arrow_basic_e_small"));
    arrow.flip(left, false);
    TextureRegionDrawable icon = new TextureRegionDrawable(arrow);
    TextButton.TextButtonStyle background =
        skin.get("blue-outline", TextButton.TextButtonStyle.class);
    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
    style.up = background.up;
    style.down = background.down;
    style.over = background.over;
    style.disabled = background.disabled;
    style.imageUp = icon.tint(Color.BLACK);
    style.imageOver = icon.tint(Color.WHITE);
    style.imageDown = icon.tint(Color.WHITE);
    style.imageDisabled = icon.tint(Color.GRAY);
    ImageButton button = new ImageButton(style);
    button.getImageCell().size(24);
    return button;
  }

  private void updatePreview(
      Image image, CharacterOptions.Option option, boolean selected, float width, float height) {
    TextureRegion region = preview(option.characterClass());
    image.setDrawable(region == null ? null : new TextureRegionDrawable(region));
    image.setSize(width, height);
    image.setScaling(Scaling.fit);
    if (!option.selectable()) {
      image.setColor(new Color(0.42f, 0.42f, 0.42f, selected ? 0.9f : 0.6f));
    } else {
      image.setColor(new Color(1f, 1f, 1f, selected ? 1f : 0.58f));
    }
  }

  private static TextureRegion preview(CharacterClass characterClass) {
    if (PREVIEWS.containsKey(characterClass)) {
      return PREVIEWS.get(characterClass);
    }
    TextureRegion result = null;
    try {
      Map<String, Animation> animations =
          Animation.loadAnimationSpritesheet(characterClass.textures());
      Animation idle =
          animations.getOrDefault(
              "idle_down", animations.values().stream().findFirst().orElse(null));
      if (idle != null) {
        result = new TextureRegion(idle.getSprite());
      }
    } catch (RuntimeException e) {
      LOGGER.warn(
          "Could not load character preview asset '{}': {}",
          characterClass.textures().pathString(),
          e.getMessage());
    }
    PREVIEWS.put(characterClass, result);
    return result;
  }

  private Label label(String text, int size, Color color) {
    return Scene2dElementFactory.createLabel(text, FontSpec.of(TITLE_FONT, size, color));
  }

  private static String key(CharacterClass characterClass) {
    return characterClass.name().toLowerCase(java.util.Locale.ROOT);
  }
}
