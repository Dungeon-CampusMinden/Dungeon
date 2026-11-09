# Building an escape room with the Wizard

The Wizard lets educators build their own escape room in the browser, without programming and
without editing any files by hand. At the end it creates a single game file
(`<title>-WizardRoom.jar`) that you hand out to all players. This guide summarises the steps;
the full description of the authoring flow is in
[`v0/wizard-ui-flow-v0.md`](v0/wizard-ui-flow-v0.md) (German).

## Before you start

The Wizard currently runs as a local program and is meant to be set up with technical support:

-   [Java SE Development Kit 25 LTS](https://jdk.java.net/25/) and Node.js 22 are required.
-   Start it from the repository with `wizard/start_wizard_dev.sh` (Linux) or
    `wizard\start_wizard_dev.cmd` (Windows). The script builds the Wizard and opens it in your
    browser at `http://127.0.0.1:27777/`.
-   Your draft is saved automatically in the browser. Work in one browser tab at a time.
-   The pages of the Wizard are in German, and in this version the room content is German
    (`de-DE`) as well.

## Step by step

1.  **Start or continue** (*Starten oder fortsetzen*): create a new draft, continue the last
    one or open another local draft.
2.  **Basic details** (*Eckdaten*) and, after the story, **game settings**
    (*Spieleinstellungen*): room title, content language, target group, prior knowledge,
    number of players (1 to 4), time limit (5 to 240 minutes) and at least one learning
    objective.
3.  **Story** (*Geschichte*): the mission, the intro pages and the pages shown on success
    (with a hard time limit also the pages shown on failure). Each page appears as its own
    screen.
4.  **Own images and files** (*Eigene Bilder und Dateien*): upload your own PNG or JPEG images,
    or pick images from the game library.
5.  **Puzzles** (*Rätsel*): each puzzle has a title, at least one learning objective, a time
    estimate, information sources and at least one input. Available inputs are a number code
    (1 to 8 digits), a multiple-choice question (2 to 6 answers, one or more correct), a
    matching task (1 to 4 boxes, 2 to 8 items) and finding an information source. Add hints in
    order; each gets one of the levels *Orientierung* (orientation), *Lösungsweg* (approach) or
    *Lösung* (solution). Players have to confirm before a hint is shown.
6.  **Game flow** (*Spielablauf*): connect the puzzles between the start and the end. An arrow
    means the next puzzle becomes available after the previous one; several incoming arrows
    mean all of them have to be solved first.
7.  **End of the game** (*Spiel-Ende*): name the exit. It opens as soon as all puzzles before
    the end are solved.
8.  **Create the game** (*Spiel erstellen*): the Wizard checks the draft in the background and
    lists all problems; a click on a problem opens the step it belongs to. Warnings do not
    block. *Spiel erstellen und herunterladen* (create and download) builds the game file.

Before you hand out the game, go through the checklist the Wizard shows: can the solution be
derived from the task and the material, is the task clear for the target group, do difficulty
and time fit the lesson, does every hint help, and is the learning objective linked to the
debriefing?

## Playing the room

Give the same `<title>-WizardRoom.jar` to the host and to every other player. With Java 25
installed, `java -jar <title>-WizardRoom.jar` opens the menu where one player hosts and the
others join. Everything in the file, including the solutions, can be read by anyone who has it.
