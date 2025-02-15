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
import core.utils.components.path.SimpleIPath;
import entities.utility.HUDVariable;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

/**
 * This class will control the variable HUD. It will display tiles on the right, left and bottom
 * side of the screen. The tile fields will be filled with the values of variable and arrays that
 * were created in blockly.
 */
public class VariableHUD extends BlocklyHUD {
  private Table hudContainer;
  private Stage stage;

  // General numbers used for table creation and scaling
  private final int xTiles = 28;
  private int yTiles = 16;
  private final int varTiles = 3;
  private int numArrayTiles = yTiles - varTiles;
  // Tables for tiles
  private Table varTable;
  private Table arrayTable;
  // Tables for labels
  private Table arrayLabels;
  private Table varLabels;
  // Monster table
  private Table monsterTable;
  private ArrayList<Texture> monsterTextures;
  private final Random RANDOM = new Random();
  // For Labels
  private float initialWidth;
  TreeSet<HUDVariable> variables = new TreeSet<>();
  HUDVariable leftArray = null;
  HUDVariable rightArray = null;

  // Textures for tiles
  private Texture textureWall;
  private Texture textureFloor;

  // Default scaling for Labels
  private final int indexScaling = 2;
  private final float valueScaling = 1.5f;
  private final float varNameScaling = 1.25f;

  /**
   * Initialize the variable and array HUD. It will add 4 tables to the stage. The variable tiles
   * table, the variable labels table, the array tiles table and the array labels table.
   *
   * @param stage The variable HUD will be added to the given stage.
   */
  public VariableHUD(Stage stage) {
    this.stage = stage;

    this.hudContainer = new Table();
    this.hudContainer.setFillParent(true);
    this.stage.addActor(hudContainer);

    // Initial size
    this.hudContainer.setHeight(this.stage.getHeight());
    this.hudContainer.setWidth(this.stage.getWidth());

    this.textureWall = createTexture(LevelElement.WALL, DesignLabel.FOREST);
    this.textureFloor = createTexture(LevelElement.FLOOR, DesignLabel.FOREST);

    this.varTable = createVariableTable(textureWall, textureFloor);
    this.hudContainer.addActor(varTable);

    this.varLabels = createVariableLabels();
    this.hudContainer.addActor(varLabels);

    this.arrayTable = createArrayTable(textureWall, textureFloor);
    this.hudContainer.addActor(arrayTable);

    this.arrayLabels = createArrayLabels();
    updateArrayLabelCells(false);
    this.hudContainer.addActor(arrayLabels);

    this.monsterTable = createMonsterTable();
    this.hudContainer.addActor(monsterTable);

    loadMonsterTextures();
  }

  /** Load all textures for the monsters. This will be called initially once. */
  private void loadMonsterTextures() {
    monsterTextures = new ArrayList<>();
    monsterTextures.add(new Texture(new SimpleIPath("monsters/chort.png").pathString()));
    monsterTextures.add(new Texture(new SimpleIPath("monsters/doc.png").pathString()));
    monsterTextures.add(new Texture(new SimpleIPath("monsters/goblin.png").pathString()));
    monsterTextures.add(new Texture(new SimpleIPath("monsters/imp.png").pathString()));
    monsterTextures.add(
        new Texture(new SimpleIPath("monsters/monster_elemental_small.png").pathString()));
  }

  /**
   * Get a random monster texture from the monster textures list that was filled initially.
   *
   * @return Returns a texture of a random monster.
   */
  private Texture getRandomMonsterTexture() {
    return monsterTextures.get(RANDOM.nextInt(monsterTextures.size()));
  }

  /**
   * Calculate the width for one tile.
   *
   * @return Returns the widht for one tile.
   */
  private float getWidth() {
    return this.hudContainer.getWidth() / xTiles;
  }

  /**
   * Calculate the height for a tile.
   *
   * @return Returns the height for one tile.
   */
  private float getHeight() {
    yTiles = (int) (this.hudContainer.getHeight() / getWidth());
    return this.hudContainer.getHeight() / (float) yTiles;
  }

  /**
   * Get the scaling for the font size if the screen size has changed. The scaling will be
   * calculated with the initial width of the screen.
   *
   * @return Returns the scaling for the font size depending on the current screen size.
   */
  private float getFontScaling() {
    return getWidth() / initialWidth;
  }

  /**
   * Creates the texture for the given level element and design label.
   *
   * @param levelElement The level element will determine the type of the tile texture. For example
   *     floor or wall.
   * @param designLabel The design label will determine the theme of the tile texture. For example
   *     forest or temple.
   * @return Returns the created texture.
   */
  private Texture createTexture(LevelElement levelElement, DesignLabel designLabel) {
    IPath texturePathWall = TileTextureFactory.findTexturePath(levelElement, designLabel);
    return new Texture(texturePathWall.pathString());
  }

  /**
   * Create an image from a given texture.
   *
   * @param texture Texture that will be used to create a new image.
   * @return Returns the created image.
   */
  private Image createImage(Texture texture) {
    Image image = new Image(texture);
    image.setSize(getWidth(), getHeight());
    return image;
  }

  // ============================= Add table for variable tiles ============================= \\

  /**
   * Creates the variable tiles table.
   *
   * @param textureWall Texture for the wall tiles. Used for the variable names.
   * @param textureFloor Texture for the floor tiles. Used for the variable values.
   * @return Returns the created table.
   */
  private Table createVariableTable(Texture textureWall, Texture textureFloor) {
    Table table = new Table();
    table.bottom();
    table.setFillParent(true);

    createVarRow(table, textureWall);

    table.row();

    for (int i = 0; i < varTiles - 1; i++) {
      createVarRow(table, textureFloor);
      table.row();
    }

    return table;
  }

  /**
   * Creates a new row in the given table. This function is intended for the variable tiles table.
   * One row will contain the defined number of tiles. The number of tiles is defined by xTiles.
   *
   * @param table Table that will receive the new row.
   * @param texture Texture for the tiles in the new row.
   */
  private void createVarRow(Table table, Texture texture) {
    for (int i = 0; i < xTiles; i++) {
      Image image = createImage(texture);
      table.add(image).expandX().width(getWidth()).height(getHeight());
    }
  }

  // ============================= Add table for variable labels ============================= \\

  /**
   * Create the table for the variable labels.
   *
   * @return Returns the created table.
   */
  private Table createVariableLabels() {
    Table table = new Table();
    table.bottom();
    table.setFillParent(true);

    for (int j = 0; j < 2; j++) {
      for (int i = 0; i < xTiles / 2; i++) {
        Label label = new Label("", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        if (j == 0) {
          label.setFontScale(varNameScaling);
        } else {
          label.setFontScale(valueScaling);
        }
        table
            .add(label)
            .expandX()
            .width(getWidth())
            .height(getHeight())
            .padBottom(getHeight() * j)
            .center();
      }
      table.row();
    }
    initialWidth = getWidth();
    return table;
  }

  // ============================= Add variables to table and show them int the dungeon
  // ============================= \\

  /**
   * Add a new variable to the hud. The variables will be sorted by their modtime meaning that the
   * variable with the latest modtime will always be displayed at the first tile on the left side.
   *
   * @param name Name of the new variable.
   * @param value Value of the new variable.
   */
  public void addVariable(String name, int value) {
    HUDVariable newVar = new HUDVariable(name, value);
    int variableSize = variables.size();
    if (variables.contains(newVar)) {
      newVar = variables.ceiling(newVar);
      assert newVar != null;
      newVar.updateVariable(value);
      variables.remove(newVar);
    } else {
      if (variableSize >= (xTiles / 2)) {
        variables.remove(variables.last());
      }
      newVar.setMonsterTexture(getRandomMonsterTexture());
    }
    variables.add(newVar);

    // Update the table
    Array<Cell> tableCells = this.varLabels.getCells();
    int counter = 0;
    for (HUDVariable var : variables) {
      updateVariableCell(tableCells, counter, var);
      counter++;
    }

    updateMonsterTable();
  }

  private void updateMonsterTable() {
    monsterTable.remove();
    monsterTable = createMonsterTable();
    this.hudContainer.addActor(monsterTable);
  }

  /**
   * Clear the variables set and the reset the variables HUD. Called by the server class when
   * resetting all values.
   */
  public void clearVariables() {
    variables.clear();
    Array<Cell> tableCells = this.varLabels.getCells();
    for (Cell cell : tableCells) {
      Label label = (Label) cell.getActor();
      label.setText("");
    }
    monsterTable.remove();
  }

  /**
   * Update the text of a variable label. This will update the name and the value of the cell at the
   * given position. This function is intended for the variable labels table.
   *
   * @param tableCells Array containing all table cells.
   * @param position Index in the given array of table cells that will be adjusted.
   * @param var Value of the variable that will be displayed below the variable name.
   */
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

  // ============================= Add table for array tiles ============================= \\

  /**
   * Create the array tiles table.
   *
   * @param textureWall Texture for the index tile.
   * @param textureFloor Texture for the value tile.
   * @return Returns the created array tiles table.
   */
  private Table createArrayTable(Texture textureWall, Texture textureFloor) {
    Table table = new Table();
    table.top();
    table.setFillParent(true);
    // Create first row
    createArrayRow(table, textureWall, textureWall);
    numArrayTiles = yTiles - 3;
    for (int i = 0; i < numArrayTiles - 1; i++) {
      createArrayRow(table, textureWall, textureFloor);
    }
    return table;
  }

  /**
   * Add a new row to the given table. This function is intended for the array tiles table. It will
   * add a row with 4 columns. The last two columns will be displayed on the right side of the
   * screen.
   *
   * @param table Table that will receive a new row.
   * @param textureWall Texture for the index tile.
   * @param textureFloor Texture for the value tile.
   */
  private void createArrayRow(Table table, Texture textureWall, Texture textureFloor) {
    float paddingLeft = getWidth() * (xTiles - 4);
    for (int i = 0; i < 2; i++) {
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

  // ============================= Add table for array labels ============================= \\

  /**
   * Create the table for the array labels.
   *
   * @return Returns the created table.
   */
  private Table createArrayLabels() {
    Table table = new Table();
    table.top();
    table.setFillParent(true);
    numArrayTiles = yTiles - 3;

    createArrayLabelFirstRow(table);
    for (int i = 0; i < numArrayTiles - 1; i++) {
      createArrayLabelRow(table);
    }
    return table;
  }

  /**
   * Add a new row to the given table. This is intended as the first row of the array label tabel.
   * Thus, this row contains two columns which contain a label with an empty string. The text of
   * these labels will be replaced of the variable name of an array.
   *
   * @param table Table that will receive a new row.
   */
  private void createArrayLabelFirstRow(Table table) {
    float paddingLeft = getWidth() * (xTiles - 4);
    float scaling = getFontScaling();

    for (int i = 0; i < 2; i++) {
      Label arrayLabel = new Label("", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
      arrayLabel.setAlignment(Align.center);
      arrayLabel.setFontScale(varNameScaling * scaling);

      if (i == 0) {
        table.add(arrayLabel).width(getWidth()).height(getHeight()).colspan(2).center();
      } else {
        table
            .add(arrayLabel)
            .width(getWidth())
            .height(getHeight())
            .colspan(2)
            .padLeft(paddingLeft * i);
      }
    }
    table.row();
  }

  /**
   * Add a new row to the given table. One row contains 4 columns which each contain a label with an
   * empty string. The last columns of the row will be displayed on the right side of the screen.
   *
   * @param table Table that will receive a new row.
   */
  private void createArrayLabelRow(Table table) {
    float paddingLeft = getWidth() * (xTiles - 4);
    float scaling = getFontScaling();
    for (int i = 0; i < 2; i++) {
      Label labelValue = new Label("", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
      labelValue.setFontScale(valueScaling * scaling);
      labelValue.setAlignment(Align.center);

      Label labelIndex = new Label("", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
      labelIndex.setFontScale(indexScaling * scaling);
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

  // ============================= Add a new array variable and display it in the dungeon
  // ============================= \\

  /**
   * Add a new array variable to the HUD. The array will either be displayed on the right or left
   * side. The first array will always be displayed on the left side. If both sides already contain
   * an array, the array with the oldest modtime will be replaced with the new array.
   *
   * @param name Name of the array variable. This is important to check if the array is already
   *     being displayed.
   * @param value Value of the array variable with the type int[].
   */
  public void addArrayVariable(String name, int[] value) {
    HUDVariable newVar = new HUDVariable(name, value);
    if (leftArray != null && leftArray.equals(newVar)) {
      leftArray = newVar;
    } else if (rightArray != null && rightArray.equals(newVar)) {
      rightArray = newVar;
    } else if (leftArray == null) {
      leftArray = newVar;
    } else if (rightArray == null) {
      rightArray = newVar;
    } else if (leftArray.compareTo(rightArray) < 0) {
      rightArray = newVar;
    } else {
      leftArray = newVar;
    }
    updateArrayLabelCells(true);
  }

  /**
   * Reset the array HUD. Set the left and right array to null and set the text of all array labels
   * to an empty string. Called by the Server class when resetting all values.
   */
  public void clearArrayVariables() {
    this.leftArray = null;
    this.rightArray = null;
    Array<Cell> tableCells = this.arrayLabels.getCells();
    for (Cell cell : tableCells) {
      Label label = (Label) cell.getActor();
      label.setText("");
    }
  }

  /**
   * Get the max index that can be displayed for a given array. This function either returns the
   * length of the given array or the number of possible array tiles that can currently be
   * displayed.
   *
   * @param arrayVar Object containing the array that will be displayed. The maximum index will be
   *     calculated for this array.
   * @return Returns the length of the given array or the number of possible array tiles that can
   *     currently be displayed.
   */
  private int getMaxIndex(HUDVariable arrayVar) {
    int maxIndex = numArrayTiles - 1;
    if (maxIndex > arrayVar.arrayValue.length) {
      maxIndex = arrayVar.arrayValue.length;
    }
    return maxIndex;
  }

  /**
   * Update the cells of the array label table. If reset is true, the text of all cells will be set
   * to an empty string. Afterwards the text of the labels will either be filled with the index or
   * the value of the right and left array.
   *
   * @param reset If true, reset the text of all labels to an empty string. This should only be
   *     false if the table was just created and the text is already empty.
   */
  private void updateArrayLabelCells(boolean reset) {
    Array<Cell> tableCells = this.arrayLabels.getCells();
    if (reset) {
      // Reset labels
      for (Cell labelCell : tableCells) {
        Label label = (Label) labelCell.getActor();
        label.setText("");
      }
    }

    // Update left array
    if (leftArray != null) {
      // Update first row
      updateArrayName(tableCells, 0, leftArray);
      int maxIndex = getMaxIndex(leftArray);
      for (int index = 0; index < maxIndex; index++) {
        int indexPosition = index * 4 + 1;
        int valuePosition = index * 4;
        updateArrayCell(
            tableCells, indexPosition, valuePosition, index, leftArray.arrayValue[index]);
      }
    }
    // Update right array
    if (rightArray != null) {
      // Update first row
      updateArrayName(tableCells, 1, rightArray);
      int maxIndex = getMaxIndex(rightArray);
      for (int index = 0; index < maxIndex; index++) {
        int indexPosition = index * 4 + 2;
        int valuePosition = index * 4 + 3;
        updateArrayCell(
            tableCells, indexPosition, valuePosition, index, rightArray.arrayValue[index]);
      }
    }
  }

  /**
   * Update the name of an array.
   *
   * @param tableCells Array with the table cells that should be modified.
   * @param position Index in the table cells array that will be modified.
   * @param arrayVar Object containing the array value and the array name. The function
   *     getFormattedName will be used to get the array name.
   */
  private void updateArrayName(Array<Cell> tableCells, int position, HUDVariable arrayVar) {
    Cell arrayNameCell = tableCells.get(position);
    Label arrayNameLabel = (Label) arrayNameCell.getActor();
    arrayNameLabel.setText(arrayVar.getFormattedName());
  }

  /**
   * Update the text of an array cell at the given position.
   *
   * @param tableCells Array with the table cells that should be modified.
   * @param indexPosition Index of the index label in the table cells array. The index will be
   *     increased by 2 because the first row of the table contains 2 columns with the variable name
   *     of the array.
   * @param valuePosition Index of the value label in the table cells array. The index will be
   *     increased by 2 because the first row of the table contains 2 columns with the variable name
   *     of the array.
   * @param index Value of the index label. This value will be set as the text.
   * @param value Value of the value label. This value will be set as the text.
   */
  private void updateArrayCell(
      Array<Cell> tableCells, int indexPosition, int valuePosition, int index, int value) {
    // Set text for index label
    // Position +2, because we the first row contains 2 cells for the array names
    Cell indexCell = tableCells.get(indexPosition + 2);
    Label indexLabel = (Label) indexCell.getActor();
    indexLabel.setText(index);
    // Set text for value label
    Cell valueCell = tableCells.get(valuePosition + 2);
    Label valueLabel = (Label) valueCell.getActor();
    valueLabel.setText(value);
  }

  // ============================= Table for monsters
  // =========================================================\\

  /**
   * Create the table for the monsters. This function will be called each time a new monster was
   * created.
   *
   * @return Returns the created table.
   */
  private Table createMonsterTable() {
    Table table = new Table();
    table.bottom();
    table.left();
    table.setFillParent(true);
    boolean isFirst = true;
    for (HUDVariable var : variables) {
      Image image = createImage(var.monsterTexture);
      if (isFirst) {
        table
            .add(image)
            .width(getWidth())
            .height(getHeight())
            .padLeft(5)
            .padRight(getWidth())
            .padBottom(5);
        isFirst = false;
      } else {
        table.add(image).width(getWidth()).height(getHeight()).padRight(getWidth()).padBottom(5);
      }
    }

    return table;
  }

  // ============================= Update different tables if screen size changed
  // ============================= \\

  /**
   * Update the tables when screen size changed. This function will be invoked by the
   * HudBlocklySystem. Update the following tables: varTable, arrayTable, varLabels and arrayLabels
   */
  @Override
  public void updateActors() {
    this.hudContainer.setHeight(this.stage.getHeight());
    this.hudContainer.setWidth(this.stage.getWidth());
    // yTiles might change after the first call of getHeight(). Save current value here to track if
    // array tables must be
    // adjusted
    int tmpYTiles = yTiles;
    // Update variable tiles
    Array<Cell> tableCells = varTable.getCells();
    for (Cell cell : tableCells) {
      cell.size(getWidth(), getHeight());
    }
    // Update Monsters
    Array<Cell> monsterCells = monsterTable.getCells();
    for (Cell cell : monsterCells) {
      cell.size(getWidth(), getHeight());
      cell.padRight(getWidth());
    }
    updateVariableLabels();
    // Update array tiles
    updateArrayTiles(tmpYTiles);
    // Update array labels
    updateArrayLabels(tmpYTiles);
  }

  /**
   * Update the size of the array tiles when the screen size changed. This will either adjust the
   * size of the tiles when the screen size has changed but the number of y-tiles is still the same
   * or the table will be recreated if the number of y-tiles changed.
   *
   * @param tmpYTiles The number of yTiles before the screen size changed
   */
  private void updateArrayTiles(int tmpYTiles) {
    // We need to recreate the array table
    if (tmpYTiles != yTiles) {
      // Create new table first
      Table newTable = createArrayTable(this.textureWall, this.textureFloor);
      // Remove old table
      this.hudContainer.getChildren().removeValue(arrayTable, true);
      // Set new table
      this.hudContainer.addActor(newTable);
      arrayTable = newTable;
      return;
    }
    Array<Cell> tableCellsArray = arrayTable.getCells();
    for (Cell cell : tableCellsArray) {
      cell.size(getWidth(), getHeight());
      if (cell.getColumn() == 2) {
        float paddingLeft = getWidth() * (xTiles - 4);
        cell.padLeft(paddingLeft);
      }
    }
  }

  /**
   * Update the size of the array labels when the screen size has been changed. This function will
   * adjust the size of the table cells and the font size scaling for the array labels table. If the
   * number of y-tiles has changed the table will be recreated.
   *
   * @param tmpYTiles The number of yTiles before the screen size changed
   */
  private void updateArrayLabels(int tmpYTiles) {
    // We need to recreate the array table
    if (tmpYTiles != yTiles) {
      // Create new table first
      Table newTable = createArrayLabels();
      // Remove old table
      this.hudContainer.getChildren().removeValue(arrayLabels, true);
      // Set new table
      this.hudContainer.addActor(newTable);
      arrayLabels = newTable;
      updateArrayLabelCells(false);
      return;
    }
    float scaling = getFontScaling();
    Array<Cell> tableCellsArray = arrayLabels.getCells();
    for (Cell cell : tableCellsArray) {
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
          label.setFontScale(valueScaling * scaling);
        } else {
          label.setFontScale(indexScaling * scaling);
        }
      } else {
        label.setFontScale(varNameScaling * scaling);
      }
    }
  }

  /**
   * Set the visibility of the variable HUD. If the visibility is set to false, the HUD will not be
   * displayed.
   *
   * @param visible True if the HUD should be visible, false otherwise.
   */
  public void visible(boolean visible) {
    hudContainer.setVisible(visible);
  }

  /**
   * Check if the variable HUD is currently visible.
   *
   * @return Returns true if the HUD is visible, false otherwise.
   */
  public boolean visible() {
    return hudContainer.isVisible();
  }

  /**
   * Update the size of the variable labels. This function will update the table cells and the font
   * size of the labels. Also, the padding to bottom must be adjusted to the new screen size,
   * because the right under the value row is a row of tiles for monsters.
   */
  private void updateVariableLabels() {
    Array<Cell> tableCells = this.varLabels.getCells();
    float scaling = getFontScaling();
    for (Cell cell : tableCells) {
      int row = cell.getRow();
      cell.size(getWidth(), getHeight());
      cell.padBottom(getHeight() * row);
      // Update size and font scale of label
      Label label = (Label) cell.getActor();
      label.setSize(getWidth(), getHeight());
      if (row == 0) {
        label.setFontScale(scaling * varNameScaling);
      } else {
        label.setFontScale(scaling * valueScaling);
      }
    }
  }

  // ============================= Create an entity for this object ============================= \\

  /**
   * Creates a new Entity with a BlocklyUIComponent which will contain this object.
   *
   * @return Returns a new Entity with a BlocklyUIComponent
   */
  @Override
  public Entity createEntity() {
    Entity entity = new Entity("VariableHUDTiles");
    entity.add(new BlocklyUIComponent(this));
    return entity;
  }
}
