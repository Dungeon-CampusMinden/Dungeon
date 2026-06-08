import * as Blockly from "blockly";
import { config } from "../config.ts";

// delete predefined blocks
delete Blockly.Blocks["logic_boolean"];
delete Blockly.Blocks["controls_if"];
delete Blockly.Blocks["controls_ifelse"];

export const blocks = Blockly.common.createBlockDefinitionsFromJsonArray([
  {
    type: "start",
    message0: "%{BKY_BLOCK_START_MSG}",
    nextStatement: null,
    colour: 120,
    tooltip: "%{BKY_BLOCK_START_TOOLTIP}",
  },
  // ---------------------- Movement ----------------------
  {
    type: "move",
    message0: "%{BKY_BLOCK_MOVE_MSG}",
    previousStatement: null,
    nextStatement: null,
    colour: 180,
    tooltip: "%{BKY_BLOCK_MOVE_TOOLTIP}",
  },
  {
    type: "rotate",
    message0: "%{BKY_BLOCK_ROTATE_MSG}",
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
    tooltip: "%{BKY_BLOCK_ROTATE_TOOLTIP}",
  },
  // ---------------------- Variables ----------------------
  {
    type: "set_number",
    previousStatement: null,
    nextStatement: null,
    message0: "%{BKY_VARIABLES_SET}",
    tooltip: "%{BKY_BLOCK_SET_NUMBER_TOOLTIP}",
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
    type: "set_number_expression",
    previousStatement: null,
    nextStatement: null,
    message0: "%{BKY_BLOCK_SET_EXPRESSION_MSG}",
    tooltip: "%{BKY_BLOCK_SET_EXPRESSION_TOOLTIP}",
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
          ["%{BKY_OP_PLUS}", "+"],
          ["%{BKY_OP_MINUS}", "-"],
          ["%{BKY_OP_MULTIPLY}", "*"],
          ["%{BKY_OP_DIVIDE}", "/"],
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
    tooltip: "%{BKY_BLOCK_EXPRESSION_TOOLTIP}",
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
    message0: "%{BKY_DIR_UP}",
    output: "Direction",
    colour: 200,
  },
  {
    type: "direction_down",
    message0: "%{BKY_DIR_DOWN}",
    output: "Direction",
    colour: 200
  },
  {
    type: "direction_left",
    message0: "%{BKY_DIR_LEFT}",
    output: "Direction",
    colour: 200
  },
  {
    type: "direction_right",
    message0: "%{BKY_DIR_RIGHT}",
    output: "Direction",
    colour: 200
  },
  {
    type: "direction_here",
    message0: "%{BKY_DIR_HERE}",
    output: "Direction",
    colour: 200
  },
  // ---------------------- Loops ----------------------
  {
    type: "repeat",
    message0: "%{BKY_CONTROLS_REPEAT_TITLE}",
    args0: [
      {
        type: "input_value",
        name: "TIMES",
        check: ["Number", "Variable", "Array_get", "Expression"]
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
    tooltip: "%{BKY_BLOCK_REPEAT_NUMBER_TOOLTIP}",
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
    tooltip: "%{BKY_BLOCK_LOGIC_BOOL_TOOLTIP}",
    colour: 120,
  },
  {
    type: "usual_condition",
    message0: "%1 %2 %3",
    args0: [
      {
        type: "input_value",
        name: "INPUT_A",
        check: ["Number", "Variable", "Array_get", "Expression"],
      },
      {
        type: "field_dropdown",
        name: "OPERATOR",
        options: [
          ["%{BKY_OP_EQ}", "=="],
          ["%{BKY_OP_NEQ}", "!="],
          ["%{BKY_OP_GTE}", ">="],
          ["%{BKY_OP_GT}", ">"],
          ["%{BKY_OP_LTE}", "<="],
          ["%{BKY_OP_LT}", "<"],
        ],
      },
      {
        type: "input_value",
        name: "INPUT_B",
        check: ["Number", "Variable", "Array_get", "Expression"],
      },
    ],
    inputsInline: true,
    output: "Boolean",
    tooltip: "%{BKY_BLOCK_USUAL_COND_TOOLTIP}",
    colour: 120,
  },
  {
    type: "not_condition",
    message0: "%{BKY_BLOCK_NOT_COND_MSG}",
    args0: [
      {
        type: "input_value",
        name: "INPUT_A",
        check: "Boolean",
      },
    ],
    inputsInline: true,
    output: "Boolean",
    tooltip: "%{BKY_BLOCK_NOT_COND_TOOLTIP}",
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
          ["%{BKY_OP_AND}", "&&"],
          ["%{BKY_OP_OR}", "||"],
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
    tooltip: "%{BKY_BLOCK_LOGIC_OP_TOOLTIP}",
    colour: 120,
  },
  // ---------------------- Conditions ----------------------
  {
    type: "logic_wall_direction",
    message0: "%{BKY_BLOCK_WALL_MSG}",
    output: "Boolean",
    tooltip: "%{BKY_BLOCK_WALL_TOOLTIP}",
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
    message0: "%{BKY_BLOCK_FLOOR_MSG}",
    output: "Boolean",
    tooltip: "%{BKY_BLOCK_FLOOR_TOOLTIP}",
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
    message0: "%{BKY_BLOCK_PIT_MSG}",
    output: "Boolean",
    tooltip: "%{BKY_BLOCK_PIT_TOOLTIP}",
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
    message0: "%{BKY_BLOCK_MONSTER_MSG}",
    output: "Boolean",
    tooltip: "%{BKY_BLOCK_MONSTER_TOOLTIP}",
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
    message0: "%{BKY_BLOCK_SWITCH_MSG}",
    output: "Boolean",
    tooltip: "%{BKY_BLOCK_SWITCH_TOOLTIP}",
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
    message0: "%{BKY_BLOCK_ACTIVE_MSG}",
    output: "Boolean",
    tooltip: "%{BKY_BLOCK_ACTIVE_TOOLTIP}",
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
    message0: "%{BKY_BLOCK_BOSS_MSG}",
    output: "Boolean",
    tooltip: "%{BKY_BLOCK_BOSS_TOOLTIP}",
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
    tooltip: "%{BKY_BLOCK_IF_TOOLTIP}",
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
    // Hier setzen wir die neue Variable ein
    tooltip: "%{BKY_BLOCK_IFELSE_TOOLTIP}",
    colour: 250,
    suppressPrefixSuffix: true,
  },
  // ---------------------- Skills ----------------------
  {
    type: "interact",
    message0: "%{BKY_BLOCK_INTERACT_MSG}",
    previousStatement: null,
    nextStatement: null,
    colour: 30,
    tooltip: "%{BKY_BLOCK_INTERACT_TOOLTIP}",
    output: "Skill",
  },
  {
    type: "fireball",
    message0: "%{BKY_BLOCK_FIREBALL_MSG}",
    previousStatement: null,
    nextStatement: null,
    colour: 30,
    tooltip: "%{BKY_BLOCK_FIREBALL_TOOLTIP}",
  },
  {
    type: "wait",
    message0: "%{BKY_BLOCK_WAIT_MSG}",
    previousStatement: null,
    nextStatement: null,
    colour: 30,
    tooltip: "%{BKY_BLOCK_WAIT_TOOLTIP}",
  },
  {
    type: "use",
    message0: "%{BKY_BLOCK_USE_MSG}",
    previousStatement: null,
    nextStatement: null,
    colour: 30,
    tooltip: "%{BKY_BLOCK_USE_TOOLTIP}",
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
    message0: "%{BKY_BLOCK_PUSH_MSG}",
    previousStatement: null,
    nextStatement: null,
    colour: 30,
    tooltip: "%{BKY_BLOCK_PUSH_TOOLTIP}",
  },
  {
    type: "pull",
    message0: "%{BKY_BLOCK_PULL_MSG}",
    previousStatement: null,
    nextStatement: null,
    colour: 30,
    tooltip: "%{BKY_BLOCK_PULL_TOOLTIP}",
  },
  {
    type: "shoot_blue_portal",
    message0: "%{BKY_BLOCK_BLUE_PORTAL_MSG}",
    previousStatement: null,
    nextStatement: null,
    colour: 30,
    tooltip: "%{BKY_BLOCK_BLUE_PORTAL_TOOLTIP}",
  },
  {
    type: "shoot_green_portal",
    message0: "%{BKY_BLOCK_GREEN_PORTAL_MSG}",
    previousStatement: null,
    nextStatement: null,
    colour: 30,
    tooltip: "%{BKY_BLOCK_GREEN_PORTAL_TOOLTIP}",
  }
]);
