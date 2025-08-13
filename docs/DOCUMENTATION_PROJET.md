# Documentation du Projet UserManager+ (PI Dev Java)

## Table des Matières
1. [Vue d'Ensemble](#vue-densemble)
2. [Architecture du Projet](#architecture-du-projet)
3. [Technologies Utilisées](#technologies-utilisées)
4. [Structure des Fichiers](#structure-des-fichiers)
5. [Modules et Fonctionnalités](#modules-et-fonctionnalités)
6. [Entités](#entités)
7. [Services](#services)
8. [Contrôleurs](#contrôleurs)
9. [Interfaces Utilisateur (FXML)](#interfaces-utilisateur-fxml)
10. [Utilitaires](#utilitaires)
11. [Configuration](#configuration)
12. [Installation et Déploiement](#installation-et-déploiement)

---

## Vue d'Ensemble

**UserManager+** est une application JavaFX de gestion d'utilisateurs développée en Java 17. L'application permet la gestion complète des utilisateurs avec authentification, autorisation basée sur les rôles, système de notation, et interface d'administration.

### Fonctionnalités Principales
- **Authentification sécurisée** avec hashage des mots de passe
- **Authentification à deux facteurs (2FA)**
- **Gestion des rôles** (Admin, Client)
- **Système de notation** entre utilisateurs
- **Réinitialisation de mot de passe** par email
- **Interface d'administration** complète
- **Profils utilisateur** avec photos
- **Tableau de bord** pour différents types d'utilisateurs

---

## Architecture du Projet

Le projet suit une architecture **MVC (Model-View-Controller)** avec les couches suivantes :

```
├── Entités (Model)     → Représentation des données
├── Services            → Logique métier
├── Contrôleurs (Controller) → Gestion des interactions
├── Vues (FXML)        → Interface utilisateur
└── Utilitaires        → Classes helper
```

---

## Technologies Utilisées

### Framework et Langages
- **Java 17** - Langage principal
- **JavaFX 17.0.6** - Interface utilisateur
- **Maven** - Gestion des dépendances

### Base de Données
- **MySQL 8.0.33** - Base de données principale
- **MySQL Connector/J** - Driver JDBC

### Sécurité
- **BCrypt (Mindrot)** - Hashage des mots de passe
- **TOTP (Time-based One-Time Password)** - Authentification 2FA

### Communication
- **JavaMail 1.6.2** - Envoi d'emails
- **Twilio** - Service SMS (optionnel)

---

## Structure des Fichiers

```
pi_dev/
├── pom.xml                     # Configuration Maven
├── java_ratrappage.sql         # Script de base de données
├── docs/                       # Documentation
│   ├── RESET_PASSWORD_SERVICE.md
│   └── DOCUMENTATION_PROJET.md
├── src/main/
│   ├── java/
│   │   ├── module-info.java    # Configuration du module Java
│   │   ├── controllers/        # Contrôleurs MVC
│   │   ├── entities/          # Entités/Modèles
│   │   ├── services/          # Services métier
│   │   ├── tests/             # Classes de test et Main
│   │   └── utils/             # Classes utilitaires
│   └── resources/
│       ├── css/               # Feuilles de style
│       ├── fxml/              # Fichiers d'interface
│       ├── images/            # Ressources images
│       ├── sql/               # Scripts SQL
│       └── styles/            # Styles additionnels
└── target/                    # Fichiers compilés
```

---

## Modules et Fonctionnalités

### 1. Module d'Authentification
- **Connexion utilisateur** avec email/mot de passe
- **Authentification à deux facteurs (2FA)**
- **Inscription de nouveaux utilisateurs**
- **Réinitialisation de mot de passe**
- **Gestion des sessions utilisateur**

### 2. Module d'Administration
- **Tableau de bord administrateur**
- **Gestion des utilisateurs** (liste, ajout, modification, suppression)
- **Gestion des rôles**
- **Statistiques globales**

### 3. Module Client
- **Tableau de bord client**
- **Profil utilisateur personnel**
- **Système de notation des autres utilisateurs**
- **Consultation des clients**

### 4. Module de Communication
- **Envoi d'emails** (réinitialisation, notifications)
- **Service SMS** pour 2FA
- **Notifications système**

---

## Entités

### User.java
**Objectif** : Représente un utilisateur du système

**Attributs principaux** :
- `id` : Identifiant unique
- `name` : Nom complet
- `email` : Adresse email (unique)
- `phone_number` : Numéro de téléphone
- `password` : Mot de passe hashé
- `isVerified` : Statut de vérification
- `isBlocked` : Statut de blocage
- `created_at` : Date de création
- `role` : Liste des rôles
- `image` : Photo de profil
- `secretKey` : Clé secrète pour 2FA

**Fonctionnalités** :
- Getters/Setters pour tous les attributs
- Constructeurs multiples
- Gestion des rôles multiples

### Rating.java
**Objectif** : Représente une évaluation entre utilisateurs

**Attributs** :
- `id` : Identifiant unique
- `raterId` : ID de l'utilisateur qui note
- `ratedId` : ID de l'utilisateur noté
- `rating` : Note (généralement 1-5)
- `comment` : Commentaire optionnel
- `createdAt` : Date de création

### Personne.java
**Objectif** : Entité de base pour les personnes

**Attributs** :
- `id_personne` : Identifiant
- `nom`, `prenom` : Nom et prénom
- `email` : Adresse email
- `telephone` : Numéro de téléphone

### UserSession.java
**Objectif** : Gestion de la session utilisateur active

**Fonctionnalités** :
- Pattern Singleton
- Stockage de l'utilisateur connecté
- Méthodes de connexion/déconnexion
- Vérification des permissions

---

## Services

### AuthService.java
**Objectif** : Gestion de l'authentification et de l'autorisation

**Fonctionnalités principales** :
- `authenticate(email, password)` : Authentification utilisateur
- `register(user)` : Inscription nouvel utilisateur
- `logout()` : Déconnexion
- `isAuthenticated()` : Vérification de l'authentification
- `validateCredentials()` : Validation des identifiants
- `hashPassword()` : Hashage sécurisé des mots de passe
- `verify2FA()` : Vérification 2FA

**Sécurité** :
- Hashage BCrypt des mots de passe
- Validation des entrées
- Gestion des comptes bloqués
- Protection contre les attaques par force brute

### UserService.java
**Objectif** : Gestion des opérations CRUD sur les utilisateurs

**Fonctionnalités** :
- `getAllUsers()` : Récupération de tous les utilisateurs
- `getUserById(id)` : Récupération par ID
- `updateUser(user)` : Mise à jour d'un utilisateur
- `deleteUser(id)` : Suppression d'un utilisateur
- `searchUsers(criteria)` : Recherche avec critères
- `getUsersByRole(role)` : Filtrage par rôle

### RatingService.java
**Objectif** : Gestion du système de notation

**Fonctionnalités** :
- `addRating(rating)` : Ajout d'une nouvelle note
- `getRatingsByUser(userId)` : Notes d'un utilisateur
- `getAverageRating(userId)` : Moyenne des notes
- `getAllUsersExceptCurrent(currentId)` : Liste pour notation
- `updateRating(rating)` : Modification d'une note
- `deleteRating(id)` : Suppression d'une note

### RoleService.java
**Objectif** : Gestion des rôles et permissions

**Fonctionnalités** :
- `assignRole(userId, role)` : Attribution de rôle
- `removeRole(userId, role)` : Suppression de rôle
- `getUserRoles(userId)` : Rôles d'un utilisateur
- `hasRole(userId, role)` : Vérification de rôle
- `getAllRoles()` : Liste de tous les rôles

### restpasseService.java
**Objectif** : Gestion de la réinitialisation des mots de passe

**Fonctionnalités** :
- `generateResetToken(email)` : Génération de token
- `sendResetEmail(email, token)` : Envoi d'email
- `validateResetToken(token)` : Validation du token
- `resetPassword(token, newPassword)` : Réinitialisation
- `cleanExpiredTokens()` : Nettoyage des tokens expirés

### SMSService.java
**Objectif** : Service d'envoi de SMS

**Fonctionnalités** :
- `sendSMS(phoneNumber, message)` : Envoi de SMS
- `send2FACode(phoneNumber, code)` : Code 2FA par SMS
- `sendNotification(phoneNumber, message)` : Notifications

---

## Contrôleurs

### LoginController.java
**Objectif** : Gestion de la page de connexion

**Fonctionnalités** :
- `handleLogin()` : Traitement de la connexion
- `handleForgotPassword()` : Redirection mot de passe oublié
- `handleRegister()` : Redirection inscription
- `handle2FA()` : Gestion de l'authentification 2FA
- Validation des champs de saisie
- Gestion des erreurs d'authentification

**Éléments FXML gérés** :
- Champs email et mot de passe
- Boutons de connexion et navigation
- Messages d'erreur et de succès

### RegisterController.java
**Objectif** : Gestion de l'inscription des nouveaux utilisateurs

**Fonctionnalités** :
- `handleRegister()` : Traitement de l'inscription
- `handleLogin()` : Retour à la connexion
- `validateForm()` : Validation du formulaire
- `checkEmailExists()` : Vérification unicité email
- Gestion des rôles par défaut
- Validation des mots de passe

### ClientDashboardController.java
**Objectif** : Tableau de bord pour les utilisateurs clients

**Fonctionnalités** :
- `initialize()` : Initialisation du tableau de bord
- `handleViewClients()` : Affichage de la liste des clients
- `handleRateClient()` : Interface de notation
- `handleViewRatings()` : Consultation des notes
- `handleProfile()` : Accès au profil
- `handleLogout()` : Déconnexion
- Affichage des statistiques personnelles

### Admin/AdminDashboardController.java
**Objectif** : Tableau de bord administrateur

**Fonctionnalités** :
- Vue d'ensemble des utilisateurs
- Statistiques globales du système
- Accès rapide aux fonctions d'administration
- Gestion des alertes système

### Admin/UserListController.java
**Objectif** : Gestion de la liste des utilisateurs (admin)

**Fonctionnalités** :
- `loadUsers()` : Chargement de la liste
- `handleAddUser()` : Ajout d'utilisateur
- `handleEditUser()` : Modification d'utilisateur
- `handleDeleteUser()` : Suppression d'utilisateur
- `handleRefresh()` : Actualisation de la liste
- `searchUsers()` : Fonction de recherche
- Interface ListView personnalisée

### ProfileUserController.java
**Objectif** : Gestion du profil utilisateur

**Fonctionnalités** :
- `loadUserProfile()` : Chargement du profil
- `handleUpdateProfile()` : Mise à jour du profil
- `handleChangePassword()` : Changement de mot de passe
- `handleImageUpload()` : Upload de photo de profil
- `handleEnable2FA()` : Activation 2FA
- Validation des modifications

### ResetPasswordController.java
**Objectif** : Gestion de la réinitialisation de mot de passe

**Fonctionnalités** :
- `handleResetRequest()` : Demande de réinitialisation
- `handlePasswordReset()` : Nouveau mot de passe
- `validateToken()` : Validation du token
- `handleBackToLogin()` : Retour à la connexion
- Validation du nouveau mot de passe

---

## Interfaces Utilisateur (FXML)

### Pages d'Authentification

#### Login.fxml
**Objectif** : Page de connexion principale

**Éléments** :
- Champs email et mot de passe
- Case "Se souvenir de moi"
- Bouton de connexion
- Liens vers inscription et mot de passe oublié
- Interface pour code 2FA

#### Register.fxml
**Objectif** : Page d'inscription

**Éléments** :
- Formulaire complet (nom, email, téléphone, mot de passe)
- Confirmation de mot de passe
- Sélection de rôle (si autorisé)
- Bouton d'inscription
- Lien retour connexion

#### ResetPassword.fxml
**Objectif** : Page de réinitialisation de mot de passe

**Éléments** :
- Champ email pour demande
- Champs nouveau mot de passe
- Champ token de validation
- Instructions utilisateur

### Tableaux de Bord

#### ClientDashboard.fxml
**Objectif** : Interface principale pour les clients

**Sections** :
- Barre de menu avec déconnexion
- Informations utilisateur
- Zone de contenu dynamique
- Boutons d'action (Noter, Voir clients, Profil)
- Zone d'affichage des données

#### admin/AdminDashboard.fxml
**Objectif** : Interface principale pour les administrateurs

**Sections** :
- Sidebar de navigation
- Zone de bienvenue
- Cartes de statistiques
- Actions rapides
- Indicateurs système

#### HomePage.fxml
**Objectif** : Page d'accueil générale

**Sections** :
- Message de bienvenue personnalisé
- Statistiques rapides
- Tableau des utilisateurs récents
- Barre de statut

### Interfaces d'Administration

#### admin/UserList.fxml
**Objectif** : Gestion de la liste des utilisateurs

**Éléments** :
- ListView des utilisateurs
- Boutons CRUD (Ajouter, Modifier, Supprimer)
- Bouton de rafraîchissement
- Barre de recherche
- Informations détaillées par utilisateur

#### admin/AdminSidebar.fxml
**Objectif** : Navigation latérale pour l'admin

**Éléments** :
- Bouton Dashboard
- Bouton Gestion Utilisateurs
- Bouton Profil
- Bouton Déconnexion
- Indicateurs visuels de la page active

### Autres Interfaces

#### profileuser.fxml
**Objectif** : Page de profil utilisateur

**Éléments** :
- Photo de profil avec upload
- Formulaire de modification
- Section changement de mot de passe
- Configuration 2FA
- Boutons de sauvegarde

#### ClientRating.fxml / ClientRatingSimple.fxml
**Objectif** : Interfaces de notation

**Éléments** :
- Système d'étoiles pour notation
- Zone de commentaire
- Liste des utilisateurs à noter
- Historique des notes données/reçues

---

## Utilitaires

### MyDatabase.java
**Objectif** : Gestionnaire de connexion à la base de données

**Fonctionnalités** :
- Pattern Singleton pour la connexion
- Configuration MySQL
- Gestion des erreurs de connexion
- Pool de connexions
- Méthodes de fermeture sécurisée

### PasswordHasher.java
**Objectif** : Utilitaire de hashage sécurisé

**Fonctionnalités** :
- `hashPassword(password)` : Hashage BCrypt
- `verifyPassword(password, hash)` : Vérification
- `generateSalt()` : Génération de sel
- Configuration de la complexité BCrypt

### ValidationUtils.java
**Objectif** : Validation des données d'entrée

**Fonctionnalités** :
- `isValidEmail(email)` : Validation email
- `isValidPassword(password)` : Critères mot de passe
- `isValidPhoneNumber(phone)` : Format téléphone
- `sanitizeInput(input)` : Nettoyage des entrées
- `validateLength(text, min, max)` : Validation longueur

### SceneManager.java
**Objectif** : Gestionnaire de navigation entre pages

**Fonctionnalités** :
- `navigateToPage(stage, fxmlPath, title)` : Navigation
- `loadFXML(path)` : Chargement FXML
- `showAlert(type, title, message)` : Alertes
- `confirmDialog(message)` : Dialogues de confirmation
- Gestion des erreurs de chargement

### DatabaseUpdater.java
**Objectif** : Mise à jour automatique de la structure de base de données

**Fonctionnalités** :
- `updateDatabaseStructure()` : Mise à jour complète
- `executeScriptFile(fileName)` : Exécution de scripts SQL
- `checkTableExists(tableName)` : Vérification structure
- `addColumnIfNotExists()` : Ajouts de colonnes
- Versioning de la base de données

### EmailConfig.java
**Objectif** : Configuration du service email

**Fonctionnalités** :
- Configuration SMTP
- Templates d'emails
- Gestion des pièces jointes
- `sendEmail(to, subject, body)` : Envoi d'email
- `sendHTMLEmail()` : Emails formatés

---

## Configuration

### pom.xml
**Dépendances principales** :
```xml
- JavaFX Controls & FXML (17.0.6)
- MySQL Connector (8.0.33)
- BCrypt (Mindrot) - Sécurité
- JavaMail (1.6.2) - Emails
- QRGen/ZXing - QR Codes 2FA
- ControlsFX - Composants UI étendus
```

### module-info.java
**Modules requis** :
```java
- java.desktop
- java.sql
- javafx.controls
- javafx.fxml
- mysql.connector.java
- java.mail
```

### Structure SQL

#### Tables principales :
1. **users** - Utilisateurs principaux
2. **user_roles** - Rôles des utilisateurs
3. **ratings** - Système de notation
4. **password_resets** - Tokens de réinitialisation
5. **personne** - Entités personne de base

#### Scripts SQL inclus :
- `add_reset_password_columns.sql` - Colonnes réinitialisation
- `add_secret_key_column.sql` - Support 2FA
- `create_user_roles_table.sql` - Table des rôles
- `initialize_roles.sql` - Rôles par défaut
- `update_user_schema.sql` - Mises à jour schema

---

## Installation et Déploiement

### Prérequis
1. **Java 17** ou supérieur
2. **MySQL 8.0** ou supérieur
3. **Maven 3.6** ou supérieur
4. **JavaFX Runtime** (si pas inclus dans le JDK)

### Configuration de la Base de Données
1. Créer une base de données MySQL
2. Importer le fichier `java_ratrappage.sql`
3. Configurer les paramètres de connexion dans `MyDatabase.java`
4. Exécuter les scripts de mise à jour si nécessaire

### Compilation et Exécution
```bash
# Compilation
mvn clean compile

# Exécution avec Maven
mvn javafx:run

# Ou directement avec Java
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -cp target/classes tests.Main
```

### Configuration Email (Optionnel)
1. Configurer les paramètres SMTP dans `EmailConfig.java`
2. Tester l'envoi d'emails de réinitialisation
3. Configurer les templates d'emails

### Configuration SMS (Optionnel)
1. Obtenir des clés API Twilio
2. Configurer `SMSService.java`
3. Tester l'envoi de codes 2FA

---

## Notes de Développement

### Patterns Utilisés
- **Singleton** : Services et connexion DB
- **MVC** : Architecture générale
- **Observer** : Gestion des événements UI
- **Factory** : Création d'objets complexes

### Sécurité Implémentée
- Hashage BCrypt des mots de passe
- Protection contre les injections SQL
- Validation stricte des entrées
- Gestion des sessions sécurisées
- Authentification à deux facteurs

### Extensibilité
- Interface `IService<T>` pour nouveaux services
- Structure modulaire pour nouvelles fonctionnalités
- Configuration externalisable
- Support multi-langue (préparé)

---

**Dernière mise à jour** : Août 2025  
**Version** : 1.0-SNAPSHOT  
**Auteur** : Équipe PI Dev Java
