package rooms.morphco.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import feature.hud.dialogs.DialogContext;

/** A book page of the factory: one sheet of paper on the work area, filled by the subclass. */
abstract class PaperUI extends MorphCoConsoleUI {

  private static final float MAX_WIDTH = 980;

  private final PanelNode paper = area().addNode(new PanelNode("paper", PanelNode.Look.PAPER, 34));

  /** Content of the sheet. */
  final Table sheet = paper.content();

  /**
   * Creates the frame with an empty sheet.
   *
   * @param context dialog context of the server dialog
   * @param title localized title
   */
  PaperUI(DialogContext context, String title) {
    super(context, title, "", "");
  }

  @Override
  protected float arrange(float width, float height) {
    float paperWidth = Math.min(MAX_WIDTH, width - 12);
    // Room above the sheet for its strip of tape.
    float paperHeight = paper.fit(paperWidth);
    paper.position((width - paperWidth) / 2, -16 - paperHeight);
    return paperHeight + 28;
  }
}
