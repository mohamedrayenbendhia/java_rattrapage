-- Ajouter les colonnes nécessaires pour la réinitialisation de mot de passe
ALTER TABLE user ADD COLUMN reset_token VARCHAR(255) DEFAULT NULL;
ALTER TABLE user ADD COLUMN reset_token_expiry DATETIME DEFAULT NULL;
ALTER TABLE user ADD COLUMN reset_code VARCHAR(10) DEFAULT NULL;