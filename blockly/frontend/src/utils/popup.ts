/**
 * Updates the popup and displays the text
 * @param text the text that should be displayed.
 */
export const updatePopup = (text : string) => {
  const popupParagraph = document.querySelector(".popupText");
  const popup = document.querySelector(".popupDiv");
  if(popupParagraph && popup) {
    popupParagraph.textContent = text;
    displayPopup();
  }
}

const displayPopup = () => {
  const popup = document.querySelector(".popupDiv");
  if (popup) {
    // @ts-ignore
    popup.style.opacity = "1"
    // @ts-ignore
    popup.style.display = "block";
  }
  setupCross();
}

const setupCross = () => {
  const cross = document.querySelector(".cross")
  cross?.addEventListener("click", () => {
    const popup = document.querySelector(".popupDiv");
    if (popup) {
      // @ts-ignore
      popup.style.opacity = "0"
      // @ts-ignore
      popup.style.transition = "0.3s";
      // @ts-ignore
      popup.style.display = "none";
    }
  })
}

/**
* Updates the alignment of the popup
*/
export function updateElementAlignment() {
  const toolbox = document.querySelector(".blocklyToolbox");
  const myElement = document.querySelector(".popupDiv");
  const flyOutElement = document.querySelector(".blocklyToolboxFlyout");

  if (toolbox && myElement && flyOutElement) {
    // 1. Get width of toolbox
    const toolboxWidth = toolbox.getBoundingClientRect().width;

    // 2. check if the flyout is visible and get width
    const flyoutStyle = window.getComputedStyle(flyOutElement);
    let flyoutWidth = 0;

    // If the fly out is visible add the width
    if (flyoutStyle.display !== "none" && flyoutStyle.visibility !== "hidden") {
      flyoutWidth = flyOutElement.getBoundingClientRect().width;
    }

    // 3. Calculate total width
    const totalOffset = toolboxWidth + flyoutWidth;

    // 4. change value in css
    document.documentElement.style.setProperty('--toolbox-width', totalOffset + 'px');
  }
}

/**
 * Add listener to fly out element
 */
export function addListenerToFlyOut()  {
  const flyOutElement = document.querySelector(".blocklyToolboxFlyout");

  const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      if (mutation.attributeName === "style") {
        updateElementAlignment(); // Deine Funktion von oben
      }
    });
  });

// Observer changes of the flyout element
  if (flyOutElement != null) {
    observer.observe(flyOutElement, { attributes: true });
  }
}
