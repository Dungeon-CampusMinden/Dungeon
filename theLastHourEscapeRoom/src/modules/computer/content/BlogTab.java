package modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import core.utils.Scene2dElementFactory;
import java.util.List;
import modules.computer.ComputerStateComponent;
import util.Lore;

public class BlogTab extends ComputerTab {

  private static final String TITLE = "Brenner Blog";
  private float timeSinceLogin = 0f; // TODO: Fill from shared state somehow

  public BlogTab(ComputerStateComponent sharedState) {
    super(sharedState, "blog", "My Blog", false);
  }

  protected void createActors() {
    this.clearChildren();

    Label title = Scene2dElementFactory.createLabel(TITLE, 48, Color.BLACK);

    Table container = new Table(skin);
    container.top();

    // TODO: show entries based on context
    List<BlogEntry> entriesToShow = Lore.BlogEntries;

    for (int i = 0; i < entriesToShow.size(); i++) {
      BlogEntry entry = entriesToShow.get(i);
      Table entryTable = createBlogEntryTable(entry);
      Cell<Table> cell = container.add(entryTable).growX();
      if (i != entriesToShow.size() - 1) {
        cell.padBottom(15).row();
      }
      cell.row();
    }

    this.add(title).padBottom(30).row();
    this.add(Scene2dElementFactory.createScrollPane(container, false, true))
        .maxWidth(800)
        .minWidth(500)
        .grow();
  }

  private Table createBlogEntryTable(BlogEntry entry) {
    Label title = Scene2dElementFactory.createLabel(entry.title, 28, Color.BLACK);
    Label content = Scene2dElementFactory.createLabel(entry.content, 18, Color.BLACK);
    content.setWrap(true);

    Table table = new Table(skin);
    table.setBackground("generic-area-depth");
    table.pad(15);

    table.add(title).growX().colspan(2).row();
    table.add(content).growX().padTop(10).colspan(2).row();

    if (!entry.comments.isEmpty()) {
      Table commentsTable = new Table();
      for (int i = 0; i < entry.comments.size(); i++) {
        BlogComment comment = entry.comments.get(i);
        // TODO: add check for when comments are shown
        commentsTable.add(createCommentTable(comment, i == 0)).growX().row();
      }

      table
          .add(Scene2dElementFactory.createHorizontalDivider())
          .growX()
          .height(4)
          .pad(20, 0, 15, 0)
          .colspan(2)
          .row();
      table
          .add(Scene2dElementFactory.createVerticalDivider())
          .growY()
          .padLeft(50)
          .padRight(10)
          .width(4);
      table.add(commentsTable).growX().row();
    }

    return table;
  }

  private Table createCommentTable(BlogComment comment, boolean isFirst) {
    Label user =
        Scene2dElementFactory.createLabel("\"" + comment.user + "\" says:", 16, Color.BLACK);
    Label content = Scene2dElementFactory.createLabel(comment.content, 18, Color.BLACK);
    content.setWrap(true);

    Table table = new Table();

    if (!isFirst) {
      table
          .add(Scene2dElementFactory.createHorizontalDivider())
          .growX()
          .height(4)
          .pad(10, 0, 15, 0)
          .row();
    }
    table.add(user).growX().row();
    table.add(content).growX().padTop(4).row();

    return table;
  }

  @Override
  protected void updateState(ComputerStateComponent newStateComp) {}

  public record BlogEntry(String title, String content, List<BlogComment> comments) {}

  public record BlogComment(String user, String content, float timeBeforeDisplay) {}
}
