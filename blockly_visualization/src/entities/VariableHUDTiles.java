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
  private Table varTable;

  private Table arrayTable;
  private Stage stage;

  private final int xTiles = 28;

  private int yTiles = 16;
  private final int varTiles = 3;
  private int numArrayTiles = yTiles - varTiles;



  private Texture textureWall;
  private Texture textureFloor;

  public VariableHUDTiles(Optional<Stage> stage) {
    if (stage.isEmpty()) {
      return;
    }
    this.stage = stage.get();

    this.textureWall = createTexture(LevelElement.WALL, DesignLabel.FOREST);
    this.textureFloor = createTexture(LevelElement.FLOOR, DesignLabel.FOREST);

    this.varTable = createVariableTable(textureWall, textureFloor);
    this.stage.addActor(varTable);

    this.arrayTable = createArrayTable(textureWall, textureFloor);
    this.stage.addActor(arrayTable);
  }

  private Table createVariableTable(Texture textureWall, Texture textureFloor) {
    Table table = new Table();
    table.bottom();
    table.setFillParent(true);

    for (int i=0; i < xTiles; i++){
      createVarRow(table, textureWall);
    }
    table.row();


    for (int j=0; j < varTiles - 1; j++) {
      for (int i=0; i < xTiles; i++){
        createVarRow(table, textureFloor);
      }
      table.row();
    }
    return table;
  }

  private void createVarRow(Table table, Texture texture) {
    for (int i=0; i < xTiles; i++){
      Image image = createImage(texture);
      table.add(image).expandX().width(getWidth()).height(getHeight());
    }
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

  private Table createArrayTable(Texture textureWall, Texture textureFloor) {
    Table table = new Table();
    table.top();
    table.setFillParent(true);
    // Create first row
    createArrayRow(table, textureWall, textureWall);
    numArrayTiles = yTiles - 3;
    for (int i=0; i < numArrayTiles - 1; i++) {
      createArrayRow(table, textureWall, textureFloor);
    }
    return table;
  }

  private void createArrayRow(Table table, Texture textureWall, Texture textureFloor) {
    float paddingLeft = getWidth() * (xTiles - 4);
    for (int i=0; i < 2; i++) {
      Image imageFloor = createImage(textureFloor);
      table.add(imageFloor).width(getWidth()).height(getHeight()).padLeft(paddingLeft * i);
      Image imageWall = createImage(textureWall);
      table.add(imageWall).width(getWidth()).height(getHeight());
      // Swap textures for right array
      Texture tmpTexture = textureFloor;
      textureFloor = textureWall;
      textureWall = tmpTexture;
    }
    table.row();
  }

  private float getWidth() {
    return this.stage.getWidth() / xTiles;
  }
  private float getHeight() {
    yTiles = (int) (this.stage.getHeight() / getWidth());
    return this.stage.getHeight() / (float) yTiles;
  }

  @Override
  public void updateActors() {
    int tmpYTiles = yTiles;
    Array<Cell> tableCells = varTable.getCells();
    for (Cell cell: tableCells) {
      cell.size(getWidth(), getHeight());
    }
    // We need to recreate the array table
    if (tmpYTiles != yTiles) {
      // Create new table first
      Table newTable = createArrayTable(this.textureWall, this.textureFloor);
      // Remove old table
      this.stage.getActors().removeValue(arrayTable, true);
      // Set new table
      this.stage.addActor(newTable);
      arrayTable = newTable;
      return;
    }
    Array<Cell> tableCellsArray = arrayTable.getCells();
    for (Cell cell: tableCellsArray) {
      cell.size(getWidth(), getHeight());
      if (cell.getColumn() == 2) {
        float paddingLeft = getWidth() * (xTiles - 4);
        cell.padLeft(paddingLeft);
      }
    }
  }

  @Override
  public Entity createEntity(){
    Entity entity = new Entity("VariableHUDTiles");
    entity.add(new BlocklyUIComponent(this));
    return entity;
  }
}
