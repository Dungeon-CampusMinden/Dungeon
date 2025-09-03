import * as Blockly from "blockly";
import { config } from "../config.ts";

// delete predefined blocks
delete Blockly.Blocks["logic_boolean"];
delete Blockly.Blocks["controls_if"];
delete Blockly.Blocks["controls_ifelse"];

export const blocks = Blockly.common.createBlockDefinitionsFromJsonArray([
  {
    type: "start",
    message0: "Start",
    nextStatement: null,
    colour: 120,
    tooltip: "Startpunkt des Spiels",
  },
  // ---------------------- Movement ----------------------
  {
    type: "move",
    message0: "Gehe",
    previousStatement: null,
    nextStatement: null,
    colour: 180,
    tooltip: "Bewegt den Spieler in die Richtung in die er schaut",
  },
  {
    type: "goToExit",
    message0: "Gehe Zum Ausgang",
    previousStatement: null,
    nextStatement: null,
    colour: 180,
    tooltip: "Bewege dich zum Ausgang des Levels.",
  },
  {
    type: "rotate",
    message0: "Drehe %1",
    args0: [
      {
        type: "input_value",
        name: "DIRECTION",
        check: "Direction",
      },
    ],
    previousStatement: null,
    nextStatement: null,
    colour: 180,
    tooltip: "Dreht den Spieler nach links oder rechts",
  },
  // ---------------------- Variables ----------------------
  {
    type: "set_number",
    previousStatement: null,
    nextStatement: null,
    message0: "%{BKY_VARIABLES_SET}",
    tooltip: "Verändert den Wert einer existierenden Variable",
    args0: [
      {
        type: "field_variable",
        name: "VAR",
        variable: "%{BKY_VARIABLES_DEFAULT_NAME}",
      },
      {
        type: "input_value",
        name: "VALUE",
        check: ["Expression", "Array_get", "Number"],
      },
    ],
    colour:220,
  },
  {
    type: "set_number_expression",
    previousStatement: null,
    nextStatement: null,
    message0: "erstelle und %{BKY_VARIABLES_SET}",
    tooltip: "Erstellt eine neue Variable mit einem Wert",
    args0: [
      {
        type: "field_variable",
        name: "VAR",
        variable: "%{BKY_VARIABLES_DEFAULT_NAME}",
      },
      {
        type: "input_value",
        name: "VALUE",
        check: ["Expression", "Array_get", "Number"],
      },
    ],
    colour: 220,
  },
  {
    type: "get_variable",
    message0: "%1",
    args0: [
      {
        type: "field_variable",
        name: "VAR",
        variable: "%{BKY_VARIABLES_DEFAULT_NAME}",
      },
    ],
    output: "Variable",
    colour: 220,
  },
  {
    type: "expression",
    message0: "%1 %2 %3",
    args0: [
      {
        type: "input_value",
        name: "INPUT_A",
        check: ["Number", "Variable", "Array_get"],
      },
      {
        type: "field_dropdown",
        name: "OPERATOR",
        options: [
          ["plus", "+"],
          ["minus", "-"],
          ["mal", "*"],
          ["geteilt", "/"],
        ],
      },
      {
        type: "input_value",
        name: "INPUT_B",
        check: ["Number", "Variable", "Array_get"],
      },
    ],
    inputsInline: true,
    output: "Expression",
    tooltip: "Rechenoperation",
    colour: 220,
  },
  {
    type: "var_number",
    message0: "%1",
    args0: [
      {
        type: "field_number",
        name: "VAR_NUMBER",
        value: 0,
        min: 0,
        max: config.VARIABLE_MAX_VALUE,
      },
    ],
    output: "Number",
    colour: 220,
  },
  // ---------------------- Directions ----------------------
  {
    type: "direction_up",
    message0: "vorne",
    output: "Direction",
    colour: 200,
  },
  {
    type: "direction_down",
    message0: "hinten",
    output: "Direction",
    colour: 200
  },
  {
    type: "direction_left",
    message0: "links",
    output: "Direction",
    colour: 200
  },
  {
    type: "direction_right",
    message0: "rechts",
    output: "Direction",
    colour: 200
  },
  {
    type: "direction_here",
    message0: "hier",
    output: "Direction",
    colour: 200
  },
  // ---------------------- Arrays ----------------------
  {
    type: "var_array",
    previousStatement: null,
    nextStatement: null,
    message0: "Array erstellen %1",
    args0: [
      {
        type: "field_variable",
        name: "VAR",
        variable: "%{BKY_VARIABLES_DEFAULT_NAME}",
      },
    ],
    message1: "mit der Größe %1",
    args1: [
      {
        type: "field_number",
        name: "INPUT_A",
        value: 1,
        min: 1,
        max: config.ARRAY_MAX_VALUE,
      },
    ],
    colour: 200,
    output: "array_set",
  },
  {
    type: "array_set",
    previousStatement: null,
    nextStatement: null,
    message0: "In Array %1",
    args0: [
      {
        type: "field_variable",
        name: "VAR",
        variable: "%{BKY_VARIABLES_DEFAULT_NAME}",
      },
    ],
    message1: "den Wert %1",
    args1: [
      {
        type: "input_value",
        name: "INPUT_VALUE",
        check: ["Variable", "Number", "Expression"],
      },
    ],
    message2: "an Index %1 setzen",
    args2: [
      {
        type: "input_value",
        name: "INPUT_INDEX",
        check: ["Variable", "Number", "Expression"],
      },
    ],
    colour: 200,
    output: "array_set",
  },
  {
    type: "array_get",
    message0: "Hole Wert aus Array %1",
    args0: [
      {
        type: "field_variable",
        name: "VAR",
        variable: "%{BKY_VARIABLES_DEFAULT_NAME}",
      },
    ],
    message1: "von dem Index %1",
    args1: [
      {
        type: "input_value",
        name: "INPUT_INDEX",
        check: ["Variable", "Number", "Expression"],
      },
    ],
    output: 'Array_get',
    colour: 200,
  },
  {
    type: "array_length",
    message0: "Länge von Array %1",
    args0: [
      {
        type: "field_variable",
        name: "VAR",
        variable: "%{BKY_VARIABLES_DEFAULT_NAME}",
      },
    ],
    output: 'Array_get',
    colour: 200,
  },
  // ---------------------- Loops ----------------------
  {
    type: "repeat",
    message0: "%{BKY_CONTROLS_REPEAT_TITLE}",
    args0: [
      {
        type: "input_value",
        name: "TIMES",
        check: ["Number",
          "Variable",
          "Array_get",
          "Expression"
        ]
      },
    ],
    message1: "%{BKY_CONTROLS_REPEAT_INPUT_DO} %1",
    args1: [
      {
        type: "input_statement",
        name: "DO",
      },
    ],
    previousStatement: null,
    nextStatement: null,
    output: "loop",
    tooltip: "%{BKY_CONTROLS_REPEAT_TOOLTIP}",
    colour: 280,
  },
  {
    type: "while_loop",
    message0: "%{BKY_CONTROLS_WHILEUNTIL_OPERATOR_WHILE} %1",
    args0: [
      {
        type: "input_value",
        name: "CONDITION",
        check: "Boolean",
      },
    ],
    message1: "%{BKY_CONTROLS_REPEAT_INPUT_DO} %1",
    args1: [
      {
        type: "input_statement",
        name: "DO",
      },
    ],
    previousStatement: null,
    nextStatement: null,
    output: "loop",
    tooltip: "%{BKY_CONTROLS_WHILEUNTIL_TOOLTIP_WHILE}",
    colour: 280,
  },
  {
    type: "repeat_number",
    message0: "%1",
    args0: [
      {
        type: "field_number",
        name: "REPEAT_NUMBER",
        value: 1,
        min: 1,
        max: config.REPEAT_MAX_VALUE,
      },
    ],
    output: "Number",
    tooltip: "Anzahl wiederholungen",
    colour: 280,
  },
  // ---------------------- truth expressions ----------------------
  {
    type: "logic_boolean",
    message0: "%1",
    args0: [
      {
        type: "field_dropdown",
        name: "BOOL",
        options: [
          ["%{BKY_LOGIC_BOOLEAN_TRUE}", "TRUE"],
          ["%{BKY_LOGIC_BOOLEAN_FALSE}", "FALSE"],
        ],
      },
    ],
    output: "Boolean",
    tooltip: "Gibt 'Wahr' oder 'Falsch' aus.",
    colour: 120,
  },
  {
    type: "usual_condition",
    message0: "%1 %2 %3",
    args0: [
      {
        type: "input_value",
        name: "INPUT_A",
        check: ["Number",
          "Variable",
          "Array_get",
          "Expression"
        ],
      },
      {
        type: "field_dropdown",
        name: "OPERATOR",
        options: [
          ["gleich", "=="],
          ["ungleich", "!="],
          ["größer gleich", ">="],
          ["größer", ">"],
          ["kleiner gleich", "<="],
          ["kleiner", "<"],
        ],
      },
      {
        type: "input_value",
        name: "INPUT_B",
        check: ["Number",
          "Variable",
          "Array_get",
          "Expression"
        ],
      },
    ],
    inputsInline: true,
    output: "Boolean",
    tooltip: "Vergleicht zwei Werte, z.B. ob sie gleich oder größer sind.",
    colour: 120,
  },
  {
    type: "not_condition",
    message0: "nicht %1",
    args0: [
      {
        type: "input_value",
        name: "INPUT_A",
        check: "Boolean",
      },
    ],
    inputsInline: true,
    output: "Boolean",
    tooltip: "Verneint oder kehrt eine Bedingung um.",
    colour: 120,
  },
  {
    type: "logic_operator",
    message0: "%1 %2 %3",
    args0: [
      {
        type: "input_value",
        name: "CONDITION_A",
        check: "Boolean",
      },
      {
        type: "field_dropdown",
        name: "LOGIC_OPERATOR",
        options: [
          ["und", "&&"],
          ["oder", "||"],
        ],
      },
      {
        type: "input_value",
        name: "CONDITION_B",
        check: "Boolean",
      },
    ],
    inputsInline: true,
    output: "Boolean",
    tooltip: "Verknüpfe zwei Bedingungen. 'Und' nur, wenn beide wahr sind. 'Oder', wenn mindestens eine wahr ist.",
    colour: 120,
  },
  // ---------------------- Conditions ----------------------
  {
    type: "logic_wall_direction",
    message0: "Wand %1",
    output: "Boolean",
    tooltip: "Überprüfe, ob eine Wand in die Richtung ist",
    args0: [
      {
        type: "input_value",
        name: "DIRECTION",
        check: "Direction",
      },
    ],
    colour: 0,
  },
  {
    type: "logic_floor_direction",
    message0: "Boden %1",
    output: "Boolean",
    tooltip: "Überprüfe, ob ein Boden in die Richtung ist",
    args0: [
      {
        type: "input_value",
        name: "DIRECTION",
        check: "Direction",
      },
    ],
    colour: 0,
  },
  {
    type: "logic_pit_direction",
    message0: "Loch %1",
    output: "Boolean",
    tooltip: "Überprüfe, ob ein Loch in die Richtung ist",
    args0: [
      {
        type: "input_value",
        name: "DIRECTION",
        check: "Direction",
      },
    ],
    colour: 0,
  },
  {
    type: "logic_monster_direction",
    message0: "Monster %1",
    output: "Boolean",
    tooltip: "Überprüfe, ob ein Monster in die Richtung ist",
    args0: [
      {
        type: "input_value",
        name: "DIRECTION",
        check: "Direction",
      },
    ],
    colour: 0,
  },
  {
    type: "logic_switch_direction",
    message0: "Schalter/Fackel %1",
    output: "Boolean",
    tooltip: "Überprüfe, ob ein Schalter in die Richtung ist",
    args0: [
      {
        type: "input_value",
        name: "DIRECTION",
        check: "Direction",
      },
    ],
    colour: 0,
  },
  {
    type: "logic_breadcrumbs_direction",
    message0: "Brotkrume %1",
    output: "Boolean",
    tooltip: "Überprüfe, ob eine Brotkrume in der Richtung liegt",
    args0: [
      {
        type: "input_value",
        name: "DIRECTION",
        check: "Direction",
      },
    ],
    colour: 0,
  },
  {
    type: "logic_clover_direction",
    message0: "Kleeblatt %1",
    output: "Boolean",
    tooltip: "Überprüfe, ob eine Kleeblatt in der Richtung liegt",
    args0: [
      {
        type: "input_value",
        name: "DIRECTION",
        check: "Direction",
      },
    ],
    colour: 0,
  },
  {
    type: "logic_active_direction",
    message0: "aktiv %1",
    output: "Boolean",
    tooltip: "Überprüfe, ob ein aktives Objekt in der Richtung ist",
    args0: [
      {
        type: "input_value",
        name: "DIRECTION",
        check: "Direction",
      },
    ],
    colour: 0,
  },
  {
    type: "logic_bossView_direction",
    message0: "boss guckt %1",
    output: "Boolean",
    tooltip: "Überprüfe, ob der Boss in eine bestimmte Richtung guckt",
    args0: [
      {
        type: "input_value",
        name: "DIRECTION",
        check: "Direction",
      },
    ],
    colour: 0,
  },
  // ---------------------- If statement ----------------------
  {
    type: "controls_if",
    message0: "%{BKY_CONTROLS_IF_MSG_IF} %1",
    args0: [
      {
        type: "input_value",
        name: "IF0",
        check: "Boolean",
      },
    ],
    message1: "%{BKY_CONTROLS_IF_MSG_THEN} %1",
    args1: [
      {
        type: "input_statement",
        name: "DO0",
      },
    ],
    previousStatement: null,
    nextStatement: null,
    output: "if",
    tooltip: "Führt Anweisung aus, wenn die Bedingung wahr ist.",
    colour: 250,
    suppressPrefixSuffix: true,
  },
  {
    type: "controls_ifelse",
    message0: "%{BKY_CONTROLS_IF_MSG_IF} %1",
    args0: [
      {
        type: "input_value",
        name: "IF0",
        check: "Boolean",
      },
    ],
    message1: "%{BKY_CONTROLS_IF_MSG_THEN} %1",
    args1: [
      {
        type: "input_statement",
        name: "DO0",
      },
    ],
    message2: "%{BKY_CONTROLS_IF_MSG_ELSE} %1",
    args2: [
      {
        type: "input_statement",
        name: "ELSE",
      },
    ],
    previousStatement: null,
    nextStatement: null,
    output: "if",
    tooltip: "Führt Anweisung aus, wenn die Bedingung wahr ist, sonst andere Anweisung.",
    colour: 250,
    suppressPrefixSuffix: true,
  },
  // ----------------------- Switch Case ---------------------
  {
    type: "switch_case",
    message0: "entscheide ueber %1",
    args0: [
      {
        type: "input_value",
        name: "SWITCH",
        check: ["Variable"],
      },
    ],
    message1: "%1", // Platz für case/default-Blöcke
    args1: [
      {
        type: "input_statement",
        name: "CASES",
      },
    ],
    suppressPrefixSuffix: true,
    previousStatement: null,
    nextStatement: null,
    colour: 210,
    tooltip: "Verzweigt abhängig vom Wert.",
  },
  {
    type: "case_block",
    message0: "fall %1",
    args0: [
      {
        type: "input_value",
        name: "CASE",
        check: ["Number"]
      },
    ],
    message1: "%1",
    args1: [
      {
        type: "input_statement",
        name: "DO",
      },
    ],
    suppressPrefixSuffix: true,
    previousStatement: null,
    nextStatement: null,
    colour: 200,
    tooltip: "Ein einzelner Fall im Switch.",
  },
  {
    type: "default_block",
    message0: "sonst",
    message1: "%1",
    args1: [
      {
        type: "input_statement",
        name: "DO",
      },
    ],
    suppressPrefixSuffix: true,
    previousStatement: null,
    nextStatement: null,
    colour: 180,
    tooltip: "Standardfall im Switch, wenn kein anderer zutrifft.",
  },
  // ---------------------- Skills ----------------------
  {
    type: "interact",
    message0: "Interagieren",
    previousStatement: null,
    nextStatement: null,
    colour: 30,
    tooltip: "Mit Items interagieren",
    output: "Skill",
  },
  {
    type: "fireball",
    message0: "Feuerball",
    previousStatement: null,
    nextStatement: null,
    colour: 30,
    tooltip: "Feuerball in Richtung schießen",
  },
  {
    type: "pickup",
    message0: "aufheben",
    previousStatement: null,
    nextStatement: null,
    colour: 30,
    tooltip: "Sammel den Gegenstand unter dir auf.",
  },
  {
    type: "drop_item",
    message0: "fallen lassen %1",
    previousStatement: null,
    nextStatement: null,
    colour: 30,
    tooltip: "Gegenstand auf den Boden werfen.",
    args0: [
      {
        type: "input_value",
        name: "ITEM",
        check: "Item",
      },
    ]
  },
  {
    type: "wait",
    message0: "warte",
    previousStatement: null,
    nextStatement: null,
    colour: 30,
    tooltip: "Warte für einen kurzen Moment.",
  },
  {
    type: "use",
    message0: "benutzen %1",
    previousStatement: null,
    nextStatement: null,
    colour: 30,
    tooltip: "Benutze den Gegenstand in der gewünschten Richtung.",
    args0: [
      {
        type: "input_value",
        name: "DIRECTION",
        check: "Direction",
      },
    ],
  },
  {
    type: "push",
    message0: "schiebe",
    previousStatement: null,
    nextStatement: null,
    colour: 30,
    tooltip: "Schieb ein bewegliches Objekt vorwärts.",
  },
  {
    type: "pull",
    message0: "ziehen",
    previousStatement: null,
    nextStatement: null,
    colour: 30,
    tooltip: "Zieh ein bewegliches Objekt rückwärts.",
  },
  //  ---------------------- Items ----------------------
  {
    type: "item_breadcrumbs",
    message0: "Brotkrumen",
    output: "Item",
    colour: 50
  },
  {
    type: "item_clover",
    message0: "Kleeblatt",
    output: "Item",
    colour: 50
  },

  //  ---------------------- Functions ----------------------
  {
    type: "func_def",
    message0: "Funktion %1 definieren",
    args0: [
      {
        type: "field_variable",
        name: "FUNC_NAME",
      },
    ],
    message1: "%{BKY_CONTROLS_REPEAT_INPUT_DO} %1",
    args1: [
      {
        type: "input_statement",
        name: "DO",
      },
    ],
    previousStatement: null,
    nextStatement: null,
    output: "Function",
    colour: 300,
  },
  {
    type: "func_call",
    message0: "Funktion %1 aufrufen",
    args0: [
      {
        type: "input_value",
        name: "FUNC_NAME",
        check: "Variable",
      },
    ],
    previousStatement: null,
    nextStatement: null,
    colour: 300,
  }
]);
