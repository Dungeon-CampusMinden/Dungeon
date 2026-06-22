import * as Blockly from "blockly";
import * as En from "blockly/msg/en";
import * as De from "blockly/msg/de";
import {call_language_route} from "./api/api.ts";

export const setupLanguageToggle = (workspace) => {
  document.querySelector(".flag")?.addEventListener("click", event => {
    const imgElement = event.currentTarget as HTMLImageElement;
    if (imgElement == null) return;

    const currentLang = imgElement.dataset.lang;

    if (currentLang == "en") {
      changeToEnglishLanguage(workspace);
      imgElement.dataset.lang="de"
      imgElement.src="german-flag.png"
      imgElement.alt = "German language"
      call_language_route("de");
    } else {
      changeToGermanLanguage(workspace);
      imgElement.dataset.lang="en"
      imgElement.src="english-flag.png"
      imgElement.alt = "English language"
      call_language_route("de");
    }
  })
}

const changeToEnglishLanguage = (workspace) => {
  console.log("changing language to english");

  Object.assign(Blockly.Msg, myCustomMessages.en);

  Blockly.setLocale(En as any);



  const state = Blockly.serialization.workspaces.save(workspace);

  workspace.clear();

  Blockly.serialization.workspaces.load(state, workspace);
}


const changeToGermanLanguage = (workspace) => {
  console.log("changing language to english");
  Object.assign(Blockly.Msg, myCustomMessages.de);

  Blockly.setLocale(De as any);



  const state = Blockly.serialization.workspaces.save(workspace);

  workspace.clear();

  Blockly.serialization.workspaces.load(state, workspace);
}

export const myCustomMessages = {
  en: {

    // Toolbox
    CAT_START: "Start",
    CAT_MOVEMENT: "Movement",
    CAT_DIRECTIONS: "Directions",
    CAT_INVENTORY: "Inventory & Character",
    CAT_QUERIES: "If-Statements",
    CAT_BOOLEAN: "Boolean Expressions",
    CAT_VARIABLES: "Variables",
    CAT_LOOPS: "Loops",
    CAT_CONDITIONS: "Conditions",

    // Start Block
    BLOCK_START_MSG: "Start",
    BLOCK_START_TOOLTIP: "Starting point of the game",

    // Move Block
    BLOCK_MOVE_MSG: "Move",
    BLOCK_MOVE_TOOLTIP: "Moves the player in the direction they are facing",

    // Rotate Block
    BLOCK_ROTATE_MSG: "Turn %1", // %1 ist wichtig für den Platzhalter der Richtung!
    BLOCK_ROTATE_TOOLTIP: "Turns the player left or right",

    // Set Number
    BLOCK_SET_NUMBER_TOOLTIP: "Changes the value of an existing variable",

    // Set Number Expression
    BLOCK_SET_EXPRESSION_MSG: "create and set %1 to %2",
    BLOCK_SET_EXPRESSION_TOOLTIP: "Creates a new variable with a value",

    // Expression
    BLOCK_EXPRESSION_TOOLTIP: "Arithmetic operation",
    OP_PLUS: "plus",
    OP_MINUS: "minus",
    OP_MULTIPLY: "times",
    OP_DIVIDE: "divided by",

    DIR_UP: "forward",
    DIR_DOWN: "backward",
    DIR_LEFT: "left",
    DIR_RIGHT: "right",
    DIR_HERE: "here",

    BLOCK_REPEAT_NUMBER_TOOLTIP: "Number of repetitions",

    BLOCK_LOGIC_BOOL_TOOLTIP: "Outputs 'True' or 'False'.",
    BLOCK_USUAL_COND_TOOLTIP: "Compares two values, e.g., whether they are equal or greater.",
    BLOCK_NOT_COND_TOOLTIP: "Negates or reverses a condition.",
    BLOCK_LOGIC_OP_TOOLTIP: "Combine two conditions. 'And' only if both are true. 'Or' if at least one is true.",

    // not block
    BLOCK_NOT_COND_MSG: "not %1",

    // Dropdown-operators
    OP_EQ: "equal to",
    OP_NEQ: "not equal to",
    OP_GTE: "greater than or equal to",
    OP_GT: "greater than",
    OP_LTE: "less than or equal to",
    OP_LT: "less than",
    OP_AND: "and",
    OP_OR: "or",

    BLOCK_WALL_MSG: "wall %1",
    BLOCK_WALL_TOOLTIP: "Check if there is a wall in that direction",

    // Floor
    BLOCK_FLOOR_MSG: "floor %1",
    BLOCK_FLOOR_TOOLTIP: "Check if there is a floor in that direction",

    // Pit (Loch)
    BLOCK_PIT_MSG: "pit %1",
    BLOCK_PIT_TOOLTIP: "Check if there is a pit in that direction",

    // Monster
    BLOCK_MONSTER_MSG: "monster %1",
    BLOCK_MONSTER_TOOLTIP: "Check if there is a monster in that direction",

    // Switch (Schalter)
    BLOCK_SWITCH_MSG: "switch/torch %1",
    BLOCK_SWITCH_TOOLTIP: "Check if there is a switch in that direction",

    // Active
    BLOCK_ACTIVE_MSG: "active %1",
    BLOCK_ACTIVE_TOOLTIP: "Check if there is an active object in that direction",

    // Boss
    BLOCK_BOSS_MSG: "boss looks %1",
    BLOCK_BOSS_TOOLTIP: "Check if the boss is looking in a specific direction",

    BLOCK_IF_TOOLTIP: "Executes statements if the condition is true.",
    BLOCK_IFELSE_TOOLTIP: "Executes statements if the condition is true, otherwise executes the alternative statements.",

    BLOCK_INTERACT_MSG: "Interact",
    BLOCK_INTERACT_TOOLTIP: "Interact with items",

    // Fireball
    BLOCK_FIREBALL_MSG: "Fireball",
    BLOCK_FIREBALL_TOOLTIP: "Shoot a fireball in the direction you are facing",

    // Wait
    BLOCK_WAIT_MSG: "wait",
    BLOCK_WAIT_TOOLTIP: "Wait for a short moment.",

    // Use
    BLOCK_USE_MSG: "use %1",
    BLOCK_USE_TOOLTIP: "Use the item in the desired direction.",

    // Push
    BLOCK_PUSH_MSG: "push",
    BLOCK_PUSH_TOOLTIP: "Push a movable object forward.",

    // Pull
    BLOCK_PULL_MSG: "pull",
    BLOCK_PULL_TOOLTIP: "Pull a movable object backward.",

    // Blue Portal
    BLOCK_BLUE_PORTAL_MSG: "shoot blue portal",
    BLOCK_BLUE_PORTAL_TOOLTIP: "Shoot a blue portal in the direction you are facing",

    // Green Portal
    BLOCK_GREEN_PORTAL_MSG: "shoot green portal",
    BLOCK_GREEN_PORTAL_TOOLTIP: "Shoot a green portal in the direction you are facing"

  },
  de: {
    // Toolbox
    CAT_START: "Start",
    CAT_MOVEMENT: "Bewegung",
    CAT_DIRECTIONS: "Richtungen",
    CAT_INVENTORY: "Inventar & Charakter",
    CAT_QUERIES: "Abfragen",
    CAT_BOOLEAN: "Wahrheitsausdruecke",
    CAT_VARIABLES: "Variablen",
    CAT_LOOPS: "Schleife",
    CAT_CONDITIONS: "Bedingung",

    // Start Block
    BLOCK_START_MSG: "Start",
    BLOCK_START_TOOLTIP: "Startpunkt des Spiels",

    // Move Block
    BLOCK_MOVE_MSG: "Gehe",
    BLOCK_MOVE_TOOLTIP: "Bewegt den Spieler in die Richtung in die er schaut",

    // Rotate Block
    BLOCK_ROTATE_MSG: "Drehe %1",
    BLOCK_ROTATE_TOOLTIP: "Dreht den Spieler nach links oder rechts",

    // Set Number
    BLOCK_SET_NUMBER_TOOLTIP: "Verändert den Wert einer existierenden Variable",

    //  Set Number Expression ===
    BLOCK_SET_EXPRESSION_MSG: "erstelle und setze %1 auf %2",
    BLOCK_SET_EXPRESSION_TOOLTIP: "Erstellt eine neue Variable mit einem Wert",

    // Expression (calculations)
    BLOCK_EXPRESSION_TOOLTIP: "Rechenoperation",
    OP_PLUS: "plus",
    OP_MINUS: "minus",
    OP_MULTIPLY: "mal",
    OP_DIVIDE: "geteilt",

    // directions
    DIR_UP: "vorne",
    DIR_DOWN: "hinten",
    DIR_LEFT: "links",
    DIR_RIGHT: "rechts",
    DIR_HERE: "hier",

    BLOCK_REPEAT_NUMBER_TOOLTIP: "Anzahl der Wiederholungen",


    // tooltips boolean
    BLOCK_LOGIC_BOOL_TOOLTIP: "Gibt 'Wahr' oder 'Falsch' aus.",
    BLOCK_USUAL_COND_TOOLTIP: "Vergleicht zwei Werte, z.B. ob sie gleich oder größer sind.",
    BLOCK_NOT_COND_TOOLTIP: "Verneint oder kehrt eine Bedingung um.",
    BLOCK_LOGIC_OP_TOOLTIP: "Verknüpfe zwei Bedingungen. 'Und' nur, wenn beide wahr sind. 'Oder', wenn mindestens eine wahr ist.",

    // not block
    BLOCK_NOT_COND_MSG: "nicht %1",

    // Dropdown-Operatoren
    OP_EQ: "gleich",
    OP_NEQ: "ungleich",
    OP_GTE: "größer gleich",
    OP_GT: "größer",
    OP_LTE: "kleiner gleich",
    OP_LT: "kleiner",
    OP_AND: "und",
    OP_OR: "oder",

    BLOCK_WALL_MSG: "Wand %1",
    BLOCK_WALL_TOOLTIP: "Überprüfe, ob eine Wand in die Richtung ist",

    // Floor
    BLOCK_FLOOR_MSG: "Boden %1",
    BLOCK_FLOOR_TOOLTIP: "Überprüfe, ob ein Boden in die Richtung ist",

    // Pit (Loch)
    BLOCK_PIT_MSG: "Loch %1",
    BLOCK_PIT_TOOLTIP: "Überprüfe, ob ein Loch in die Richtung ist",

    // Monster
    BLOCK_MONSTER_MSG: "Monster %1",
    BLOCK_MONSTER_TOOLTIP: "Überprüfe, ob ein Monster in die Richtung ist",

    // Switch (Schalter)
    BLOCK_SWITCH_MSG: "Schalter/Fackel %1",
    BLOCK_SWITCH_TOOLTIP: "Überprüfe, ob ein Schalter in die Richtung ist",

    // Active
    BLOCK_ACTIVE_MSG: "aktiv %1",
    BLOCK_ACTIVE_TOOLTIP: "Überprüfe, ob ein aktives Objekt in der Richtung ist",

    // Boss
    BLOCK_BOSS_MSG: "boss guckt %1",
    BLOCK_BOSS_TOOLTIP: "Überprüfe, ob der Boss in eine bestimmte Richtung guckt",

    BLOCK_IF_TOOLTIP: "Führt Anweisung aus, wenn die Bedingung wahr ist.",
    BLOCK_IFELSE_TOOLTIP: "Führt Anweisung aus, wenn die Bedingung wahr ist, sonst andere Anweisung.",

    BLOCK_INTERACT_MSG: "Interagieren",
    BLOCK_INTERACT_TOOLTIP: "Mit Items interagieren",

    // Fireball
    BLOCK_FIREBALL_MSG: "Feuerball",
    BLOCK_FIREBALL_TOOLTIP: "Feuerball in Richtung schießen",

    // Wait
    BLOCK_WAIT_MSG: "warte",
    BLOCK_WAIT_TOOLTIP: "Warte für einen kurzen Moment.",

    // Use
    BLOCK_USE_MSG: "benutzen %1",
    BLOCK_USE_TOOLTIP: "Benutze den Gegenstand in der gewünschten Richtung.",

    // Push
    BLOCK_PUSH_MSG: "schiebe",
    BLOCK_PUSH_TOOLTIP: "Schieb ein bewegliches Objekt vorwärts.",

    // Pull
    BLOCK_PULL_MSG: "ziehen",
    BLOCK_PULL_TOOLTIP: "Zieh ein bewegliches Objekt rückwärts.",

    // Blue Portal
    BLOCK_BLUE_PORTAL_MSG: "blaues Portal schießen",
    BLOCK_BLUE_PORTAL_TOOLTIP: "Blaues Portal in Blickrichtung schießen",

    // Green Portal
    BLOCK_GREEN_PORTAL_MSG: "grünes Portal schießen",
    BLOCK_GREEN_PORTAL_TOOLTIP: "Grünes Portal in Blickrichtung schießen"
  }
};
