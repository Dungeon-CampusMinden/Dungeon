package contrib.systems;

import contrib.components.AttachmentComponent;
import core.Entity;
import core.System;
import core.components.PositionComponent;
import core.utils.components.MissingComponentException;

import java.util.HashMap;

public class AttachmentSystem extends System {

  private static HashMap<PositionComponent, PositionComponent> attachmentMap = new HashMap<>();

  public AttachmentSystem() {
    super(AttachmentComponent.class);
    onEntityRemove = (entity -> {
      entity.fetch(PositionComponent.class).ifPresent(pc -> {
        if(attachmentMap.containsKey(pc)) {
          attachmentMap.remove(pc);
        } else if (attachmentMap.containsValue(pc)) {
          attachmentMap.remove(attachmentMap.keySet().stream().filter(positionComponent -> attachmentMap.get(positionComponent).equals(pc)).findFirst().get());
        }
      });
    });
  }

  @Override
  public void execute() {
    filteredEntityStream()
      .map(this::buildDataObject)
      .forEach(this::applyAttachment);
  }


  public static void registerAttachment(PositionComponent copy, PositionComponent origin) {
    attachmentMap.put(copy, origin);
  }

  public static void unregisterAttachment(PositionComponent copy) {
    attachmentMap.remove(copy);
  }

  private ASData buildDataObject(Entity e) {
    PositionComponent copypc =
      e.fetch(PositionComponent.class)
        .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class));

    AttachmentComponent ac =
      e.fetch(AttachmentComponent.class)
        .orElseThrow(() -> MissingComponentException.build(e, AttachmentComponent.class));

    PositionComponent originpc = attachmentMap.get(copypc);

    return new ASData(e, ac, copypc, originpc);
  }

  private void applyAttachment(ASData asData) {
    if (asData.ac.isRotatingWithOrigin()) {
      asData.copypc.position(asData.originpc.position().translate(asData.originpc.viewDirection()));
      switch(asData.originpc.viewDirection()) {
        case UP -> {
          asData.copypc.rotation(90);
        }
        case RIGHT -> {
          asData.copypc.rotation(0);
        }
        case DOWN -> {
          asData.copypc.rotation(270);
        }
        case LEFT -> {
          asData.copypc.rotation(180);
        }
        default -> {}
      }
//      asData.copypc.rotation(asData.originpc.rotation());
    } else {
      asData.copypc.position(asData.originpc.position().translate(asData.ac.getOffset()));
    }
  }

  private record ASData(Entity e, AttachmentComponent ac, PositionComponent copypc, PositionComponent originpc) {}
}
