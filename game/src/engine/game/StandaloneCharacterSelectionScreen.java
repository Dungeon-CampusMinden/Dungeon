package engine.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.language.Translation;
import engine.network.messages.s2c.CharacterOptions;
import engine.network.server.CharacterSelection;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import feature.entities.CharacterClass;
import feature.entities.HeroBuilder;
import feature.hud.UIUtils;
import java.util.List;

/** Character selection screen for standalone local single-player game startup. */
final class StandaloneCharacterSelectionScreen extends com.badlogic.gdx.ScreenAdapter {

  private static final String TITLE_FONT = "fonts/Roboto-Bold.ttf";
  private static final Translation trans = new Translation("main_menu");

  private Stage stage;
  private Skin skin;
  private CharacterCarousel carousel;
  private Label status;
  private TextButton confirm;
  private List<Entity> localHeroes = List.of();
  private float refreshElapsed;

  @Override
  public void show() {
    int width = Game.windowWidth() > 0 ? Game.windowWidth() : PreRunConfiguration.windowWidth();
    int height = Game.windowHeight() > 0 ? Game.windowHeight() : PreRunConfiguration.windowHeight();
    stage = new Stage(new ScalingViewport(Scaling.stretch, width, height), new SpriteBatch());
    skin = UIUtils.defaultSkin();

    Table content = new Table(skin);
    content.setBackground("window_background_big");
    Label title =
        Scene2dElementFactory.createLabel(
            trans.text("character_selection_title"), FontSpec.of(TITLE_FONT, 38, Color.BLACK));
    title.setAlignment(Align.center);
    carousel = new CharacterCarousel(skin);
    carousel.onSelectionChanged(this::updateConfirm);
    status =
        Scene2dElementFactory.createLabel(
            "", FontSpec.of(TITLE_FONT, 18, new Color(0.6f, 0f, 0f, 1f)));
    status.setAlignment(Align.center);
    confirm = new TextButton(trans.text("confirm"), skin, "green");
    confirm.addListener(
        new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
          @Override
          public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
            chooseCharacter();
          }
        });
    TextButton cancel = new TextButton(trans.text("cancel"), skin, "red-outline");
    cancel.addListener(
        new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
          @Override
          public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
            Game.exit("Character selection cancelled");
          }
        });

    Table buttons = new Table();
    buttons.add(cancel).width(175).padRight(10);
    buttons.add(confirm).width(175);
    content.add(title).padBottom(10).row();
    content.add(carousel).padBottom(10).row();
    content.add(status).width(440).minHeight(24).padBottom(8).row();
    content.add(buttons);

    Table root = new Table();
    root.setFillParent(true);
    root.add(content);
    stage.addActor(root);
    Gdx.input.setInputProcessor(stage);
    localHeroes =
        Game.allPlayers()
            .filter(
                entity ->
                    entity.fetch(PlayerComponent.class).map(PlayerComponent::isLocal).orElse(false))
            .toList();
    localHeroes.forEach(Game::remove);
    refreshOptions();
  }

  private void refreshOptions() {
    refreshElapsed = 0;
    List<CharacterOptions.Option> options =
        CharacterSelection.options(PreRunConfiguration.username());
    CharacterClass preferred =
        carousel
            .selectedOption()
            .map(CharacterOptions.Option::characterClass)
            .orElseGet(() -> PreRunConfiguration.multiplayerCharacterClass().orElse(null));
    carousel.setOptions(options, preferred);
    updateConfirm();
  }

  private void chooseCharacter() {
    carousel
        .selectedOption()
        .filter(CharacterOptions.Option::selectable)
        .ifPresent(
            option -> {
              String playerName = PreRunConfiguration.username();
              if (!CharacterSelection.assign(playerName, option.characterClass())) {
                refreshOptions();
                status.setText(trans.text("character_changed"));
                return;
              }
              PreRunConfiguration.multiplayerCharacterClass(option.characterClass());
              materializeSelectedHeroes(option.characterClass());
              GameLoop.finishStandaloneCharacterSelection();
            });
  }

  private void materializeSelectedHeroes(CharacterClass characterClass) {
    if (localHeroes.isEmpty()) {
      Game.add(
          HeroBuilder.builder()
              .characterClass(characterClass)
              .isLocalPlayer(true)
              .username(PreRunConfiguration.username())
              .build());
      return;
    }
    localHeroes.forEach(
        hero -> {
          HeroBuilder.applyCharacterClass(hero, characterClass);
          Game.add(hero);
        });
    localHeroes = List.of();
  }

  private void updateConfirm() {
    confirm.setDisabled(carousel.selectedOption().map(option -> !option.selectable()).orElse(true));
  }

  @Override
  public void render(float delta) {
    refreshElapsed += delta;
    if (refreshElapsed >= CharacterCarousel.AVAILABILITY_REFRESH_SECONDS) {
      refreshOptions();
    }
    Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    if (width <= 0 || height <= 0 || stage == null) {
      return;
    }
    stage.getViewport().setWorldSize(width, height);
    stage.getViewport().update(width, height, true);
  }

  @Override
  public void hide() {
    dispose();
  }

  @Override
  public void dispose() {
    if (stage != null) {
      stage.dispose();
      stage = null;
    }
  }
}
