package entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import components.BlocklyUIComponent;
import core.Entity;
import entities.utility.HUDVariable;

import java.util.Optional;
import java.util.TreeSet;

public class VariableHUDNames extends BlocklyHUD {

  private Table table;
  private Stage stage;

  private float initialWidth;
  private final int numTiles = 28;

  TreeSet<HUDVariable> variables = new TreeSet<>();

  public VariableHUDNames(Optional<Stage> stage) {
    if (stage.isEmpty()) {
      return;
    }
    this.stage = stage.get();

    table = new Table();
    table.bottom();
    table.setFillParent(true);

    for (int j=0; j < 2; j++) {
      for (int i=0; i < numTiles / 2; i++){
        Label label = new Label("", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        label.setFontScale(j+1);
        table.add(label).expandX().width(getWidth()).height(getHeight()).padBottom(getHeight()*j).center();
      }
      table.row();
    }

    this.stage.addActor(table);
    initialWidth = getWidth();
  }

  private float getWidth() {
    float tilesWidth = (float) numTiles / 2;
    return (this.stage.getWidth() - 10) / tilesWidth;
  }
  private float getHeight() {
    float aspectRatio = Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
    return this.stage.getHeight() /  numTiles * aspectRatio;
  }
  @Override
  public void updateActors() {
    Array<Cell> tableCells = table.getCells();
    float scaling = getWidth() / initialWidth;
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
    Entity entity = new Entity("VariableHUDNames");
    entity.add(new BlocklyUIComponent(this));
    return entity;
  }

  public void addVariable(String name, int value) {
    HUDVariable newVar = new HUDVariable(name, value);
    int variableSize = variables.size();
    if (variables.contains(newVar)) {
      variables.remove(newVar);
    } else if(variableSize >= (numTiles / 2)) {
      variables.remove(variables.last());
    }
    variables.add(newVar);
    // Update the table
    Array<Cell> tableCells = table.getCells();
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
    Cell valueCell = tableCells.get(position + (numTiles / 2));
    Label valueLabel = (Label) valueCell.getActor();
    valueLabel.setText(var.value);
  }
}
