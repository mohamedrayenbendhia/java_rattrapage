# Configuration de l'envoi d'emails

Pour permettre à l'application d'envoyer des emails (notamment pour la réinitialisation de mot de passe), vous devez configurer les paramètres d'email dans le fichier `src/main/java/utils/EmailConfig.java`.

## Étapes pour configurer l'envoi d'emails avec Gmail

1. **Créer un compte Gmail** (si vous n'en avez pas déjà un)

2. **Activer l'authentification à deux facteurs** sur votre compte Gmail
   - Allez dans les paramètres de votre compte Google
   - Sélectionnez "Sécurité"
   - Activez "Validation en deux étapes"

3. **Créer un mot de passe d'application**
   - Après avoir activé l'authentification à deux facteurs, retournez dans "Sécurité"
   - Cliquez sur "Mots de passe des applications"
   - Sélectionnez "Autre (nom personnalisé)" dans le menu déroulant
   - Entrez un nom pour l'application (par exemple "EduEvent+")
   - Cliquez sur "Générer"
   - Google vous fournira un mot de passe d'application de 16 caractères
   - Copiez ce mot de passe

4. **Modifier le fichier EmailConfig.java**
   - Ouvrez le fichier `src/main/java/utils/EmailConfig.java`
   - Remplacez `votre-email@gmail.com` par votre adresse Gmail
   - Remplacez `votre-mot-de-passe-app` par le mot de passe d'application généré à l'étape précédente
   - Vous pouvez également personnaliser le nom d'expéditeur en modifiant `EMAIL_FROM_NAME`

## Exemple de configuration

```java
public class EmailConfig {
    public static final String EMAIL_USERNAME = "monapp@gmail.com";
    public static final String EMAIL_PASSWORD = "abcd efgh ijkl mnop"; // Mot de passe d'application
    public static final String EMAIL_HOST = "smtp.gmail.com";
    public static final String EMAIL_PORT = "587";
    public static final String EMAIL_FROM_NAME = "EduEvent+";
}
```

## Remarques importantes

- Ne partagez jamais votre mot de passe d'application
- Si vous utilisez un système de contrôle de version (comme Git), assurez-vous de ne pas committer vos informations d'identification
- Vous pouvez révoquer un mot de passe d'application à tout moment depuis les paramètres de votre compte Google
- Si vous rencontrez des problèmes de connexion, vérifiez que vous avez correctement activé l'accès SMTP dans votre compte Gmail

## Dépannage

Si vous rencontrez des erreurs lors de l'envoi d'emails, vérifiez les points suivants :

1. **Erreur d'authentification** : Assurez-vous que votre mot de passe d'application est correct
2. **Erreur de connexion** : Vérifiez que votre pare-feu ou antivirus ne bloque pas la connexion SMTP
3. **Erreur "Less secure app access"** : Cette option n'est plus disponible, vous devez utiliser un mot de passe d'application
4. **Quota dépassé** : Gmail limite le nombre d'emails que vous pouvez envoyer par jour
