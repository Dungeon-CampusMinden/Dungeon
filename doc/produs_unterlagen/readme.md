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
   ./gradlew runBasicStarter
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
* 
