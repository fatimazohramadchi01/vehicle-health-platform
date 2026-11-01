# Vehicle Health Analysis Platform

Plateforme d’analyse de données télématiques développée avec Java 21 et Spring Boot.

Le projet permet de collecter les mesures d’un véhicule, de calculer un **health score** et de générer des alertes en tenant compte des données reçues et des campagnes de rappel fournies par une API externe simulée avec WireMock.

## 1. Fonctionnalités

- Réception et validation des données télématiques.
- Stockage des télémétries.
- Déclenchement asynchrone des analyses.
- Consultation d’un fournisseur externe de campagnes de rappel.
- Calcul d’un score de santé à partir d’un score initial de 100.
- Génération d’alertes selon les anomalies détectées.
- Consultation du rapport de santé et des alertes via des API REST.
- Gestion des échecs de transmission avec une limite de trois tentatives.

## 2. Architecture

```text
                         Client / Véhicule
                                |
                                | POST /api/v1/telemetry
                                v
                    +-------------------------+
                    | telemetry-service       |
                    |                         |
                    | Validation              |
                    | Stockage des données    |
                    | Création du job         |
                    | Traitement asynchrone    |
                    +------------+------------+
                                 |
                                 | POST /internal/v1/analyses
                                 v
                  +------------------------------+
                  | vehicle-analysis-service     |
                  |                              |
                  | Consultation des rappels     |
                  | Calcul du health score       |
                  | Génération des alertes       |
                  | Stockage du rapport          |
                  +---------------+--------------+
                                  |
                                  | GET /recalls?vin=...
                                  v
                       +---------------------+
                       | WireMock            |
                       |                     |
                       | API externe simulée |
                       +---------------------+
```

Les résultats sont consultables auprès de `vehicle-analysis-service` :

```text
GET /api/v1/vehicles/{vin}/health-report
GET /api/v1/vehicles/{vin}/alerts
```

### Responsabilités des services

| Service | Responsabilités |
|---|---|
| `telemetry-service` | Recevoir, valider et stocker les télémétries, puis déclencher leur analyse |
| `vehicle-analysis-service` | Analyser les données, consulter les rappels, calculer le score et exposer les résultats |
| `recall-mock` | Simuler le fournisseur externe de campagnes de rappel avec WireMock |

### Architecture propre

Le projet sépare les responsabilités métier et techniques :

- Domaine : règles de scoring, calcul du score et génération des alertes.
- Application : orchestration des cas d’usage.
- Ports : contrats des dépendances utilisées par les cas d’usage.
- Adaptateurs et infrastructure : contrôleurs REST, persistance JPA et clients HTTP.

Les règles métier sont isolées des détails de transport et de persistance afin de faciliter leurs tests.

## 3. Technologies

| Technologie | Utilisation |
|---|---|
| Java 21 | Développement et exécution |
| Spring Boot | Microservices |
| Spring Web | API REST |
| Spring Data JPA | Persistance |
| H2 | Bases de données en mémoire |
| Maven | Construction et tests |
| Docker | Conteneurisation |
| Docker Compose | Démarrage de la plateforme |
| WireMock | Simulation de l’API externe |

## 4. Prérequis

Pour exécuter la plateforme :

- Docker démarré ;
- Docker Compose disponible ;
- ports `8080`, `8081` et `8089` libres ;
- `curl` pour tester les API.

Pour construire le projet et exécuter les tests depuis la machine hôte :

- JDK 21 ;
- Maven ;
- Docker accessible pour les tests qui en dépendent.

Toutes les commandes suivantes sont à exécuter depuis la racine du dépôt.

## 5. Démarrage avec Docker

### Construire et démarrer les services

```bash
docker compose up --build -d
```

### Vérifier les conteneurs

```bash
docker compose ps -a
```

### Afficher les logs

```bash
docker compose logs --tail=100
```

### Suivre les logs en direct

```bash
docker compose logs -f --tail=100
```

### Relancer les services sans reconstruire les images

```bash
docker compose up -d
```

### Arrêter les services

```bash
docker compose stop
```

### Arrêter et supprimer les conteneurs du projet

```bash
docker compose down
```

> Les bases H2 sont en mémoire. Les données sont perdues lorsque les processus applicatifs sont arrêtés.

## 6. Adresses des services

| Service | Adresse depuis la machine hôte |
|---|---|
| Telemetry Service | `http://localhost:8080` |
| Vehicle Analysis Service | `http://localhost:8081` |
| WireMock | `http://localhost:8089` |

Entre les conteneurs, le service de télémétrie appelle le service d’analyse à l’adresse :

```text
http://vehicle-analysis-service:8080
```

Le port `8081` est le port publié sur la machine hôte. Le service d’analyse écoute sur `8080` à l’intérieur de son conteneur.

## 7. Tester le parcours complet

Les commandes sont écrites sur une seule ligne pour faciliter leur copie dans le terminal.

### 7.1. Vérifier le fournisseur de rappels

```bash
curl -i "http://localhost:8089/recalls?vin=VF1RFA00567891234"
```

Résultat attendu :

- HTTP `200 OK` ;
- campagne de rappel active pour le véhicule de démonstration.

Le mapping de démonstration utilise la campagne `RC-2026-014`.

### 7.2. Envoyer une télémétrie

```bash
curl -i -X POST "http://localhost:8080/api/v1/telemetry" -H "Content-Type: application/json" -d '{"vin":"VF1RFA00567891234","measuredAt":"2026-10-31T16:00:00Z","mileageKm":125000,"tirePressuresBar":[2.1,2.1,2.4,2.4],"batteryVoltage":11.8,"engineTemperatureC":118,"fuelLevelPercent":8,"errorCodes":["P0301","P0301"]}'
```

Résultat attendu :

```text
HTTP 202 Accepted
```

La réponse fournit un `telemetryId`.

Conserver cet identifiant pour vérifier que le rapport consulté correspond à cette télémétrie.

> Le traitement est asynchrone : la réponse `202` confirme l’acceptation de la demande, pas la fin de l’analyse.

### 7.3. Consulter le rapport de santé

Après quelques secondes :

```bash
curl -i "http://localhost:8081/api/v1/vehicles/VF1RFA00567891234/health-report"
```

Résultat attendu pour le scénario de démonstration :

- HTTP `200 OK` ;
- même `telemetryId` que celui retourné par le POST ;
- score de santé de `43/100`.

Le score attendu dépend des règles métier et du mapping WireMock fournis avec le projet.

### 7.4. Consulter les alertes

```bash
curl -i "http://localhost:8081/api/v1/vehicles/VF1RFA00567891234/alerts"
```

Résultat attendu : HTTP `200 OK`.

Exemple de réponse obtenue pour le scénario de démonstration :

```json
[
  {
    "type": "MILEAGE_HIGH",
    "severity": "INFO",
    "message": "Kilométrage élevé : 125000 km"
  },
  {
    "type": "TIRE_PRESSURE_LOW",
    "severity": "MEDIUM",
    "message": "Au moins un pneu sous-gonflé"
  },
  {
    "type": "BATTERY_LOW",
    "severity": "HIGH",
    "message": "Batterie 12 V faible : 11.8 V"
  },
  {
    "type": "ENGINE_TEMPERATURE_HIGH",
    "severity": "HIGH",
    "message": "Température moteur élevée : 118.0 °C"
  },
  {
    "type": "FUEL_LOW",
    "severity": "LOW",
    "message": "Niveau de carburant bas : 8 %"
  },
  {
    "type": "OBD_ERROR_CODE",
    "severity": "HIGH",
    "message": "Codes erreur détectés : [P0301, P0301]"
  },
  {
    "type": "RECALL_CAMPAIGN",
    "severity": "CRITICAL",
    "message": "Campagne(s) de rappel active(s) : 1"
  },
  {
    "type": "HEALTH_SCORE_LOW",
    "severity": "CRITICAL",
    "message": "Health score critique : 43/100"
  }
]
```

## 8. API

### API publiques

| Méthode | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/telemetry` | Envoyer une télémétrie |
| `GET` | `/api/v1/vehicles/{vin}/health-report` | Consulter le rapport de santé |
| `GET` | `/api/v1/vehicles/{vin}/alerts` | Consulter les alertes |

### API interne

| Méthode | Endpoint | Description |
|---|---|---|
| `POST` | `/internal/v1/analyses` | Déclencher une analyse depuis le service de télémétrie |

### Structure d’une télémétrie

```json
{
  "vin": "VF1RFA00567891234",
  "measuredAt": "2026-10-31T16:00:00Z",
  "mileageKm": 125000,
  "tirePressuresBar": [2.1, 2.1, 2.4, 2.4],
  "batteryVoltage": 11.8,
  "engineTemperatureC": 118,
  "fuelLevelPercent": 8,
  "errorCodes": ["P0301", "P0301"]
}
```

| Champ | Description |
|---|---|
| `vin` | Identifiant du véhicule |
| `measuredAt` | Date et heure de mesure au format ISO 8601 |
| `mileageKm` | Kilométrage en kilomètres |
| `tirePressuresBar` | Pressions des pneus en bars |
| `batteryVoltage` | Tension de batterie en volts |
| `engineTemperatureC` | Température moteur en degrés Celsius |
| `fuelLevelPercent` | Niveau de carburant en pourcentage |
| `errorCodes` | Codes défaut remontés par le véhicule |

## 9. Hypothèses métier et calcul du health score

Le health score représente l’état global du véhicule sur une échelle de 0 à 100. Il est calculé à partir des données télématiques et des campagnes de rappel actives.

Le barème retenu est propre à ce projet. Il permet d’illustrer le fonctionnement de l’analyse et ne constitue pas une référence de diagnostic ou de sécurité automobile.

### 9.1. Hypothèses retenues

- La batterie analysée est la batterie auxiliaire 12 V.
- Les pressions sont exprimées en bars.
- Les quatre pressions correspondent, dans l’ordre, aux pneus avant-gauche, avant-droit, arrière-gauche et arrière-droit.
- La température moteur est exprimée en degrés Celsius.
- Le kilométrage est exprimé en kilomètres.
- Le niveau de carburant est exprimé en pourcentage.
- Les codes défaut dupliqués sont acceptés en entrée, mais un même code ne compte qu’une fois dans le calcul de sa pénalité.
- L’API externe fournit les campagnes de rappel associées au VIN.
- Les campagnes actives retournées par le fournisseur sont prises en compte dans le score.
- Les pénalités des différentes règles se cumulent.
- Pour une règle à paliers, seul le palier applicable le plus pénalisant est retenu.

### 9.2. Formule de calcul

Le score initial est de 100. Les pénalités sont soustraites, puis le résultat est borné entre 0 et 100.

    score = max(0, min(100, 100 - somme des pénalités)))

Les pénalités sont considérées comme des valeurs positives dans cette formule. Les tableaux ci-dessous les présentent comme des retraits de points.

### 9.3. Barème

| Facteur | Condition | Retrait de points | Plafond |
|---|---|---|---|
| Kilométrage | Tranches complètes de 25 000 km au-delà de 100 000 km | 5 points par tranche | 20 points |
| Pression des pneus | Pression strictement inférieure à 2,2 bar ou strictement supérieure à 2,8 bar | 5 points par pneu hors plage | 20 points |
| Batterie 12 V | Tension < 12,4 V | 5 points | Un seul palier |
| Batterie 12 V | Tension < 12,0 V | 10 points | Un seul palier |
| Batterie 12 V | Tension < 11,5 V | 15 points | Un seul palier |
| Température moteur | Température > 105 °C | 5 points | Un seul palier |
| Température moteur | Température > 115 °C | 10 points | Un seul palier |
| Température moteur | Température ≥ 120 °C | 15 points | Un seul palier |
| Carburant | Niveau < 10 % | 2 points | Un seul palier |
| Carburant | Niveau < 5 % | 5 points | Un seul palier |
| Codes défaut OBD | Présence de codes défaut | 5 points par code unique | 20 points |
| Campagne de rappel active | Présence d’au moins une campagne active | 15 points | 15 points |

### 9.4. Précisions sur les seuils

#### Kilométrage

Seules les tranches complètes de 25 000 km au-delà de 100 000 km sont pénalisées.

Exemples :

- 100 000 km : aucun retrait.
- 124 999 km : aucun retrait.
- 125 000 km : retrait de 5 points.
- 150 000 km : retrait de 10 points.
- 200 000 km ou plus : retrait plafonné à 20 points.

#### Pression des pneus

La plage nominale est comprise entre 2,2 et 2,8 bars, bornes incluses.

Une pression exactement égale à 2,2 ou 2,8 bars ne génère donc pas de pénalité.

#### Batterie

Les comparaisons sont strictes :

- 12,4 V : aucun retrait.
- 12,0 V : retrait de 5 points.
- 11,5 V : retrait de 10 points.
- En dessous de 11,5 V : retrait de 15 points.

Les paliers ne se cumulent pas.

#### Température moteur

- 105 °C : aucun retrait.
- 115 °C : retrait de 5 points.
- 120 °C : retrait de 15 points.

Le dernier palier utilise une comparaison inclusive : température supérieure ou égale à 120 °C.

Les paliers ne se cumulent pas.

#### Carburant

- 10 % : aucun retrait.
- 5 % : retrait de 2 points.
- En dessous de 5 % : retrait de 5 points.

Les paliers ne se cumulent pas.

#### Codes défaut

Les codes sont dédupliqués pour le calcul.

Exemple :

    ["P0301", "P0301"]

Cette liste contient un seul code unique et entraîne donc un retrait de 5 points, et non de 10 points.

### 9.5. Exemple complet : score de 43/100

Données du scénario de démonstration :

    mileageKm = 125000
    tirePressuresBar = [2.1, 2.1, 2.4, 2.4]
    batteryVoltage = 11.8
    engineTemperatureC = 118
    fuelLevelPercent = 8
    errorCodes = ["P0301", "P0301"]
    campagnes de rappel actives = 1

Calcul :

| Règle | Justification | Retrait |
|---|---|---|
| Kilométrage | Une tranche complète de 25 000 km au-delà de 100 000 km | 5 points |
| Pression des pneus | Deux pneus à 2,1 bars, sous la limite de 2,2 bars | 10 points |
| Batterie | 11,8 V, sous le seuil de 12,0 V | 10 points |
| Température moteur | 118 °C, au-dessus de 115 °C et en dessous de 120 °C | 10 points |
| Carburant | 8 %, sous le seuil de 10 % | 2 points |
| Codes défaut | Un code unique après déduplication | 5 points |
| Rappel | Une campagne active | 15 points |
| Total | | 57 points |

    score = 100 - 57 = 43/100

Avec les mêmes mesures, mais sans campagne de rappel active :

    score = 100 - 42 = 58/100

La comparaison entre les scores 58 et 43 permet de vérifier que la réponse du fournisseur externe influence réellement le calcul.

### 9.6. Règles de génération des alertes

Les alertes sont calculées séparément des pénalités.

| Type | Sévérité | Condition |
|---|---|---|
| MILEAGE_HIGH | INFO | La pénalité liée au kilométrage est supérieure à zéro |
| TIRE_PRESSURE_LOW | MEDIUM | Au moins un pneu est sous la limite de 2,2 bars |
| TIRE_PRESSURE_HIGH | MEDIUM | Au moins un pneu dépasse la limite de 2,8 bars |
| BATTERY_LOW | HIGH | Tension de batterie < 12,0 V |
| ENGINE_TEMPERATURE_HIGH | HIGH | Température moteur > 115 °C |
| FUEL_LOW | LOW | Niveau de carburant < 10 % |
| OBD_ERROR_CODE | HIGH | Présence d’au moins un code défaut |
| RECALL_CAMPAIGN | CRITICAL | Présence d’au moins une campagne de rappel active |
| HEALTH_SCORE_LOW | CRITICAL | Score final < 50 |

Les seuils d’alerte peuvent différer des seuils de pénalité.

Par exemple, une batterie à 12,2 V entraîne une pénalité de 5 points, mais ne déclenche pas l’alerte BATTERY_LOW, réservée aux valeurs strictement inférieures à 12,0 V.

Une pénalité ne déclenche donc pas systématiquement une alerte.

## 10. API externe simulée

Le fournisseur externe est interprété comme une API de campagnes de rappel associées à un VIN.

Endpoint simulé :

```text
GET /recalls?vin={vin}
```

WireMock permet de :

- simuler la réponse du fournisseur ;
- disposer de données de démonstration déterministes ;
- tester la prise en compte des rappels sans dépendre d’une API réelle.

Les campagnes retournées sont des données de test. Aucun système réel Renault n’est interrogé.

## 11. Traitement asynchrone et gestion des échecs

Le traitement suit les étapes suivantes :

1. Réception et validation du payload.
2. Stockage de la télémétrie.
3. Création d’un job d’analyse.
4. Transmission au service d’analyse par le traitement planifié.
5. Consultation du fournisseur de rappels.
6. Calcul et stockage du rapport.
7. Consultation du rapport et des alertes par le client.

En cas d’échec de transmission au service d’analyse, le job est retenté dans la limite de trois tentatives.

Après trois échecs, le job est abandonné et une erreur est journalisée.

Pour vérifier le retour à la normale après résolution d’un incident, envoyer une nouvelle télémétrie et comparer son `telemetryId` avec celui du rapport.

## 12. Tests automatisés

Depuis la racine du projet :

```bash
mvn clean verify
```

Si le Maven Wrapper est présent dans le dépôt :

```bash
./mvnw clean verify
```

Résultat attendu :

```text
BUILD SUCCESS
```

Le bilan d’exécution doit confirmer :

- l’exécution des tests unitaires ;
- l’exécution des tests d’intégration configurés ;
- l’absence d’échecs et d’erreurs.

Les tests portent sur les règles métier, les cas d’usage et les interactions techniques. Les requêtes `curl` permettent de compléter cette validation par un parcours manuel de la plateforme exécutée avec Docker.

## 13. Tests manuels complémentaires

### 13.1. VIN vide

```bash
curl -i -X POST "http://localhost:8080/api/v1/telemetry" -H "Content-Type: application/json" -d '{"vin":"","measuredAt":"2026-10-31T16:00:00Z","mileageKm":125000,"tirePressuresBar":[2.1,2.1,2.4,2.4],"batteryVoltage":11.8,"engineTemperatureC":118,"fuelLevelPercent":8,"errorCodes":["P0301"]}'
```

Résultat attendu :

```text
HTTP 400 Bad Request
```

### 13.2. JSON mal formé

```bash
curl -i -X POST "http://localhost:8080/api/v1/telemetry" -H "Content-Type: application/json" --data-binary '{"vin":"VF1RFA00567891234",'
```

Résultat attendu :

```text
HTTP 400 Bad Request
```

### 13.3. Rapport inexistant

Utiliser un VIN pour lequel aucune télémétrie n’a été soumise :

```bash
curl -i "http://localhost:8081/api/v1/vehicles/VF1RFA00567890000/health-report"
```

Résultat attendu :

```text
HTTP 404 Not Found
```

### 13.4. Indisponibilité du service d’analyse

> Effectuer ce test après les tests fonctionnels. L’arrêt du service entraîne la perte de ses données H2 en mémoire.

Arrêter le service d’analyse :

```bash
docker compose stop vehicle-analysis-service
```

Envoyer une télémétrie :

```bash
curl -i -X POST "http://localhost:8080/api/v1/telemetry" -H "Content-Type: application/json" -d '{"vin":"VF1RFA00567891235","measuredAt":"2026-10-31T16:10:00Z","mileageKm":125000,"tirePressuresBar":[2.1,2.1,2.4,2.4],"batteryVoltage":11.8,"engineTemperatureC":118,"fuelLevelPercent":8,"errorCodes":["P0301"]}'
```

Suivre les logs :

```bash
docker compose logs -f --since=2m telemetry-service
```

Vérifier :

- les échecs de transmission ;
- les tentatives successives ;
- l’abandon après la troisième tentative.

Quitter le suivi avec `Ctrl+C`, puis redémarrer le service :

```bash
docker compose up -d vehicle-analysis-service
```

Attendre la fin du démarrage et envoyer une nouvelle télémétrie pour vérifier la reprise du parcours.

## 14. Dépannage

### Le POST retourne `202`, mais le rapport est introuvable

L’analyse est asynchrone. Attendre quelques secondes puis consulter à nouveau le rapport.

Si le problème persiste :

```bash
docker compose logs --since=5m --tail=150 telemetry-service vehicle-analysis-service
```

Vérifier les erreurs de communication, les tentatives et les éventuels abandons de jobs.

### Le rapport consulté semble ancien

Comparer le `telemetryId` du rapport avec celui retourné par le dernier POST.

La présence d’un rapport pour un VIN ne garantit pas que la dernière télémétrie envoyée a été traitée.

### Timeout entre les services

Vérifier l’état des conteneurs :

```bash
docker compose ps -a
```

Vérifier leurs réseaux :

```bash
docker inspect "$(docker compose ps -q telemetry-service)" "$(docker compose ps -q vehicle-analysis-service)" --format '{{.Name}} {{json .NetworkSettings.Networks}}'
```

Les deux services doivent partager un réseau Docker.

Le service de télémétrie doit joindre :

```text
http://vehicle-analysis-service:8080
```

L’accès à `http://localhost:8081` depuis la machine hôte ne valide pas à lui seul la communication entre les conteneurs.


## 15. Hypothèses et limites

- Les seuils de détection, pénalités et niveaux de gravité sont des choix d’implémentation du cas d’usage.
- Le fournisseur externe est simulé avec WireMock.
- Les données H2 en mémoire ne sont pas conservées après l’arrêt des applications.
- Le traitement asynchrone introduit un délai entre l’acceptation de la télémétrie et la disponibilité du rapport.
- Les jobs sont abandonnés après trois tentatives en échec.
- La plateforme est conçue pour le développement et la démonstration.
- Les alertes et scores illustrent des règles applicatives ; ils ne constituent pas un diagnostic automobile certifié.
- Une mise en production nécessiterait notamment une persistance durable et une revue de la sécurité, de l’observabilité et de l’exploitation.

## 16. Critères d’acceptation

| Critère | Vérification |
|---|---|
| Collecte des télémétries | Un payload valide retourne `202 Accepted` |
| Analyse du véhicule | Un rapport est produit pour la télémétrie soumise |
| Calcul du score | Le score correspond aux règles métier |
| Fournisseur externe | La réponse WireMock est prise en compte |
| Génération des alertes | Les anomalies produisent les alertes attendues |
| Consultation du rapport | Le GET retourne le rapport associé au véhicule |
| Tests unitaires | Exécution sans échec |
| Tests d’intégration | Exécution sans échec |
| Docker | Démarrage des services avec Docker Compose |
| Architecture propre | Séparation du métier, des cas d’usage et des adaptateurs |