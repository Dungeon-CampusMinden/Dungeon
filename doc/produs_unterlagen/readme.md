# How to Produs Guide

> **[Deutsche Version](./readme_de.md)**

Welcome to the **Produs Guide**!

Produs is a platform for learning to code through play. In interactive dungeon levels you solve puzzles, control a hero, and improve your programming skills step by step. There are three different modules aimed at different target groups:

- **Blockly Dungeon (Web Version):** Introduction to programming with a block-based interface in the browser. Ideal for complete beginners with no prior knowledge (from grade 6).
- **Java Dungeon (Desktop Version):** The same dungeon levels, but the solution is written as Java code in Visual Studio Code. Suitable for students from grade 8 with some initial programming experience.
- **Advanced Dungeon:** An advanced dungeon project where real Java classes are edited in an IDE (e.g. IntelliJ). Suitable for students from grade 10 with prior Java knowledge.

For a deeper understanding of the workshop concept there are detailed descriptions:
- [Blockly Dungeon Workshop](./materials/blockly_workshop.md)
- [Java Dungeon Workshop](./materials/java_workshop.md)
- [Advanced Dungeon Workshop](./materials/advanced_workshop.md)

---

## Table of Contents

1. [Blockly Dungeon (Web Version)](#1-blockly-dungeon-web-version)
2. [Java Dungeon (Desktop Version)](#2-java-dungeon-desktop-version)
3. [Advanced Dungeon](#3-advanced-dungeon)
4. [Tips and Common Pitfalls](#4-tips-and-common-pitfalls)

---

## 1. Blockly Dungeon (Web Version)

### What is it?

The Blockly Dungeon is the beginner-friendly variant. You program the hero using visual blocks (similar to Scratch) directly in the browser. This way you can gain your first programming experience without needing to know a programming language.

### What needs to be installed?

You only need **Java 21** on your computer. Nothing else.

**Installing Java 21:**

- **Windows / Mac / Linux:** Download [Java 21](https://www.oracle.com/de/java/technologies/downloads/#java21). Choose the version matching your operating system (Windows, macOS or Linux) and install it. Restart your device. 

**Tip:** To verify that Java is installed correctly, open a terminal (Windows: `cmd` or PowerShell; Mac/Linux: Terminal) and type:

```bash
java -version
```

You should see output like `openjdk version "21.x.x"`.

### Download and start the Blockly Dungeon

1. Go to the **Releases page**: [https://github.com/Dungeon-CampusMinden/Dungeon/releases](https://github.com/Dungeon-CampusMinden/Dungeon/releases)
2. Download the file **`Blockly-web.jar`** (under "Assets" in the latest release).
3. Start the dungeon by **double-clicking** the JAR file.
   - Alternatively: Open a terminal, navigate to the download folder and run:
     ```bash
     java -jar Blockly-web.jar
     ```
4. Open your browser and go to: [http://localhost:8081/](http://localhost:8081/)
5. The Blockly interface appears - you can start right away!

---

## 2. Java Dungeon (Desktop Version)

### What is it?

In the Java Dungeon you solve the same dungeon levels as in the web version, but you write the solution as real Java code in Visual Studio Code. A special VS Code extension sends the code to the running dungeon game.

### What needs to be installed?

1. **Java 21** (see [above](#installing-java-21))
2. **Visual Studio Code** - download from: [https://code.visualstudio.com/](https://code.visualstudio.com/)
   - Available for Windows, Mac and Linux.
3. **The Blockly Code Runner extension (.vsix file)**

### Step by step: Setting up the Java Dungeon

**Step 1: Download the JAR file**

1. Go to the Releases page: [https://github.com/Dungeon-CampusMinden/Dungeon/releases](https://github.com/Dungeon-CampusMinden/Dungeon/releases)
2. Download the file **`Blockly-desktop.jar`**.
3. Start the dungeon by **double-clicking** the JAR file.
   - Alternatively in the terminal:
     ```bash
     java -jar Blockly-desktop.jar
     ```

**Step 2: Install the VS Code extension**

1. Download the **`.vsix` file** from the same Releases page.
2. Open **Visual Studio Code**.
3. Click the **Extensions icon** in the left sidebar (or press `Ctrl+Shift+X` or `Cmd+Shift+X` on Mac).
4. Click the **three dots (`...`)** in the top-right corner of the Extensions view → "More Actions...".
5. Select **"Install from VSIX..."**.
6. Navigate to the downloaded `.vsix` file and select it.
7. VS Code installs the extension. You may be prompted to reload the window - do so.

**Step 3: Write and run code**

1. Create a **new file** in VS Code with the `.java` extension (e.g. `MyLevel.java`).
2. Write your Java code to control the hero. Use the cheat sheets for reference:
   - [Dungeon Commands (Cheat Sheet)](./materials/commands_cheat_sheet.md)
   - [Simple Java Commands (Cheat Sheet)](./materials/java-cheat-sheet_simple.md)
3. Click **"Run Blockly Code"** in the top-right corner of VS Code to send the code to the running dungeon.
4. In the game window you can see the hero executing your commands!

---

## 3. Advanced Dungeon

### What is it?

The Advanced Dungeon is a standalone dungeon project for advanced users. Here you no longer work through an external interface but instead edit Java classes directly in a real development environment (IDE). The game uses hot-reloading: code changes are applied automatically **without needing to restart the game**.

### What needs to be installed?

1. **Java** - a Java version compatible with the repository (at least Java 21). Download: [https://jdk.java.net/21/](https://jdk.java.net/21/) or [Adoptium Temurin 21](https://adoptium.net/temurin/releases/?version=21).
2. **Git** - needed to download the project. Download: [https://git-scm.com/downloads](https://git-scm.com/downloads)
   - **Windows:** Download the installer and follow the instructions. The default settings are usually sufficient.
   - **Mac:** Git is often already pre-installed. If not: run `xcode-select --install` in the terminal or download from [https://git-scm.com/downloads/mac](https://git-scm.com/downloads/mac).
   - **Linux:** Install via the package manager, e.g. `sudo apt install git` (Ubuntu/Debian) or `sudo dnf install git` (Fedora).
3. **A Java IDE** - we recommend **IntelliJ IDEA** (Community Edition is sufficient). Download: [https://www.jetbrains.com/idea/download/](https://www.jetbrains.com/idea/download/)
   - Alternatively: Eclipse or VS Code with Java extensions.

### Step by step: Setting up the Advanced Dungeon

**Step 1: Clone the repository**

Open a terminal and run:

```bash
git clone https://github.com/Dungeon-CampusMinden/Dungeon.git
```

This downloads the entire project.

**Step 2: Open the project in your IDE**

Open the downloaded `Dungeon` folder in IntelliJ IDEA (or your preferred IDE). IntelliJ automatically recognises that it is a Gradle project and downloads the dependencies. This may take a few minutes the first time.

**Step 3: Start the Advanced Dungeon**

Open a terminal in the project folder and run:

- **Windows:**
  ```bash
  .\gradlew.bat runPortal
  ```
- **Mac / Linux:**
  ```bash
  ./gradlew runPortal
  ```

The game starts and shows the first puzzle.

### Where do you work? - The workspace in detail

Students work exclusively in the **`riddles`** package within the project. The path in the repository is:

```
advancedDungeon/src/portal/riddles/
```

This folder contains several Java files, each corresponding to a puzzle in the dungeon. Each file contains one or more methods that throw an `UnsupportedOperationException` - this is the placeholder that students need to replace with their own code.

### What needs to be changed?

**The principle is always the same:** Every method that throws an `UnsupportedOperationException` must be filled with a working implementation.

An example - this is what a method looks like **before** editing:

```java
@Override
public void activate(Entity emitter) {
    throw new UnsupportedOperationException("Implement this method!");
}
```

And this is what it could look like **after** editing (example `MyBridgeSwitch`):

```java
@Override
public void activate(Entity emitter) {
    LightBridgeFactory.activate(emitter);
}
```

Students need to figure out which method to call, which parameters are needed, and how the logic of the respective puzzle works. The existing helper classes in the `abstraction` package and the Javadoc comments in the code can help with this.

### Overview of puzzle files

Here is an overview of all files in the `riddles` package and what needs to be implemented:

| File | Task |
|---|---|
| `MyPlayerController` | Process keyboard input (W/A/S/D for movement, Q/F for abilities, E for interaction) |
| `MyCalculations` | Calculate portal exit position, determine endpoints of light walls/bridges, calculate tractor beam forces |
| `MyCube` | Create a portal cube with specific properties (mass, texture, pickupability) |
| `MySphere` | Create a portal sphere with specific properties |
| `MyPortalConfig` | Set portal configuration (cooldown, speed, range, target position) |
| `MyBridgeSwitch` | Switch a light bridge on and off |
| `MyLightWallSwitch` | Switch a light wall on and off |
| `MyLaserGridSwitch` | Activate and deactivate laser grids |
| `MyTractorBeamLever` | Reverse the thrust direction of the tractor beam |
| `MyEnergyPelletCatcherBehavior` | Define behaviour when catching an energy pellet |

### Example: Solving an Advanced Dungeon puzzle

Let's take the file **`MyPlayerController`** as an example. Here you need to implement the keyboard controls for the hero (W/A/S/D to move).

**Step 1:** Open the file `MyPlayerController.java` in the `riddles` package. You will find the method `processKey(String key)`, which throws an `UnsupportedOperationException`.

**Step 2:** Think: What should happen when a key is pressed? When "W" is pressed, the hero should move up, "S" moves down, "A" moves left and "D" moves right. You can use the methods `hero.setXSpeed()` and `hero.setYSpeed()` for this.

**Step 3:** Replace the placeholder code:

```java
protected void processKey(String key) {
    if (key.equals("W")) move(0, 5);
    if (key.equals("S")) move(0, -5);
    if (key.equals("A")) move(-5, 0);
    if (key.equals("D")) move(5, 0);
}

private void move(int x, int y) {
    if (x != 0) hero.setXSpeed(x);
    if (y != 0) hero.setYSpeed(y);
}
```

**Step 4:** Save the file. Thanks to hot-reloading, the change is automatically applied to the running game. Press W/A/S/D in the game and check whether the hero moves. Also test Q, F and E for the other actions.

---

## 4. Tips and Common Pitfalls

### General

- **The game doesn't respond to input?** Sometimes you need to **click out of and back into the game window** for the focus to be set correctly. Simply click once on the game window to restore focus.
- **You don't need to restart the game every time!** When you change your code and run it again, the current state is updated. You don't need to close and reopen the game. This applies to both the web/desktop version (just send the code again with "Run Blockly Code") and the Advanced Dungeon (hot-reloading).

### Blockly / Java Dungeon

- **Switching levels:** In Java code you can load a level directly with `loadLevel(INDEX)` or `loadNextLevel()`. So you don't always have to solve all previous levels again.
- **Browser cache:** If the Blockly web interface looks odd or doesn't respond, try a hard reload in the browser (`Ctrl+Shift+R` or `Cmd+Shift+R`).

### Advanced Dungeon

- **Use hot-reloading:** Code changes are applied automatically. Save the file and the change takes effect immediately in the running game - no restart needed.
- **Only work in the `riddles` package:** Students should only modify files in the `riddles` package. The rest of the code is the framework and should not be touched.
- **`UnsupportedOperationException` is your guide:** Search the files for `throw new UnsupportedOperationException(...)` - these are exactly the places where your own code needs to be written.
- **Use the helper classes:** The `abstraction` package and classes like `Tools`, `LightBridgeFactory`, `LightWallFactory`, `TractorBeamFactory` etc. offer pre-built methods that help with solving the puzzles. Use your IDE's auto-completion to see which methods are available.
- **Start via Gradle, not the main class directly:** The game must be started via Gradle (`./gradlew runPortal`) for hot-reloading to work. Starting directly via the IDE's run configuration does not work correctly.

---

## Materials

- [Dungeon Commands (Cheat Sheet)](./materials/commands_cheat_sheet.md)
- [Simple Java Commands (Cheat Sheet)](./materials/java-cheat-sheet_simple.md)
- [Detailed Java Cheat Sheet](./materials/java-cheat-sheet.md)
- [Blockly Level Solutions](./solution/blockly)
- [Advanced Dungeon Solutions](./solution/advancedDungeon)

---

*Produs is part of the "Produs" project funded by: EFRE-20300105, [Pakt für Informatik 2.0](https://www.efre.nrw/einfach-machen/foerderung-finden/pakt-fuer-informatik-20), [EFRE/JTF NRW 2021-27](https://www.efre.nrw/)*
