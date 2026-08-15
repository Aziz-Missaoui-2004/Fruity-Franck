# CONTRAT

* Équipe : 5  
* Jeu : Fruity Frank 
* Contact : Ghassen.Rouatbi@etu.univ-grenoble-alpes.fr
* Version du 18 juin 14h

# 1. Point technique à expliquer durant la démo
Au choix
* [o] gestion de la chute des pommes avec ligne épaisse / ligne fine
* [o] gestion de la poussée des pommes par Frank

# 2. Contrat spécificique au jeu choisi

Frank doit ramasser tous les fruits du niveau sans toucher les monstres. Quand tous les fruits ont été collectés, le niveau est gagné et on peut passer au niveau suivant. Les pommes servent d'obstacles et d'armes : elles peuvent tomber et écraser les monstres ou le joueur. Frank peut aussi lancer un projectile unique, récupérable après avoir collecté une cerise.  
Le jeu est joué sur une pelouse que Frank et certain monstre peuvent détruire.

### Difficultés principales à gérer

* [x] __creusage de la terre__  
  - Explications: La carte contient des cases de terre et des galeries. Quand Frank se déplace dans une case de terre creusable, cette case devient une galerie. Les monstres se déplacent principalement dans les galeries, avec éventuellement certains monstres capables de creuser selon leur comportement.  
  - **Démo**
    - On déplace Frank dans une zone pleine de terre
    - On démontre que les cases traversées changent d'état car ensuite un monstre peut emprunter la galerie créée.

* [x] __collecte des fruits et condition de victoire__  
  - Explications: Chaque cerise est une entité collectable. Lorsqu'elle entre en collision avec Frank, elle disparaît, fait passer Frank en état de tirer, augmente le score et on diminue le nombre de fruits restants. Le niveau se termine quand ce nombre atteint zéro.
  - **Démo** 
    - On collecte plusieurs fruits en affichant le score et le compteur de fruits restants
    - on ramasse le dernier fruit pour déclencher la victoire.

* [x] __pommes qui tombent__  
  - Explications: Une pomme est stable tant qu'elle est soutenue. Si la case sous elle devient vide ou trop fine, elle passe en état de chute. Pendant la chute, elle peut écraser un monstre ou tuer Frank. Elle s'arrête quand elle rencontre un obstacle solide ou le bas de la map.  
  - **Démo**
    - Frank creuse sous une pomme ; la ligne devient fine ou inexistante
    - La pomme tombe sur un monstre posé sur une ligne fine, elle le détruit puis continue à tomber
    - La pomme s'arrête sur un obstacle (ligne épaisse)
    
* [x] __poussée des pommes_  
  - Explications: Frank peut pousser une pomme horizontalement tant que le case après la pomme est vide ou contient une pelouse.  
  - [#MP: Solution tecnhique] : une pomme peut sans doute être réalisé par une automate GAL
    avec la condition Pushed? et l'action Move.  
  - **Démo** 
    - Frank pousse une pomme vers une zone libre
    - Frank tente de pousser une pomme contre un ennemi ou un un autre fruit :  le déplacement est impossible.

* [x] __projectile unique de Frank__  
  - Explications: Frank dispose d'un seul projectile qu’il récupère à chaque fois qu’il collecte une cerise. Quand il le lance, il devient indisponible jusqu’à la récupération d’une nouvelle cerise. Le projectile peut éliminer un monstre ou se détruire après la première collision.  
  - **Démo** 
    - On lance le projectile sur un monstre et on montre la destruction. 
    - On tente ensuite de tirer une deuxième fois immédiatement pour montrer que c'est impossible tant qu’il n’a pas encore récupéré une autre cerise. 
    - [#MP: comment prouver que Franck tente de tirer ? En mode debug, le stunt de Frank affiche l'action qu'il tente d'effectuer]


* [x] __IA des monstres__  
  - Explications:  Les monstres pourront utiliser des comportements différents : un bot lent qui suit les galeries, un bot plus agressif qui poursuit Frank, un bot capable de creuser lorsqu'il est bloqué. Le moteur permet de remplacer le comportement sans réécrire l'entité.  
  - **Démo : Changement de comportement**
    - deux monstres avec des comportements différents
    - affichage des automates GAL des monstres
    - ils ne prennent pas les mêmes décisions face à Frank.


# 3. Contrat concernant le moteur de jeu

## Partie Modèle

### Géometrie  
* [x] __monde torique__  
  - Explications: Oui retenu pour notre version de Fruity Frank on aura un monde torique en X et avec des bords en Y.  
  - **Démo : Frank ou un monstre qui arrive au bord de la carte est bloqué ou forcé de rester dans la zone jouable, sans téléportation sur l'axe Y et le contraire sur l'axe X.**

* [ ] __défilement / scrolling__  

* [ ] __taille de la map modifiable ?__  

* [x] __bords du monde__   
  - Explications: 
    - Les bords du monde sont des limites solides en Y. 
    - Une entité ne peut pas sortir de la zone jouable [#MP: qu'est-ce que ça signifie?]
    - Les pommes tombantes, les tirs et les monstres doivent aussi respecter ces limites.  
  - **Démo**
    - On dirige Frank contre chaque bord et on montre qu'il ne sort pas. 
    - un monstre ou une pomme ne traverse pas le bord de la carte.

### Génération de la map

* [ ] __fixe__ 
s
* [x] __par configuration__ 
  - Explications: 
    - Les maps seront chargées depuis un fichier ASCII ou [#MP: une structure de configuration = ??]. 
    - Chaque caractère ou symbole représente un élément : terre creusable, galerie vide, mur/bord, fruit, pomme, joueur, monstre.  [#MP: suggestion : utilisez les conventions de notations de GAL.]
    - Cela permettra de modifier un niveau sans modifier le code Java du moteur.  
  - **Démo : On modifie un fichier de niveau, par exemple en ajoutant un fruit ou en déplaçant une pomme, puis on relance le jeu et on montre que la modification est prise en compte.**

* [ ] __aléatoire__  

* [ ] __aléatoire avec mémorisation__  

### Actions

* [ ] __annulation d'une action__  
  - Explications: Non retenu. Fruity Frank est un jeu d'action en temps réel : les déplacements, tirs, chutes de pommes et collisions sont définitifs une fois exécutés.  

* [x] __collisions__
  - Explication: Les interactions seront traitées ~~par priorité et~~ par collision. 
    - Frank ne peut pas entrer ~~dans une case occupée~~ par un monstre sans perdre une vie 
    - deux monstres ne peuvent pas se superposer
    - une pomme tombante peut écraser un monstre et Frank
    - un fruit est collecté si Frank atteint sa case.  
   - Solution technique : gérée par le moteur 
  - **Démo : On prépare une situation pour chacun des 4 cas mentionnés**
 
* [ ] __résolution de conflit lorsque deux entités veulent atteindre la même case__  
  - Explications: [#MP: y'a t'il une notion de case dans Fruity Frank?, je ne crois pas.]

### Affichage et animation 

* [x]  __barre de santé / points de vie de chaque personnage__  
  - Explications: Frank aura un compteur de vies affiché à l'écran (3 coeurs) à chaque mort l'état de la map reste le même et frank revient à sa position initiale . Les monstres n'auront pas forcément de barre de vie, car ils sont éliminés directement par une pomme ou par le projectile.  
  - **Démo : collision entre Frank et un monstre : le compteur de vies diminue ; Frank réapparaît ou la partie se termine s'il ne lui reste aucune vie.**

* [ ] __animation lors de la création / apparition__  

* [x] __animation lors de la destruction / disparition__  
  - Explications: 
    - Les monstres détruits par une pomme ou par le projectile n'auront pas d'animation de destruction.
    - Frank aura une animation de destruction lors de la perte de sa dernière vie.  
  - **Démo : Alors que Frank n'a plus qu'une vie, on l'écrase avec une pomme ou on le touche avec un monstre, ce qui déclenche l'animation de disparition.**
   
* [x] __animations spécifiques temporaires__
  - Explications: invulnérabilité et clignotement pendant un certain temps
    - Frank clignotera lors de la perte de ces deux premières vies et 
   - Réalisation technique:
  - **Démo : Lorsqu'il est touché, Frank clignote et est invulnérable pendant une certain temps**

* [?] __animation en fonction de l'état du Bot (Waiting, Resting, ...)__  
  - Explications:  L'animation ou l'affichage de Frank peut changer selon son état (Peut tiré / Ou pas)  
  - **Démo : En mode debug, on affiche l'état des entités au-dessus : leur aspect ou animation changent quand l'état change**

* [ ] __animations / affichage spécifiques__  

#### Adaptabilité = changement pendant le jeu  

* [ ] changement de Stunt   
  - Explications: 
    - Le Stunt associé à Frank pourra changer selon l'action [#MP: non, un stunt est capable de faire plusieurs actions, on le change si le code pour faire les actions devient très différent, par exemple au lieu d'avoir une tondeuse, Frank avait un tracteur] 
    - action du Stunt de Frank : déplacement simple, creusage, tir du projectile, poussée de pomme. Les actions compatibles pourront se combiner si le moteur le permet, par exemple avancer tout en creusant  
  - Démo: __animation en fonction de l'état du Bot (Waiting, Resting, ...)__  
  - Explications:  L'animation ou l'affichage de Frank peut changer selon son état (Peut tiré / Ou pas)  
  - Démo : En mode debug, on affiche l'état du Frank au-dessus de lui et on montre que son comportement change.

  - Explications: L'avatar affiché pourra changer selon l'état ou la direction : Frank ou monstre orienté haut/bas/gauche/droite, Frank en train de creuser etc.  
  - Démo : changement d'image : on déplace Frank / les monstres dans plusieurs directions et on montre que le sprite change.

* [ ] changement dynamique  de bot/FSM   
        
## Partie Vue

* [x] view port centré sur le joueur  
* [ ] zoom
* [ ] scrolling  
  
## Mode debug

* [x] affichage du temps entre deux _tick_ (min, max, moyenne)  
* [x] affichage du temps entre deux _paint_ (min, max, moyenne)  
* [x] affichage du nombre de _FPS_ (min, max, moyenne)  
* [x] gestion de la charge   
    - Explication: _création d'un bot spawner avec l'automate (1)--EGG-> (1) qui génère sans fin de nouvelles entités. Ce qui permet de voir à partir de combien d'entités le moteur tombe à < 24 fps._
    - **Démo spécifique : spawner et affichage du nombre de FPS**  

* [x] affichage de l'action au dessus du personnage  
* [x] affichage des bounding box    

## Partie Controlleur

#### Bot  
  
* [ ] Bot en Java  
* [x] Bot en FSM Java  
* [x] Bot en GAL - parser  
* Démo : modification du comportement des entités   
  - [ ] en changeant de classe Bot   
  - [x] en changeant de FSM Java  
  - [x] en changeant le fichier `.gal`   
  - Explications: 
    - Les monstres de Fruity Frank seront contrôlés par des bots. 
    - La version Java servira de base simple, la FSM Java permettra de rendre explicites les états, et GAL permettra de décrire un comportement dans un fichier séparé.  
  - **Démo : on fait tourner le même niveau avec trois comportements :** 
    - un monstre basique en Java, 
    - un monstre à états en FSM Java, 
    - un monstre dont le comportement change après modification du fichier `.gal`.

## Partie Stunt : Action unique _versus_ actions multiples simultanées 

  * [x] une action à la fois    
  * [ ] actions multiples
  * [ ] gestion des actions compatibles/incompatibles

# [#MP: Moteur physique]

* [?] vitesse
  + Explication: lancement d'un projectile
    + quand s'arrête t'il ?
  
* [?] gravité 
  + Explication: comment est simulé la chute des pommes ?