# PLE2026 — Fruity Frank

Projet Java réalisé dans le cadre du PLE2026. Le projet contient un moteur de
jeu générique, le jeu Fruity Frank et un parser pour le langage d'automates
GAL.

## Contenu du projet

- `src-engine/engine` : moteur générique de simulation, entités, collisions,
  physique, affichage et IA.
- `src-Frank/frank` : jeu Fruity Frank, sa carte, ses entités, ses règles,
  contrôles, graphismes et comportements IA.
- `src-parser/gal` : AST, parser JavaCC et outils de visualisation GAL.
- `gal_fsm/` : automates GAL utilisés par les jeux.
- `sprites/` : sprites, écrans et effets sonores.
- `game.cfg` : configuration de Fruity Frank.
- `given.jar` : bibliothèque fournie contenant les composants graphiques et le
  runtime de tâches utilisés par le moteur.

## Prérequis

- Java 16 ou version supérieure ;
- `javac` et `java` disponibles dans le `PATH` ;
- `given.jar` présent à la racine du projet.

La configuration Eclipse est fournie dans `.project` et `.classpath`. Le chemin
de la bibliothèque dans `.classpath` peut devoir être adapté à la machine
utilisée.

## Compilation

Depuis la racine du projet :

```bash
rm -rf /tmp/ple2026-build
mkdir -p /tmp/ple2026-build
javac -cp given.jar -d /tmp/ple2026-build \
  $(find src-engine src-Frank src-parser \
    -type f -name '*.java' ! -path '*/tests/*')
```

## Lancer Fruity Frank

Après compilation :

```bash
java -cp /tmp/ple2026-build:given.jar frank.game.GameApp
```

Le jeu charge sa configuration depuis `game.cfg` et ses ressources depuis le
dossier `sprites/`. Il est donc recommandé de lancer la commande depuis la
racine du projet.

## Automates GAL

Les automates peuvent être décrits dans des fichiers `.gal`. Le parser se situe
dans `src-parser/gal/parser` et sa documentation détaillée est disponible dans
[`src-parser/gal/README.md`](src-parser/gal/README.md).

Pour régénérer le parser depuis `parser.jj`, JavaCC est nécessaire :

```bash
cd src-parser/gal/parser
make
```

Les automates de démonstration sont dans `src-parser/gal/demo/test`.

## Architecture d'exécution

Le moteur sépare plusieurs responsabilités :

- `Ticker` cadence la simulation et le rendu ;
- `Model` gère les entités, les déplacements et les collisions ;
- `Brain` réveille les bots et exécute les comportements ;
- `Stunt` applique les commandes de mouvement ;
- `View`, `Painter`, `Avatar` et `Camera` assurent l'affichage ;
- les automates GAL décrivent des comportements comme la poursuite ou la fuite.

Les règles propres à Fruity Frank restent dans son modèle, tandis que le
moteur ne dépend pas des catégories d'entités du jeu.

## État du projet

Le code Java principal compile avec `javac` et `given.jar`.

## Équipe et licence

Projet universitaire PLE2026. Aucune licence open source n'est définie pour le
moment.
