import {
  call_level_route, call_levels_route,
} from "../api/api.ts";

let currentLevelIndex = 0
const levelNames: string[] = [];
const blockedBlocks: string[] = [];

/**
 * Interface for the level changed event
 *
 * <p> This event is dispatched when the level is changed.
 */
export interface LevelChangedEvent {
  levelName: string;
  blockBlocks: string[];
}

/**
 * Sets the current level to the given level name
 *
 * <p> If the level name is not in the level list, this function does nothing.
 *
 * @param newLevelName The name of the new level
 */
export const setCurrentLevel = (newLevelName: string)=> {
  if (!levelNames.includes(newLevelName)) {
    console.error(`Level ${newLevelName} not found in level list`);
    return;
  }
  currentLevelIndex = levelNames.indexOf(newLevelName);

  call_level_route(newLevelName).then((response) => {
    const blockBlocks = response.block_blocks ?? [];
    if (blockBlocks.length === 0) {
      console.log(`No block blocks found for level ${newLevelName}`);
      return;
    }
    blockedBlocks.push(...blockBlocks);

    const event: LevelChangedEvent = {
      levelName: newLevelName,
      blockBlocks: blockedBlocks,
    }
    document.getElementById("levelSelector")!.dispatchEvent(
      new CustomEvent("levelChanged", { detail: event })
    );
  });
}

/**
 * Returns the current level
 *
 * @returns The name of the current level
 */
export const getCurrentLevel = () => {
  return levelNames[currentLevelIndex];
}

/**
 * This function updates the level list using the API.
 *
 * <p> This function is used to get the level list from the server and update the level list.
 *
 * <p> Also sets the current level index to 0.
 *
 * @returns true if the level list was successfully retrieved, false otherwise
 */
export const updateLevelList = async () => {
  currentLevelIndex = 0;
  const allLevels = await call_levels_route();
  levelNames.push(...allLevels);
  updateLevelListUI();

  if (levelNames.length > 0) {
    setCurrentLevel(levelNames[0]);
  }
}

const updateLevelListUI = () => {
  const select = document.getElementById("levelSelector") as HTMLSelectElement;
  if (!select) { return; }
  // remove all options
  while (select.firstChild) {
    select.removeChild(select.firstChild);
  }

  if (levelNames.length === 0) {
    const option = document.createElement("option");
    option.value = "";
    option.innerText = "No levels available";
    select.appendChild(option);
    return;
  }

  // add option for each level
  levelNames.forEach((levelName) => {
    const option = document.createElement("option");
    option.value = levelName;
    option.innerText = levelName;
    select.appendChild(option);
  });
}

export const setupLevelSelector = () => {
  const header = document.getElementsByTagName("header")[0];
  // new select element
  const select = document.createElement("select");
  select.id = "levelSelector";
  // add event listener to select element
  select.addEventListener("change", (event) => {
    const target = event.target as HTMLSelectElement;
    const selectedLevelName = target.value;
    setCurrentLevel(selectedLevelName);
  });
  // add select element to header
  header.appendChild(select);
  updateLevelListUI();

  return select;
}
