import * as Blockly from "blockly";

export const toolbox: Blockly.utils.toolbox.ToolboxDefinition = {
  kind: "categoryToolbox",
  contents: [
    {
      kind: "category",
      name: "Bewegung",
      colour: "290",
      contents: [
        {
          kind: "block",
          type: "move_up",
        },
        {
          kind: "block",
          type: "move_up_var",
        },
        {
          kind: "block",
          type: "move_down",
        },
        {
          kind: "block",
          type: "move_down_var",
        },
        {
          kind: "block",
          type: "move_left",
        },
        {
          kind: "block",
          type: "move_left_var",
        },
        {
          kind: "block",
          type: "move_right",
        },
        {
          kind: "block",
          type: "move_right_var",
        },
        {
          kind: "block",
          type: "get_number",
        },
        {
          kind: "block",
          type: "repeat_number",
        },
      ],
    },
    {
      kind: "category",
      name: "Variablen erstellen",
      colour: "230",
      contents: [
        {
          kind: "button",
          text: "Variable erstellen",
          callbackkey: "createVariable",
        },
        {

          kind: "block",
          type: "var_number",
        },
        {
          kind: "block",
          type: "set_number_expression",
        },
        {
          kind: "block",
          type: "expression",
        },
        {
          kind: "block",
          type: "get_variable",
        },
        {
          kind: "block",
          type: "array_get",
        },
      ],
    },
    {
      kind: "category",
      name: "Arrays",
      colour: "200",
      contents: [
        {
          kind: "block",
          type: "var_array",
        },
        {
          kind: "block",
          type: "array_set",
        },
        {
          kind: "block",
          type: "array_get",
        },
        {
          kind: "block",
          type: "array_length",
        },
        {
          kind: "block",
          type: "expression",
        },
        {
          kind: "block",
          type: "get_variable",
        },
        {
          kind: "block",
          type: "var_number",
        },
      ],
    },
    {
      kind: "category",
      name: "Schleife",
      colour: "30",
      contents: [
        {
          kind: "block",
          type: "repeat",
        },
        {
          kind: "block",
          type: "while_loop",
        },
        {
          kind: "block",
          type: "repeat_number",
        },
        {
          kind: "block",
          type: "get_number",
        },
        {
          kind: "block",
          type: "not_condition",
        },
        {
          kind: "block",
          type: "logic_operator",
        },
        {
          kind: "block",
          type: "logic_wall",
        },
        {
          kind: "block",
          type: "logic_wall_up",
        },
        {
          kind: "block",
          type: "logic_wall_down",
        },
        {
          kind: "block",
          type: "logic_wall_left",
        },
        {
          kind: "block",
          type: "logic_wall_right",
        },
      ],
    },
    {
      kind: "category",
      name: "Bedingung",
      colour: "60",
      contents: [
        {
          kind: "block",
          type: "get_number",
        },
        {
          kind: "block",
          type: "repeat_number",
        },
        {
          kind: "block",
          type: "logic_boolean",
        },
        {
          kind: "block",
          type: "usual_condition",
        },
        {
          kind: "block",
          type: "not_condition",
        },
        {
          kind: "block",
          type: "logic_operator",
        },
        {
          kind: "block",
          type: "logic_wall",
        },
        {
          kind: "block",
          type: "logic_wall_up",
        },
        {
          kind: "block",
          type: "logic_wall_down",
        },
        {
          kind: "block",
          type: "logic_wall_left",
        },
        {
          kind: "block",
          type: "logic_wall_right",
        },
        {
          kind: "block",
          type: "controls_if",
        },
        {
          kind: "block",
          type: "controls_ifelse",
        },
      ],
    },
    {
      kind: "category",
      name: "Skills",
      colour: "0",
      contents: [
        /* {
          kind: "block",
          type: "interact",
        }, */
        {
          kind: "block",
          type: "fireball_up",
        },
        {
          kind: "block",
          type: "fireball_down",
        },
        {
          kind: "block",
          type: "fireball_left",
        },
        {
          kind: "block",
          type: "fireball_right",
        },
      ],
    },
    {
      kind: "category",
      name: "Funktionen",
      colour: "300",
      contents: [
        {
          kind: "block",
          type: "func_def",
        },
        {
          kind: "block",
          type: "func_call",
        },
        {
          kind: "block",
          type: "get_variable",
        },
      ],
    },
  ],
};
