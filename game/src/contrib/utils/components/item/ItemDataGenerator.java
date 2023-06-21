package contrib.utils.components.item;

import core.utils.components.draw.Animation;

import java.util.List;
import java.util.Random;

/** Generator which creates a random ItemData based on the Templates prepared. */
public class ItemDataGenerator {
    private static final List<String> artusamakBookTexture =
            List.of("items/books/artusamak_book.png");
    private static final List<String> magicScrollTexture = List.of("items/books/magic_scroll.png");
    private static final List<String> spellBookTexture = List.of("items/books/spell_book.png");
    private static final List<String> wisdomScrollTexture =
            List.of("items/books/wisdom_scroll.png");
    private static final List<String> letterTexture = List.of("items/books/letter.png");
    private static final List<String> beholderRingTexture = List.of("items/ring/beholder_ring.png");
    private static final List<String> heartRingTexture = List.of("items/ring/heart_ring.png");
    private static final List<String> fireGlovesTexture = List.of("items/gloves/fire_gloves.png");
    private static final List<String> steelGlovesTexture = List.of("items/gloves/steel_gloves.png");
    private static final List<String> goldenNecklaseTexture =
            List.of("items/necklace/golden_necklace.png");
    private static final List<String> magicNecklaceTexture =
            List.of("items/necklace/magic_necklace.png");
    private static final List<String> knightShieldTexture =
            List.of("items/shield/knight_shield.png");
    private static final List<String> vikingShieldTexture =
            List.of("items/shield/viking_shield.png");
    private static final List<String> bearArmorTexture = List.of("items/armor/bear_armor.png");
    private static final List<String> chainmailArmorTexture =
            List.of("items/armor/chainmail_armor.png");
    private static final List<String> chickenArmorTexture =
            List.of("items/armor/chicken_armor.png");
    private static final List<String> plateArmorTexture = List.of("items/armor/plate_armor.png");
    private static final List<String> pinkArmorTexture = List.of("items/armor/pink_armor.png");
    private static final List<String> undeadArmorTexture = List.of("items/armor/undead_armor.png");
    private static final List<String> bearHelmetTexture = List.of("items/helmet/bear_helmet.png");
    private static final List<String> chainmailHelmetTexture =
            List.of("items/helmet/chainmail_helmet.png");
    private static final List<String> chickenHelmetTexture =
            List.of("items/helmet/chicken_helmet.png");
    private static final List<String> plateHelmetTexture = List.of("items/helmet/plate_helmet.png");
    private static final List<String> pinkHelmetTexture = List.of("items/helmet/pink_helmet.png");
    private static final List<String> undeadHelmetTexture =
            List.of("items/helmet/undead_helmet.png");
    private static final List<String> bearPantsTexture = List.of("items/pants/bear_pants.png");
    private static final List<String> chainmailPantsTexture =
            List.of("items/pants/chainmail_pants.png");
    private static final List<String> chickenPantsTexture =
            List.of("items/pants/chicken_pants.png");
    private static final List<String> platePantsTexture = List.of("items/pants/plate_pants.png");
    private static final List<String> pinkPantsTexture = List.of("items/pants/pink_pants.png");
    private static final List<String> undeadPantsTexture = List.of("items/pants/undead_pants.png");
    private static final List<String> basaltTexture = List.of("items/resource/basalt.png");
    private static final List<String> beerTexture = List.of("items/resource/beer.png");
    private static final List<String> berryTexture = List.of("items/resource/berry.png");
    private static final List<String> blobsTexture = List.of("items/resource/blobs.png");
    private static final List<String> boneTexture = List.of("items/resource/bone.png");
    private static final List<String> brezelTexture = List.of("items/resource/brezel.png");
    private static final List<String> cheeseTexture = List.of("items/resource/cheese.png");
    private static final List<String> cloverLeafTexture = List.of("items/resource/cloverleaf.png");
    private static final List<String> coffeeTexture = List.of("items/resource/coffee.png");
    private static final List<String> donutTexture = List.of("items/resource/donut.png");
    private static final List<String> dragonToothTexture =
            List.of("items/resource/dragon_tooth.png");
    private static final List<String> dragonWingTexture = List.of("items/resource/dragon_wing.png");
    private static final List<String> eggTexture = List.of("items/resource/egg.png");
    private static final List<String> emeraldTexture = List.of("items/resource/emerald.png");
    private static final List<String> goldTexture = List.of("items/resource/gold.png");
    private static final List<String> kleafTexture = List.of("items/resource/leaf.png");
    private static final List<String> leatherTexture = List.of("items/resource/leather.png");
    private static final List<String> meatTexture = List.of("items/resource/meat.png");
    private static final List<String> opalTexture = List.of("items/resource/opal.png");
    private static final List<String> saphirTexture = List.of("items/resource/saphir.png");
    private static final List<String> skullTexture = List.of("items/resource/skull.png");
    private static final List<String> steelTexture = List.of("items/resource/steel.png");
    private static final List<String> sulphurTexture = List.of("items/resource/sulphur.png");
    private static final List<String> tentacleTexture = List.of("items/resource/tentacle.png");
    private static final List<String> toadstoolTexture = List.of("items/resource/toadstool.png");
    private static final List<String> topasTexture = List.of("items/resource/topas.png");
    private static final List<String> woodTexture = List.of("items/resource/wood.png");
    private static final List<String> fireSwordTexture = List.of("items/weapon/fire_sword.png");
    private static final List<String> iceSwordTexture = List.of("items/weapon/ice_sword.png");
    private static final List<String> legendarySwordTexture =
            List.of("items/weapon/legendary_sword.png");
    private static final List<String> lightningSwordTexture =
            List.of("items/weapon/lightning_sword.png");
    private static final List<String> rainbowSwordTexture =
            List.of("items/weapon/rainbow_sword.png");
    private static final List<String> snakeSwordTexture = List.of("items/weapon/snake_sword.png");
    private static final List<String> blueKeyTexture = List.of("items/key/blue_key.png");
    private static final List<String> goldKeyTexture = List.of("items/key/gold_key.png");
    private static final List<String> redKeyTexture = List.of("items/key/red_key.png");
    private static final List<String> anidotePotionTexture =
            List.of("items/potion/antidote_potion.png");
    private static final List<String> healthPotionTexture =
            List.of("items/potion/health_potion.png");
    private static final List<String> manaPotionTexture = List.of("items/potion/mana_potion.png");

    private final List<ItemData> templates =
            List.of(
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.BOOK,
                            new Animation(artusamakBookTexture, 1),
                            new Animation(artusamakBookTexture, 1),
                            "Buch des Artusamak",
                            "legendäre Kampfkunst der etwas anderen Art."),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.BOOK,
                            new Animation(magicScrollTexture, 1),
                            new Animation(magicScrollTexture, 1),
                            "magische Rolle",
                            "Enthält sehr wirksame Zaubersprüche"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.BOOK,
                            new Animation(spellBookTexture, 1),
                            new Animation(spellBookTexture, 1),
                            "Zauberbuch",
                            "Ein Buch mit magischem Wissen"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.BOOK,
                            new Animation(wisdomScrollTexture, 1),
                            new Animation(wisdomScrollTexture, 1),
                            "Schriftrolle der Weisheit",
                            "ermöglicht einen Aufstieg zur Intelligenzbestie"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.BOOK,
                            new Animation(letterTexture, 1),
                            new Animation(letterTexture, 1),
                            "Brief",
                            "Enthält private Inhalte"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RING,
                            new Animation(beholderRingTexture, 1),
                            new Animation(beholderRingTexture, 1),
                            "Ring des Betrachters",
                            "erweitert den Horizont"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RING,
                            new Animation(heartRingTexture, 1),
                            new Animation(heartRingTexture, 1),
                            "Herz Ring",
                            "betört Gegner mit seinem Liebreiz"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.GLOVES,
                            new Animation(fireGlovesTexture, 1),
                            new Animation(fireGlovesTexture, 1),
                            "Feuer Handschuhe",
                            "verbrenne die Gegner mit einem high five"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.GLOVES,
                            new Animation(steelGlovesTexture, 1),
                            new Animation(steelGlovesTexture, 1),
                            "Stahl Handschuhe",
                            "hart aber gerecht"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.NECKLACE,
                            new Animation(goldenNecklaseTexture, 1),
                            new Animation(goldenNecklaseTexture, 1),
                            "Goldene Halskette",
                            "zum funkeln und glänzen"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.NECKLACE,
                            new Animation(magicNecklaceTexture, 1),
                            new Animation(magicNecklaceTexture, 1),
                            "Magische Halskette",
                            "jeder kann damit ein großer Zauberer sein"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.SHIELD,
                            new Animation(knightShieldTexture, 1),
                            new Animation(knightShieldTexture, 1),
                            "Ritterlicher Schild",
                            "hält Gegner edel vom Leib"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.SHIELD,
                            new Animation(vikingShieldTexture, 1),
                            new Animation(vikingShieldTexture, 1),
                            "Wikinger Schild",
                            "kreisrund und hält gesund"),
                    new ItemData(
                            ItemType.Active,
                            ItemNature.ARMOR,
                            new Animation(bearArmorTexture, 1),
                            new Animation(bearArmorTexture, 1),
                            "Bärenrüstung",
                            "Rüstung für unbändige Berserker"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.ARMOR,
                            new Animation(chainmailArmorTexture, 1),
                            new Animation(chainmailArmorTexture, 1),
                            "Kettenhemd",
                            "Loch an Loch und hält doch"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.ARMOR,
                            new Animation(chickenArmorTexture, 1),
                            new Animation(chickenArmorTexture, 1),
                            "Hühner Rüstung",
                            "für Kampfhähne und nichts für feige Hühner"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.ARMOR,
                            new Animation(plateArmorTexture, 1),
                            new Animation(plateArmorTexture, 1),
                            "Platten Rüstung",
                            "schwer zu durchdringen"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.ARMOR,
                            new Animation(pinkArmorTexture, 1),
                            new Animation(pinkArmorTexture, 1),
                            "Rosa Rüstung",
                            "nicht nur für zart beseitete Gemüter"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.ARMOR,
                            new Animation(undeadArmorTexture, 1),
                            new Animation(undeadArmorTexture, 1),
                            "Untote Rüstung",
                            "selbst nach dem Tod immer noch unschlagbar"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.HELMET,
                            new Animation(bearHelmetTexture, 1),
                            new Animation(bearHelmetTexture, 1),
                            "Bärenhelm",
                            "süße Ewok Kopfbedeckung"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.HELMET,
                            new Animation(chainmailHelmetTexture, 1),
                            new Animation(chainmailHelmetTexture, 1),
                            "Kettenhelm",
                            "einmal drauf immer auf"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.HELMET,
                            new Animation(chickenHelmetTexture, 1),
                            new Animation(chickenHelmetTexture, 1),
                            "Hühnerhelm",
                            "mit Gegacker ins Schlachtgetümmel"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.HELMET,
                            new Animation(plateHelmetTexture, 1),
                            new Animation(plateHelmetTexture, 1),
                            "Plattenhelm",
                            "mit dem Kopf durch die Wand"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.HELMET,
                            new Animation(pinkHelmetTexture, 1),
                            new Animation(pinkHelmetTexture, 1),
                            "Rosa Helm",
                            "unterstreicht die zarte Seite"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.HELMET,
                            new Animation(undeadHelmetTexture, 1),
                            new Animation(undeadHelmetTexture, 1),
                            "Untoten-Helm",
                            "Hirntod ausgeschlossen"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.PANTS,
                            new Animation(bearPantsTexture, 1),
                            new Animation(bearPantsTexture, 1),
                            "Bärenhose",
                            "verleiht Bärenkräfte und ist flauschig und warm"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.PANTS,
                            new Animation(chainmailPantsTexture, 1),
                            new Animation(chainmailPantsTexture, 1),
                            "Kettenhose",
                            "sicher und gut belüftet"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.PANTS,
                            new Animation(chickenPantsTexture, 1),
                            new Animation(chickenPantsTexture, 1),
                            "Hühnerhose",
                            "nur keine Eier legen"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.PANTS,
                            new Animation(pinkPantsTexture, 1),
                            new Animation(pinkPantsTexture, 1),
                            "Rosa Hose",
                            "der Hingucker im Kampf"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.PANTS,
                            new Animation(undeadPantsTexture, 1),
                            new Animation(undeadPantsTexture, 1),
                            "Untoten-Hose",
                            "alles fit im Schritt"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(basaltTexture, 1),
                            new Animation(basaltTexture, 1),
                            "Basalt",
                            "vulkanisches Gestein. Enthält Eisen und Magnesium."),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(beerTexture, 1),
                            new Animation(beerTexture, 1),
                            "Bier",
                            "flüssiges Brot"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(berryTexture, 1),
                            new Animation(berryTexture, 1),
                            "Beere",
                            "nahrhaft und schmackhaft"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(blobsTexture, 1),
                            new Animation(blobsTexture, 1),
                            "Blobs",
                            "niedliche Lebewesen für Zaubertränke"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(boneTexture, 1),
                            new Animation(boneTexture, 1),
                            "Knochen",
                            "alt und knackig"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(brezelTexture, 1),
                            new Animation(brezelTexture, 1),
                            "Brezel",
                            "salziges Gebäck"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(cheeseTexture, 1),
                            new Animation(cheeseTexture, 1),
                            "Käse",
                            "riecht und schmeckt zugleich"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(cloverLeafTexture, 1),
                            new Animation(cloverLeafTexture, 1),
                            "Kleeblatt",
                            "Bringt Glück"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(coffeeTexture, 1),
                            new Animation(coffeeTexture, 1),
                            "Kaffee",
                            "belebt Körper und Geist"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(donutTexture, 1),
                            new Animation(donutTexture, 1),
                            "Donut",
                            "süß und lecker für zwischendurch"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(dragonToothTexture, 1),
                            new Animation(dragonToothTexture, 1),
                            "Drachenzahn",
                            "für Zaubertränke und Zahnprothesen"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(dragonWingTexture, 1),
                            new Animation(dragonWingTexture, 1),
                            "Drachenflügel",
                            "geschuppte Chicken wings"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(eggTexture, 1),
                            new Animation(eggTexture, 1),
                            "Ei",
                            "brüte es aus und finde es raus. Keine Haftung für den Verlust von Gliedmaßen"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(emeraldTexture, 1),
                            new Animation(emeraldTexture, 1),
                            "Smaragd",
                            "Grün"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(goldTexture, 1),
                            new Animation(goldTexture, 1),
                            "Gold",
                            "Lockt Drachen und Elstern gleichermaßen an"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.PANTS,
                            new Animation(platePantsTexture, 1),
                            new Animation(platePantsTexture, 1),
                            "Plattenhose",
                            "schwer und bissfest"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(kleafTexture, 1),
                            new Animation(kleafTexture, 1),
                            "Blatt",
                            "vielseitig verwendbar"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(leatherTexture, 1),
                            new Animation(leatherTexture, 1),
                            "Leder",
                            "robust und vielfältig einsetzbar"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(meatTexture, 1),
                            new Animation(meatTexture, 1),
                            "Fleisch",
                            "lecker und nicht Vegan"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(opalTexture, 1),
                            new Animation(opalTexture, 1),
                            "Opal",
                            "die Seele des Berges"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(saphirTexture, 1),
                            new Animation(saphirTexture, 1),
                            "Saphir",
                            "blau schimmernder Edelstein"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(skullTexture, 1),
                            new Animation(skullTexture, 1),
                            "Schädel",
                            "Resultat einer friedlichen Auseinandersetzung. Als Trinkgefäß geeignet"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(steelTexture, 1),
                            new Animation(steelTexture, 1),
                            "Stahl",
                            "widerstandsfähig und hart"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(sulphurTexture, 1),
                            new Animation(sulphurTexture, 1),
                            "Schwefel",
                            "riecht gut"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(tentacleTexture, 1),
                            new Animation(tentacleTexture, 1),
                            "Tentakel",
                            "schleimig mit Saugnäpfen"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(toadstoolTexture, 1),
                            new Animation(toadstoolTexture, 1),
                            "Fliegenpilz",
                            "ändert die Realität und macht groß und stark"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(topasTexture, 1),
                            new Animation(topasTexture, 1),
                            "Topas",
                            "Steine mit der Farbe frischen Öls"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.RESSOURCE,
                            new Animation(woodTexture, 1),
                            new Animation(woodTexture, 1),
                            "Holz",
                            "vielseitiger Rohstoff der nicht Termitensicher ist"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.WEAPON,
                            new Animation(fireSwordTexture, 1),
                            new Animation(fireSwordTexture, 1),
                            "Feuer Schwert",
                            "zum grillen und einäschern von Feinden"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.WEAPON,
                            new Animation(iceSwordTexture, 1),
                            new Animation(iceSwordTexture, 1),
                            "Eis Schwert",
                            "eiskalt und glasklar"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.WEAPON,
                            new Animation(legendarySwordTexture, 1),
                            new Animation(legendarySwordTexture, 1),
                            "Legendäres Schwert",
                            "Das Schwert aus dem Stein für glohrreiche Taten"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.WEAPON,
                            new Animation(lightningSwordTexture, 1),
                            new Animation(lightningSwordTexture, 1),
                            "Blitz Schwert",
                            "altertümlicher elektroschocker oder defibrillator"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.WEAPON,
                            new Animation(rainbowSwordTexture, 1),
                            new Animation(rainbowSwordTexture, 1),
                            "Regenbogen Schwert",
                            "bringt Farbe in den Tag"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.WEAPON,
                            new Animation(snakeSwordTexture, 1),
                            new Animation(snakeSwordTexture, 1),
                            "Schlangen Schwert",
                            "giftig und geschmeidig zugleich"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.KEY,
                            new Animation(blueKeyTexture, 1),
                            new Animation(blueKeyTexture, 1),
                            "Blauer Schlüssel",
                            "Der Schlüssel zu himmlischen Reichtümern. Freibier für alle!"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.KEY,
                            new Animation(goldKeyTexture, 1),
                            new Animation(goldKeyTexture, 1),
                            "Goldener Schlüssel",
                            "eröffnet neue Möglichkeiten sich zu bereichern"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.KEY,
                            new Animation(redKeyTexture, 1),
                            new Animation(redKeyTexture, 1),
                            "Roter Schlüssel",
                            "schiebe ihn rein und schau ob er passt"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.POTION,
                            new Animation(anidotePotionTexture, 1),
                            new Animation(anidotePotionTexture, 1),
                            "Gegengift",
                            "bei vergiftung jeglicher Art"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.POTION,
                            new Animation(healthPotionTexture, 1),
                            new Animation(healthPotionTexture, 1),
                            "Lebenselixier",
                            "verlängert das Leben"),
                    new ItemData(
                            ItemType.Basic,
                            ItemNature.POTION,
                            new Animation(manaPotionTexture, 1),
                            new Animation(manaPotionTexture, 1),
                            "Mana",
                            "stellt Mana wieder her"));
    private final Random rand = new Random();

    /**
     * @return a new randomItemData
     */
    public ItemData generateItemData() {
        return templates.get(rand.nextInt(templates.size()));
    }
}
