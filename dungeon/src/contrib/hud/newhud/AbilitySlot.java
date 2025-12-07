package contrib.hud.newhud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.utils.components.skill.Resource;

public class AbilitySlot extends Table {
  private final Image icon;
  private final Table activeAbility;
  private final Table iconTable;
  private final Table ressource;
  private final Label costLabel;
  private final Image ressourceIcon;

  public AbilitySlot(Skin skin) {
    setSize(64, 64);
    setBackground(skin.getDrawable("gray"));

    Stack stack = new Stack();
    stack.setSize(64, 64);
    addActor(stack);

    // ability icon
    icon = new Image();

    iconTable = new Table();
    iconTable.add(icon).size(48, 48);

    // Ability Ressource Kosten
    ressource = new Table();
    ressource.bottom().left();

    costLabel = new Label("", skin);
    costLabel.setFontScale(1f);
    ressourceIcon = new Image();

    ressource.add(costLabel);
    ressource.add(ressourceIcon).size(16, 16);
    ressource.setVisible(false);

    // Hintergrund der aktiven Ability
    activeAbility = new Table();
    Drawable yellowBorder = skin.newDrawable("white", 1f, 1f, 0f, 1f);
    activeAbility.setBackground(yellowBorder);
    activeAbility.setVisible(false);

    // Reihenfolge im Stack:
    stack.add(iconTable);
    stack.add(activeAbility); // ganz unten
    stack.add(ressource); // Kosten-Label + Icon über dem ability icon
  }

  public void setTexture(Texture texture) {
    icon.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
  }

  public void setCost(Resource resource, int cost) {
    ressource.setVisible(true);
    costLabel.setText(String.valueOf(cost));

    // Farbe je nach Resource
    switch (resource) {
      case MANA ->
          ressourceIcon.setDrawable(
              new TextureRegionDrawable(
                  new Texture("dungeon/assets/hud/ressourceIcons/mana_icon.png")));
      case STAMINA ->
          ressourceIcon.setDrawable(
              new TextureRegionDrawable(
                  new Texture("dungeon/assets/hud/ressourceIcons/stamina_icon.png")));
      case HP ->
          ressourceIcon.setDrawable(
              new TextureRegionDrawable(
                  new Texture("dungeon/assets/hud/ressourceIcons/health_icon.png")));
      case ARROW ->
          ressourceIcon.setDrawable(
              new TextureRegionDrawable(
                  new Texture("dungeon/assets/hud/ressourceIcons/arrows_icon.png")));
      default -> costLabel.setColor(Color.WHITE);
    }
  }

  public void setActive(boolean active) {
    activeAbility.setVisible(active);
    activeAbility.toBack();
  }
}
