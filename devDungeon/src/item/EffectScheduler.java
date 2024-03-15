package item;

import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.badlogic.gdx.utils.TimeUtils;

public class EffectScheduler {

  private final DelayedRemovalArray<ScheduledAction> scheduledActions = new DelayedRemovalArray<>();
  private static EffectScheduler INSTANCE;

  private EffectScheduler() {}

  public static EffectScheduler getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new EffectScheduler();
    }
    return INSTANCE;
  }

  public void update() {
    long currentTime = TimeUtils.millis();
    scheduledActions.begin();
    for (int i = 0; i < scheduledActions.size; i++) {
      ScheduledAction scheduledAction = scheduledActions.get(i);
      if (currentTime >= scheduledAction.executeAt) {
        scheduledAction.action.applyEffect();
        scheduledActions.removeIndex(i);
      }
    }
    scheduledActions.end();
  }

  public void scheduleAction(TimedEffect action, long delayMillis) {
    long executeAt = TimeUtils.millis() + delayMillis;
    scheduledActions.add(new ScheduledAction(action, executeAt));
  }

  public void clear() {
    scheduledActions.clear();
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
