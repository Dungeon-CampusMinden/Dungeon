package contrib.utils.components.item;
/*Ausrüstung, Items und Ressorcen sind in die Kategorien eingeteilt. Damit bestimmte Items nur an bestimmten Stellen ausgerüstet
 * werden können (z.B Helm kann nicht als Hose ausgerüstet werden)*/
public enum ItemNature {
    UNDEFINED("DefaultSlotBackground"),
    PANTS("PantSlotBackground"),
    ARMOR("ArmorSlotBackground"),
    SHIELD("ShieldSlotBackground"),
    GLOVES("GlovesSlotBackground"),
    NECKLACE("NecklaceSlotBackground"),
    HELMET("HelmetSlotBackground"),
    WEAPON("WeaponSlotBackground"),
    /*Schrifftrollen, Briefe, Bücher und andere Schrifften fallen unter die Kategorie BOOK*/
    BOOK("BookSlotBackground"),
    RING("RingSlotBackground"),
    RESSOURCE("RessourceSlotBackground"),
    POTION("PotionSlotBackground"),
    KEY("KeySlotBackground");

    ItemNature(String slotBackground) {
        this.slotBackground = slotBackground;
    }

    public String slotBackground;
}
