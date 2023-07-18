package contrib.utils.components.interaction;

import contrib.components.InteractionComponent;

import core.Entity;
import core.components.PositionComponent;
import core.utils.position.Point;
import core.utils.position.Position;

public record InteractionData(
        Entity e, PositionComponent pc, InteractionComponent ic, float dist, Position unitDir) {}
