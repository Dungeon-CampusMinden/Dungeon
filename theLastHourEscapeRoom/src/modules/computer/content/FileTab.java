package modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import core.utils.Scene2dElementFactory;
import core.utils.Tuple;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modules.computer.ComputerStateComponent;
import modules.computer.ComputerStateLocal;
import util.Lore;

/** Tab for displaying the contents of files in the computer UI. */
public class FileTab extends ComputerTab {

  private static Map<String, Actor> files = null;

  private String fileName;
  private Table content;

  /**
   * Constructs a new FileTab for the specified file name.
   *
   * @param sharedState the shared computer state component
   * @param fileName the name of the file to display in this tab
   */
  public FileTab(ComputerStateComponent sharedState, String fileName) {
    super(sharedState, "file-" + fileName, String.format("\"%s\"", fileName), true);
    Actor fileContent = getFileMap().getOrDefault(fileName, create404Page(fileName));
    content.add(fileContent).grow();

    this.fileName = fileName;
    localState().openFiles().add(fileName);
  }

  protected void createActors() {
    this.clearChildren();
    this.content = new Table();
    this.add(content).grow();
  }

  private static Actor create404Page(String fileName) {
    Table table = new Table();
    Label label =
        Scene2dElementFactory.createLabel(
            "Could not read file '%s' :(".formatted(fileName), 48, new Color(0.8f, 0, 0, 1));
    table.add(label);
    return table;
  }

  private static Actor createHelpPage() {
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

  private static Map<String, Actor> getFileMap() {
    if (files == null) registerFiles();
    return files;
  }

  private static void registerFiles() {
    files = new HashMap<>();
    files.put(
        "Hello.html", Scene2dElementFactory.createLabel("Helloooo world :D", 96, Color.BLACK));
    files.put("Help.html", createHelpPage());
    files.put(Lore.AccessCodeDownloadFileName, createUnlockCodePage());
  }

  private static Actor createUnlockCodePage() {
    Skin skin = UIUtils.defaultSkin();

    Table table = new Table();
    table.top().left();
    table.pad(20);
    ScrollPane scrollPane = Scene2dElementFactory.createScrollPane(table, false, true);

    // Header banner
    Image header = new Image(skin, "sg4-header");
    table.add(header).width(600).height(200).center().colspan(1).row();

    table
        .add(Scene2dElementFactory.createHorizontalDivider())
        .height(4)
        .pad(10, 0, 10, 0)
        .fillX()
        .row();

    // Document title
    Label title =
        Scene2dElementFactory.createLabel(
            "SG-4 Door Access Code - Secure Delivery", 28, Color.BLACK);
    title.setAlignment(Align.left);
    table.add(title).left().padBottom(5).row();

    Label refLabel =
        Scene2dElementFactory.createLabel(
            "SecuGate Systems  |  Document Ref: SG4-AC-0042  |  Classification: Confidential",
            14,
            Color.GRAY);
    refLabel.setAlignment(Align.left);
    table.add(refLabel).left().padBottom(15).row();

    table
        .add(Scene2dElementFactory.createHorizontalDivider())
        .height(2)
        .pad(0, 0, 15, 0)
        .fillX()
        .row();

    Label greeting = Scene2dElementFactory.createLabel("Dear Dr. Mertens,", 20, Color.BLACK);
    greeting.setAlignment(Align.left);
    table.add(greeting).left().padBottom(15).row();

    Label body1 =
        Scene2dElementFactory.createLabel(
            "Thank you for using the SecuGate SG-4 Recovery Portal. Your identity has been "
                + "successfully verified and your request for access code recovery has been "
                + "processed.",
            18,
            Color.DARK_GRAY);
    body1.setWrap(true);
    body1.setAlignment(Align.left);
    table.add(body1).growX().left().padBottom(15).row();

    Label body2 =
        Scene2dElementFactory.createLabel(
            "This document contains the encrypted door code for your SG-4 unit. As an "
                + "additional security layer, the code has been encoded using a secondary "
                + "format. Please use your 2nd decryption manual to decode the data below "
                + "and obtain the final access code for your door system.",
            18,
            Color.DARK_GRAY);
    body2.setWrap(true);
    body2.setAlignment(Align.left);
    table.add(body2).growX().left().padBottom(20).row();

    // Encrypted code section
    Label codeHeader = Scene2dElementFactory.createLabel("Encrypted Door Code", 22, Color.BLACK);
    codeHeader.setAlignment(Align.left);
    table.add(codeHeader).left().padBottom(8).row();

    // Morse code displayed horizontally
    String[] morseDigits = Lore.DoorCodeMorse.split(" ");
    Table morseBox = new Table(skin);
    morseBox.setBackground("blue_square_border");
    morseBox.pad(16);
    for (int i = 0; i < morseDigits.length; i++) {
      if (i > 0) {
        Label spacer = Scene2dElementFactory.createLabel("|", 28, Color.GRAY);
        morseBox.add(spacer).padLeft(16).padRight(16);
      }
      Label digitLabel =
          Scene2dElementFactory.createLabel(morseDigits[i], 28, new Color(0, 0.5f, 0, 1));
      digitLabel.setAlignment(Align.center);
      morseBox.add(digitLabel).center();
    }
    table.add(morseBox).growX().padBottom(20).row();

    table
        .add(Scene2dElementFactory.createHorizontalDivider())
        .height(2)
        .pad(0, 0, 15, 0)
        .fillX()
        .row();

    // Closing filler
    Label body3 =
        Scene2dElementFactory.createLabel(
            "Please keep this document in a safe location and do not share it with "
                + "unauthorized personnel. If you believe this document was delivered in "
                + "error, contact SecuGate Support immediately.",
            18,
            Color.DARK_GRAY);
    body3.setWrap(true);
    body3.setAlignment(Align.left);
    table.add(body3).growX().left().padBottom(20).row();

    table
        .add(Scene2dElementFactory.createHorizontalDivider())
        .height(1)
        .pad(0, 0, 10, 0)
        .fillX()
        .row();

    // Auto-generated notice
    Label autoNotice =
        Scene2dElementFactory.createLabel(
            "This is an automatically generated document.\n"
                + "SecuGate Systems GmbH - Secure Access Solutions since 1997",
            12,
            Color.GRAY);
    autoNotice.setWrap(true);
    autoNotice.setAlignment(Align.left);
    table.add(autoNotice).growX().left().padBottom(10).row();

    return scrollPane;
  }

  @Override
  protected void updateState(ComputerStateComponent newStateComp) {}

  @Override
  public void onRemove() {
    ComputerStateLocal.getInstance().openFiles().remove(fileName);
  }
}
