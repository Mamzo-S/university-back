package com.universite.service.impl;

import com.universite.dto.BulletinResponse;
import com.universite.dto.BulletinSummaryResponse;
import com.universite.dto.CoursBulletinLine;
import com.universite.dto.NoteResponse;
import com.universite.dto.NoteSaisieRequest;
import com.universite.entity.Cours;
import com.universite.entity.Etudiant;
import com.universite.entity.Note;
import com.universite.repository.CoursRepository;
import com.universite.repository.EtudiantRepository;
import com.universite.repository.NoteRepository;
import com.universite.repository.UtilisateurRepository;
import com.universite.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final EtudiantRepository etudiantRepository;
    private final CoursRepository coursRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional
    public NoteResponse saisirOuModifier(NoteSaisieRequest request) {
        validateNoteRequest(request);

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
        note.setBulletinPublie(false);
        note.setDatePublication(null);

        return mapToResponse(noteRepository.save(note));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> listerNotesEtudiant(Long etudiantId) {
        return noteRepository.findByEtudiantId(etudiantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> listerMesNotes(String emailUtilisateurConnecte) {
        Etudiant etudiant = resolveEtudiantConnecte(emailUtilisateurConnecte);
        return noteRepository.findByEtudiantIdAndBulletinPublieTrue(etudiant.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public BulletinResponse publierBulletin(Long etudiantId, String semestre, String anneeAcademique) {
        List<Note> notes = noteRepository.findByEtudiantIdAndSemestreAndAnneeAcademique(
                etudiantId, semestre, anneeAcademique
        );

        if (notes.isEmpty()) {
            throw new RuntimeException("Aucune note trouvée pour ce bulletin");
        }

        LocalDate today = LocalDate.now();
        notes.forEach(note -> {
            note.setBulletinPublie(true);
            note.setDatePublication(today);
        });

        noteRepository.saveAll(notes);
        return construireBulletin(notes);
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public BulletinResponse consulterMonBulletinPublie(
            String emailUtilisateurConnecte,
            String semestre,
            String anneeAcademique
    ) {
        Etudiant etudiant = resolveEtudiantConnecte(emailUtilisateurConnecte);
        return consulterBulletinPublie(etudiant.getId(), semestre, anneeAcademique);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BulletinSummaryResponse> listerMesBulletins(String emailUtilisateurConnecte) {
        Etudiant etudiant = resolveEtudiantConnecte(emailUtilisateurConnecte);
        List<Note> notes = noteRepository.findByEtudiantIdAndBulletinPublieTrue(etudiant.getId());

        Map<String, List<Note>> grouped = notes.stream()
                .collect(Collectors.groupingBy(
                        note -> note.getAnneeAcademique() + "::" + note.getSemestre(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<BulletinSummaryResponse> summaries = new ArrayList<>();
        for (List<Note> bulletinNotes : grouped.values()) {
            BulletinResponse bulletin = construireBulletin(bulletinNotes);
            summaries.add(BulletinSummaryResponse.builder()
                    .semestre(bulletin.getSemestre())
                    .anneeAcademique(bulletin.getAnneeAcademique())
                    .moyenneGenerale(bulletin.getMoyenneGenerale())
                    .mention(bulletin.getMention())
                    .datePublication(bulletin.getDatePublication())
                    .nombreNotes(bulletin.getNotes().size())
                    .build());
        }

        summaries.sort(Comparator
                .comparing(BulletinSummaryResponse::getAnneeAcademique, Comparator.nullsLast(String::compareTo))
                .thenComparing(BulletinSummaryResponse::getSemestre, Comparator.nullsLast(String::compareTo))
                .reversed());

        return summaries;
    }

    private Etudiant resolveEtudiantConnecte(String emailUtilisateurConnecte) {
        if (!utilisateurRepository.findByEmail(emailUtilisateurConnecte).isPresent()) {
            throw new RuntimeException("Utilisateur introuvable");
        }

        return etudiantRepository.findByUtilisateur_Email(emailUtilisateurConnecte)
                .orElseThrow(() -> new RuntimeException("Aucun profil étudiant associé à cet utilisateur"));
    }

    private BulletinResponse construireBulletin(List<Note> notes) {
        Etudiant etudiant = notes.get(0).getEtudiant();
        List<CoursBulletinLine> lignesCours = buildCoursLines(notes);
        double moyenne = computeMoyenneFromLines(lignesCours);

        LocalDate datePublication = notes.stream()
                .map(Note::getDatePublication)
                .filter(date -> date != null)
                .max(LocalDate::compareTo)
                .orElse(null);

        return BulletinResponse.builder()
                .etudiantId(etudiant.getId())
                .etudiantNomComplet(formatNomComplet(etudiant))
                .etudiantIne(etudiant.getIne())
                .filiereNom(etudiant.getFiliere() != null ? etudiant.getFiliere().getNom() : null)
                .semestre(notes.get(0).getSemestre())
                .anneeAcademique(notes.get(0).getAnneeAcademique())
                .moyenneGenerale(round(moyenne))
                .mention(resolveMention(moyenne))
                .datePublication(datePublication != null ? datePublication.toString() : null)
                .lignesCours(lignesCours)
                .notes(notes.stream().map(this::mapToResponse).toList())
                .build();
    }

    private List<CoursBulletinLine> buildCoursLines(List<Note> notes) {
        Map<Long, List<Note>> byCours = notes.stream()
                .collect(Collectors.groupingBy(note -> note.getCours().getId(), LinkedHashMap::new, Collectors.toList()));

        return byCours.values().stream()
                .map(coursNotes -> {
                    Cours cours = coursNotes.get(0).getCours();
                    double moyenneCours = coursNotes.stream()
                            .mapToDouble(Note::getValeur)
                            .average()
                            .orElse(0.0);

                    return CoursBulletinLine.builder()
                            .coursId(cours.getId())
                            .coursCode(cours.getCode())
                            .coursNom(cours.getNom())
                            .coefficient(cours.getCoefficient())
                            .moyenneCours(round(moyenneCours))
                            .build();
                })
                .sorted(Comparator.comparing(CoursBulletinLine::getCoursNom, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    private double computeMoyenneFromLines(List<CoursBulletinLine> lignesCours) {
        double somme = 0.0;
        double sommeCoef = 0.0;

        for (CoursBulletinLine ligne : lignesCours) {
            double coef = ligne.getCoefficient() == null ? 1.0 : ligne.getCoefficient();
            double moyenneCours = ligne.getMoyenneCours() == null ? 0.0 : ligne.getMoyenneCours();
            somme += moyenneCours * coef;
            sommeCoef += coef;
        }

        return sommeCoef == 0 ? 0 : somme / sommeCoef;
    }

    private String resolveMention(double moyenne) {
        if (moyenne >= 16) {
            return "Très Bien";
        }
        if (moyenne >= 14) {
            return "Bien";
        }
        if (moyenne >= 12) {
            return "Assez Bien";
        }
        if (moyenne >= 10) {
            return "Passable";
        }
        return "Ajourné";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private void validateNoteRequest(NoteSaisieRequest request) {
        if (request.getValeur() == null || request.getValeur() < 0 || request.getValeur() > 20) {
            throw new RuntimeException("La note doit être comprise entre 0 et 20");
        }
        if (request.getTypeEvaluation() == null || request.getTypeEvaluation().isBlank()) {
            throw new RuntimeException("Le type d'évaluation est obligatoire");
        }
        if (request.getSemestre() == null || request.getSemestre().isBlank()) {
            throw new RuntimeException("Le semestre est obligatoire");
        }
        if (request.getAnneeAcademique() == null || request.getAnneeAcademique().isBlank()) {
            throw new RuntimeException("L'année académique est obligatoire");
        }
    }

    private NoteResponse mapToResponse(Note note) {
        Cours cours = note.getCours();
        return NoteResponse.builder()
                .id(note.getId())
                .etudiantId(note.getEtudiant().getId())
                .etudiantNomComplet(formatNomComplet(note.getEtudiant()))
                .coursId(cours.getId())
                .coursCode(cours.getCode())
                .coursNom(cours.getNom())
                .coursCoefficient(cours.getCoefficient())
                .typeEvaluation(note.getTypeEvaluation())
                .valeur(note.getValeur())
                .anneeAcademique(note.getAnneeAcademique())
                .semestre(note.getSemestre())
                .bulletinPublie(note.getBulletinPublie())
                .build();
    }

    private String formatNomComplet(Etudiant etudiant) {
        if (etudiant.getUtilisateur() == null) {
            return "";
        }
        return etudiant.getUtilisateur().getPrenom() + " " + etudiant.getUtilisateur().getNom();
    }
}
