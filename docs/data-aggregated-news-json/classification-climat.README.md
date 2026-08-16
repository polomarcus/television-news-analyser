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
