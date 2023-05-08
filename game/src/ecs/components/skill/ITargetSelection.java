package ecs.components.skill;

import tools.Point;

import java.io.Serializable;

public interface ITargetSelection extends Serializable {

    Point selectTargetPoint();
}
