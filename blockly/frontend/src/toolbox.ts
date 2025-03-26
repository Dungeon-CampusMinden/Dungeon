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
          type: "move",
        },
        {
          kind: "block",
          type: "rotate",
        },
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
          type: "goToExit"
        },
      ],
    },
    {
      kind: "category",
      name: "Variablen",
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
        {
          kind: "block",
          type: "direction_up",
        },
        {
          kind: "block",
          type: "direction_down",
        },
        {
          kind: "block",
          type: "direction_left",
        },
        {
          kind: "block",
          type: "direction_right",
        },
        {
          kind: "block",
          type: "item_breadcrumbs",
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
          type: "logic_wall_direction"
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
          type: "logic_wall_direction"
        },
        {
          kind: "block",
          type: "logic_floor_direction"
        },
        {
          kind: "block",
          type: "logic_pit_direction"
        },
        {
          kind: "block",
          type: "logic_monster_direction"
        },
        {
          kind: "block",
          type: "logic_switch_direction"
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
          type: "fireball",
        },
        {
          kind: "block",
          type: "wait",
        },
        {
          kind: "block",
          type: "use",
        },
        {
          kind: "block",
          type: "pickup",
        },
        {
          kind: "block",
          type: "push",
        },
        {
          kind: "block",
          type: "pull",
        },
        {
          kind: "block",
          type: "drop_item",
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
