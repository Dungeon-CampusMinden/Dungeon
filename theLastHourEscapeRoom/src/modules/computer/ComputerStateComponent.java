package modules.computer;

import core.Component;
import core.Entity;
import core.game.ECSManagement;

import java.io.Serializable;
import java.util.Set;

public record ComputerStateComponent(ComputerState state) implements Component, Serializable {

  public static void setState(ComputerState state){
    ECSManagement.levelEntities(Set.of(ComputerStateComponent.class)).findFirst().ifPresent(e -> {
      e.remove(ComputerStateComponent.class);
      e.add(new ComputerStateComponent(state));
    });
  }

  public static ComputerStateComponent getState(){
    ComputerStateComponent csc = ECSManagement.levelEntities(Set.of(ComputerStateComponent.class))
        .findFirst()
        .flatMap(e -> e.fetch(ComputerStateComponent.class))
        .orElse(null);
    if(csc == null){
      throw new IllegalStateException("No ComputerStateComponent found in current level!");
    }
    return csc;
  }

}
