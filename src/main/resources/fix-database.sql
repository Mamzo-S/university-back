-- ========================================
-- SCRIPT DE CORRECTION DE LA BASE DE DONNÉES
-- ========================================
-- Exécutez ce script dans pgAdmin ou psql pour corriger les contraintes

-- 1. Supprimer la contrainte CHECK obsolète
ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_nom_check;

-- 2. Vider la table roles (car elle a des anciennes valeurs)
TRUNCATE TABLE roles CASCADE;

-- 3. Recréer les rôles corrects
INSERT INTO roles (nom) VALUES ('ADMIN');
INSERT INTO roles (nom) VALUES ('PROFESSEUR');
INSERT INTO roles (nom) VALUES ('ETUDIANT');

-- 4. Vérifier que les rôles sont créés
SELECT * FROM roles;

-- ========================================
-- Vous pouvez maintenant redémarrer le backend
-- ========================================
