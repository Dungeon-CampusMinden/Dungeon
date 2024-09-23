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

public class VariableHUDTiles extends BlocklyHUD {
  private Table table;
  private Stage stage;

  private final int numTiles = 28;

  public VariableHUDTiles(Optional<Stage> stage) {
    if (stage.isEmpty()) {
      return;
    }
    this.stage = stage.get();

    table = new Table();
    table.bottom();
    table.setFillParent(true);

    Texture textureWall = createTexture(LevelElement.WALL, DesignLabel.FOREST);
    for (int i=0; i < numTiles; i++){
      Image image = createImage(textureWall);
      table.add(image).expandX().width(getWidth()).height(getHeight());
    }
    table.row();

    Texture textureFloor = createTexture(LevelElement.FLOOR, DesignLabel.FOREST);
    for (int j=0; j< 2; j++) {
      for (int i=0; i < numTiles; i++){
        Image image = createImage(textureFloor);
        table.add(image).expandX().width(getWidth()).height(getHeight());
      }
      table.row();
    }

    this.stage.addActor(table);
  }

  private Texture createTexture(LevelElement levelElement, DesignLabel designLabel) {
    IPath texturePathWall = TileTextureFactory.findTexturePath(levelElement, designLabel);
    return new Texture(texturePathWall.pathString());
  }

  private Image createImage(Texture texture) {
    Image image = new Image(texture);
    image.setSize(getWidth(), getHeight());
    return image;
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
      cell.size(getWidth(), getHeight());
    }
  }

  @Override
  public Entity createEntity(){
    Entity entity = new Entity("VariableHUDTiles");
    entity.add(new BlocklyUIComponent(this));
    return entity;
  }
}
