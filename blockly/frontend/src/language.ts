import * as Blockly from "blockly";
import * as En from "blockly/msg/en";
import * as De from "blockly/msg/de";

export const setupLanguageToggle = (workspace) => {
  document.querySelector(".flag")?.addEventListener("click", event => {
    const imgElement = event.currentTarget as HTMLImageElement;
    if (imgElement == null) return;

    const currentLang = imgElement.dataset.lang;

    if (currentLang == "en") {
      changeToEnglishLanguage(workspace);
      imgElement.dataset.lang="de"
      imgElement.src="german-flag.png"
      imgElement.alt = "German language"
    } else {

      imgElement.dataset.lang="en"
      imgElement.src="english-flag.png"
      imgElement.alt = "English language"
    }
  })
}

const changeToEnglishLanguage = (workspace) => {
  console.log("changing language to english");
  Blockly.setLocale(En as any);

  const state = Blockly.serialization.workspaces.save(workspace);

  workspace.clear();

  Blockly.serialization.workspaces.load(state, workspace);
}


