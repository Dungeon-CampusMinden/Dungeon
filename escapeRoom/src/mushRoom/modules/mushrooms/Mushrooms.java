package mushRoom.modules.mushrooms;

import com.badlogic.gdx.graphics.Color;

public enum Mushrooms {
  RedBlue(
      true,
      Color.RED,
      Color.BLUE,
      "Roter Pilz",
      "Ein roter Pilz mit blauem Schimmer",
      "Auch bekannt als 'Des Täuschers Herz'\nWährend das Rot lebendig wirkt, verrät der blaue Schimmer das Vorhandensein von Zyanid-Verbindungen im Gewebe. Schon eine kleine Menge würde auch den größten Krieger sofort hinrichten."),

  RedYellow(
      false,
      Color.RED,
      Color.YELLOW,
      "Roter Pilz",
      "Ein roter Pilz mit gelbem Schimmer",
      "Der 'Phönix-Schwamm'\nDas Gelb am Rand erinnert an die aufgehende Sonne. Alte Schriften besagen, dass er das Blut reinigt und Fieber senkt. Er fühlt sich warm an."),

  GreenGreen(
      false,
      Color.GREEN,
      Color.GREEN,
      "Grüner Pilz",
      "Ein grüner Pilz mit grünem Schimmer",
      "Der 'Waldwächter'\nEr tarnt sich perfekt im Moos. Er enthält wichtige Nährstoffe und stärkende Essenzen. Er schmeckt zwar bitter wie alte Rinde, ist aber für die Basis des Heiltranks hervorragend geeignet."),

  GreenBrown(
      true,
      Color.GREEN,
      new Color(0.36f, 0.26f, 0.2f, 1f),
      "Grüner Pilz",
      "Ein grüner Pilz mit braunem Schimmer",
      "'Sumpfleiche'\nDer braune Rand ist kein Schmutz, sondern Nekrose des Pilzes selbst. Er riecht modrig und süßlich. Die Einnahme führt zu Lähmungserscheinungen. Für einen Trank absolut unbrauchbar."),

  BlueRed(
      false,
      Color.BLUE,
      Color.RED,
      "Blauer Pilz",
      "Ein blauer Pilz mit rotem Schimmer",
      "Die 'Königsbeere'\nDie rote Umrandung wirkt wie eine Warnung der Natur, ist aber tatsächlich ein Schutzmechanismus gegen Insekten. Für Menschen ist das Fleisch jedoch harmlos und wirkt stark schmerzlindernd."),

  BlueCyan(
      true,
      Color.BLUE,
      Color.CYAN,
      "Blauer Pilz",
      "Ein blauer Pilz mit türkisem Schimmer",
      "Der 'Kaltgriff'\nDas helle Cyan leuchtet im Dunkeln. Wer ihn berührt, spürt sofortige Taubheit in den Fingern. Er lässt das Blut in den Adern gefrieren. Eine Zutat für Gifte, nicht für Heilung."),

  CyanOrange(
      true,
      Color.CYAN,
      Color.ORANGE,
      "Türkiser Pilz",
      "Ein türkiser Pilz mit orangem Schimmer",
      "'Narrenfeuer'\nDie Farbkombination kommt so in der Natur kaum vor und wirkt fast künstlich. Er verursacht schwere Halluzinationen und Wahnvorstellungen."),

  CyanRed(
      false,
      Color.CYAN,
      Color.RED,
      "Türkiser Pilz",
      "Ein türkiser Pilz mit rotem Schimmer",
      "Die 'Lebensader'\nSehr selten zu finden. Die rote Linie pulsiert manchmal schwach, wenn man den Pilz pflückt. Er ist bekannt dafür, innere Blutungen zu stillen. Ein Glücksfund!"),

  MagentaWhite(
      false,
      Color.MAGENTA,
      Color.WHITE,
      "Magenta Pilz",
      "Ein magenta Pilz mit weißem Schimmer",
      "Der 'Feensitz'\nEr sieht aus wie aus einem Kindermärchen. Der weiße Rand symbolisiert Reinheit. Er wirkt entgiftend auf die Leber und stärkt den Willen zu leben. Eine exzellente Zutat."),

  MagentaBlack(
      true,
      Color.MAGENTA,
      Color.BLACK,
      "Magenta Pilz",
      "Ein magenta Pilz mit schwarzem Schimmer",
      "Der 'Witwenmacher'\nDer schwarze Rand scheint das Licht der Umgebung zu verschlucken. Er entzieht dem Körper Sauerstoff. Nur für dunkle Rituale geeignet, nicht zum Verzehr."),

  YellowGreen(
      true,
      Color.YELLOW,
      Color.GREEN,
      "Gelber Pilz",
      "Ein gelber Pilz mit grünem Schimmer",
      "'Gallenkappe'\nDas Grün erinnert an ranzige Galle. Er verursacht heftiges Erbrechen und Krämpfe, nicht gerade von Vorteil für Abenteurer. Finger weg!"),

  YellowMagenta(
      false,
      Color.YELLOW,
      Color.MAGENTA,
      "Gelber Pilz",
      "Ein gelber Pilz mit magenta Schimmer",
      "'Morgendämmerung'\nDie Farben erinnern an den Sonnenaufgang nach einer langen Nacht. Er bringt Energie und Wärme in den Körper zurück.");

  public final boolean poisonous;
  public final Color baseColor;
  public final Color outlineColor;

  public final String name;
  public final String descriptionShort;
  public final String descriptionLong;

  Mushrooms(
      boolean poisonous,
      Color baseColor,
      Color outlineColor,
      String name,
      String descriptionShort,
      String descriptionLong) {
    this.poisonous = poisonous;
    this.baseColor = baseColor;
    this.outlineColor = outlineColor;
    this.name = name;
    this.descriptionShort = descriptionShort;
    this.descriptionLong = descriptionLong;
  }

  public String getTexturePath() {
    return "@gen/mushrooms/" + this.name().toLowerCase() + ".png";
  }

  public Color getColor() {
    return baseColor;
  }

  public Color getOutlineColor() {
    return outlineColor;
  }

  public String getName() {
    return name;
  }

  public String getDescriptionShort() {
    return descriptionShort;
  }

  public String getDescription() {
    return descriptionLong;
  }
}
