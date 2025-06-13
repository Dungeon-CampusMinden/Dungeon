import {
  call_level_route, call_levels_route,
} from "../api/api.ts";
import {sleep} from "./utils.ts";

let currentLevelIndex = 0
const levelNames: string[] = [];

const getLevelProgress = () => {
  const levelProgress = localStorage.getItem("levelProgress") || "0";
  const levelProgressNumber = parseInt(levelProgress, 10);
  if (isNaN(levelProgressNumber)) {
    return 0;
  }
  return levelProgressNumber;
}

const setLevelProgress = (levelIndex: number) => {
  localStorage.setItem("levelProgress", levelIndex.toString());
}

/**
 * Interface for the level changed event
 *
 * <p> This event is dispatched when the level is changed.
 */
export interface LevelChangedEvent {
  oldLevelName: string;
  newLevelName: string;
  blockedElements: string[];
}

/**
 * Returns if the given level is available based on the current level progress
 *
 * <p> This function checks if the level is in the level list and if the level progress is enough to unlock the level.
 *
 * @param levelName The name of the level to check
 * @return true if the level is available, false otherwise
 */
export const isLevelAvailable = (levelName: string) => {
  if (!levelNames.includes(levelName)) {
    return false;
  }
  const levelIndex = levelNames.indexOf(levelName);
  return getLevelProgress() >= levelIndex;
}

/**
 * Sets the current level to the given level name
 *
 * <p> If the level name is not in the level list, this function does nothing.
 *
 * @param newLevelName The name of the new level
 * @param force If true, the level will be set even if the levelProgress is not enough
 */
export const setCurrentLevel = (newLevelName: string, force: boolean = false)=> {
  if (!levelNames.includes(newLevelName)) {
    console.error(`Level ${newLevelName} not found in level list`);
    return;
  }

  if (!force && !isLevelAvailable(newLevelName)) {
    return;
  }

  call_level_route(newLevelName).then((response) => {
    const blockBlocks = response.block_blocks ?? [];

    const event: LevelChangedEvent = {
      oldLevelName: levelNames[currentLevelIndex],
      newLevelName: newLevelName,
      blockedElements: blockBlocks,
    }
    document.getElementById("levelSelector")!.dispatchEvent(
      new CustomEvent("levelChanged", { detail: event })
    );
    currentLevelIndex = levelNames.indexOf(newLevelName);
    const select = document.getElementById("levelSelector") as HTMLSelectElement;
    if (!select) { return; }
    select.selectedIndex = currentLevelIndex;
    updateLevelListUI();
  });
}

/**
 * Sets the current level to the next level
 *
 * <p> If the current level is the last level, this function does nothing.
 *
 * <p> This function is used to complete the current level and move to the next level.
 */
export const completeLevel = () => {
  const newLevelIndex = currentLevelIndex + 1;
  if (newLevelIndex >= levelNames.length) {
    console.info("[CompleteLevel] No more levels available");
    return;
  }
  if (newLevelIndex > getLevelProgress()) {
    setLevelProgress(newLevelIndex);
  }
  setCurrentLevel(levelNames[newLevelIndex]);
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
 * Handles the failure to load levels from the server.
 *
 * If the delay exceeds 60 seconds, it logs an error. Otherwise, it logs a warning.
 *
 * @param delay The current retry delay in seconds.
 * @returns True if retrying should continue, false otherwise.
 */
const handleLevelLoadFailure = (delay: number): boolean => {
  if (delay > 60) {
    console.error("Failed to load levels after multiple retries. Stopping.");
    return false; // Stop retrying
  } else {
    console.warn(`No levels found. Retrying in ${delay} seconds...`);
    return true; // Continue retrying
  }
};

/**
 * Asynchronously retrieves and updates the list of levels from the server.
 *
 * Clears existing `levelNames` and resets `currentLevelIndex` to 0.
 * Repeatedly calls `call_levels_route()` every 5 seconds until a non-empty list
 * is returned. After successfully loading levels, updates the UI and
 * selects the first level.
 *
 * @async
 * @returns {Promise<void>}
 */
export const updateLevelList = async (): Promise<void> => {
  currentLevelIndex = 0;
  levelNames.length = 0; // Clear existing levels
  let delay = 5;

  while (true) {
    const allLevels = await call_levels_route();
    if (allLevels && allLevels.length > 0) {
      levelNames.push(...allLevels);
      updateLevelListUI();
      setCurrentLevel(levelNames[0]);
      break;
    } else {
      if (!handleLevelLoadFailure(delay)) {
        break; // Stop retrying if the delay exceeds 60 seconds
      }
      await sleep(delay);
      delay += 5;
    }
  }
}

const updateLevelListUI = () => {
  const select = document.getElementById("levelSelector") as HTMLSelectElement;
  if (!select) { return; }
  const oldIndex = select.selectedIndex;
  // remove all options
  while (select.firstChild) {
    select.removeChild(select.firstChild);
  }

  if (levelNames.length === 0) {
    const option = document.createElement("option");
    option.value = "";
    option.innerText = "Keine Level gefunden";
    select.appendChild(option);
    return;
  }

  // add option for each level
  levelNames.forEach((levelName) => {
    const option = document.createElement("option");
    const isAvailable = isLevelAvailable(levelName);
    option.value = levelName;
    option.innerText = levelName;
    option.disabled = !isAvailable;
    select.appendChild(option);
  });
  select.selectedIndex = oldIndex; // restore old index

  // Update arrow buttons
  const prevButton = document.getElementById("prev-level") as HTMLButtonElement;
  const nextButton = document.getElementById("next-level") as HTMLButtonElement;
  const selectedIndex = select.selectedIndex;
  prevButton.disabled = selectedIndex === 0;
  nextButton.disabled = selectedIndex === levelNames.length - 1 || !isLevelAvailable(levelNames[selectedIndex + 1]);
}

export const setupLevelSelector = () => {
  const header = document.getElementsByTagName("header")[0];

  const selectorContainer = document.createElement("div");
  selectorContainer.id = "level-selector-container";
  selectorContainer.classList.add("level-selector-container");

  const prevButton = document.createElement("button");
  prevButton.innerHTML = "◀";
  prevButton.id = "prev-level";
  prevButton.classList.add("level-nav-button");
  prevButton.disabled = true;

  const select = document.createElement("select");
  select.id = "levelSelector";
  select.classList.add("level-selector");

  const nextButton = document.createElement("button");
  nextButton.innerHTML = "▶";
  nextButton.id = "next-level";
  nextButton.classList.add("level-nav-button");
  nextButton.disabled = levelNames.length <= 1;

  selectorContainer.append(prevButton, select, nextButton);

  select.addEventListener("change", (event) => {
    const target = event.target as HTMLSelectElement;
    const selectedIndex = target.selectedIndex;
    const selectedLevelName = target.value;

    prevButton.disabled = selectedIndex === 0;
    nextButton.disabled = selectedIndex === levelNames.length - 1 || !isLevelAvailable(levelNames[selectedIndex + 1]);

    setCurrentLevel(selectedLevelName);
  });

  prevButton.addEventListener("click", () => {
    if (select.selectedIndex > 0) {
      select.selectedIndex -= 1;
      select.dispatchEvent(new Event("change"));
    }
  });

  nextButton.addEventListener("click", () => {
    if (select.selectedIndex < levelNames.length - 1 && isLevelAvailable(levelNames[select.selectedIndex + 1])) {
      select.selectedIndex += 1;
      select.dispatchEvent(new Event("change"));
    }
  });

  header.insertBefore(selectorContainer, header.lastElementChild || null);

  updateLevelListUI();

  if (select.options.length > 0) {
    prevButton.disabled = select.selectedIndex === 0;
    nextButton.disabled = select.selectedIndex === levelNames.length - 1 || !isLevelAvailable(levelNames[select.selectedIndex + 1]);
  }

  // Initialize level progress if not set
  if (localStorage.getItem("levelProgress") === null) {
    setLevelProgress(0);
  }

  return select;
}
