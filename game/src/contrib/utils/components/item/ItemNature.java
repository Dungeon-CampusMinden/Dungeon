package contrib.utils.components.item;
/*Ausrüstung, Items und Ressorcen sind in die Kategorien eingeteilt. Damit bestimmte Items nur an bestimmten Stellen ausgerüstet
 * werden können (z.B Helm kann nicht als Hose ausgerüstet werden)*/
public enum ItemNature {
    UNDEFINED,
    PANTS,
    ARMOR,
    SHIELD,
    GLOVES,
    NECKLACE,
    HELMET,
    WEAPON,
    /*Schrifftrollen, Briefe, Bücher und andere Schrifften fallen unter die Kategorie BOOK*/
    BOOK,
    RING,
    RESSOURCE,
    POTION,
    KEY
}
