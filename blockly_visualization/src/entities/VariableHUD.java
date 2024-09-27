package entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import components.BlocklyUIComponent;
import core.Entity;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.TileTextureFactory;
import core.utils.components.path.IPath;
import entities.utility.HUDVariable;

import java.util.Optional;
import java.util.TreeSet;

public class VariableHUD extends BlocklyHUD {
  private Table varTable;

  private Table arrayTable;

  private Stage stage;

  private final int xTiles = 28;

  private int yTiles = 16;
  private final int varTiles = 3;
  private int numArrayTiles = yTiles - varTiles;

  // For Labels
  private float initialWidth;
  TreeSet<HUDVariable> variables = new TreeSet<>();

  private Table arrayLabels;
  private Table varLabels;


  private Texture textureWall;
  private Texture textureFloor;

  public VariableHUD(Optional<Stage> stage) {
    if (stage.isEmpty()) {
      return;
    }
    this.stage = stage.get();

    this.textureWall = createTexture(LevelElement.WALL, DesignLabel.FOREST);
    this.textureFloor = createTexture(LevelElement.FLOOR, DesignLabel.FOREST);

    this.varTable = createVariableTable(textureWall, textureFloor);
    this.stage.addActor(varTable);

    this.varLabels = createVariableLabels();
    this.stage.addActor(varLabels);

    this.arrayTable = createArrayTable(textureWall, textureFloor);
    this.stage.addActor(arrayTable);

    this.arrayLabels = createArrayLabels();
    this.stage.addActor(arrayLabels);
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

  private Table createVariableLabels() {
    Table table = new Table();
    table.bottom();
    table.setFillParent(true);

    for (int j=0; j < 2; j++) {
      for (int i=0; i < xTiles / 2; i++){
        Label label = new Label("", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        label.setFontScale(j+1);
        table.add(label).expandX().width(getWidth()).height(getHeight()).padBottom(getHeight()*j).center();
      }
      table.row();
    }
    initialWidth = getWidth();
    return table;
  }
  public void addVariable(String name, int value) {
    HUDVariable newVar = new HUDVariable(name, value);
    int variableSize = variables.size();
    if (variables.contains(newVar)) {
      variables.remove(newVar);
    } else if(variableSize >= (xTiles / 2)) {
      variables.remove(variables.last());
    }
    variables.add(newVar);
    // Update the table
    Array<Cell> tableCells = this.varLabels.getCells();
    int counter = 0;
    for (HUDVariable var : variables) {
      updateVariableCell(tableCells, counter, var);
      counter++;
    }
  }

  private void updateVariableCell(Array<Cell> tableCells, int position, HUDVariable var) {
    // Set text for variable name label
    Cell nameCell = tableCells.get(position);
    Label nameLabel = (Label) nameCell.getActor();
    nameLabel.setText(var.getFormattedName());
    // Set text for variable value label
    Cell valueCell = tableCells.get(position + (xTiles / 2));
    Label valueLabel = (Label) valueCell.getActor();
    valueLabel.setText(var.value);
  }

  private Table createArrayLabels() {
    Table table = new Table();
    table.top();
    table.setFillParent(true);
    numArrayTiles = yTiles - 3;

    createArrayLabelFirstRow(table);
    for (int i=0; i < numArrayTiles - 1; i++) {
      createArrayLabelRow(table);
    }
    return table;
  }

  private void createArrayLabelFirstRow(Table table) {
    float paddingLeft = getWidth() * (xTiles - 4);
    float scaling = getFontScaling();

    for (int i=0; i < 2; i++) {
      Label arrayLabel = new Label("123456789...", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
      arrayLabel.setAlignment(Align.center);
      arrayLabel.setFontScale(scaling);

      if (i == 0) {
        table.add(arrayLabel).width(getWidth()).height(getHeight()).colspan(2).center();
      } else {
        table.add(arrayLabel).width(getWidth()).height(getHeight()).colspan(2).padLeft(paddingLeft * i);
      }
    }
    table.row();
  }

  private void createArrayLabelRow(Table table) {
    float paddingLeft = getWidth() * (xTiles - 4);
    float scaling = getFontScaling();
    for (int i=0; i < 2; i++) {
      Label labelValue = new Label("11", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
      labelValue.setFontScale(1.5f * scaling);
      labelValue.setAlignment(Align.center);

      Label labelIndex = new Label("00", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
      labelIndex.setFontScale(2 * scaling);
      labelIndex.setAlignment(Align.center);

      if (i == 0) {
        table.add(labelValue).width(getWidth()).height(getHeight()).center();
        table.add(labelIndex).width(getWidth()).height(getHeight()).center();
      } else {
        table.add(labelIndex).width(getWidth()).height(getHeight()).padLeft(paddingLeft * i);
        table.add(labelValue).width(getWidth()).height(getHeight()).center();
      }
    }
    table.row();
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
    // Update variable tiles
    Array<Cell> tableCells = varTable.getCells();
    for (Cell cell: tableCells) {
      cell.size(getWidth(), getHeight());
    }
    updateVariableLabels();
    // Update array tiles
    updateArrayTiles(tmpYTiles);
    // Update array labels
    updateArrayLabels(tmpYTiles);
  }

  private void updateArrayTiles(int tmpYTiles) {
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

  private void updateArrayLabels(int tmpYTiles) {
    // We need to recreate the array table
    if (tmpYTiles != yTiles) {
      // Create new table first
      Table newTable = createArrayLabels();
      // Remove old table
      this.stage.getActors().removeValue(arrayLabels, true);
      // Set new table
      this.stage.addActor(newTable);
      arrayLabels = newTable;
      return;
    }
    float scaling = getFontScaling();
    Array<Cell> tableCellsArray = arrayLabels.getCells();
    for (Cell cell: tableCellsArray) {
      int row = cell.getRow();
      cell.size(getWidth(), getHeight());
      if (cell.getColumn() == 2) {
        float paddingLeft = getWidth() * (xTiles - 4);
        cell.padLeft(paddingLeft);
      }
      // Update size and font scale of label
      Label label = (Label) cell.getActor();
      label.setSize(getWidth(), getHeight());
      if (row != 0) {
        if (cell.getColumn() == 0 || cell.getColumn() == 3) {
          label.setFontScale(1.5f * scaling);
        } else {
          label.setFontScale(2 * scaling);
        }

      } else {
        label.setFontScale(scaling);
      }

    }
  }

  private float getFontScaling() {
    return getWidth() / initialWidth;
  }

  private void updateVariableLabels() {
    Array<Cell> tableCells = this.varLabels.getCells();
    float scaling = getFontScaling();
    for (Cell cell: tableCells) {
      int row = cell.getRow();
      cell.size(getWidth(), getHeight());
      cell.padBottom(getHeight()*row);
      // Update size and font scale of label
      Label label = (Label) cell.getActor();
      label.setSize(getWidth(), getHeight());
      label.setFontScale(scaling * (row + 1));
    }
  }

  @Override
  public Entity createEntity(){
    Entity entity = new Entity("VariableHUDTiles");
    entity.add(new BlocklyUIComponent(this));
    return entity;
  }
}
