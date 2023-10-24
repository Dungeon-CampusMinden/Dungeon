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
        name: "amount",
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
        name: "amount",
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
        name: "amount",
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
        name: "amount",
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
    tooltip: "%{BKY_CONTROLS_REPEAT_TOOLTIP}",
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
        check: "Not_If",
      },
    ],
    previousStatement: "If",
    nextStatement: "If",
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
        check: "Not_If",
      },
    ],
    message2: "%{BKY_CONTROLS_IF_MSG_ELSE} %1",
    args2: [
      {
        type: "input_statement",
        name: "ELSE",
        check: "Not_If",
      },
    ],
    previousStatement: "If",
    nextStatement: "If",
    colour: 60,
    suppressPrefixSuffix: true,
  },
  {
    type: "interact",
    message0: "Interagieren",
    previousStatement: null,
    nextStatement: null,
    colour: 0,
    tooltip: "Mit Items interagieren",
  },
  {
    type: "fireball_up",
    message0: "Feuerball oben",
    previousStatement: null,
    nextStatement: null,
    colour: 0,
    tooltip: "Feuerball nach oben schießen",
  },
  {
    type: "fireball_down",
    message0: "Feuerball unten",
    previousStatement: null,
    nextStatement: null,
    colour: 0,
    tooltip: "Feuerball nach unten schießen",
  },
  {
    type: "fireball_left",
    message0: "Feuerball links",
    previousStatement: null,
    nextStatement: null,
    colour: 0,
    tooltip: "Feuerball nach links schießen",
  },
  {
    type: "fireball_right",
    message0: "Feuerball rechts",
    previousStatement: null,
    nextStatement: null,
    colour: 0,
    tooltip: "Feuerball nach rechts schießen",
  },
]);
