import * as Blockly from "blockly";
import {myCustomMessages} from "./language.ts";

Object.assign(Blockly.Msg, myCustomMessages.de);

export const toolbox: Blockly.utils.toolbox.ToolboxDefinition = {
  kind: "categoryToolbox",
  contents: [
    {
      kind: "category",
      name: "%{BKY_CAT_START}",
      colour: "160",
      contents: [
        {
          kind: "block",
          type: "start",
        },
      ]
    },
    {
      kind: "category",
      name: "%{BKY_CAT_MOVEMENT}",
      colour: "180",
      contents: [
        {
          kind: "block",
          type: "move",
        },
        {
          kind: "block",
          type: "rotate",
        },
      ],
    },
    {
      kind: "category",
      name: "%{BKY_CAT_DIRECTIONS}",
      colour: "200",
      contents: [
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
          type: "direction_here",
        },
      ],
    },
    {
      kind: "category",
      name: "%{BKY_CAT_INVENTORY}",
      colour: "40",
      contents: [
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
          type: "push",
        },
        {
          kind: "block",
          type: "pull",
        },
        {
          kind: "block",
          type: "shoot_blue_portal",
        },
        {
          kind: "block",
          type: "shoot_green_portal",
        },
      ],
    },
    {
      kind: "category",
      name: "%{BKY_CAT_QUERIES}",
      colour: "250",
      contents: [
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
      name: "%{BKY_CAT_BOOLEAN}",
      colour: "120",
      contents: [
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
      ],
    },
    {
      kind: "category",
      name: "%{BKY_CAT_VARIABLES}",
      colour: "220",
      contents: [
        {
          kind: "block",
          type: "set_number",
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
      ],
    },
    {
      kind: "category",
      name: "%{BKY_CAT_LOOPS}",
      colour: "280",
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
      ],
    },
    {
      kind: "category",
      name: "%{BKY_CAT_CONDITIONS}",
      colour: "0",
      contents: [
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
          type: "logic_active_direction"
        },
        {
          kind: "block",
          type: "logic_bossView_direction"
        },
      ],
    },
  ],
};
