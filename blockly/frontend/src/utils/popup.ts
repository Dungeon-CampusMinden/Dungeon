export const updatePopup = (text : string) => {
  const popupParagraph = document.querySelector(".popupText");
  const popup = document.querySelector(".popupDiv");
  if(popupParagraph && popup) {
    popupParagraph.textContent = text;
    displayPopup();
  }
}

export const displayPopup = () => {
  const popup = document.querySelector(".popupDiv");
  if (popup) {
    popup.style.opacity = "1"
    popup.style.display = "block";
  }
}


export function updateElementAlignment() {
  const toolbox = document.querySelector(".blocklyToolbox"); // Die tatsächliche Toolbox-Box
  const myElement = document.querySelector(".popupDiv");
  const flyOutElement = document.querySelector(".blocklyToolboxFlyout");

  if (toolbox && myElement && flyOutElement) {
    // 1. Breite der Haupt-Toolbox holen
    const toolboxWidth = toolbox.getBoundingClientRect().width;

    // 2. Prüfen, ob das Flyout sichtbar ist und seine Breite holen
    const flyoutStyle = window.getComputedStyle(flyOutElement);
    let flyoutWidth = 0;

    // Wenn das Flyout nicht versteckt ist, nehmen wir dessen Breite dazu
    if (flyoutStyle.display !== "none" && flyoutStyle.visibility !== "hidden") {
      flyoutWidth = flyOutElement.getBoundingClientRect().width;
    }

    // 3. Gesamtbreite berechnen
    const totalOffset = toolboxWidth + flyoutWidth;

    // 4. Den Wert an CSS übergeben
    document.documentElement.style.setProperty('--toolbox-width', totalOffset + 'px');
  }
}

// Einmal ausführen und bei jedem Fenster-Resize wiederholen
// window.addEventListener('resize', updateElementAlignment);
export function addListenerToFlyOut()  {
  const flyOutElement = document.querySelector(".blocklyToolboxFlyout");

  const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      if (mutation.attributeName === "style") {
        updateElementAlignment(); // Deine Funktion von oben
      }
    });
  });

// Beobachte Änderungen am Style-Attribut des Flyouts
  if (flyOutElement != null) {
    observer.observe(flyOutElement, { attributes: true });
  }

}
