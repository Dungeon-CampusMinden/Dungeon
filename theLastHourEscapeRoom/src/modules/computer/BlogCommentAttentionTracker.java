package modules.computer;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Tracks when new blog comments become visible and whether the blog tab should request attention.
 */
public final class BlogCommentAttentionTracker {

  public enum AttentionChange {
    NONE,
    REQUEST,
    DISMISS
  }

  private static final float DEFAULT_CHECK_INTERVAL_SECONDS = 1.0f;

  private final IntSupplier visibleCommentCount;
  private final IntSupplier acknowledgedCommentCount;
  private final IntConsumer acknowledgedCommentCountUpdater;
  private final float checkIntervalSeconds;

  private float nextCheckAtSeconds = 0f;
  private int lastVisibleCommentCount = 0;

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

  public void onBlogTabViewed() {
    acknowledgeVisibleComments(visibleCommentCount.getAsInt());
  }

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
