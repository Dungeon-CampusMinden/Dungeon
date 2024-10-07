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
    type: "move_up",
    message0: "Oben %1",
    previousStatement: null,
    nextStatement: null,
    colour: 290,
    tooltip: "Nach oben gehen",
    args0: [
      {
        type: "field_number",
        name: "REPEAT_NUMBER",
        value: 1,
        min: 1,
        max: config.CHARACTER_MAX_MOVEMENT,
      },
    ],
  },
  {
    type: "move_up_var",
    message0: "Oben %1",
    previousStatement: null,
    nextStatement: null,
    colour: 290,
    tooltip: "Nach oben gehen",
    args0: [
      {
        type: "input_value",
        name: "amount",
        check: "Move",
      },
    ],
  },
  {
    type: "move_down",
    message0: "Unten %1",
    previousStatement: null,
    nextStatement: null,
    colour: 290,
    tooltip: "Nach unten gehen",
    args0: [
      {
        type: "field_number",
        name: "REPEAT_NUMBER",
        value: 1,
        min: 1,
        max: config.CHARACTER_MAX_MOVEMENT,
      },
    ],
  },
  {
    type: "move_down_var",
    message0: "Unten %1",
    previousStatement: null,
    nextStatement: null,
    colour: 290,
    tooltip: "Nach unten gehen",
    args0: [
      {
        type: "input_value",
        name: "amount",
        check: "Move",
      },
    ],
  },
  {
    type: "move_left",
    message0: "Links %1",
    previousStatement: null,
    nextStatement: null,
    colour: 290,
    tooltip: "Nach links gehen",
    args0: [
      {
        type: "field_number",
        name: "REPEAT_NUMBER",
        value: 1,
        min: 1,
        max: config.CHARACTER_MAX_MOVEMENT,
      },
    ],
  },
  {
    type: "move_left_var",
    message0: "Links %1",
    previousStatement: null,
    nextStatement: null,
    colour: 290,
    tooltip: "Nach links gehen",
    args0: [
      {
        type: "input_value",
        name: "amount",
        check: "Move",
      },
    ],
  },
  {
    type: "move_right",
    message0: "Rechts %1",
    previousStatement: null,
    nextStatement: null,
    colour: 290,
    tooltip: "Nach rechts gehen",
    args0: [
      {
        type: "field_number",
        name: "REPEAT_NUMBER",
        value: 1,
        min: 1,
        max: config.CHARACTER_MAX_MOVEMENT,
      },
    ],
  },
  {
    type: "move_right_var",
    message0: "Rechts %1",
    previousStatement: null,
    nextStatement: null,
    colour: 290,
    tooltip: "Nach rechts gehen",
    args0: [
      {
        type: "input_value",
        name: "amount",
        check: "Move",
      },
    ],
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
        check: "Move",
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
    output: "Move",
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
        check: "Move",
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
        check: "Move",
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
    type: "logic_wall",
    message0: "nahe Wand",
    output: "Boolean",
    colour: 60,
  },
  {
    type: "logic_wall_up",
    message0: "Wand oben",
    output: "Boolean",
    colour: 60,
  },
  {
    type: "logic_wall_down",
    message0: "Wand unten",
    output: "Boolean",
    colour: 60,
  },
  {
    type: "logic_wall_left",
    message0: "Wand links",
    output: "Boolean",
    colour: 60,
  },
  {
    type: "logic_wall_right",
    message0: "Wand rechts",
    output: "Boolean",
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
    type: "fireball_up",
    message0: "Feuerball oben",
    previousStatement: null,
    nextStatement: null,
    colour: 0,
    tooltip: "Feuerball nach oben schießen",
    output: "Skill",
  },
  {
    type: "fireball_down",
    message0: "Feuerball unten",
    previousStatement: null,
    nextStatement: null,
    colour: 0,
    tooltip: "Feuerball nach unten schießen",
    output: "Skill",
  },
  {
    type: "fireball_left",
    message0: "Feuerball links",
    previousStatement: null,
    nextStatement: null,
    colour: 0,
    tooltip: "Feuerball nach links schießen",
    output: "Skill",
  },
  {
    type: "fireball_right",
    message0: "Feuerball rechts",
    previousStatement: null,
    nextStatement: null,
    colour: 0,
    tooltip: "Feuerball nach rechts schießen",
    output: "Skill",
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
