package contrib.modules.levelHide;

import core.Component;
import core.utils.Rectangle;

public record LevelHideComponent(Rectangle region, float transitionSize) implements Component {}
