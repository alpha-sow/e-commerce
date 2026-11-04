# E-Commerce — DDD, Clean Architecture & Hexagonal Architecture

Application e-commerce développée avec **Java 25**, **Spring Boot** et **Maven**, conçue comme un **monolithe modulaire** suivant les principes de Domain-Driven Design (DDD), Clean Architecture et Architecture Hexagonale.

L'objectif du projet est de construire une application maintenable, testable et évolutive, en plaçant les règles métier au centre de l'architecture.

## 1. Stack technique

| Technologie | Utilisation |
|---|---|
| Java 25 | Langage de programmation |
| Spring Boot 4.1.2 | Framework applicatif |
| Spring Web MVC | API REST |
| Spring Validation | Validation des DTO web |
| Spring Data JPA | Persistance |
| Hibernate | ORM |
| H2 Database | Base de développement, en fichier (`jdbc:h2:file:./data/ecommerce;AUTO_SERVER=TRUE`) |
| Maven | Gestion des dépendances et build |
| JUnit 5 | Tests unitaires |
| Mockito | Tests des cas d'utilisation |
| MockMvc (`@WebMvcTest`) | Test de la couche web |

Lombok est déclaré dans le `pom.xml` mais n'est pas utilisé ; DevTools est une dépendance optionnelle.

## 2. Les trois concepts architecturaux

DDD, Clean Architecture et Architecture Hexagonale ne sont pas trois architectures concurrentes.

Elles répondent à des questions différentes et se complètent.

| Concept | Question principale | Objectif |
|---|---|---|
| **DDD** | Comment modéliser le métier ? | Construire un modèle métier expressif |
| **Clean Architecture** | Comment organiser les responsabilités et les dépendances ? | Protéger les règles métier des détails techniques |
| **Architecture Hexagonale** | Comment communiquer avec les systèmes externes ? | Isoler l'application grâce aux ports et adaptateurs |

### 2.1. Domain-Driven Design (DDD)

DDD est une approche de conception logicielle qui place le **domaine métier** au cœur du développement.

L'objectif est de représenter les concepts, les règles et le vocabulaire du métier directement dans le code.

#### Concepts fondamentaux

**Entity (Entité)**

Objet métier possédant une identité propre, qui reste la même au cours de son cycle de vie.

Exemple : `Order`.

**Value Object (Objet Valeur)**

Objet défini par ses valeurs, généralement immuable, sans identité métier propre.

Exemples : `Money`, `OrderId`.

**Aggregate (Agrégat)**

Ensemble cohérent d'objets métier dont les invariants sont protégés par une racine d'agrégat (*Aggregate Root*).

Dans notre projet :

- `Order` est la racine d'agrégat.
- `OrderItem` (implémenté comme `record`, avec son propre UUID) est une entité interne à l'agrégat `Order`.
- Les modifications passent par les méthodes de `Order`.

**Domain Invariant (Invariant métier)**

Règle qui doit toujours rester vraie dans le modèle.

Exemples :

- Une commande vide ne peut pas être confirmée.
- Une commande confirmée ne peut plus recevoir de produits.
- Une quantité commandée doit être strictement positive.
- Un montant ne peut pas être négatif.

`Money` et `OrderItem` signalent ces violations par `OrderDomainException` (HTTP 409 via `OrderExceptionHandler`).

**Bounded Context (Contexte délimité)**

Frontière dans laquelle un modèle métier et son vocabulaire possèdent une signification précise.

Seul le module `order` existe aujourd'hui. Les modules suivants sont **prévus, non implémentés** (aucun répertoire) :

- `stock` : disponibilités et réservations.
- `catalog` : produits et prix.
- `payment` : paiements.
- `checkout` : orchestration du processus d'achat.

Ces modules sont des frontières métier candidates, pas des bounded contexts autonomes garantis. Le couplage actuel est un choix/limite assumé : `schema.sql` définit des clés étrangères entre tables de domaines différents (ex. `order_item.product_id` → `product`, `customer_order.customer_id` → `customer`). Une séparation réelle demandera de les revoir.

#### Exemple DDD

```java
public void confirm() {
    ensureDraft();

    if (items.isEmpty()) {
        throw new OrderDomainException(
            "Cannot confirm an empty order"
        );
    }

    status = OrderStatus.CONFIRMED;
}
```

La règle métier est directement portée par l'agrégat `Order`, et non par le contrôleur REST ou le repository JPA.

### 2.2. Clean Architecture

La Clean Architecture organise le système en couches et impose une règle fondamentale :

**Les dépendances du code source doivent pointer vers les parties les plus internes et les plus stables de l'application.**

Dans notre projet, nous utilisons trois couches principales.

**Domain**

Contient le modèle et les règles métier.

- Entités et agrégats.
- Value Objects.
- Exceptions métier.
- Invariants.

Cette couche ne dépend ni de Spring Boot, ni de JPA, ni d'HTTP.

**Application**

Contient les cas d'utilisation qui orchestrent le domaine.

- `CreateOrder`.
- `AddOrderItem`.
- `ConfirmOrder`.
- `CancelOrder`.
- `OrderRepository` (port sortant, dans `application/port/out`).

Elle dépend du domaine, mais pas de l'infrastructure.

**Infrastructure**

Contient les détails techniques et les points d'entrée.

- Contrôleurs REST.
- Entités JPA.
- Repositories Spring Data.
- Adaptateurs de persistance.
- Mappers.
- Configuration Spring.

Cette couche dépend de l'application et du domaine.

#### Règle de dépendance

```text
Infrastructure ──────► Application ──────► Domain
       │
       └─────────────────────────────────► Domain
```

Les flèches représentent les dépendances du code source.

Le domaine ne connaît pas les couches externes.

Par conséquent, nous pouvons tester nos règles métier sans démarrer Spring Boot ni connecter une base de données.

### 2.3. Architecture Hexagonale (Ports & Adapters)

L'Architecture Hexagonale, également appelée *Ports and Adapters*, isole le cœur applicatif des technologies externes.

Elle distingue deux grandes catégories de ports et d'adaptateurs.

**Ports entrants (Driving Ports)**

Ils représentent les opérations que les acteurs externes peuvent demander à l'application.

Exemples :

- Créer une commande.
- Ajouter un produit.
- Confirmer une commande.
- Annuler une commande.

Dans notre implémentation, les classes concrètes `CreateOrder`, `AddOrderItem`, `ConfirmOrder` et `CancelOrder` implémentent directement ces opérations ; il n'existe pas d'interface de port entrant dédiée (choix acceptable, non obligatoire).

**Adaptateurs entrants (Driving Adapters)**

Ils permettent aux acteurs externes d'appeler l'application.

Exemples :

- `OrderController` pour les requêtes HTTP.
- Un consommateur de messages.
- Une interface en ligne de commande.

**Ports sortants (Driven Ports)**

Ils définissent les services techniques dont l'application a besoin, sans imposer leur implémentation.

Exemple :

```java
public interface OrderRepository {

    Optional<Order> findById(OrderId id);

    void save(Order order);
}
```

**Adaptateurs sortants (Driven Adapters)**

Ils implémentent les ports sortants en utilisant une technologie particulière.

Exemple :

```java
public class JpaOrderRepositoryAdapter
        implements OrderRepository {

    private final SpringDataOrderRepository repository;
    private final OrderMapper mapper;

    // Implémentation de la persistance JPA
}
```

Le cas d'utilisation connaît `OrderRepository`, mais ne connaît pas `JpaOrderRepositoryAdapter`.

C'est le principe d'**inversion des dépendances**.

## 3. Comment les trois approches fonctionnent ensemble

Prenons l'exemple de la confirmation d'une commande.

**Étape 1 — Infrastructure entrante**

`OrderController` reçoit une requête HTTP :

```http
POST /api/order/{orderId}/confirm
```

Il construit une commande applicative et appelle `ConfirmOrder`.

**Étape 2 — Application**

`ConfirmOrder` orchestre le scénario :

```java
Order order = repository.findById(orderId)
    .orElseThrow(...);

order.confirm();

repository.save(order);
```

**Étape 3 — Domain**

`Order.confirm()` vérifie les règles métier et modifie l'état de l'agrégat.

**Étape 4 — Infrastructure sortante**

`JpaOrderRepositoryAdapter` utilise `OrderMapper` et Spring Data JPA pour enregistrer les changements dans H2.

#### Répartition des responsabilités

| Responsabilité | Approche principale |
|---|---|
| Modéliser `Order` et ses invariants | DDD |
| Séparer Domain, Application et Infrastructure | Clean Architecture |
| Définir `OrderRepository` comme port | Architecture Hexagonale |
| Implémenter le port avec JPA | Architecture Hexagonale |
| Orienter les dépendances vers le cœur | Clean Architecture |
| Délimiter les modules métier | DDD stratégique |

Ces approches se recouvrent partiellement. Elles sont utilisées ensemble pour clarifier les responsabilités, pas pour multiplier inutilement les abstractions.

## 4. Organisation du projet

```text
e-commerce/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/dev/alphasow/ecommerce/
│   │   │   ├── ECommerceApplication.java
│   │   │   │
│   │   │   ├── order/
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   └── exception/
│   │   │   │   │
│   │   │   │   ├── application/
│   │   │   │   │   ├── command/
│   │   │   │   │   ├── usecase/
│   │   │   │   │   ├── port/out/
│   │   │   │   │   └── exception/
│   │   │   │   │
│   │   │   │   └── infrastructure/
│   │   │   │       ├── persistence/
│   │   │   │       │   ├── entity/
│   │   │   │       │   ├── mapper/
│   │   │   │       │   ├── repository/
│   │   │   │       │   └── adapter/
│   │   │   │       ├── configuration/
│   │   │   │       └── web/          (+ dto/)
│   │   │   │
│   │   │   └── (stock, catalog, payment, checkout : prévus, non créés)
│   │   │
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── schema.sql
│   │
│   └── test/java/dev/alphasow/ecommerce/
└── README.md
```

Le projet commence avec un seul module Maven et une organisation par fonctionnalités métier.

Les modules autres que `order` sont des étapes futures et n'existent pas dans le dépôt.

## 5. Module Order

Le module `order` est le seul module implémenté.

### Domain

| Classe | Rôle |
|---|---|
| `Order` | Racine d'agrégat |
| `OrderItem` | Ligne de commande |
| `OrderId` | Identifiant métier |
| `Money` | Montant monétaire |
| `OrderStatus` | État de la commande |
| `OrderDomainException` | Violation d'une règle métier |

### Application

| Classe | Rôle |
|---|---|
| `CreateOrder` | Créer une commande |
| `AddOrderItem` | Ajouter une ligne |
| `ConfirmOrder` | Confirmer une commande |
| `CancelOrder` | Annuler une commande |
| `OrderRepository` | Port de persistance |

### Infrastructure

| Classe | Rôle |
|---|---|
| `OrderController` | API REST |
| `OrderJpaEntity` | Mapping de `customer_order` |
| `OrderItemJpaEntity` | Mapping de `order_item` |
| `SpringDataOrderRepository` | Repository Spring Data |
| `JpaOrderRepositoryAdapter` | Implémentation du port sortant |
| `OrderMapper` | Conversion Domain / JPA |
| `OrderConfiguration` | Déclaration des beans Spring |
| `OrderExceptionHandler` | Traduction des erreurs en HTTP (404 / 409) |

## 6. Base de données

Le projet utilise H2 en mode fichier (`./data/ecommerce`) pour le développement.

Tables définies dans `schema.sql` :

```text
PRODUCT
CUSTOMER
CUSTOMER_ORDER
ORDER_ITEM
STOCK
STOCK_RESERVATION
PAYMENT
```

Seules `CUSTOMER_ORDER` et `ORDER_ITEM` sont mappées par le module `order` ; les autres tables sont définies mais pas encore utilisées par du code. Ces tables sont reliées par des clés étrangères inter-domaines (voir section Bounded Context).

Les entités JPA restent dans `infrastructure` afin de ne pas coupler le domaine à Hibernate.

Le passage à PostgreSQL est envisagé pour une configuration plus proche de la production.

## 7. API REST Order

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/order` | Créer une commande |
| POST | `/api/order/{id}/items` | Ajouter un produit |
| POST | `/api/order/{id}/confirm` | Confirmer |
| POST | `/api/order/{id}/cancel` | Annuler |

Exemple de création :

```http
POST /api/order
Content-Type: application/json

{
  "customerId": "11111111-1111-1111-1111-111111111111"
}
```

Exemple d'ajout de produit :

```http
POST /api/order/{id}/items
Content-Type: application/json

{
  "productId": "22222222-2222-2222-2222-222222222222",
  "quantity": 2,
  "unitPrice": 79.90
}
```

Le prix est fourni par l'appelant dans cette première version pédagogique. Il devra ensuite provenir du module `catalog` pour éviter qu'un client puisse choisir arbitrairement le prix.

## 8. Tests

Les tests sont organisés selon les responsabilités architecturales.

**Tests du domaine**

Ils vérifient les règles métier sans Spring Boot.

Exemples :

- Impossible de confirmer une commande vide.
- Impossible d'ajouter un produit après confirmation.
- Calcul correct du montant total.

**Tests applicatifs**

Ils vérifient l'orchestration des cas d'utilisation avec des mocks Mockito de `OrderRepository` (pas de fake en mémoire).

**Tests web**

`OrderControllerTest` (`@WebMvcTest` + MockMvc) vérifie les routes et le header `Location`.

**Tests d'intégration**

**Non encore présents** : aucun test ne couvre l'adaptateur JPA, le mapper ou le repository. À ajouter.

## 9. Principes de conception

Le projet applique les principes suivants :

1. Le domaine ne dépend d'aucun framework.
2. Les règles métier appartiennent aux agrégats.
3. Les cas d'utilisation orchestrent les opérations métier.
4. L'application dépend de ports, pas d'implémentations techniques.
5. Les adaptateurs sont remplaçables.
6. La configuration Spring est placée dans l'infrastructure.
7. Les interfaces sont créées lorsqu'elles apportent une frontière utile.
8. Les modules métier sont privilégiés par rapport à une organisation globale par couche technique.
9. Les tests métier ne nécessitent pas le démarrage de Spring Boot.
10. La simplicité est privilégiée tant qu'une abstraction supplémentaire n'apporte pas de valeur.

## 10. Prochaines étapes

- Ajouter les tests d'intégration du module `Order`.
- Renforcer la gestion des transactions et de la concurrence.
- Développer le domaine et les cas d'utilisation du module `Stock`.
- Ajouter le module `Catalog` pour gérer les prix des produits.
- Implémenter `Checkout` pour orchestrer commande, stock et paiement.
- Introduire Flyway pour les migrations SQL.
- Ajouter des tests d'intégration PostgreSQL avec Testcontainers.

---

## Conclusion

**DDD définit comment représenter le métier.**

**Clean Architecture définit comment protéger et organiser les responsabilités.**

**L'Architecture Hexagonale définit comment connecter le cœur applicatif au monde extérieur.**

En combinant ces trois approches, le projet `e-commerce` vise à construire une application dans laquelle les règles métier restent indépendantes des technologies utilisées pour les exposer ou les persister.