package com.universite.repository;

import com.universite.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByEtudiantId(Long etudiantId);
    List<Note> findByEtudiantIdAndBulletinPublieTrue(Long etudiantId);
    List<Note> findByEtudiantIdAndSemestreAndAnneeAcademique(Long etudiantId, String semestre, String anneeAcademique);
    List<Note> findByEtudiantIdAndSemestreAndAnneeAcademiqueAndBulletinPublieTrue(Long etudiantId, String semestre, String anneeAcademique);
    List<Note> findByCoursId(Long coursId);
    Optional<Note> findByEtudiantIdAndCoursIdAndTypeEvaluationAndSemestreAndAnneeAcademique(
            Long etudiantId, Long coursId, String typeEvaluation, String semestre, String anneeAcademique
    );
}
