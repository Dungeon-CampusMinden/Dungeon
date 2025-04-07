import * as Blockly from "blockly/core";

const storageKey = "dungeonWorkspace/";

/**
 * Saves the state of the workspace to browser's local storage.
 * @param workspace Blockly workspace to save.
 * @param level Level name to save the workspace state under.
 */
export const save = function (workspace: Blockly.Workspace, level: string) {
  if (level === undefined) {
    console.error("Level name is undefined");
    return;
  }
  const data = Blockly.serialization.workspaces.save(workspace);
  window.localStorage?.setItem(storageKey + level, JSON.stringify(data));
};

/**
 * Loads saved state from local storage into the given workspace.
 * @param workspace Blockly workspace to load into.
 * @param level Level name to load the workspace state from.
 */
export const load = function (workspace: Blockly.Workspace, level: string) {
  const data = window.localStorage?.getItem(storageKey + level);
  if (!data) return;

  // Don't emit events during loading.
  Blockly.Events.disable();
  Blockly.serialization.workspaces.load(JSON.parse(data), workspace, undefined);
  Blockly.Events.enable();
};
