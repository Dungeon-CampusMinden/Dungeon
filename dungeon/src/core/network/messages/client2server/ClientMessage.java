package core.network.messages.client2server;

import core.network.messages.NetworkMessage;

public sealed interface ClientMessage extends NetworkMessage
    permits HeroMoveCommand, HeroTargetMoveCommand, UseSkillCommand, InteractCommand {

  void process();
}
