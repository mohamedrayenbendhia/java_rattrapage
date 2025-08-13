# Service de Réinitialisation de Mot de Passe avec Code OTP (JavaFX + Backend Java)

## Objectif
Fournir un guide complet pour implémenter dans votre projet JavaFX un service sécurisé permettant à un utilisateur de demander la réinitialisation de son mot de passe via un code (OTP) envoyé par e-mail (Gmail / SMTP), puis de valider ce code et définir un nouveau mot de passe.

---
## Vue d'ensemble du Flux
1. L'utilisateur clique sur « Mot de passe oublié ? ».
2. Il saisit son adresse e-mail.
3. Le serveur génère un OTP (code numérique ou alphanumérique) + date d'expiration et le stocke (en base ou cache sécurisé).
4. Un e-mail est envoyé avec : code + éventuellement un lien de réinitialisation (token signé).
5. L'utilisateur saisit le code reçu (ou clique sur le lien).
6. Le serveur valide (email existe, code correct, non expiré, non déjà utilisé).
7. L'utilisateur saisit le nouveau mot de passe (avec confirmation).
8. Le mot de passe est hashé (BCrypt / Argon2) et sauvegardé, le code invalidé.

---
## Architecture Recommandée

Couche | Rôle
-------|-----
Controller JavaFX | UI (saisie e-mail, code, nouveau mot de passe)
Service (PasswordResetService) | Logique métier (génération OTP, validation, orchestration)
DAO / Repository | Accès aux données (Users, PasswordResetTokens)
Entity (User, PasswordResetToken) | Modèle persistant
Utilitaire (MailSender / EmailService) | Envoi e-mails SMTP
Utilitaire (PasswordHasher) | Hash / vérif des mots de passe
Config | Chargement propriétés (SMTP, expirations, etc.)

---
## Modèle de Données (Tables / Entités)

### Table `users`
- id (PK)
- email (UNIQUE)
- password_hash
- enabled / status
- created_at / updated_at

### Table `password_reset_token`
- id (PK)
- user_id (FK users.id)
- code (OTP) INDEX
- expires_at (timestamp)
- used (boolean)
- created_at

Index recommandés : (user_id), (code), (expires_at)

Option : Au lieu de `code`, stocker un `hashed_code` pour ne jamais conserver le code en clair (comme le principe des mots de passe).

---
## Génération de l'OTP
Critères :
- Longueur : 6 (numérique) ou 8 (alphanumérique)
- Unicité courte durée (optionnel) : ne pas réutiliser le même code actif
- Expiration : 10 min (configurable)

Exemple (numérique) :
```java
private String generateNumericOtp(int length) {
    SecureRandom rnd = new SecureRandom();
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
        sb.append(rnd.nextInt(10));
    }
    return sb.toString();
}
```

Pour un code alphanumérique :
```java
private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // pas de 0/O/1/I
private String generateAlphaNumOtp(int length) {
    SecureRandom rnd = new SecureRandom();
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
        sb.append(ALPHABET.charAt(rnd.nextInt(ALPHABET.length())));
    }
    return sb.toString();
}
```

---
## Hashage du Mot de Passe
Utilisez BCrypt (Spring Security) ou une lib comme `org.mindrot:jbcrypt`.
```xml
<!-- pom.xml -->
<dependency>
  <groupId>org.mindrot</groupId>
  <artifactId>jbcrypt</artifactId>
  <version>0.4</version>
</dependency>
```

```java
import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    public static String hash(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt(12));
    }
    public static boolean matches(String raw, String hash) {
        return BCrypt.checkpw(raw, hash);
    }
}
```

---
## Envoi d'E-mail (Gmail SMTP avec javax.mail)
### Dépendance utilisée
Le projet utilise la librairie stable `javax.mail` :
```xml
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>javax.mail</artifactId>
    <version>1.6.2</version>
</dependency>
```
Cette version suffit pour l'envoi SMTP Gmail (STARTTLS). Aucun changement d'import n'est requis.

### Paramètres (config.properties)
```
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.username=VOTRE_ADRESSE_GMAIL
mail.smtp.password=VOTRE_MOT_DE_PASSE_APPLICATION
mail.smtp.auth=true
mail.smtp.starttls.enable=true
reset.token.expiration.minutes=10
reset.code.length=6
```
NB: Utiliser un "Mot de passe d'application" généré dans la sécurité Google (auth 2FA requise). Jamais votre mot de passe principal.

### Service d'envoi
Imports attendus : `javax.mail.*`, `javax.mail.internet.*`.
```java
public class EmailService {
    private final Properties props = new Properties();
    private final String username;
    private final String password;

    public EmailService() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de charger config.properties", e);
        }
        username = props.getProperty("mail.smtp.username");
        password = props.getProperty("mail.smtp.password");
    }

    private Session createSession() {
        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.auth", props.getProperty("mail.smtp.auth", "true"));
        mailProps.put("mail.smtp.starttls.enable", props.getProperty("mail.smtp.starttls.enable", "true"));
        mailProps.put("mail.smtp.host", props.getProperty("mail.smtp.host"));
        mailProps.put("mail.smtp.port", props.getProperty("mail.smtp.port"));
        return Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void send(String to, String subject, String htmlBody) {
        try {
            Session session = createSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "Support"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(htmlBody, "text/html; charset=UTF-8");
            Transport.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi mail", e);
        }
    }
}
```

### Modèle d'e-mail (HTML minimal)
```java
String html = """
<html><body>
<p>Bonjour,</p>
<p>Voici votre code de réinitialisation : <b>%s</b></p>
<p>Il expirera dans %d minutes.</p>
<p>Si vous n'êtes pas à l'origine de cette demande, ignorez cet e-mail.</p>
<hr><small>Support Application</small>
</body></html>
""".formatted(code, expirationMinutes);
```

---
## Service Métier PasswordResetService
Fonctions clés :
- requestReset(String email)
- verifyCode(String email, String code)
- resetPassword(String email, String code, String newPassword)

Pseudo-implémentation :
```java
public class PasswordResetService {
    private final UserDao userDao;
    private final PasswordResetTokenDao tokenDao;
    private final EmailService emailService;
    private final int codeLength;
    private final int expirationMinutes;

    public PasswordResetService(UserDao userDao, PasswordResetTokenDao tokenDao, EmailService emailService, Properties cfg) {
        this.userDao = userDao;
        this.tokenDao = tokenDao;
        this.emailService = emailService;
        this.codeLength = Integer.parseInt(cfg.getProperty("reset.code.length", "6"));
        this.expirationMinutes = Integer.parseInt(cfg.getProperty("reset.token.expiration.minutes", "10"));
    }

    public void requestReset(String email) {
        User user = userDao.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Email inconnu"));
        // Invalider anciens codes non utilisés (optionnel)
        tokenDao.invalidateActiveTokens(user.getId());
        String code = generateNumericOtp(codeLength);
        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(user.getId());
        token.setCode(code); // ou hash du code
        token.setExpiresAt(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES));
        token.setUsed(false);
        tokenDao.save(token);

        String body = buildEmailBody(code);
        emailService.send(user.getEmail(), "Réinitialisation de mot de passe", body);
    }

    public boolean verifyCode(String email, String code) {
        User user = userDao.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Email inconnu"));
        return tokenDao.findActiveByUserAndCode(user.getId(), code)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
    }

    public void resetPassword(String email, String code, String newPassword) {
        User user = userDao.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Email inconnu"));
        PasswordResetToken token = tokenDao.findActiveByUserAndCode(user.getId(), code)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new IllegalArgumentException("Code invalide ou expiré"));
        // Complexité mot de passe
        validatePasswordStrength(newPassword);
        user.setPasswordHash(PasswordHasher.hash(newPassword));
        userDao.update(user);
        token.setUsed(true);
        tokenDao.update(token);
    }

    private void validatePasswordStrength(String pwd) {
        if (pwd.length() < 8 || !pwd.matches(".*[A-Z].*") || !pwd.matches(".*[a-z].*") || !pwd.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Mot de passe trop faible");
        }
    }

    private String buildEmailBody(String code) {
        return """
            <html><body>
            <p>Bonjour,</p>
            <p>Code OTP: <b>%s</b></p>
            <p>Expiration: %d minutes.</p>
            </body></html>
        """.formatted(code, expirationMinutes);
    }

    // ... generateNumericOtp comme vu plus haut ...
}
```

---
## DAO Simplifié (Exemple JDBC)
```java
public class PasswordResetTokenDao {
    private final DataSource ds;
    public PasswordResetTokenDao(DataSource ds) { this.ds = ds; }

    public void save(PasswordResetToken t) {
        String sql = "INSERT INTO password_reset_token(user_id, code, expires_at, used, created_at) VALUES (?,?,?,?,NOW())";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, t.getUserId());
            ps.setString(2, t.getCode());
            ps.setTimestamp(3, Timestamp.from(t.getExpiresAt()));
            ps.setBoolean(4, t.isUsed());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public Optional<PasswordResetToken> findActiveByUserAndCode(long userId, String code) {
        String sql = "SELECT * FROM password_reset_token WHERE user_id=? AND code=? AND used=FALSE ORDER BY id DESC LIMIT 1";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void invalidateActiveTokens(long userId) {
        String sql = "UPDATE password_reset_token SET used=TRUE WHERE user_id=? AND used=FALSE";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void update(PasswordResetToken t) {
        String sql = "UPDATE password_reset_token SET used=?, expires_at=? WHERE id=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, t.isUsed());
            ps.setTimestamp(2, Timestamp.from(t.getExpiresAt()));
            ps.setLong(3, t.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private PasswordResetToken map(ResultSet rs) throws SQLException {
        PasswordResetToken t = new PasswordResetToken();
        t.setId(rs.getLong("id"));
        t.setUserId(rs.getLong("user_id"));
        t.setCode(rs.getString("code"));
        t.setExpiresAt(rs.getTimestamp("expires_at").toInstant());
        t.setUsed(rs.getBoolean("used"));
        t.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        return t;
    }
}
```

---
## Intégration JavaFX (Exemple de Contrôleur)
Trois vues possibles :
1. EmailFormView (saisie email) -> action requestReset
2. CodeVerificationView (saisie code) -> action verifyCode
3. NewPasswordView (code + nouveau mot de passe) -> action resetPassword

Exemple (EmailFormController) :
```java
public class EmailFormController {
    @FXML private TextField emailField;
    @FXML private Label statusLabel;
    private PasswordResetService resetService;

    public void initialize() { /* injection via setter */ }

    @FXML
    public void onSendCode() {
        try {
            resetService.requestReset(emailField.getText().trim());
            statusLabel.setText("Code envoyé si l'email existe.");
            // Naviguer vers l'écran CodeVerification
        } catch (Exception ex) {
            statusLabel.setText(ex.getMessage());
        }
    }
}
```

CodeVerificationController :
```java
public class CodeVerificationController {
    @FXML private TextField emailField; // pré-rempli
    @FXML private TextField codeField;
    @FXML private Label statusLabel;
    private PasswordResetService resetService;

    @FXML
    public void onVerify() {
        boolean ok = resetService.verifyCode(emailField.getText().trim(), codeField.getText().trim());
        if (ok) {
            statusLabel.setText("Code valide.");
            // Aller à NewPasswordView
        } else {
            statusLabel.setText("Code invalide ou expiré.");
        }
    }
}
```

NewPasswordController :
```java
public class NewPasswordController {
    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private PasswordField pwdField;
    @FXML private PasswordField confirmField;
    @FXML private Label statusLabel;
    private PasswordResetService resetService;

    @FXML
    public void onReset() {
        String email = emailField.getText().trim();
        String code = codeField.getText().trim();
        String pwd = pwdField.getText();
        String confirm = confirmField.getText();
        if (!pwd.equals(confirm)) {
            statusLabel.setText("Les mots de passe ne correspondent pas");
            return;
        }
        try {
            resetService.resetPassword(email, code, pwd);
            statusLabel.setText("Mot de passe mis à jour");
            // Retour à l'écran de login
        } catch (Exception e) {
            statusLabel.setText(e.getMessage());
        }
    }
}
```

---
## Sécurité et Bonnes Pratiques
- Ne divulguez pas si un e-mail existe (message générique). (Option : toujours afficher succès.)
- Limiter le nombre de tentatives (rate limiting / compteur par IP ou par email). Exemple: max 5 codes/h.
- Expirer les codes rapidement (5-15 min) et rendre usage unique.
- Logger les actions (audit sécurité). Ne pas logger le code en clair.
- Hash du code OTP en base (SHA-256 + salt) si sensibilité élevée.
- Utiliser TLS (STARTTLS sur 587 ou SMTPS 465) – déjà couvert.
- Politique robustesse mot de passe (longueur, classes de caractères, pas sur liste noire).
- Éviter d'envoyer le mot de passe dans un mail (jamais).
- Ajouter un CAPTCHA si abus (optionnel).

---
## Variante : Lien de Réinitialisation avec Token UUID Signé
Au lieu du code manuel : envoyer un lien contenant un token (UUID signé ou JWT). Ex:
`https://votre-domaine/reset?token=<token>`
- Stocker le token (hash) + expiration.
- Quand l'utilisateur clique, ouvrir une vue pré-validée (email déjà connu) puis saisir nouveau mot de passe.

---
## Tests (Stratégie)
Tests unitaires :
- Génération OTP (longueur, charset)
- Expiration calculée correctement
- requestReset crée un token et envoie un mail (mock EmailService)
- verifyCode vrai/faux selon scénarios (expiré, utilisé, mauvais code)
- resetPassword hash + invalidation token

Tests d'intégration :
- Avec base H2 en mémoire
- Vérifier transactionnalité (si envoi mail échoue, token annulé?)

---
## Exemple de Test JUnit (pseudo)
```java
class PasswordResetServiceTest {
    @Test
    void resetFlow_ok() {
        // given user + mock daos
        // when requestReset -> capture code
        // when verifyCode -> true
        // when resetPassword -> password mis à jour, token used=true
    }
}
```

---
## Migration Base de Données (Exemple SQL)
```sql
CREATE TABLE password_reset_token (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  code VARCHAR(100) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  used BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_prt_user ON password_reset_token(user_id);
CREATE INDEX idx_prt_code ON password_reset_token(code);
CREATE INDEX idx_prt_expires ON password_reset_token(expires_at);
```

---
## Intégration Progressive dans un Nouveau Projet
1. Ajouter dépendances (jakarta.mail, bcrypt, éventuellement HikariCP + driver DB).
2. Créer `config.properties` + charger via classloader.
3. Implémenter `EmailService` (tester avec une adresse test Gmail).
4. Créer tables DB (script SQL).
5. Implémenter entités + DAO.
6. Implémenter `PasswordResetService`.
7. Créer vues FXML + contrôleurs JavaFX.
8. Connecter contrôleurs au service (injection simple / singleton).
9. Tester localement (simulateur : afficher code en console en mode dev).
10. Durcir sécurité (hash code, rate limiting, logs).

---
## Débogage Courant
Problème | Cause | Solution
---------|-------|--------
Auth SMTP échoue | Mauvais mot de passe | Utiliser mot de passe application Google
Timeout | Port bloqué firewall | Vérifier port 587 sortant
Mail non reçu | Filtré spam | Améliorer sujet / DKIM / SPF (production)
Code toujours invalide | Fuseau horaire / horloge serveur | Synchroniser NTP
Caractères spéciaux cassés | Encodage | Forcer UTF-8 dans setContent

---
## Améliorations Futures
- Support multi-langue des e-mails (fichiers properties i18n)
- Interface admin pour suivre les demandes
- Ajout reCAPTCHA
- Passage à un provider d'e-mail (SendGrid, Mailgun) pour scalabilité
- JWT signé pour reset link
- Historique de changements de mot de passe (empêcher réutilisation récente)

---
## Récapitulatif
Ce document fournit : flux complet, modèles de données, génération OTP, envoi e-mail via Gmail, service métier, DAO, intégration JavaFX, sécurité, tests et améliorations. Suivre les étapes d'intégration pour répliquer rapidement la fonctionnalité dans un autre projet JavaFX.

---
## Licence d'Utilisation
Vous pouvez réutiliser ce guide et adapter le code dans vos projets internes.
