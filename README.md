# Fruity Frank — Java Game Engine & FSM-Based AI

University software-engineering project developed in Java as part of PLE2026 by a five-person team.

### the final & discussed game engine is developed by : ROUATBI Ghassen

The project combines a reusable game engine, the **Fruity Frank** game, and a parser for **GAL**, a small language used to describe finite-state-machine behaviors for game entities.

## Why this project matters

The main goal was not simply to build a playable game. The project required us to separate generic engine responsibilities from game-specific rules, model real-time entity behavior, manage collisions and physics-like interactions, and make bot behavior configurable through finite-state machines.

## Engineering highlights

- Built a reusable Java game-engine layer separated from Fruity Frank business/game rules.
- Implemented entity simulation, collisions, movement, rendering and camera management.
- Modeled bot behavior with Java finite-state machines and external GAL automata.
- Integrated a JavaCC-based parser and AST for GAL behavior files.
- Supported configurable levels and entity behavior without rewriting the core engine.
- Implemented gameplay interactions such as destructible terrain, collectible fruits, falling/pushable apples, projectiles and enemy collisions.
- Added debugging and performance instrumentation for tick time, paint time, FPS, bounding boxes and entity actions.
- Worked in a multi-developer Git workflow on a shared codebase.

## Architecture

The codebase is split into three main parts:

```text
                         ┌─────────────────────────┐
                         │     Fruity Frank        │
                         │ Rules · Map · Entities  │
                         │ Controls · Game Logic   │
                         └───────────┬─────────────┘
                                     │ uses
                         ┌───────────▼─────────────┐
                         │   Generic Java Engine   │
                         │ Model · View · Physics  │
                         │ Ticker · Brain · Stunt  │
                         └───────────┬─────────────┘
                                     │ behavior
                         ┌───────────▼─────────────┐
                         │      GAL / FSM          │
                         │ JavaCC Parser · AST     │
                         │ External Bot Behaviors  │
                         └─────────────────────────┘
```

### Engine responsibilities

The generic engine separates several responsibilities:

- `Ticker` drives simulation and rendering cycles;
- `Model` manages entities, movement and collisions;
- `Brain` activates bots and evaluates their behavior;
- `Stunt` applies actions to entities;
- `View`, `Painter`, `Avatar` and `Camera` handle rendering;
- GAL / FSM descriptions control behavior independently from entity implementation.

The Fruity Frank layer contains the game-specific rules, map logic, entities and interactions, while the engine remains independent from Fruity Frank-specific entity categories.

## Gameplay and domain logic

The game objective is to collect all fruits while avoiding monsters.

Implemented mechanics include:

- destructible terrain and gallery creation;
- fruit collection and level-completion conditions;
- falling apples that can eliminate monsters or the player;
- horizontal apple pushing when the destination is valid;
- a single-use projectile restored after collecting a cherry;
- multiple monster behaviors;
- player lives, temporary invulnerability and visual state changes;
- configurable maps loaded from external configuration.

The horizontal world is toroidal while the vertical axis uses solid boundaries.

## AI and finite-state machines

Monster behavior can be expressed with finite-state machines rather than being tightly coupled to entity classes.

The project supports:

- Java FSM implementations;
- GAL-based behavior descriptions;
- behavior changes by replacing or editing `.gal` files;
- different monster strategies such as following galleries, pursuing the player or adapting when blocked.

This makes behavior easier to inspect, modify and demonstrate independently from the rest of the engine.

## GAL parser

The project includes an AST and JavaCC parser for the GAL language in:

```text
src-parser/gal
```

Detailed parser documentation is available in:

[`src-parser/gal/README.md`](src-parser/gal/README.md)

To regenerate the parser from `parser.jj`:

```bash
cd src-parser/gal/parser
make
```

## Debugging and performance instrumentation

The engine includes a debug mode capable of displaying and measuring:

- time between simulation ticks;
- time between paint cycles;
- minimum / maximum / average FPS;
- entity bounding boxes;
- current entity actions;
- behavior under increasing entity load.

A bot spawner can continuously create entities to observe how the engine behaves under load and identify the point where rendering performance drops below the target frame rate.

## Repository structure

```text
Fruity-Franck/
├── src-engine/engine/       # Generic simulation/game engine
├── src-Frank/frank/         # Fruity Frank game implementation
├── src-parser/gal/          # GAL AST, parser and tools
├── gal_fsm/                 # GAL finite-state-machine definitions
├── sprites/                 # Visual and audio assets
├── game.cfg                 # Game configuration
├── given.jar                # Provided graphical/task runtime library
└── README.md
```

## Build

Requirements:

- Java 16 or newer;
- `javac` and `java` available in the `PATH`;
- `given.jar` at the repository root.

From the repository root:

```bash
rm -rf /tmp/ple2026-build
mkdir -p /tmp/ple2026-build
javac -cp given.jar -d /tmp/ple2026-build \
  $(find src-engine src-Frank src-parser \
    -type f -name '*.java' ! -path '*/tests/*')
```

## Run Fruity Frank

```bash
java -cp /tmp/ple2026-build:given.jar frank.game.GameApp
```

The application should be launched from the repository root so that `game.cfg` and the assets in `sprites/` can be resolved correctly.

## Academic context

Fruity Frank was developed as a team project for **PLE2026**.

The project emphasized:

- object-oriented software design;
- separation of concerns;
- event-driven / simulation-oriented architecture;
- finite-state-machine modeling;
- parsing and language tooling;
- teamwork on a shared Java codebase;
- debugging and performance analysis.

## What this project demonstrates

Java and object-oriented programming, modular software architecture, game-engine design, event-driven simulation, finite-state machines, parser integration, debugging, performance instrumentation and collaborative software development.

## License

Academic project. No open-source license is currently defined.
