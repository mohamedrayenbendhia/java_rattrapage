# Service de Réinitialisation de Mot de Passe

Cette documentation décrit la version actuelle et simplifiée du service de réinitialisation de mot de passe basée sur:

* Stockage de tokens (UUID) en base
* Envoi d'un code par email via Gmail (JavaMail 1.6.2 - dépendance `com.sun.mail:javax.mail`)
*c Hachage des nouveaux mots de passe avec BCrypt (lib `org.mindrot:jbcrypt:0.4`)

---
## 1. Flux Fonctionnel

1. L'utilisateur saisit son email dans l'écran "Mot de passe oublié".
2. Le backend vérifie que l'email existe (`emailExists`).
3. Génération d'un token UUID (`generateToken`).
4. Stockage du token + date d'expiration (24h) dans la table `password_reset_tokens` (en supprimant au préalable les anciens tokens de cet email).
5. Envoi d'un email contenant le code (token) en clair.
6. L'utilisateur copie-colle le code dans l'interface de vérification + choisit un nouveau mot de passe.
7. Le backend vérifie le token (non expiré) → récupère l'email associé (`getEmailFromToken`).
8. Hachage du nouveau mot de passe avec BCrypt (`BCrypt.hashpw`) et mise à jour dans la table `user`.
9. Suppression du token (`invalidateToken`).

---
## 2. Modèle de Données

Table utilisée: `password_reset_tokens`

| Colonne       | Type          | Description                               |
|---------------|---------------|-------------------------------------------|
| id            | INT AUTO_INC  | PK                                        |
| email         | VARCHAR(255)  | Email utilisateur                         |
| token         | VARCHAR(255)  | UUID stocké tel quel                      |
| expiry_date   | TIMESTAMP     | Date/heure d'expiration                   |
| created_at    | TIMESTAMP     | Par défaut CURRENT_TIMESTAMP              |

Index/contrainte: `UNIQUE KEY unique_token (token)`

---
## 3. Sécurité & Choix Techniques

| Aspect                | Décision                                          | Raison |
|-----------------------|---------------------------------------------------|--------|
| Hachage mot de passe  | BCrypt (cost 12)                                  | Aligné avec dépendance existante, bon compromis sécurité/perf |
| Algorithme alternatif | Argon2id NON utilisé                              | Demande explicite de conserver BCrypt |
| Transport email       | STARTTLS (port 587)                               | Support Gmail standard |
| Auth email            | Mot de passe d'application Gmail                  | Sécurité + OAuth non requis ici |
| Longueur token        | UUID v4 (~36 chars)                               | Suffisant pour usage ponctuel |
| Expiration            | 24 heures                                         | Simplicité + sécurité raisonnable |

---
## 4. Configuration Email

Fichier: `src/main/java/utils/EmailConfig.java`

```java
public class EmailConfig {
    public static final String EMAIL_USERNAME = "youssef.alaya40@gmail.com";
    public static final String EMAIL_PASSWORD = "stjg uvpc keup sxzq"; // mot de passe d'application
    public static final String EMAIL_HOST = "smtp.gmail.com";
    public static final String EMAIL_PORT = "587"; // STARTTLS
    public static final String EMAIL_FROM_NAME = "esprit";
}
```

Étapes côté Gmail:
1. Activer l'authentification à deux facteurs.
2. Générer un mot de passe d'application: https://myaccount.google.com/apppasswords
3. Coller ce mot de passe dans `EMAIL_PASSWORD` (sans espaces optionnellement, les espaces sont purement visuels).

---
## 5. Points Clés du Code (`restpasseService`)

Méthodes principales:
* `requestPasswordReset(email)` → crée/sauve token puis `sendResetEmail`
* `resetPassword(token, newPassword)` → vérifie token, Bcrypt, update, invalide token
* `getEmailFromToken(token)` → vérifie non-expiration
* `invalidateToken(token)` → suppression directe

Propriétés SMTP utilisées:
```java
props.put("mail.smtp.auth", "true");
props.put("mail.smtp.starttls.enable", "true");
props.put("mail.smtp.host", SMTP_HOST);
props.put("mail.smtp.port", SMTP_PORT);
props.put("mail.smtp.ssl.trust", SMTP_HOST);
props.put("mail.smtp.ssl.protocols", "TLSv1.2");
```

---
## 6. Bonnes Pratiques / Améliorations Futures

1. Ajouter un compteur de tentatives de réinitialisation / rate limiting.
2. Logger via SLF4J à la place de `System.out.println`.
3. Utiliser un code court (6-8 chiffres) stocké hashé (actuel: UUID en clair) si expérience utilisateur à optimiser.
4. Nettoyage planifié (cron) des tokens expirés (optionnel, deletes implicites lors de nouvelles demandes déjà partiel).
5. Externaliser `EmailConfig` (fichier `.properties` ou variables d'environnement) pour éviter commit des secrets.

---
## 7. Commandes Maven Utiles

```bash
mvn -q -DskipTests clean compile
mvn javafx:run
```

---
## 8. Dépannage

| Erreur                                     | Cause probable                                   | Action |
|--------------------------------------------|--------------------------------------------------|--------|
| 535 5.7.8 Username and Password not accepted | Mauvais mot de passe d'application ou non généré | Re-générer et mettre à jour EmailConfig |
| Timeout SMTP                               | Pare-feu / proxy / port bloqué                   | Vérifier réseau |
| Aucun email reçu                           | Spam / retard / quota Gmail                      | Vérifier dossier spam, attendre, limiter essais |

---
## 9. Résumé

Implémentation minimale conservée: Gmail + JavaMail 1.6.2 + BCrypt. Aucune intégration Argon2 ni changement de dépendances non demandé. Documentation ajoutée dans ce fichier pour maintenance.
