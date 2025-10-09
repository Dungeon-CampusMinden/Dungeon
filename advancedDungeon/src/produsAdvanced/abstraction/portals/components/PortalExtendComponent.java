package produsAdvanced.abstraction.portals.components;

import core.Component;

public class PortalExtendComponent implements Component {
  public boolean throughBlue;
  public boolean throughGreen;

  public boolean checkGreen(){
    return throughGreen;
  }

  public boolean checkBlue(){
    return throughBlue;
  }
}
