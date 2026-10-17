# README design

The README combines illustrated game features with native HTML navigation, download fields and authoring steps. Interface text uses GitHub's own typography and light/dark theme. There are no image buttons, custom CSS, scripts, external badges or image-hosting dependencies.

The introduction, image captions and promotional copy do not state a fixed number of escape rooms. Each room is an independent section with its own banner, description, download and documentation link. Add a section when another room is ready.

## Visual system

- Slate, ivory and champagne for the illustrated Dungeon identity.
- Room-specific accent colors for feature artwork.
- A compact masthead with desktop and mobile variants.
- Native HTML table cells for navigation, downloads and Wizard steps. Text remains selectable, accessible and readable when images are unavailable.
- Platform information and the learning loop are ordinary HTML text. Their colors follow GitHub's active theme.

## Sources and disclosures

The header and feature banners use the previously approved AI-generated promotional artwork in `doc/img/readme/soulweaver.jpg`, `system-recovery.jpg` and `the-last-hour.jpg`. They are illustrations based on project assets, not gameplay screenshots. Each generated banner contains an AI disclosure; the README also identifies the promotional artwork.

The cat mark is `doc/img/logo/cat_logo_128x128.png`. Existing project credits, licenses and funding acknowledgements remain in the root README.

Actual gameplay links directly to `game/assets/images/lasthour.png` and `doc/press_kit/escape_room/escape_room_intro.gif`, without retouching. The screenshot is from an earlier build, so its displayed timer does not describe the current room's time limit.

## Regenerate the artwork

`generate-assets.cjs` uses Node.js and `sharp` in a separate tools directory. From the repository root:

```sh
npm install --prefix /tmp/dungeon-readme-tools sharp
NODE_PATH=/tmp/dungeon-readme-tools/node_modules node doc/readme-design/generate-assets.cjs
```

On Windows, set `NODE_PATH` to the tools directory's `node_modules` in the current shell before running Node.

The script creates desktop/mobile mastheads and one desktop/mobile feature pair per entry in its `games` list. It uses the list's length to arrange the header artwork and does not include a room total or position in its copy. Add an entry with its source image, genre, category, title and hook to create artwork for another room. The native interface is edited directly in `README.md` and needs no regeneration.

## GitHub rendering

The layout is checked through GitHub's Markdown API with GitHub's own stylesheets in light/dark modes and across phone, tablet and desktop widths. GitHub's page renderer adds heading anchors after Markdown conversion; the local preview mirrors that step.

GitHub strips custom scripts and styles; see [github/markup](https://github.com/github/markup). The native layout uses [tables](https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/organizing-information-with-tables), links and collapsed sections.

## References

Reviewed on 17 October 2026. No artwork or text was copied:

- [Veloren](https://github.com/veloren/veloren): a branded opening and deeper player documentation.
- [shapez](https://github.com/tobspr-games/shapez.io): linked promotional artwork and direct game entry points.
- [Endless Sky](https://github.com/endless-sky/endless-sky): a gameplay-led introduction and separate player/developer information.
- [OpenRA](https://github.com/OpenRA/OpenRA): separate paths for playing and creating.
