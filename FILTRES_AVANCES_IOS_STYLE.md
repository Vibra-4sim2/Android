# ✅ FILTRES AVANCÉS - STYLE iOS IMPLÉMENTÉ

## 🎯 FONCTIONNALITÉS

### Bouton Filtre
- Bouton circulaire à côté de la barre de recherche
- Badge orange quand des filtres sont actifs
- Ouvre un Bottom Sheet moderne

### Bottom Sheet avec 2 Modes

#### 📅 Mode "Filtrer par date"
Cartes cliquables avec :
- **Toutes** - Toutes les sorties disponibles
- **Aujourd'hui** - Sorties prévues aujourd'hui
- **Cette semaine** - Sorties de cette semaine
- **Ce mois** - Sorties de ce mois
- **À venir** - Toutes les sorties à venir

Chaque carte affiche :
- Icône appropriée
- Label
- Description
- Checkmark si sélectionné

#### 📍 Mode "Filtrer par localisation"
- **Ma position** - Carte avec bouton refresh
- **Rayon de recherche** - Affichage en grand (ex: "50 km")
- **Slider** - De 5 km à 200 km
- **Raccourcis** - Boutons 10km, 25km, 50km, 100km

### Boutons d'action
- **Réinitialiser** - Efface tous les filtres
- **Appliquer** - Applique et ferme le Bottom Sheet

---

## 📁 FICHIERS MODIFIÉS

### `HomeExploreViewModel.kt`
```kotlin
enum class DateFilterOption(val label: String, val daysAhead: Int?) {
    ALL("Toutes", null),
    TODAY("Aujourd'hui", 0),
    THIS_WEEK("Cette semaine", 7),
    THIS_MONTH("Ce mois", 30),
    NEXT_3_MONTHS("À venir", 90)
}

enum class ProximityFilterOption(val label: String, val radiusKm: Int?) {
    ALL("Toutes distances", null),
    NEARBY_5("< 5 km", 5),
    NEARBY_10("< 10 km", 10),
    NEARBY_25("< 25 km", 25),
    NEARBY_50("< 50 km", 50),
    NEARBY_100("< 100 km", 100)
}
```

### `HomeExploreScreen.kt`
- Bouton filtre circulaire avec badge
- `ModalBottomSheet` 
- `AdvancedFiltersContent` - Contenu du Bottom Sheet
- `DateFilterCard` - Carte individuelle pour chaque option de date
- Slider pour la proximité
- Raccourcis de distance

---

## 🎨 DESIGN

### Bouton Filtre
```
┌───┐
│ ≡ │  ← Icône FilterList
└───┘
  🔴   ← Badge si filtres actifs
```

### Bottom Sheet - Mode Date
```
┌────────────────────────────────────────────┐
│  Filtrer par date         [📍 Proximité]  │
├────────────────────────────────────────────┤
│ ┌────────────────────────────────────────┐ │
│ │ 📅 Toutes                           ✓  │ │
│ │    Toutes les sorties disponibles      │ │
│ └────────────────────────────────────────┘ │
│ ┌────────────────────────────────────────┐ │
│ │ 📆 Aujourd'hui                         │ │
│ │    Sorties prévues aujourd'hui         │ │
│ └────────────────────────────────────────┘ │
│ ┌────────────────────────────────────────┐ │
│ │ 📋 Cette semaine                       │ │
│ │    Sorties de cette semaine            │ │
│ └────────────────────────────────────────┘ │
│ ...                                        │
├────────────────────────────────────────────┤
│  [Réinitialiser]  [    Appliquer    ]     │
└────────────────────────────────────────────┘
```

### Bottom Sheet - Mode Proximité
```
┌────────────────────────────────────────────┐
│  Filtrer par localisation    [📅 Date]    │
├────────────────────────────────────────────┤
│ ┌────────────────────────────────────────┐ │
│ │ 🧭 Ma position                    🔄   │ │
│ │    Position activée                    │ │
│ └────────────────────────────────────────┘ │
│ ┌────────────────────────────────────────┐ │
│ │ 📍 Rayon de recherche                  │ │
│ │    50 km                               │ │
│ │    ●━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ │
│ │    5 km                        200 km  │ │
│ └────────────────────────────────────────┘ │
│ ┌────────────────────────────────────────┐ │
│ │ Raccourcis                             │ │
│ │ [10km] [25km] [50km] [100km]           │ │
│ └────────────────────────────────────────┘ │
├────────────────────────────────────────────┤
│  [Réinitialiser]  [    Appliquer    ]     │
└────────────────────────────────────────────┘
```

---

## ✅ FILTRES EXISTANTS NON MODIFIÉS

- Explore ✅
- Recommended ✅
- People ✅
- Following ✅
- Cycling ✅
- Hiking ✅
- Camping ✅

---

## 🧪 TEST

1. Ouvrir l'écran Explore
2. Cliquer sur le bouton filtre (à droite de la barre de recherche)
3. Tester le filtre par date :
   - Cliquer sur "Aujourd'hui" → Checkmark vert
   - Cliquer sur "Appliquer"
   - Les sorties sont filtrées
4. Rouvrir le Bottom Sheet
5. Cliquer sur "📍 Proximité"
6. Activer "Ma position"
7. Déplacer le slider ou utiliser les raccourcis
8. Cliquer sur "Appliquer"
9. Vérifier que le badge orange apparaît sur le bouton filtre

**Recompilez et testez ! 🚀**

