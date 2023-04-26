package ecs.entities;


import ecs.components.PositionComponent;

/**Basic Baustein für fallen, Haupsächlich wird hier nur Position Component himzugefügt*/

public class Traps extends Entity {
    public int usages;
    public Traps() {
        super();
        usages= 10000;
        new PositionComponent(this);
    }

    void setUsages(int usages){
        this.usages=usages;
    }
    int getUsages(){
        return usages;
    }
}
