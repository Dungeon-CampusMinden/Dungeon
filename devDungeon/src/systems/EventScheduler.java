package systems;

import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.badlogic.gdx.utils.TimeUtils;
import core.System;
import utils.TimedEffect;

public class EventScheduler extends System {

  private static EventScheduler INSTANCE;
  private final DelayedRemovalArray<ScheduledAction> scheduledActions = new DelayedRemovalArray<>();

  private EventScheduler() {}

  public static EventScheduler getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new EventScheduler();
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

  @Override
  public void execute() {
    this.update();
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
