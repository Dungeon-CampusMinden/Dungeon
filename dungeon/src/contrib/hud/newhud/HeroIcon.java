package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.components.CharacterClassComponent;
import contrib.entities.CharacterClass;
import core.Game;

public class HeroIcon extends Table implements HUDElement {

  private final Image heroIcon;

  public HeroIcon(Skin skin) {
    setSize(64, 64);
    setBackground(skin.getDrawable("gray"));
    pad(8);

    heroIcon = new Image();
    add(heroIcon).size(48, 48);

    Label tooltipLabel = new Label("Character (C)", skin);
    Tooltip<Label> tooltip = new Tooltip<>(tooltipLabel);
    tooltip.setInstant(true);
    addListener(tooltip);
  }

  @Override
  public void init() {
    layoutElement();
    setHeroIcon();
  }

  @Override
  public void layoutElement() {
    pack();
    setPosition(16, Gdx.graphics.getHeight() - 82);
  }

  @Override
  public void update() {}

  public void setHeroIcon() {
    Game.player()
        .flatMap(player -> player.fetch(CharacterClassComponent.class))
        .ifPresent(
            characterClassComp -> {
              CharacterClass heroClass = characterClassComp.characterClass();
              String png;
              switch (heroClass) {
                case WIZARD:
                  png = "dungeon/assets/character/wizard/wizard_icon.png";
                  break;
                case HUNTER:
                  png = null;
                  break;
                default:
                  png = null;
                  break;
              }
              if (png != null) {
                heroIcon.setDrawable(new TextureRegionDrawable(new Texture(png)));
              }
            });
  }
}
