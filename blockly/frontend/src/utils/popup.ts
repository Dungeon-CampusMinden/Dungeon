export const changePopupText = (text : string) => {
  const popupParagraph = document.querySelector(".popupText");
  const popup = document.querySelector(".popupDiv");
  if(popupParagraph && popup) {
    popupParagraph.textContent = text;
    popup.style.opacity = "1"
    popup.style.display = "block";
  }
}
