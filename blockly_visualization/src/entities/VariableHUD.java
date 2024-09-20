package entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import components.BlocklyUIComponent;
import core.Entity;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.TileTextureFactory;
import core.utils.components.path.IPath;
import java.util.Optional;

public class VariableHUD extends BlocklyHUD {
  private Table table;
  private Stage stage;

  private final int numTiles = 32;

  public VariableHUD(Optional<Stage> stage) {
    if (stage.isEmpty()) {
      return;
    }
    this.stage = stage.get();

    table = new Table();
    table.bottom();
    table.setFillParent(true);

    IPath texturePathWall = TileTextureFactory.findTexturePath(LevelElement.WALL, DesignLabel.FOREST);
    Texture textureWall = new Texture(texturePathWall.pathString());

    for (int i=0; i < numTiles; i++){
      Image image = new Image(textureWall);
      table.add(image).expandX().width(getWidth()).height(getHeight());
    }
    table.row();

    IPath texturePathFloor = TileTextureFactory.findTexturePath(LevelElement.FLOOR, DesignLabel.FOREST);
    Texture textureFloor = new Texture(texturePathFloor.pathString());
    for (int j=0; j< 2; j++) {
      for (int i=0; i < numTiles; i++){
        Image image = new Image(textureFloor);
        table.add(image).expandX().width(getWidth()).height(getHeight());
      }
      table.row();
    }

    this.stage.addActor(table);
  }

  private float getWidth() {
    return this.stage.getWidth() / numTiles;
  }
  private float getHeight() {
    float aspectRatio = Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
    return this.stage.getHeight() /  numTiles * aspectRatio;
  }

  @Override
  public void updateActors() {
    Array<Cell> tableCells = table.getCells();
    for (Cell cell: tableCells) {
      cell.width(getWidth());
      cell.height(getHeight());
    }
  }

  public Entity createEntity(){
    Entity entity = new Entity("VariableHUD");
    entity.add(new BlocklyUIComponent(this));
    return entity;
  }
}
