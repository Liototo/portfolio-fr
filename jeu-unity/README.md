# Projet d'"Interaction personne-système": jeu Unity

Développé par Eliott Bell, Victor Faltings et Belén Gómez García

Bachelor, semestre 4 (févr. 2022 - juil. 2022), EPFL (Ecole Polytechnique Fédérale de Lausanne)

Ce projet a été développé en trois étapes:

- Développement d'un mode de jeu prédéterminé (mode "classic") à partir de ressources déjà fournies en suivant des instructions précises
- Ajout d'améliorations prédéterminées au dit mode de jeu sans instructions claires
- Création de notre propre mode de jeu (mode "maze") sans contraintes ou instructions

Le jeu est jouable au clavier. Il est également possible d'y jouer avec des Cellulos, des robots hexagonaux à retour de force développés à l'EPFL. Les personnages jouables prennent la forme desdits robots.

Il est possible de jouer au jeu en exécutant jeu-compile/SpaceGhostSheep.exe ou de lire le code source dans le dossier projet-unity.

### Travail effectué

Le code que nous avons écrit se trouve dans le dossier projet-unity/Assets/Scripts. Nous avons écrit la plupart des fichiers en C# dans les dossiers Core/Behaviors/, Game/, Maze Game/, Menu/ et UI/ ainsi que le fichier PlayerMovement.cs. Dans le dossier projet-unity/Assets/Resources, les effets sonores GemCollected.wav et PointStolen.wav ont été composés par moi-même.

### Compétences acquises/travaillées

- Développement de jeu sur la plateforme Unity

- Programmation en C#

- Design d'interfaces utilisateur

- Travail d'équipe et organisation

## Captures d'écran

Le mode de jeu "classic", implémenté selon des instructions précises. Les deux joueurs (bleu et magenta) doivent repousser le robot (vert) dans la zone centrale pour gagner des points; à intervalles réguliers, le robot vert devient rouge et se met à pourchasser les joueurs, leur faisant perdre des points à l'impact.
<p align="center"><img src="..\Resources\sgs_classic.png" width="50%"></p>

Le mode de jeu "maze", conceptualisé par notre équipe. Les deux joueurs doivent récupérer des gemmes (vertes, roses ou oranges), puis passer dans la zone centrale avant de retourner dans leur base pour gagner des points. Si les joueurs s'entrechoquent, ils échangent les gemmes tenues.
<p align="center"><img src="..\Resources\sgs_maze.png" width="50%"></p>

Une version plus difficile du mode de jeu "maze", où le labyrinthe est presque entièrement plongé dans la pénombre.
<p align="center"><img src="..\Resources\sgs_maze_hard.png" width="50%"></p>
