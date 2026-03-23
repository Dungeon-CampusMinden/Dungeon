# How to Produs Guide

Welcome to the **How to Produs Guide**.

This guide will help you get started with Produs, a platform that lets you learn coding through fun, interactive dungeons. Whether you are a total beginner or already know some programming, Produs provides challenges to test your skills.

**What is Produs?**
Produs is a platform for coding and game-based learning. You solve puzzles, complete dungeon levels, and improve your logic and programming skills while having fun.

To understand the workshop concept read
* [Blockly Dungeon](./materials/blockly_workshop.md)
* [Java Dungeon](./materials/java_workshop.md)
* [Advanced Dungeon](./materials/advanced_workshop.md)

---
For the blockly and Java Dungeon there are two different approaches for installation: There is a simple way for
non-coders and a more advanced way for experienced programmers.

In all cases you have to install **Java 21**

## Blockly Dungeon (easy Installation)

1. Install **Java 21**
2. Download the blockly Jar.
   1. Go to https://github.com/Dungeon-CampusMinden/Dungeon
   2. Click on the releases on the right side of the page
   3. Scroll to the last release and download the **Blockly-web.jar**
3. Make sure there are no spaces in the filename, this leads to an error
4. Start the dungeon by double-clicking the JAR file
5. Open your browser and go to [http://localhost:8081/](http://localhost:8081/)

## Java Dungeon (easy Installation)

1. Install **Java 21**
2. Download the blockly Jar.
    1. Go to https://github.com/Dungeon-CampusMinden/Dungeon
    2. Click on the releases on the right side of the page
    3. Scroll to the last release and download the **Blockly-desktop.jar**
3. Make sure there are no spaces in the filename, this leads to an error
4. Start the dungeon by double-clicking the JAR file
5. Install [Visual Studio Code](https://code.visualstudio.com/)
6. Download the `.vsix` file below and install the extension (see next)
7. In VS Code, create a new file with a `.java` extension
8. Use the cheat sheets to write your code:
    * [Dungeon Commands](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/produs_unterlagen/materials/commands_cheat_sheet.md)
    * [Simple Java Commands](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/produs_unterlagen/materials/java-cheat-sheet_simple.md)
9. Click `Run Blockly Code` in the top-right corner of Visual Studio Code to see your work in action





## Step 1: Install the Required Tools

Before you start, you need to install a few things:

1. **Java 21**. This is required to run the game.
2. **Git**. This is used for downloading the project files.
3. **Java IDE**. We recommend **IntelliJ** for editing Java code.
4. **VS Code**. This is needed for the Java Dungeon.
5. **Node.js and npm**. These are required for Blockly and web components.
6. **Clone the dungeon repository**. This is the project you will work on.

Important: For Blockly and Java Dungeon, follow the installation guides:

* [Installation](../../blockly/doc/installation.md)
* [Install Extension](../../blockly/doc/install-extension.md)
---

## Step 2: Test the Basic Game

After installing everything:

1. Open your terminal or command prompt.
2. Navigate to the project folder.
3. Run the basic starter game with:

   ```bash
   .\gradlew.bat runBasicStarter
   ```
or
   ```bash
   bash gradlew runBasicStarter
   ```

   You should see a simple dungeon level start. If it works, you are ready to move on.

---

## Step 3: Setting Up Blockly and Java Dungeon

### For Blockly Web Version

1. **Windows**: run `start_blockly.bat`
2. **Mac or Linux**: run `start_blockly.sh`
3. When asked:

    * Select **Yes** to start the Web Version
    * Select **No** for VS Code

This will open the Blockly web interface.

* [Solutions](./solution/blockly)

**How to add solutions**

1. Copy the JSON string from a solution file.
2. Open Chrome and go to Inspect → Application → Local Storage → `http://localhost:5173`.
3. Add a key like `dungeonWorkspace/level001` and paste the JSON as its value.
4. To unlock all levels, set `levelProgress` to `100`.

---

### For Java Dungeon

1. Copy the code from the solution folder into a `.java` file.
2. Open it in VS Code with the required extension.
3. Run the code to see it in action.

---

## Step 4: Advanced Dungeon

1. Run the advanced dungeon with:

   ```bash
   gradlew runAdvancedDungeon
   ```
2. Edit files in `advancedDungeon/produsAdvanced/riddles` to change puzzles and logic.

* [Solutions](./solution/advancedDungeon)
