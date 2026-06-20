-- ========================================
-- MIGRATION MANUELLE (si le démarrage échoue encore)
-- psql -U hackbou -d universite_db -f src/main/resources/fix-database.sql
-- ========================================

-- 1. Supprimer la contrainte obsolète sur les noms de rôles
ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_nom_check;

-- 2. Colonne description
ALTER TABLE roles ADD COLUMN IF NOT EXISTS description VARCHAR(255);

-- 3. Renommer l'ancien rôle enseignant
UPDATE roles
SET nom = 'FORMATEUR',
    description = COALESCE(description, 'Enseignant / formateur')
WHERE nom = 'PROFESSEUR';

-- 4. Insérer les rôles manquants (sans doublon sur nom)
INSERT INTO roles (nom, description)
SELECT v.nom, v.description
FROM (VALUES
    ('ADMIN', 'Administrateur système'),
    ('FORMATEUR', 'Enseignant / formateur'),
    ('ETUDIANT', 'Étudiant'),
    ('PERSONNEL_ADMIN', 'Personnel administratif'),
    ('TUTEUR', 'Tuteur pédagogique'),
    ('RESPONSABLE_FORMATION', 'Responsable de formation'),
    ('SERVICE_INSERTION', 'Service insertion professionnelle')
) AS v(nom, description)
WHERE NOT EXISTS (SELECT 1 FROM roles r WHERE r.nom = v.nom);

-- 5. Table de jointure M2M
CREATE TABLE IF NOT EXISTS utilisateur_role (
    id_utilisateur BIGINT NOT NULL,
    id_role BIGINT NOT NULL,
    PRIMARY KEY (id_utilisateur, id_role)
);

-- 6. Migrer role_id → utilisateur_role (adapter la colonne PK si besoin)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'utilisateurs' AND column_name = 'role_id'
    ) THEN
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'utilisateurs' AND column_name = 'id_utilisateur'
        ) THEN
            INSERT INTO utilisateur_role (id_utilisateur, id_role)
            SELECT u.id_utilisateur, u.role_id
            FROM utilisateurs u
            WHERE u.role_id IS NOT NULL
            ON CONFLICT DO NOTHING;
        ELSIF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'utilisateurs' AND column_name = 'id'
        ) THEN
            INSERT INTO utilisateur_role (id_utilisateur, id_role)
            SELECT u.id, u.role_id
            FROM utilisateurs u
            WHERE u.role_id IS NOT NULL
            ON CONFLICT DO NOTHING;
        END IF;
    END IF;
END $$;

-- 7. Nettoyer la table etudiants (colonnes legacy)
UPDATE etudiants
SET annee_entree = annee_debut
WHERE annee_entree IS NULL AND annee_debut IS NOT NULL;

ALTER TABLE etudiants ALTER COLUMN nom DROP NOT NULL;
ALTER TABLE etudiants ALTER COLUMN prenom DROP NOT NULL;
ALTER TABLE etudiants ALTER COLUMN genre DROP NOT NULL;
ALTER TABLE etudiants ALTER COLUMN annee_debut DROP NOT NULL;

ALTER TABLE etudiants DROP COLUMN IF EXISTS nom;
ALTER TABLE etudiants DROP COLUMN IF EXISTS prenom;
ALTER TABLE etudiants DROP COLUMN IF EXISTS genre;
ALTER TABLE etudiants DROP COLUMN IF EXISTS annee_debut;
ALTER TABLE etudiants DROP COLUMN IF EXISTS formation_id;

-- 8. Catalogue : années académiques + colonnes slug/titre
CREATE TABLE IF NOT EXISTS annees_academiques (
    id_annee_academique BIGSERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT
);

ALTER TABLE formations ADD COLUMN IF NOT EXISTS titre VARCHAR(255);
ALTER TABLE formations ADD COLUMN IF NOT EXISTS slug VARCHAR(255);
ALTER TABLE formations ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE formations ADD COLUMN IF NOT EXISTS image_url VARCHAR(255);

ALTER TABLE promotions ADD COLUMN IF NOT EXISTS titre VARCHAR(255);
ALTER TABLE promotions ADD COLUMN IF NOT EXISTS slug VARCHAR(255);
ALTER TABLE promotions ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE promotions ADD COLUMN IF NOT EXISTS id_annee_academique BIGINT
    REFERENCES annees_academiques(id_annee_academique);

UPDATE formations SET titre = nom WHERE (titre IS NULL OR titre = '') AND nom IS NOT NULL;
UPDATE formations SET slug = 'formation-' || id WHERE slug IS NULL OR slug = '';
UPDATE promotions SET titre = nom WHERE (titre IS NULL OR titre = '') AND nom IS NOT NULL;
UPDATE promotions SET slug = 'promotion-' || id_promotion WHERE slug IS NULL OR slug = '';

