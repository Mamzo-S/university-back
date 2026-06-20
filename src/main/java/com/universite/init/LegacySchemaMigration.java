package com.universite.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Corrige le schéma PostgreSQL hérité (contrainte roles_nom_check, PROFESSEUR, role_id)
 * avant le seed des rôles et de l'admin.
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class LegacySchemaMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        migrateRolesTable();
        migrateUtilisateurRoles();
        migrateEtudiantsTable();
        migrateEmploisDuTemps();
        migrateCatalog();
    }

    private void migrateCatalog() {
        migrateAnneesAcademiquesTable();
        migrateGroupesEtudiantsTable();
        migrateFormateurFormationsTable();
        migrateEtudiantsFiliereColumn();
        migrateNiveauEtudeColumns();
        migratePromotionsFormationOptional();
        migrateCatalogSlugs();
    }

    private void migratePromotionsFormationOptional() {
        if (!tableExists("promotions") || !columnExists("promotions", "formation_id")) {
            return;
        }
        try {
            jdbcTemplate.execute("ALTER TABLE promotions ALTER COLUMN formation_id DROP NOT NULL");
            log.info("Migration catalogue : promotions.formation_id rendu optionnel");
        } catch (Exception ex) {
            log.debug("Migration promotions.formation_id optionnel : {}", ex.getMessage());
        }
    }

    private void migrateNiveauEtudeColumns() {
        if (tableExists("etudiants") && !columnExists("etudiants", "niveau")) {
            jdbcTemplate.execute("ALTER TABLE etudiants ADD COLUMN niveau VARCHAR(32)");
            log.info("Migration catalogue : colonne etudiants.niveau ajoutée");
        }

        if (tableExists("etudiants") && columnExists("etudiants", "niveau")) {
            int backfilled = jdbcTemplate.update("""
                    UPDATE etudiants
                    SET niveau = 'LICENCE_1'
                    WHERE niveau IS NULL OR TRIM(niveau) = ''
                    """);
            if (backfilled > 0) {
                log.info("Migration catalogue : {} étudiant(s) avec niveau par défaut LICENCE_1", backfilled);
            }
            int invalidEtudiants = jdbcTemplate.update("""
                    UPDATE etudiants
                    SET niveau = 'LICENCE_1'
                    WHERE niveau NOT IN (
                        'LICENCE_1', 'LICENCE_2', 'LICENCE_3',
                        'MASTER_1', 'MASTER_2', 'DOCTORAT'
                    )
                    """);
            if (invalidEtudiants > 0) {
                log.info("Migration catalogue : {} étudiant(s) avec niveau invalide corrigé(s)", invalidEtudiants);
            }
        }

        if (tableExists("formations") && columnExists("formations", "niveau")) {
            jdbcTemplate.update("""
                    UPDATE formations SET niveau = 'LICENCE_1'
                    WHERE LOWER(niveau) LIKE '%licence%1%' OR LOWER(niveau) = 'l1'
                    """);
            jdbcTemplate.update("""
                    UPDATE formations SET niveau = 'LICENCE_2'
                    WHERE LOWER(niveau) LIKE '%licence%2%' OR LOWER(niveau) = 'l2'
                    """);
            jdbcTemplate.update("""
                    UPDATE formations SET niveau = 'LICENCE_3'
                    WHERE LOWER(niveau) LIKE '%licence%3%' OR LOWER(niveau) = 'l3'
                    """);
            jdbcTemplate.update("""
                    UPDATE formations SET niveau = 'MASTER_1'
                    WHERE LOWER(niveau) LIKE '%master%1%' OR LOWER(niveau) = 'm1'
                    """);
            jdbcTemplate.update("""
                    UPDATE formations SET niveau = 'MASTER_2'
                    WHERE LOWER(niveau) LIKE '%master%2%' OR LOWER(niveau) = 'm2'
                    """);
            jdbcTemplate.update("""
                    UPDATE formations SET niveau = 'DOCTORAT'
                    WHERE LOWER(niveau) LIKE '%doctor%'
                    """);
            int invalidFormations = jdbcTemplate.update("""
                    UPDATE formations SET niveau = 'LICENCE_1'
                    WHERE niveau IS NULL
                       OR TRIM(niveau) = ''
                       OR niveau NOT IN (
                           'LICENCE_1', 'LICENCE_2', 'LICENCE_3',
                           'MASTER_1', 'MASTER_2', 'DOCTORAT'
                       )
                    """);
            if (invalidFormations > 0) {
                log.info("Migration catalogue : {} formation(s) avec niveau invalide corrigé(s)", invalidFormations);
            }
            log.info("Migration catalogue : niveaux formations normalisés");
        }
    }

    private void migrateEtudiantsFiliereColumn() {
        if (!tableExists("etudiants") || !tableExists("filieres")) {
            return;
        }

        if (!columnExists("etudiants", "id_filiere")) {
            jdbcTemplate.execute("""
                    ALTER TABLE etudiants
                    ADD COLUMN id_filiere BIGINT REFERENCES filieres(id)
                    """);
            log.info("Migration catalogue : colonne etudiants.id_filiere ajoutée");
        }

        if (tableExists("promotions") && tableExists("formations")) {
            int backfilled = jdbcTemplate.update("""
                    UPDATE etudiants e
                    SET id_filiere = f.filiere_id
                    FROM promotions p
                    JOIN formations f ON f.id = p.formation_id
                    WHERE e.id_promotion = p.id_promotion
                      AND e.id_filiere IS NULL
                      AND f.filiere_id IS NOT NULL
                    """);
            if (backfilled > 0) {
                log.info("Migration catalogue : {} étudiant(s) rattaché(s) à une filière", backfilled);
            }
        }
    }

    private void migrateFormateurFormationsTable() {
        if (!tableExists("formateur_formations")) {
            jdbcTemplate.execute("""
                    CREATE TABLE formateur_formations (
                        id_formateur BIGINT NOT NULL REFERENCES formateurs(id_formateur),
                        formation_id BIGINT NOT NULL REFERENCES formations(id),
                        PRIMARY KEY (id_formateur, formation_id)
                    )
                    """);
            log.info("Migration catalogue : table formateur_formations créée");
        }
    }

    private void migrateGroupesEtudiantsTable() {
        if (!tableExists("groupes_etudiants")) {
            jdbcTemplate.execute("""
                    CREATE TABLE groupes_etudiants (
                        id_groupe_etudiant BIGSERIAL PRIMARY KEY,
                        titre VARCHAR(255) NOT NULL,
                        slug VARCHAR(255) NOT NULL UNIQUE,
                        description TEXT,
                        id_promotion BIGINT NOT NULL REFERENCES promotions(id_promotion)
                    )
                    """);
            log.info("Migration catalogue : table groupes_etudiants créée");
        }

        if (tableExists("etudiants") && !columnExists("etudiants", "id_groupe_etudiant")) {
            jdbcTemplate.execute("""
                    ALTER TABLE etudiants
                    ADD COLUMN id_groupe_etudiant BIGINT
                    REFERENCES groupes_etudiants(id_groupe_etudiant)
                    """);
            log.info("Migration catalogue : colonne etudiants.id_groupe_etudiant ajoutée");
        }
    }

    private void migrateAnneesAcademiquesTable() {
        if (!tableExists("annees_academiques")) {
            jdbcTemplate.execute("""
                    CREATE TABLE annees_academiques (
                        id_annee_academique BIGSERIAL PRIMARY KEY,
                        titre VARCHAR(255) NOT NULL,
                        slug VARCHAR(255) NOT NULL UNIQUE,
                        description TEXT
                    )
                    """);
            log.info("Migration catalogue : table annees_academiques créée");
        }

        if (tableExists("promotions") && !columnExists("promotions", "id_annee_academique")) {
            jdbcTemplate.execute("""
                    ALTER TABLE promotions
                    ADD COLUMN id_annee_academique BIGINT
                    REFERENCES annees_academiques(id_annee_academique)
                    """);
            log.info("Migration catalogue : colonne promotions.id_annee_academique ajoutée");
        }

        if (!tableExists("formations")) {
            return;
        }

        addColumnIfMissing("formations", "titre", "VARCHAR(255)");
        addColumnIfMissing("formations", "slug", "VARCHAR(255)");
        addColumnIfMissing("formations", "description", "TEXT");
        addColumnIfMissing("formations", "image_url", "VARCHAR(255)");
        addColumnIfMissing("formations", "contenu_parcours", "TEXT");

        if (!tableExists("promotions")) {
            return;
        }

        addColumnIfMissing("promotions", "titre", "VARCHAR(255)");
        addColumnIfMissing("promotions", "slug", "VARCHAR(255)");
        addColumnIfMissing("promotions", "description", "TEXT");
    }

    private void addColumnIfMissing(String tableName, String columnName, String sqlType) {
        if (!columnExists(tableName, columnName)) {
            jdbcTemplate.execute(String.format(
                    "ALTER TABLE %s ADD COLUMN %s %s",
                    tableName,
                    columnName,
                    sqlType
            ));
            log.info("Migration catalogue : colonne {}.{} ajoutée", tableName, columnName);
        }
    }

    private void migrateCatalogSlugs() {
        if (!tableExists("formations")) {
            return;
        }
        if (columnExists("formations", "titre")) {
            int formations = jdbcTemplate.update("""
                    UPDATE formations
                    SET titre = nom
                    WHERE (titre IS NULL OR titre = '') AND nom IS NOT NULL
                    """);
            if (formations > 0) {
                log.info("Migration formations : {} titre(s) renseigné(s) depuis nom", formations);
            }
        }
        if (columnExists("formations", "slug")) {
            jdbcTemplate.update("""
                    UPDATE formations
                    SET slug = 'formation-' || id
                    WHERE slug IS NULL OR slug = ''
                    """);
        }

        if (!tableExists("promotions")) {
            return;
        }
        if (columnExists("promotions", "titre")) {
            int promotions = jdbcTemplate.update("""
                    UPDATE promotions
                    SET titre = nom
                    WHERE (titre IS NULL OR titre = '') AND nom IS NOT NULL
                    """);
            if (promotions > 0) {
                log.info("Migration promotions : {} titre(s) renseigné(s) depuis nom", promotions);
            }
        }
        if (columnExists("promotions", "slug")) {
            jdbcTemplate.update("""
                    UPDATE promotions
                    SET slug = 'promotion-' || id_promotion
                    WHERE slug IS NULL OR slug = ''
                    """);
        }
    }

    private void migrateRolesTable() {
        jdbcTemplate.execute("ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_nom_check");

        if (!columnExists("roles", "description")) {
            jdbcTemplate.execute("ALTER TABLE roles ADD COLUMN description VARCHAR(255)");
        }

        int renamed = jdbcTemplate.update("""
                UPDATE roles
                SET nom = 'FORMATEUR',
                    description = COALESCE(description, 'Enseignant / formateur')
                WHERE nom = 'PROFESSEUR'
                """);

        if (renamed > 0) {
            log.info("Migration roles : {} entrée(s) PROFESSEUR → FORMATEUR", renamed);
        }
    }

    private void migrateUtilisateurRoles() {
        if (!tableExists("utilisateur_role")) {
            return;
        }

        if (!columnExists("utilisateurs", "role_id")) {
            return;
        }

        String userPk = resolveUtilisateurPrimaryKeyColumn();
        if (userPk == null) {
            log.warn("Migration utilisateur_role ignorée : clé primaire utilisateurs introuvable");
            return;
        }

        int migrated = jdbcTemplate.update(String.format("""
                INSERT INTO utilisateur_role (id_utilisateur, id_role)
                SELECT u.%s, u.role_id
                FROM utilisateurs u
                WHERE u.role_id IS NOT NULL
                ON CONFLICT DO NOTHING
                """, userPk));

        if (migrated > 0) {
            log.info("Migration utilisateur_role : {} lien(s) créé(s) depuis role_id", migrated);
        }
    }

    private void migrateEtudiantsTable() {
        if (!tableExists("etudiants")) {
            return;
        }

        if (columnExists("etudiants", "annee_debut") && columnExists("etudiants", "annee_entree")) {
            int migrated = jdbcTemplate.update("""
                    UPDATE etudiants
                    SET annee_entree = annee_debut
                    WHERE annee_entree IS NULL AND annee_debut IS NOT NULL
                    """);
            if (migrated > 0) {
                log.info("Migration etudiants : {} annee_entree copiée(s) depuis annee_debut", migrated);
            }
        }

        for (String legacyColumn : new String[]{
                "nom", "prenom", "genre", "annee_debut", "formation_id"
        }) {
            dropNotNullIfExists("etudiants", legacyColumn);
            dropColumnIfExists("etudiants", legacyColumn);
        }
    }

    private void migrateEmploisDuTemps() {
        if (!tableExists("promotions")) {
            return;
        }

        if (!tableExists("emplois_du_temps")) {
            return;
        }

        int created = jdbcTemplate.update("""
                INSERT INTO emplois_du_temps (id_promotion, libelle, publie)
                SELECT p.id_promotion, 'EDT — ' || p.nom, false
                FROM promotions p
                WHERE NOT EXISTS (
                    SELECT 1 FROM emplois_du_temps e WHERE e.id_promotion = p.id_promotion
                )
                """);

        if (created > 0) {
            log.info("Migration emplois_du_temps : {} emploi(s) créé(s) pour les promotions existantes", created);
        }

        if (!tableExists("seances")) {
            return;
        }

        if (!columnExists("seances", "id_promotion")) {
            return;
        }

        if (!columnExists("seances", "id_emploi_du_temps")) {
            return;
        }

        int linked = jdbcTemplate.update("""
                UPDATE seances s
                SET id_emploi_du_temps = e.id_emploi_du_temps
                FROM emplois_du_temps e
                WHERE s.id_promotion = e.id_promotion
                  AND s.id_emploi_du_temps IS NULL
                """);

        if (linked > 0) {
            log.info("Migration seances : {} séance(s) rattachée(s) à un emploi du temps", linked);
        }

        migrateSeancesLegacyPromotionColumn();
    }

    /**
     * Ancien schéma : séance liée directement à {@code id_promotion}.
     * Nouveau modèle : séance → emploi du temps → promotion.
     */
    private void migrateSeancesLegacyPromotionColumn() {
        if (!tableExists("seances") || !columnExists("seances", "id_promotion")) {
            return;
        }

        if (columnExists("seances", "id_emploi_du_temps")) {
            int backfilled = jdbcTemplate.update("""
                    UPDATE seances s
                    SET id_emploi_du_temps = e.id_emploi_du_temps
                    FROM emplois_du_temps e
                    WHERE s.id_promotion = e.id_promotion
                      AND s.id_emploi_du_temps IS NULL
                    """);
            if (backfilled > 0) {
                log.info(
                        "Migration seances : {} séance(s) avec id_emploi_du_temps renseigné depuis id_promotion",
                        backfilled
                );
            }
        }

        dropNotNullIfExists("seances", "id_promotion");
        dropColumnIfExists("seances", "id_promotion");
    }

    private void dropNotNullIfExists(String tableName, String columnName) {
        if (!columnExists(tableName, columnName)) {
            return;
        }

        try {
            jdbcTemplate.execute(String.format(
                    "ALTER TABLE %s ALTER COLUMN %s DROP NOT NULL",
                    tableName,
                    columnName
            ));
        } catch (Exception ex) {
            log.warn(
                    "Migration {}.{} : impossible de retirer NOT NULL ({})",
                    tableName,
                    columnName,
                    ex.getMessage()
            );
        }
    }

    private void dropColumnIfExists(String tableName, String columnName) {
        if (!columnExists(tableName, columnName)) {
            return;
        }

        try {
            jdbcTemplate.execute(String.format(
                    "ALTER TABLE %s DROP COLUMN IF EXISTS %s",
                    tableName,
                    columnName
            ));
            log.info("Migration {} : colonne legacy {} supprimée", tableName, columnName);
        } catch (Exception ex) {
            log.warn(
                    "Migration {}.{} : impossible de supprimer la colonne ({})",
                    tableName,
                    columnName,
                    ex.getMessage()
            );
        }
    }

    private String resolveUtilisateurPrimaryKeyColumn() {
        if (columnExists("utilisateurs", "id_utilisateur")) {
            return "id_utilisateur";
        }
        if (columnExists("utilisateurs", "id")) {
            return "id";
        }
        return null;
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.tables
                        WHERE table_schema = 'public' AND table_name = ?
                        """,
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.columns
                        WHERE table_schema = 'public'
                          AND table_name = ?
                          AND column_name = ?
                        """,
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }
}
