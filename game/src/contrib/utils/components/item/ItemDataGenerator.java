package contrib.utils.components.item;

import core.utils.components.draw.Animation;

import java.util.List;
import java.util.Random;

/** Generator which creates a random ItemData based on the Templates prepared. */
public class ItemDataGenerator {
    private static final List<String> artusamakBookTexture = List.of("items/Inventory/artusamak_book.png");
    private static final List<String> basaltTexture = List.of("items/Inventory/basalt.png");
    private static final List<String> bearArmorTexture = List.of("items/Inventory/bear_armor.png");
    private static final List<String> bearHelmetTexture = List.of("items/Inventory/bear_helmet.png");
    private static final List<String> bearPantsTexture = List.of("items/Inventory/bear_pants.png");
    private static final List<String> beerTexture = List.of("items/Inventory/beer.png");
    private static final List<String> beholderRingTexture = List.of("items/Inventory/beholder_ring.png");
    private static final List<String> berryTexture = List.of("items/Inventory/berry.png");
    private static final List<String> blobsTexture = List.of("items/Inventory/blobs.png");
    private static final List<String> boneTexture = List.of("items/Inventory/bone.png");
    private static final List<String> brezelTexture = List.of("items/Inventory/brezel.png");
    private static final List<String> chainmailArmorTexture = List.of("items/Inventory/chainmail_armor.png");
    private static final List<String> chainmailHelmetTexture = List.of("items/Inventory/chainmail_helmet.png");
    private static final List<String> chainmailPantsTexture = List.of("items/Inventory/chainmail_pants.png");
    private static final List<String> cheeseTexture = List.of("items/Inventory/cheese.png");
    private static final List<String> chickenArmorTexture = List.of("items/Inventory/chicken_armor.png");
    private static final List<String> chickenHelmetTexture = List.of("items/Inventory/chicken_helmet.png");
    private static final List<String> chickenPantsTexture = List.of("items/Inventory/chicken_pants.png");
    private static final List<String> cloverLeafTexture = List.of("items/Inventory/cloverleaf.png");
    private static final List<String> coffeeTexture = List.of("items/Inventory/coffee.png");
    private static final List<String> donutTexture = List.of("items/Inventory/donut.png");
    private static final List<String> dragonToothTexture = List.of("items/Inventory/dragon_tooth.png");
    private static final List<String> dragonWingTexture = List.of("items/Inventory/dragon_wing.png");
    private static final List<String> eggTexture = List.of("items/Inventory/egg.png");
    private static final List<String> emeraldTexture = List.of("items/Inventory/emerald.png");

    private static final List<String> fireGlovesTexture = List.of("items/Inventory/fire_gloves.png");

    private static final List<String> goldTexture = List.of("items/Inventory/gold.png");

    private static final List<String> goldenNecklaseTexture = List.of("items/Inventory/golden_necklace.png");

    private static final List<String> heartRingTexture = List.of("items/Inventory/heart_ring.png");

    private static final List<String> heroArmorTexture = List.of("items/Inventory/hero_armor.png");

    private static final List<String> heroHelmetTexture = List.of("items/Inventory/hero_helmet.png");

    private static final List<String> heroPantsTexture = List.of("items/Inventory/hero_pants.png");

    private static final List<String> knightShieldTexture = List.of("items/Inventory/knight_shield.png");

    private static final List<String> kleafTexture = List.of("items/Inventory/leaf.png");

    private static final List<String> leatherTexture = List.of("items/Inventory/leather.png");

    private static final List<String> magicNecklaceTexture = List.of("items/Inventory/magic_necklace.png");

    private static final List<String> magicScrollTexture = List.of("items/Inventory/magic_scroll.png");

    private static final List<String> meatTexture = List.of("items/Inventory/meat.png");

    private static final List<String> opalTexture = List.of("items/Inventory/opal.png");

    private static final List<String> pinkArmorTexture = List.of("items/Inventory/pink_armor.png");

    private static final List<String> pinkHelmetTexture = List.of("items/Inventory/pink_helmet.png");

    private static final List<String> pinkPantsTexture = List.of("items/Inventory/pink_pants.png");

    private static final List<String> saphirTexture = List.of("items/Inventory/saphir.png");

    private static final List<String> skullTexture = List.of("items/Inventory/skull.png");

    private static final List<String> spellBookTexture = List.of("items/Inventory/spell_book.png");

    private static final List<String> steelTexture = List.of("items/Inventory/steel.png");

    private static final List<String> steelGlovesTexture = List.of("items/Inventory/steel_gloves.png");

    private static final List<String> sulphurTexture = List.of("items/Inventory/sulphur.png");

    private static final List<String> tentacleTexture = List.of("items/Inventory/tentacle.png");

    private static final List<String> toadstoolTexture = List.of("items/Inventory/toadstool.png");

    private static final List<String> topasTexture = List.of("items/Inventory/topas.png");

    private static final List<String> undeadArmorTexture = List.of("items/Inventory/undead_armor.png");

    private static final List<String> undeadHelmetTexture = List.of("items/Inventory/undead_helmet.png");

    private static final List<String> undeadPantsTexture = List.of("items/Inventory/undead_pants.png");

    private static final List<String> vikingShieldTexture = List.of("items/Inventory/viking_shield.png");

    private static final List<String> wisdomScrollTexture = List.of("items/Inventory/wisdom_scroll.png");

    private static final List<String> woodTexture = List.of("items/Inventory/wood.png");

    private final List<ItemData> templates =
            List.of(
                new ItemData(
                    ItemType.Basic,
                    ItemNature.BOOK,
                    new Animation(artusamakBookTexture, 1),
                    new Animation(artusamakBookTexture, 1),
                    "Zauberbuch mir der Ergänzung der uralten Kultur Artusamk",
                    "Zaubersprüche der Kampfkunst."),

                new ItemData(
                        ItemType.Basic,
                        ItemNature.RESSOURCE,
                        new Animation(basaltTexture, 1),
                        new Animation(basaltTexture, 1),
                        "Basalt",
                        "Basalte ist ein dunkler magmatischen Naturstein."),

                new ItemData(
                        ItemType.Active,
                        ItemNature.ARMOR,
                        new Animation(bearArmorTexture, 1),
                        new Animation(bearArmorTexture, 1),
                        "Bärenrüstung",
                        "Rüstung mit der einfachen Schutzstufe"),

                new ItemData(
                        ItemType.Basic,
                        ItemNature.HELMET,
                        new Animation(bearHelmetTexture, 1),
                        new Animation(bearHelmetTexture, 1),
                        "Bärenhelm",
                        "Helm mit der einfachen Schutzstufe"),

                new ItemData(
                        ItemType.Basic,
                        ItemNature.PANTS,
                        new Animation(bearPantsTexture, 1),
                        new Animation(bearPantsTexture, 1),
                        "Bärenhose",
                        "Hose mit der einfachen Schutzstufe"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(beerTexture, 1),
                    new Animation(beerTexture, 1),
                    "Bier",
                    "Sehr schmackhaftes Getränk"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RING,
                    new Animation(beholderRingTexture, 1),
                    new Animation(beholderRingTexture, 1),
                    "Ring des Betrachters",
                    "Ring erlaubt die Umgbung zu erkünden"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(berryTexture, 1),
                    new Animation(berryTexture, 1),
                    "Beere",
                    "Sehr nahrhaft und schmackhaft"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(blobsTexture, 1),
                    new Animation(blobsTexture, 1),
                    "Kleckse",
                    "Fallen tropfenweise"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(boneTexture, 1),
                    new Animation(boneTexture, 1),
                    "Knochen",
                    "Jemand ist hier gestorben"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(brezelTexture, 1),
                    new Animation(brezelTexture, 1),
                    "Brezel",
                    "Sehr nahrhaft und schmackhaft"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.ARMOR,
                    new Animation(chainmailArmorTexture, 1),
                    new Animation(chainmailArmorTexture, 1),
                    "Kettenhemd",
                    "Hat die erhöchte Schutzstufe"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.HELMET,
                    new Animation(chainmailHelmetTexture, 1),
                    new Animation(chainmailHelmetTexture, 1),
                    "Kettenhelm",
                    "Hat die erhöchte Schutzstufe"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.PANTS,
                    new Animation(chainmailPantsTexture, 1),
                    new Animation(chainmailPantsTexture, 1),
                    "Kettenhose",
                    "Hat die erhöchte Schutzstufe"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(cheeseTexture, 1),
                    new Animation(cheeseTexture, 1),
                    "Käse",
                    "Sehr nahrhaft und schmackhaft"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.ARMOR,
                    new Animation(chickenArmorTexture, 1),
                    new Animation(chickenArmorTexture, 1),
                    "Hühnerpanzer",
                    "Hat die einfache Schutzstufe"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.HELMET,
                    new Animation(chickenHelmetTexture, 1),
                    new Animation(chickenHelmetTexture, 1),
                    "Hühnerhelm",
                    "Hat die einfache Schutzstufe"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.PANTS,
                    new Animation(chickenPantsTexture, 1),
                    new Animation(chickenPantsTexture, 1),
                    "Hühnerhose",
                    "Hat die einfache Schutzstufe"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(cloverLeafTexture, 1),
                    new Animation(cloverLeafTexture, 1),
                    "Kleeblatt",
                    "Bringt viel Glück"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(coffeeTexture, 1),
                    new Animation(coffeeTexture, 1),
                    "Kaffeebohne",
                        "Schmackhaftes Getränk läßt sich daraus machen"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(donutTexture, 1),
                    new Animation(donutTexture, 1),
                    "Donut",
                    "Sehr nahrhaft und schmackhaft"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(dragonToothTexture, 1),
                    new Animation(dragonToothTexture, 1),
                    "Drachenzahn",
                    "Verleiht sehr starke Intelligenz und magische Begabung"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(dragonWingTexture, 1),
                    new Animation(dragonWingTexture, 1),
                    "Drachenflügel",
                    "Verleiht Flugfähigkeit und Feueratem"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(eggTexture, 1),
                    new Animation(eggTexture, 1),
                    "Ei",
                    "Was für ein geschöpf gebohren wird ist aber noch unklar"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(emeraldTexture, 1),
                    new Animation(emeraldTexture, 1),
                    "Smaragd",
                    "Smaragd ist eine dunkel- bis hellgrüne Varietät des Silikat-Minerals Beryll"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.GLOVES,
                    new Animation(fireGlovesTexture, 1),
                    new Animation(fireGlovesTexture, 1),
                    "Feuer Handschuhe",
                    "Handschue mit der höheren Sicherheitstufe und Brennverhalten"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(goldTexture, 1),
                    new Animation(goldTexture, 1),
                    "Gold",
                    "Es ist ein chemisches Element mit höhem Wert"),


                new ItemData(
                    ItemType.Basic,
                    ItemNature.NECKLACE,
                    new Animation(goldenNecklaseTexture, 1),
                    new Animation(goldenNecklaseTexture, 1),
                    "Goldene Halskette",
                    "Ist schwer und hat betimmt einen höhen Wert"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RING,
                    new Animation(heartRingTexture, 1),
                    new Animation(heartRingTexture, 1),
                    "Herzen Ring",
                    "Bringt liebe und viel Glück"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.ARMOR,
                    new Animation(heroArmorTexture, 1),
                    new Animation(heroArmorTexture, 1),
                    "Heldenrüstung",
                    "Hat höchste Sicherheitstufe und bringt viel Glück"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.HELMET,
                    new Animation(heroHelmetTexture, 1),
                    new Animation(heroHelmetTexture, 1),
                    "Heldenhelm",
                    "Hat höchste Sicherheitstufe und bringt viel Glück"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.PANTS,
                    new Animation(heroPantsTexture, 1),
                    new Animation(heroPantsTexture, 1),
                    "Heldenhose",
                    "Haben höchste Sicherheitstufe und bringt viel Glück"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.SHIELD,
                    new Animation(knightShieldTexture, 1),
                    new Animation(knightShieldTexture, 1),
                    "Heldenhose",
                    "Mittlere Sicherheitstufe"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(kleafTexture, 1),
                    new Animation(kleafTexture, 1),
                    "Blatt",
                    "Dient zum Kochen, Heilen und SChmücken"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(leatherTexture, 1),
                    new Animation(leatherTexture, 1),
                    "Leder",
                    "Leder ist robust und vielfältig einsetzbar"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.NECKLACE,
                    new Animation(magicNecklaceTexture, 1),
                    new Animation(magicNecklaceTexture, 1),
                    "Magische Halskette",
                    "Verleiht starke Magie, Kräfte und macht junger"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.BOOK,
                    new Animation(magicScrollTexture, 1),
                    new Animation(magicScrollTexture, 1),
                    "Magische Rolle",
                    "Enthält sehr wirksame Zaubersprüche"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(meatTexture, 1),
                    new Animation(meatTexture, 1),
                    "Fleisch",
                    "Ist sehr lecker. Für Tier und Mensch"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(opalTexture, 1),
                    new Animation(opalTexture, 1),
                    "Opal",
                    "Er erhöht die Lebenskraft und wirkt Antriebslosigkeit entgegen"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.ARMOR,
                    new Animation(pinkArmorTexture, 1),
                    new Animation(pinkArmorTexture, 1),
                    "Rosa Panzer",
                    "Mittlere Sicherheitstufe für Krigerinnen"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.HELMET,
                    new Animation(pinkHelmetTexture, 1),
                    new Animation(pinkHelmetTexture, 1),
                    "Rosa Helm",
                    "Mittlere Sicherheitstufe für Krigerinnen"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.PANTS,
                    new Animation(pinkPantsTexture, 1),
                    new Animation(pinkPantsTexture, 1),
                    "Rosa Hose",
                    "Mittlere Sicherheitstufe für Krigerinnen"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(saphirTexture, 1),
                    new Animation(saphirTexture, 1),
                    "Saphir",
                    "Hat die Fähigkeit die Nerven zu beruhigen und sie zu kräftigen."),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(skullTexture, 1),
                    new Animation(skullTexture, 1),
                    "Totenkopf",
                    "Zeichen des Todes da er die Zerbrechlichkeit und Kürze des Lebens symbolisiert"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.BOOK,
                    new Animation(spellBookTexture, 1),
                    new Animation(spellBookTexture, 1),
                    "Zauberbuch",
                    "Es ist ein Buch mit magischem Wissen"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(steelTexture, 1),
                    new Animation(steelTexture, 1),
                    "Stahl",
                    "Stahl ist widerstandsfähig, hat eine große Härte und ist zäh"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.GLOVES,
                    new Animation(steelGlovesTexture, 1),
                    new Animation(steelGlovesTexture, 1),
                    "Stahlhandschuhe",
                    "Die hat höhe Sicherheitstufe"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(sulphurTexture, 1),
                    new Animation(sulphurTexture, 1),
                    "Schwefel",
                    "Aussehen, unter Normalbedingungen: gelber Feststoff"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(tentacleTexture, 1),
                    new Animation(tentacleTexture, 1),
                    "Tentakel",
                    "Fangarme befinden sich am Kopf von wirbellosen Tieren"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(toadstoolTexture, 1),
                    new Animation(toadstoolTexture, 1),
                    "Fliegenpilz",
                    "Ist mit seinem auffälligen roten, weiß gepunkteten Hut weit und gut zu sehen"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(topasTexture, 1),
                    new Animation(topasTexture, 1),
                    "Topas",
                    "Er regt zur Entfaltung der eigenen Fähigkeiten an"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.ARMOR,
                    new Animation(undeadArmorTexture, 1),
                    new Animation(undeadArmorTexture, 1),
                    "Untoten-Rüstung",
                    "Höchste Sicherheitstufe"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.HELMET,
                    new Animation(undeadHelmetTexture, 1),
                    new Animation(undeadHelmetTexture, 1),
                    "Untoten-Helm",
                    "Höchste Sicherheitstufe"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.PANTS,
                    new Animation(undeadPantsTexture, 1),
                    new Animation(undeadPantsTexture, 1),
                    "Untoten-Helm",
                    "Höchste Sicherheitstufe"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.SHIELD,
                    new Animation(vikingShieldTexture, 1),
                    new Animation(vikingShieldTexture, 1),
                    "Vikinger-chield",
                    "Mittlere Sicherheitstufe"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.BOOK,
                    new Animation(wisdomScrollTexture, 1),
                    new Animation(wisdomScrollTexture, 1),
                    "Schriftrolle der Weisheit",
                    "Die Weisheiten dieer Schriftrollen sind die Lebenstützen"),

                new ItemData(
                    ItemType.Basic,
                    ItemNature.RESSOURCE,
                    new Animation(woodTexture, 1),
                    new Animation(woodTexture, 1),
                    "Holz",
                    "Holz ist ein vielseitiger Rohstoff: Möbel, Fußböden, Häuser")
                );
    private final Random rand = new Random();

    /**
     * @return a new randomItemData
     */
    public ItemData generateItemData() {
        return templates.get(rand.nextInt(templates.size()));
    }
}
