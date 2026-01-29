package modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import core.utils.Scene2dElementFactory;
import core.utils.Tuple;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modules.computer.ComputerStateComponent;

public class FileTab extends ComputerTab {

  private static Map<String, Actor> files = null;

  private Table content;

  public FileTab(ComputerStateComponent sharedState, String fileName) {
    super(sharedState, "file-" + fileName, String.format("\"%s\"", fileName), true);
    Actor fileContent = getFileMap().getOrDefault(fileName, create404Page(fileName));
    content.add(fileContent).grow();
  }

  protected void createActors() {
    this.clearChildren();
    this.content = new Table();
    this.add(content).grow();
  }

  private Actor create404Page(String fileName) {
    Table table = new Table();
    Label label =
        Scene2dElementFactory.createLabel(
            "Could not read file '%s' :(".formatted(fileName), 48, new Color(0.8f, 0, 0, 1));
    table.add(label);
    return table;
  }

  private Actor createHelpPage() {
    Table table = new Table();
    ScrollPane scrollPane = Scene2dElementFactory.createScrollPane(table, false, true);
    table.align(Align.topLeft);

    List<Tuple<String, Integer>> helpEntries =
        List.of(
            new Tuple<>("Welcome to the Help Page!", 48),
            new Tuple<>("To open a file, click on its name in the file explorer.", 24),
            new Tuple<>("Use the scroll bar to navigate through long documents.", 24),
            new Tuple<>("For further assistance, contact support at test@company.xyz", 24),
            new Tuple<>("Fodder entries for more space", 24),
            new Tuple<>("Fodder entries for more space", 24),
            new Tuple<>("Fodder entries for more space", 24),
            new Tuple<>("Fodder entries for more space", 24),
            new Tuple<>("Fodder entries for more space", 24),
            new Tuple<>("Fodder entries for more space", 24),
            new Tuple<>("Fodder entries for more space", 24),
            new Tuple<>("Fodder entries for more space", 24),
            new Tuple<>("Fodder entries for more space", 24),
            new Tuple<>("Fodder entries for more space", 24),
            new Tuple<>("Fodder entries for more space", 16),
            new Tuple<>("Fodder entries for more space", 16),
            new Tuple<>("Fodder entries for more space", 16),
            new Tuple<>("Fodder entries for more space", 16),
            new Tuple<>("Fodder entries for more space", 16),
            new Tuple<>("Fodder entries for more space", 16),
            new Tuple<>("Fodder entries for more space", 24),
            new Tuple<>("Fodder entries for more space", 24));

    table
        .add(Scene2dElementFactory.createLabel("~~~ HELP ~~~", 64, new Color(0, 0.7f, 0, 1)))
        .row();
    for (Tuple<String, Integer> entry : helpEntries) {
      Label label = Scene2dElementFactory.createLabel(entry.a(), entry.b(), Color.BLACK);
      label.setWrap(true);
      label.setAlignment(Align.left);
      table.add(label).growX().padTop(15).row();
    }

    return scrollPane;
  }

  private Map<String, Actor> getFileMap() {
    if (files == null) registerFiles();
    return files;
  }

  private void registerFiles() {
    files = new HashMap<>();
    files.put(
        "Hello.html", Scene2dElementFactory.createLabel("Helloooo world :D", 96, Color.BLACK));
    files.put("Help.html", createHelpPage());
  }

  @Override
  protected void updateState(ComputerStateComponent newStateComp) {}
}
