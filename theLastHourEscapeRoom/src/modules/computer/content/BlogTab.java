package modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import core.utils.Scene2dElementFactory;
import java.util.List;
import modules.computer.ComputerStateComponent;
import util.Lore;

/**
 * Computer tab that displays a blog with comments, as the help system in the Last Hour escape room.
 */
public class BlogTab extends ComputerTab {

  private static BlogTab Instance;

  private static int timestampOfLogin = 0;
  private int lastVisibleCommentCount = 0;

  /**
   * Constructs a new BlogTab with the given shared ComputerStateComponent.
   *
   * @param sharedState the shared computer state component
   */
  public BlogTab(ComputerStateComponent sharedState) {
    super(sharedState, "blog", "My Blog", false);
    Instance = this;
  }

  /**
   * Gets the singleton instance of BlogTab.
   *
   * @return the current BlogTab instance, or null if none exists
   */
  public static BlogTab getInstance() {
    return Instance;
  }

  /**
   * Returns the number of seconds elapsed since the login timestamp.
   *
   * @return seconds since login, or 0 if no login timestamp has been set
   */
  public static int secondsSinceLogin() {
    int timestampOfLogin =
        ComputerStateComponent.getState().map(ComputerStateComponent::timestampOfLogin).orElse(0);
    if (timestampOfLogin == 0) return 0;
    return (int) (System.currentTimeMillis() / 1000L) - timestampOfLogin;
  }

  /**
   * Returns whether a comment should be visible based on the time elapsed since login.
   *
   * @param comment the blog comment to check
   * @return true if the comment's display delay has been reached
   */
  public static boolean isCommentVisible(BlogComment comment) {
    return secondsSinceLogin() >= comment.timeBeforeDisplay();
  }

  /**
   * Counts the total number of currently visible comments across all blog entries.
   *
   * @return the total count of visible comments
   */
  public static int countVisibleComments() {
    int count = 0;
    for (BlogTab.BlogEntry entry : Lore.BlogEntries) {
      for (BlogTab.BlogComment comment : entry.comments()) {
        if (isCommentVisible(comment)) count++;
      }
    }
    return count;
  }

  protected void createActors() {
    this.clearChildren();
    this.clearActions();

    lastVisibleCommentCount = countVisibleComments();

    Label title = Scene2dElementFactory.createLabel(Lore.ScientistBlogName, 48, Color.BLACK);

    Table container = new Table(skin);
    container.top();

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

    // Periodically check if new comments should appear
    this.addAction(
        Actions.forever(
            Actions.sequence(
                Actions.delay(5f),
                Actions.run(
                    () -> {
                      int current = countVisibleComments();
                      if (current != lastVisibleCommentCount) {
                        createActors();
                      }
                    }))));
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
      List<BlogComment> visibleComments =
          entry.comments.stream().filter(BlogTab::isCommentVisible).toList();

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

      if (!visibleComments.isEmpty()) {
        Table commentsTable = new Table();
        for (int i = 0; i < visibleComments.size(); i++) {
          BlogComment comment = visibleComments.get(i);
          commentsTable.add(createCommentTable(comment, i == 0)).growX().row();
        }
        table.add(commentsTable).growX().row();
      } else {
        Label noComments = Scene2dElementFactory.createLabel("No comments yet.", 18, Color.GRAY);
        table.add(noComments).growX().padTop(4).row();
      }
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
  public void onShow() {
    int current = countVisibleComments();
    if (current != lastVisibleCommentCount) {
      createActors();
    }
  }

  @Override
  protected void updateState(ComputerStateComponent newStateComp) {}

  /**
   * Data class representing a blog entry.
   *
   * @param title the title of the blog entry
   * @param content the content of the blog entry
   * @param comments the list of comments on the blog entry
   */
  public record BlogEntry(String title, String content, List<BlogComment> comments) {}

  /**
   * Data class representing a comment on a blog entry.
   *
   * @param user the username of the commenter
   * @param content the content of the comment
   * @param timeBeforeDisplay the time in seconds before the comment should be displayed after the
   *     blog entry is shown
   */
  public record BlogComment(String user, String content, float timeBeforeDisplay) {}
}
