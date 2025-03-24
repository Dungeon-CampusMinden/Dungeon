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
    message0: "Gehe nach %1",
    previousStatement: null,
    nextStatement: null,
    colour: 290,
    tooltip: "Bewegt den Spieler in eine Richtung",
    args0: [
      {
        type: "input_value",
        name: "DIRECTION",
        check: "Direction",
      }
    ],
  },
  {
    type: "goToExit",
    message0: "Gehe Zum Ausgang",
    previousStatement: null,
    nextStatement: null,
    colour: 290,
    tooltip: "Bewege dich zum Ausgang des Levels.",
  },
  // ---------------------- Variables ----------------------
  {
    type: "get_number",
    message0: "%1",
    args0: [
      {
        type: "field_variable",
        name: "VAR",
        variable: "%{BKY_VARIABLES_DEFAULT_NAME}",
      },
    ],
    output: "Move",
    colour: 290,
  },
  {
    type: "set_number",
    previousStatement: null,
    nextStatement: null,
    message0: "%{BKY_VARIABLES_SET}",
    args0: [
      {
        type: "field_variable",
        name: "VAR",
        variable: "%{BKY_VARIABLES_DEFAULT_NAME}",
      },
      {
        type: "input_value",
        name: "VALUE",
        check: "Number",
      },
    ],
    colour: 230,
  },
  {
    type: "set_number_expression",
    previousStatement: null,
    nextStatement: null,
    message0: "%{BKY_VARIABLES_SET}",
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
    colour: 230,
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
    colour: 260,
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
    colour: 260,
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
    colour: 230,
  },
  {
    type: "direction_up",
    message0: "oben",
    output: "Direction",
    colour: 230,
  },
  {
    type: "direction_down",
    message0: "unten",
    output: "Direction",
    colour: 230
  },
  {
    type: "direction_left",
    message0: "links",
    output: "Direction",
    colour: 230
  },
  {
    type: "direction_right",
    message0: "rechts",
    output: "Direction",
    colour: 230
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
        check: "Number",
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
    colour: 30,
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
    colour: 30,
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
    colour: 30,
  },
  // ---------------------- Conditions ----------------------
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
    colour: 60,
  },
  {
    type: "usual_condition",
    message0: "%1 %2 %3",
    args0: [
      {
        type: "input_value",
        name: "INPUT_A",
        check: "Number",
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
        check: "Number",
      },
    ],
    inputsInline: true,
    output: "Boolean",
    colour: 60,
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
    colour: 60,
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
    colour: 60,
  },
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
    colour: 60,
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
    colour: 60,
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
    colour: 60,
    suppressPrefixSuffix: true,
  },
  // ---------------------- Skills ----------------------
  {
    type: "interact",
    message0: "Interagieren",
    previousStatement: null,
    nextStatement: null,
    colour: 0,
    tooltip: "Mit Items interagieren",
    output: "Skill",
  },
  {
    type: "fireball_direction",
    message0: "Feuerball %1",
    previousStatement: null,
    nextStatement: null,
    colour: 0,
    tooltip: "Feuerball in Richtung schießen",
    args0: [
      {
        type: "input_value",
        name: "DIRECTION",
        check: "Direction",
      },
    ]
  },
  {
    type: "pickup",
    message0: "aufheben",
    previousStatement: null,
    nextStatement: null,
    colour: 0,
    tooltip: "Sammel den Gegenstand unter dir auf.",
  },
  {
    type: "drop_item",
    message0: "fallen lassen %1",
    previousStatement: null,
    nextStatement: null,
    colour: 0,
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
    colour: 0,
    tooltip: "Warte für einen kurzen Moment.",
  },
  {
    type: "use",
    message0: "benutzen",
    previousStatement: null,
    nextStatement: null,
    colour: 0,
    tooltip: "Benutze den Gegenstand vor dir.",
  },
  {
    type: "push",
    message0: "schiebe",
    previousStatement: null,
    nextStatement: null,
    colour: 0,
    tooltip: "Nur einmal feste drücken!",
  },
  {
    type: "pull",
    message0: "ziehen",
    previousStatement: null,
    nextStatement: null,
    colour: 0,
    tooltip: "Nur einmal feste ziehen!",
  },
  {
    type: "item_breadcrumbs",
    message0: "Brotkrumen",
    output: "Item",
    colour: 230
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
