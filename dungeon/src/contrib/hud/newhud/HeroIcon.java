package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.components.CharacterClassComponent;
import contrib.entities.CharacterClass;
import core.Game;

public class HeroIcon extends Table implements HUDElement {

  private final Image heroIcon;

  public HeroIcon(Skin skin) {
    super(skin);
    setSize(64, 64);
    setBackground(skin.getDrawable("gray"));

    Stack stack = new Stack();
    stack.setSize(64, 64);
    addActor(stack);

    heroIcon = new Image();
    Table iconTable = new Table();
    iconTable.add(heroIcon).size(48, 48);
    stack.add(iconTable);
  }

  @Override
  public void init() {
    layout();
    setHeroIcon();
  }

  @Override
  public void layout() {
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
