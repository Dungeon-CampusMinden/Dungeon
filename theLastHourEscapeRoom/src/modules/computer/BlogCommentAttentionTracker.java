package modules.computer;

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
  private final float checkIntervalSeconds;

  private float nextCheckAtSeconds = 0f;
  private int lastVisibleCommentCount = 0;

  /**
   * Creates a tracker with the default check interval.
   *
   * @param visibleCommentCount supplies the current number of visible comments
   * @param acknowledgedCommentCount supplies the number of acknowledged comments
   * @param acknowledgedCommentCountUpdater persists the acknowledged comment count
   */
  public BlogCommentAttentionTracker(
      IntSupplier visibleCommentCount,
      IntSupplier acknowledgedCommentCount,
      IntConsumer acknowledgedCommentCountUpdater) {
    this(
        visibleCommentCount,
        acknowledgedCommentCount,
        acknowledgedCommentCountUpdater,
        DEFAULT_CHECK_INTERVAL_SECONDS);
  }

  /**
   * Creates a tracker with a custom check interval.
   *
   * @param visibleCommentCount supplies the current number of visible comments
   * @param acknowledgedCommentCount supplies the number of acknowledged comments
   * @param acknowledgedCommentCountUpdater persists the acknowledged comment count
   * @param checkIntervalSeconds minimum number of seconds between checks
   */
  public BlogCommentAttentionTracker(
      IntSupplier visibleCommentCount,
      IntSupplier acknowledgedCommentCount,
      IntConsumer acknowledgedCommentCountUpdater,
      float checkIntervalSeconds) {
    this.visibleCommentCount = visibleCommentCount;
    this.acknowledgedCommentCount = acknowledgedCommentCount;
    this.acknowledgedCommentCountUpdater = acknowledgedCommentCountUpdater;
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
      acknowledged = currentVisible;
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
    acknowledgedCommentCountUpdater.accept(visibleCommentCount);
    lastVisibleCommentCount = visibleCommentCount;
  }
}
