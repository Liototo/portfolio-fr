# "Projet de recherche en informatique": collaboration en temps réel

Développé par Eliott Bell à partir du projet [jupyter-collaboration](https://github.com/jupyterlab/jupyter-collaboration) (Jupyter Development Team) et supervisé par Zhenyu Cai

Bachelor, semestre 6 (févr. 2024 - juil. 2024), EPFL (Ecole Polytechnique Fédérale de Lausanne)

L'objectif de ce projet de recherche semestriel est d'imaginer, concevoir et implémenter une version améliorée de l'extension jupyter-collaboration (permettant la collaboration en temps réel sur les notebooks Jupyter), visant à rendre cette méthode de travail utile dans des contextes éducatifs. Ce projet a été divisé en deux moitiés:

- La première moitié du semestre a été dédiée à la conception de plusieurs petites améliorations permettant de transformer un serveur Jupyter en un outil de collaboration pratique et complet. Les fonctionnalités en question incluent une messagerie, un système de sondages et une forme de contrôle d'accès à base de rôles.
- Le reste du semestre a été consacré à une fonctionnalité plus complexe et développée, à savoir un système permettant aux modérateurs du serveur Jupyter de surveiller l'activité des autres utilisateurs à travers le notebook.

Le projet était supervisé par Zhenyu Cai, un professeur assistant de l'EPFL, à qui je devais faire des rapports hebdomadaires sur ma progression.

### Travail effectué

Tout mon code se trouve dans le dossier jupyter-notebook-collaboration-main/packages/collaboration/src/. Les fichiers activitybargraph.tsx, activitydisplay.tsx, activitydotplot.tsx, cellTracker.ts, chatbox.tsx, messageEncoding.ts, ownerdialog.ts, polls.tsx et roles.ts ont été entièrement écrits par moi-même, ainsi que plusieurs modifications dans d'autres fichiers du même dossier. J'ai également écrit plusieurs classes CSS du fichier jupyter-notebook-collaboration-main/packages/collaboration/style/sidepanel.css pour améliorer le rendu visuel des composants que j'ai écrits.

### Compétences acquises/travaillées

- Développement full-stack

- Programmation en JavaScript/TypeScript

- Conception de fonctionnalités de bout en bout

- Organisation et communication

## Captures d'écran

La messagerie implémentée. Le nom de l'utilisateur actuel est souligné à des fins de visualisation.
<p align="center"><img src="..\Resources\jup_chat.png" width="30%"></p>

Le système de sondages, permettant de poser des questions à 2-4 choix possibles.
<p align="center"><img src="..\Resources\jup_poll.png" width="30%"></p>

Une visualisation du contrôle d'accès à base de rôles: seuls les utilisateurs désignés comme "professeurs" peuvent créer des sondages, les autres ne peuvent qu'y participer.
<p align="center"><img src="..\Resources\jup_rbac.png" width="30%"></p>

Deux visualisations de l'activité des utilisateurs à travers un notebook; dans la deuxième, chaque point représente un utilisateur. Il est possible de cliquer sur un point pour directement envoyer un message adressé à l'utilisateur correspondant dans la messagerie.
<p align="center"><img src="..\Resources\jup_track.png" width="30%"></p>
