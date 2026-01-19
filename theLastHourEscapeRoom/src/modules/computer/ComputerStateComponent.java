package modules.computer;

import core.Component;

import java.io.Serializable;

public record ComputerStateComponent(ComputerState state) implements Component, Serializable {
}
