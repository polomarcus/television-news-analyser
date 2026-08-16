# classification-climat.json — classification fine des reportages climat

Jeu de données dérivé : les 3 448 reportages du corpus détectés comme mentionnant
le climat (flag `containsWordGlobalWarming`) ont été classifiés par LLM
(Claude Haiku 4.5, août 2026) en deux passes, à partir du titre et de la
description de chaque reportage.

## Champs

| Champ | Valeurs | Description |
|---|---|---|
| `date`, `media`, `title`, `url` | | identification du reportage (jointure par `url`) |
| `cat` | `consequences_constat`, `causes`, `solutions_collectives`, `solutions_individuelles`, `adaptation`, `politique_negociations`, `science_rapports`, `mention_passagere` | catégorie principale (mono-label) |
| `fossile` | bool | pétrole/charbon/gaz nommés comme cause |
| `angle` | `episodique`, `thematique`, `mixte` | cadrage au sens d'Iyengar (*Is Anyone Responsible?*, 1991) |
| `emotion` | bool | registre témoignage émotionnel (larmes, détresse, « tout perdu »…) |
| `qui` | liste : `scientifique`, `politique`, `industriel`, `militant`, `temoin`, `professionnel_touche`, `aucun` | types d'intervenants cités |
| `secteurs` | liste : `transport_routier`, `aerien`, `maritime`, `alimentation_viande`, `agriculture_autre`, `logement_energie`, `industrie`, `numerique`, `energies_fossiles_general` | secteurs émetteurs nommés comme contribuant aux émissions, selon les grands postes d'empreinte carbone ([nosgestesclimat.fr](https://nosgestesclimat.fr/)) ; liste vide = aucun secteur nommé |

## Méthode et validation

- Pilote de 250 reportages (échantillon stratifié par année) validé par relecture
  manuelle avant le run complet ; contrôle d'intégrité (couverture des identifiants,
  valeurs de catégories) avec passe de réparation. Couverture : 3 444/3 448 pour
  la passe 1 (`cat`…), 3 448/3 448 pour la passe 2 (`qui`, `secteurs`).
- La taxonomie `cat` est volontairement proche de la grille causes / constats /
  conséquences / solutions de l'[Observatoire des médias sur l'écologie](https://observatoiremediaecologie.fr/audiovisuel/methodologie/),
  avec des catégories supplémentaires (adaptation, politique, science, mention passagère).

## Limites

- Classification mono-label pour `cat` : un reportage abordant causes ET solutions
  est classé sur son angle dominant.
- Basée sur le texte des pages web des JT, pas sur la vidéo diffusée.
- Jeu figé (généré en août 2026) : les reportages postérieurs ne sont pas classifiés
  automatiquement — regénération à la demande (voir l'historique du projet).
- Ordres de grandeur robustes, frontières de catégories approximatives : pour toute
  citation publique, vérifier les exemples individuels (le champ `url` est là pour ça).

## Validation par juge indépendant (août 2026)

98 reportages tirés au sort (stratifiés par année) ont été ré-annotés en aveugle
par un modèle plus puissant (Claude Sonnet) muni d'une consigne reformulée
indépendamment (définitions réécrites, ordre des catégories mélangé, aucun accès
aux labels de production). Résultats :

- accord brut sur les 8 catégories : **54 %** (kappa de Cohen : 0,44) ;
- accord sur la dichotomie « fond » (causes + politique + science) vs reste,
  qui porte le résultat principal : **80 %** ;
- les désaccords se concentrent aux frontières sémantiques
  (constat ↔ science_rapports, constat ↔ adaptation) et vont majoritairement
  dans le sens d'une sous-estimation du « fond » par la classification de
  production ;
- la tendance centrale est robuste au changement d'annotateur : sur ce même
  échantillon, la part de « fond » passe de 47 % (2013–2021) à 22 % (2022–2026)
  selon la production, et de 53 % à 25 % selon le juge.

Conclusion : les effectifs par catégorie fine sont à ±quelques points, la
dichotomie fond/non-fond et sa trajectoire sont fiables.
