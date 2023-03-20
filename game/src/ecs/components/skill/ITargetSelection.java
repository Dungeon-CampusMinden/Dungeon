package ecs.components.skill;

import java.io.Serializable;
import tools.Point;

public interface ITargetSelection extends Serializable {

    Point selectTargetPoint();
}
