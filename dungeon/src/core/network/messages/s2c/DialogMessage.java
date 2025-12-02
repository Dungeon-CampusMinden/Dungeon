package core.network.messages.s2c;

import contrib.components.UIComponent;
import core.network.messages.NetworkMessage;

public record DialogMessage(UIComponent component) implements NetworkMessage {}
