package portal.riddles;

import core.Entity;
import portal.tractorBeam.TractorBeamFactory;
import portal.tractorBeam.TractorBeamLever;

public class MyTractorBeamLever extends TractorBeamLever {

  @Override
  public void reverse(Entity tractorBeam) {
    TractorBeamFactory.reverse(tractorBeam);
  }
}
