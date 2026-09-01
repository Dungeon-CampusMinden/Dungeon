package rooms.lasthour.modules.computer;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Tracks when new blog comments become visible and whether the blog tab should request attention.
 */
public final class BlogCommentAttentionTracker {

  /** Describes how the blog tab's attention indicator should change after an update. */
  public enum AttentionChange {
    /** No change to the attention indicator. */
    NONE,
    /** The blog tab should request attention. */
    REQUEST,
    /** The blog tab should dismiss its attention request. */
    DISMISS
  }

  private static final float DEFAULT_CHECK_INTERVAL_SECONDS = 1.0f;

  private final IntSupplier visibleCommentCount;
  private final IntSupplier acknowledgedCommentCount;
  private final IntConsumer acknowledgedCommentCountUpdater;
  private final IntConsumer commentViewed;
  private final float checkIntervalSeconds;

  private float nextCheckAtSeconds = 0f;
  private int lastVisibleCommentCount = 0;

  /**
   * Creates a tracker with the default check interval.
   *
   * @param visibleCommentCount supplies the current number of visible comments
   * @param acknowledgedCommentCount supplies the number of acknowledged comments
   * @param acknowledgedCommentCountUpdater persists the acknowledged comment count
   * @param commentViewed receives the zero-based index of each newly viewed comment
   */
  public BlogCommentAttentionTracker(
      IntSupplier visibleCommentCount,
      IntSupplier acknowledgedCommentCount,
      IntConsumer acknowledgedCommentCountUpdater,
      IntConsumer commentViewed) {
    this(
        visibleCommentCount,
        acknowledgedCommentCount,
        acknowledgedCommentCountUpdater,
        commentViewed,
        DEFAULT_CHECK_INTERVAL_SECONDS);
  }

  /**
   * Creates a tracker with a custom check interval.
   *
   * @param visibleCommentCount supplies the current number of visible comments
   * @param acknowledgedCommentCount supplies the number of acknowledged comments
   * @param acknowledgedCommentCountUpdater persists the acknowledged comment count
   * @param commentViewed receives the zero-based index of each newly viewed comment
   * @param checkIntervalSeconds minimum number of seconds between checks
   */
  public BlogCommentAttentionTracker(
      IntSupplier visibleCommentCount,
      IntSupplier acknowledgedCommentCount,
      IntConsumer acknowledgedCommentCountUpdater,
      IntConsumer commentViewed,
      float checkIntervalSeconds) {
    this.visibleCommentCount = visibleCommentCount;
    this.acknowledgedCommentCount = acknowledgedCommentCount;
    this.acknowledgedCommentCountUpdater = acknowledgedCommentCountUpdater;
    this.commentViewed = commentViewed;
    this.checkIntervalSeconds = checkIntervalSeconds;
  }

  /**
   * Computes the initial attention state when the computer dialog is opened.
   *
   * @param blogTabActive whether the blog tab is currently the active tab
   * @param blogTabPresent whether the blog tab is present at all
   * @return the resulting attention change
   */
  public AttentionChange initialize(boolean blogTabActive, boolean blogTabPresent) {
    int currentVisible = visibleCommentCount.getAsInt();
    int acknowledged = acknowledgedCommentCount.getAsInt();
    if (acknowledged < 0) {
      acknowledged = 0;
      acknowledgedCommentCountUpdater.accept(acknowledged);
    }

    lastVisibleCommentCount = currentVisible;
    nextCheckAtSeconds = 0f;

    if (!blogTabPresent || currentVisible <= acknowledged) {
      return AttentionChange.NONE;
    }
    if (blogTabActive) {
      acknowledgeVisibleComments(currentVisible);
      return AttentionChange.DISMISS;
    }
    return AttentionChange.REQUEST;
  }

  /** Acknowledges all currently visible comments because the blog tab was viewed. */
  public void onBlogTabViewed() {
    acknowledgeVisibleComments(visibleCommentCount.getAsInt());
  }

  /**
   * Periodically re-evaluates whether the blog tab should request attention.
   *
   * @param nowSeconds the current time in seconds
   * @param blogTabActive whether the blog tab is currently the active tab
   * @param blogTabPresent whether the blog tab is present at all
   * @return the resulting attention change
   */
  public AttentionChange tick(float nowSeconds, boolean blogTabActive, boolean blogTabPresent) {
    if (!blogTabPresent || nowSeconds < nextCheckAtSeconds) {
      return AttentionChange.NONE;
    }
    nextCheckAtSeconds = nowSeconds + checkIntervalSeconds;

    int currentVisible = visibleCommentCount.getAsInt();
    if (currentVisible <= lastVisibleCommentCount) {
      lastVisibleCommentCount = currentVisible;
      return AttentionChange.NONE;
    }

    if (blogTabActive) {
      acknowledgeVisibleComments(currentVisible);
      return AttentionChange.DISMISS;
    }

    int acknowledged = acknowledgedCommentCount.getAsInt();
    if (acknowledged < 0) {
      acknowledged = lastVisibleCommentCount;
      acknowledgedCommentCountUpdater.accept(acknowledged);
    }
    lastVisibleCommentCount = currentVisible;

    if (currentVisible <= acknowledged) {
      return AttentionChange.NONE;
    }
    return AttentionChange.REQUEST;
  }

  private void acknowledgeVisibleComments(int visibleCommentCount) {
    int acknowledged = Math.max(0, acknowledgedCommentCount.getAsInt());
    for (int commentIndex = acknowledged; commentIndex < visibleCommentCount; commentIndex++) {
      commentViewed.accept(commentIndex);
    }
    acknowledgedCommentCountUpdater.accept(visibleCommentCount);
    lastVisibleCommentCount = visibleCommentCount;
  }
}
