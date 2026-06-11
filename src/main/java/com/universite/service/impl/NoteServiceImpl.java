package com.universite.service.impl;

import com.universite.dto.BulletinResponse;
import com.universite.dto.NoteResponse;
import com.universite.dto.NoteSaisieRequest;
import com.universite.entity.Cours;
import com.universite.entity.Etudiant;
import com.universite.entity.Note;
import com.universite.entity.Utilisateur;
import com.universite.repository.CoursRepository;
import com.universite.repository.EtudiantRepository;
import com.universite.repository.NoteRepository;
import com.universite.repository.UtilisateurRepository;
import com.universite.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final EtudiantRepository etudiantRepository;
    private final CoursRepository coursRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public NoteResponse saisirOuModifier(NoteSaisieRequest request) {
        Etudiant etudiant = etudiantRepository.findById(request.getEtudiantId())
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));

        Cours cours = coursRepository.findById(request.getCoursId())
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        Note note = noteRepository.findByEtudiantIdAndCoursIdAndTypeEvaluationAndSemestreAndAnneeAcademique(
                        request.getEtudiantId(),
                        request.getCoursId(),
                        request.getTypeEvaluation(),
                        request.getSemestre(),
                        request.getAnneeAcademique()
                )
                .orElse(Note.builder()
                        .etudiant(etudiant)
                        .cours(cours)
                        .typeEvaluation(request.getTypeEvaluation())
                        .semestre(request.getSemestre())
                        .anneeAcademique(request.getAnneeAcademique())
                        .bulletinPublie(false)
                        .build());

        note.setValeur(request.getValeur());
        note.setEtudiant(etudiant);
        note.setCours(cours);

        noteRepository.save(note);
        return mapToResponse(note);
    }

    @Override
    public List<NoteResponse> listerNotesEtudiant(Long etudiantId) {
        return noteRepository.findByEtudiantId(etudiantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<NoteResponse> listerMesNotes(String emailUtilisateurConnecte) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(emailUtilisateurConnecte)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        List<Etudiant> correspondances = etudiantRepository.findByNomAndPrenom(
                utilisateur.getNom(),
                utilisateur.getPrenom()
        );

        if (correspondances.isEmpty()) {
            throw new RuntimeException("Aucun profil étudiant associé à cet utilisateur");
        }

        Long etudiantId = correspondances.get(0).getId();
        return listerNotesEtudiant(etudiantId);
    }

    @Override
    public BulletinResponse publierBulletin(Long etudiantId, String semestre, String anneeAcademique) {
        List<Note> notes = noteRepository.findByEtudiantIdAndSemestreAndAnneeAcademique(
                etudiantId, semestre, anneeAcademique
        );

        if (notes.isEmpty()) {
            throw new RuntimeException("Aucune note trouvée pour ce bulletin");
        }

        notes.forEach(n -> {
            n.setBulletinPublie(true);
            n.setDatePublication(LocalDate.now());
        });

        noteRepository.saveAll(notes);
        return construireBulletin(notes);
    }

    @Override
    public BulletinResponse consulterBulletinPublie(Long etudiantId, String semestre, String anneeAcademique) {
        List<Note> notes = noteRepository.findByEtudiantIdAndSemestreAndAnneeAcademiqueAndBulletinPublieTrue(
                etudiantId, semestre, anneeAcademique
        );

        if (notes.isEmpty()) {
            throw new RuntimeException("Aucun bulletin publié trouvé");
        }

        return construireBulletin(notes);
    }

    @Override
    public BulletinResponse consulterMonBulletinPublie(String emailUtilisateurConnecte, String semestre, String anneeAcademique) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(emailUtilisateurConnecte)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        List<Etudiant> correspondances = etudiantRepository.findByNomAndPrenom(
                utilisateur.getNom(),
                utilisateur.getPrenom()
        );

        if (correspondances.isEmpty()) {
            throw new RuntimeException("Aucun profil étudiant associé à cet utilisateur");
        }

        return consulterBulletinPublie(correspondances.get(0).getId(), semestre, anneeAcademique);
    }

    private BulletinResponse construireBulletin(List<Note> notes) {
        Etudiant etudiant = notes.get(0).getEtudiant();

        double somme = 0.0;
        double sommeCoef = 0.0;
        for (Note note : notes) {
            Double coef = note.getCours().getCoefficient() == null ? 1.0 : note.getCours().getCoefficient();
            somme += note.getValeur() * coef;
            sommeCoef += coef;
        }
        double moyenne = sommeCoef == 0 ? 0 : somme / sommeCoef;

        return BulletinResponse.builder()
                .etudiantId(etudiant.getId())
                .etudiantNomComplet(etudiant.getPrenom() + " " + etudiant.getNom())
                .semestre(notes.get(0).getSemestre())
                .anneeAcademique(notes.get(0).getAnneeAcademique())
                .moyenneGenerale(Math.round(moyenne * 100.0) / 100.0)
                .notes(notes.stream().map(this::mapToResponse).toList())
                .build();
    }

    private NoteResponse mapToResponse(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .etudiantId(note.getEtudiant().getId())
                .etudiantNomComplet(note.getEtudiant().getPrenom() + " " + note.getEtudiant().getNom())
                .coursId(note.getCours().getId())
                .coursNom(note.getCours().getNom())
                .typeEvaluation(note.getTypeEvaluation())
                .valeur(note.getValeur())
                .anneeAcademique(note.getAnneeAcademique())
                .semestre(note.getSemestre())
                .bulletinPublie(note.getBulletinPublie())
                .build();
    }
}
