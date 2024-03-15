package item;

import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.badlogic.gdx.utils.TimeUtils;

public class EffectScheduler {

  private static EffectScheduler INSTANCE;
  private final DelayedRemovalArray<ScheduledAction> scheduledActions = new DelayedRemovalArray<>();

  private EffectScheduler() {}

  public static EffectScheduler getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new EffectScheduler();
    }
    return INSTANCE;
  }

  public void update() {
    long currentTime = TimeUtils.millis();
    this.scheduledActions.begin();
    for (int i = 0; i < this.scheduledActions.size; i++) {
      ScheduledAction scheduledAction = this.scheduledActions.get(i);
      if (currentTime >= scheduledAction.executeAt) {
        scheduledAction.action.applyEffect();
        this.scheduledActions.removeIndex(i);
      }
    }
    this.scheduledActions.end();
  }

  public void scheduleAction(TimedEffect action, long delayMillis) {
    long executeAt = TimeUtils.millis() + delayMillis;
    this.scheduledActions.add(new ScheduledAction(action, executeAt));
  }

  public void clear() {
    this.scheduledActions.clear();
  }

  private static class ScheduledAction {
    TimedEffect action;
    long executeAt;

    ScheduledAction(TimedEffect action, long executeAt) {
      this.action = action;
      this.executeAt = executeAt;
    }
  }
}
