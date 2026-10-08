# Aim Trainer

A 2D aim-training game written in Java. Shoot targets against the clock, keep your accuracy up, and watch your scores improve on a graph that is saved between sessions.

Built as an individual first-year project for BSc Computer Science at City St George's, University of London.

## Features

- **Three levels of increasing difficulty**
  - Level 1: targets appear one at a time on a 3x3 grid
  - Level 2: moving targets that bounce around the screen
  - Level 3: durable "switching" targets that must be tracked with continuous fire
- **Live HUD** showing score, time remaining, lives and accuracy
- **Progress tracking**: scores and unlocked levels are saved to disk, and the last ten scores are drawn as a line graph on the pause and game-over screens
- **Customisation**: selectable crosshairs, target skins and backgrounds, all remembered between sessions
- **Settings menu** with volume controls, mute, and full key rebinding (keyboard and mouse buttons)
- **Collectible hearts** that restore lost lives when shot

## Tech stack

| Area | Used |
| --- | --- |
| Language | Java |
| Physics | CityEngine (a teaching library built on JBox2D) |
| Interface | Java Swing and AWT (Graphics2D) |
| Storage | Java file I/O (plain text save files) |
| Testing | JUnit 5 |

## Controls

All controls can be rebound in the settings menu.

| Action | Default |
| --- | --- |
| Aim | Mouse movement |
| Shoot | Left click |
| Pause | Esc |
| Quick restart | F3 |

## Project structure

```
src/game/
  core/      Game loop, timer, the abstract GameLevel class and Level1 to Level3
  bodies/    Physics objects: targets, bullets, hearts, crosshair
  managers/  Saving and loading, sound, and the crosshair, target and background choices
  input/     Keyboard and mouse handling, key bindings
  ui/        Menus, overlays and the score graph
  utils/     Polygon editor used to outline collision shapes
test/game/   Unit tests
data/        Images, sounds and save files
```

### Design notes

- **Inheritance:** every level extends the abstract `GameLevel` class, which holds the shared rules (timer, lives, scoring). Each subclass only defines what makes that level different.
- **Collision handling:** bullets and targets use the physics engine's collision and sensor listeners, so hits are detected by the engine rather than by manual coordinate checks.
- **Separation of concerns:** saving, sound and asset choices each live in their own manager class, so the game logic never deals with files directly.

## Running the game

You need:

- JDK 21 or newer
- The CityEngine library. This is provided to City students and is not included in this repository.

Steps:

1. Clone this repository and open the folder in IntelliJ IDEA.
2. Add the CityEngine library to the project (File > Project Structure > Libraries).
3. Run `game.core.Game`.

## Running the tests

The tests cover score saving, level progression and key-binding labels. They use a temporary folder, so they never change real save data.

1. In IntelliJ, right-click the `test` folder and choose Mark Directory as > Test Sources Root.
2. Open a test file and accept the prompt to add JUnit 5 to the classpath.
3. Right-click the `test` folder and choose Run 'All Tests'.

## What I learned

- Structuring a larger program with packages, inheritance and polymorphism
- Event-driven programming with listeners for input, collisions and timed steps
- Building interfaces by hand with nested Swing layout managers
- Reading and writing files safely, including handling missing or corrupted data
- Using Git and GitHub with branches throughout a project

## Acknowledgements

AI tools were used during development to explain the physics library, generate some Swing layout boilerplate, help with the graph scaling maths and debug asset loading. The game design, architecture and feature set are my own.
