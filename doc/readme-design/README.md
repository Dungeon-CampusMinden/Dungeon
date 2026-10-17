# README design

The repository README uses native Markdown/HTML and local graphics. Its layout does not depend on custom CSS, JavaScript, external badge services or an image-hosting account. The source text remains readable without its images. Download links and room documentation links are ordinary anchors.

## Visual system

- Slate `#0b1018`, ivory `#f5f1e8`, champagne `#e1bd7b` for the project identity.
- Lavender `#c6a1f0`, cyan `#78d8dc` and coral `#f3a08d` identify the three rooms.
- A cinematic masthead, three navigation cards and one wide feature banner per game.
- Desktop platform information, action buttons and conceptual diagrams are local, self-contained SVGs.
- Prominent headings, alt text and native room links preserve the reading structure.

## Sources and disclosures

The header and three feature banners are graphic compositions of `doc/img/readme/soulweaver.jpg`, `system-recovery.jpg` and `the-last-hour.jpg`. Those are the previously approved AI-generated promotional illustrations based on project assets. They are not gameplay screenshots. Each composition contains an AI disclosure and the README also identifies them as promotional artwork.

The small Dungeon cat mark comes from `doc/img/logo/cat_logo_128x128.png`. Existing project credits and licenses remain in the root README.

The gameplay image and GIF link directly to `game/assets/images/lasthour.png` and `doc/press_kit/escape_room/escape_room_intro.gif`. They have not been retouched. The screenshot is from an earlier build; its displayed timer is not a statement of the current room's time limit. The Wizard diagram depicts the documented workflow; it is not a screenshot of the authoring interface.

## Regenerate the graphics

The source compositor is `generate-assets.cjs`. It uses Node.js and `sharp` in a separate tools directory, so the game's dependencies and build remain unchanged. From the repository root, for example:

```sh
npm install --prefix /tmp/dungeon-readme-tools sharp
NODE_PATH=/tmp/dungeon-readme-tools/node_modules node doc/readme-design/generate-assets.cjs
```

On Windows, set `NODE_PATH` to the tools directory's `node_modules` in the current shell before running the Node command.

The script reads the three original JPEGs and the cat logo and generates two responsive header JPEGs, three feature JPEGs and thirteen small SVGs. The header and platform strip use native `picture` elements with smaller-screen variants. Edit the script to change titles, colors and graphic layouts; edit `README.md` for the body text and destinations.

The older `dungeon-hero.svg`, `gameplay-preview.gif` and `gameplay-the-last-hour.png` remain in the asset directory for compatibility with older references. The new README links directly to the existing gameplay originals instead of adding more duplicates.

## Reference research

Reviewed on 17 October 2026. These repositories informed the information hierarchy and use of images; no artwork or text was copied:

- [Veloren](https://github.com/veloren/veloren): large branded opening, player downloads and links to deeper documentation.
- [shapez](https://github.com/tobspr-games/shapez.io): linked promotional artwork and direct game entry points.
- [Endless Sky](https://github.com/endless-sky/endless-sky): a gameplay-led introduction and separate player/developer information.
- [OpenRA](https://github.com/OpenRA/OpenRA): distinct entry points for playing, mapping, modding and contributing.

GitHub's rendering pipeline sanitizes custom styles and scripts; see [github/markup](https://github.com/github/markup). The design therefore uses image assets and supported native elements, including [collapsed sections](https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/organizing-information-with-collapsed-sections).
