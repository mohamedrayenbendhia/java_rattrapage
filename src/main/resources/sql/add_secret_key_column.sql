-- Ajouter la colonne secret_key à la table user
ALTER TABLE user ADD COLUMN secret_key VARCHAR(255) DEFAULT NULL;