package com.universite.service;

import com.universite.dto.BulletinResponse;
import com.universite.dto.BulletinSummaryResponse;
import com.universite.dto.NoteResponse;
import com.universite.dto.NoteSaisieRequest;

import java.util.List;

public interface NoteService {
    NoteResponse saisirOuModifier(NoteSaisieRequest request);
    List<NoteResponse> listerNotesEtudiant(Long etudiantId);
    List<NoteResponse> listerMesNotes(String emailUtilisateurConnecte);
    BulletinResponse publierBulletin(Long etudiantId, String semestre, String anneeAcademique);
    BulletinResponse consulterBulletinPublie(Long etudiantId, String semestre, String anneeAcademique);
    BulletinResponse consulterMonBulletinPublie(String emailUtilisateurConnecte, String semestre, String anneeAcademique);
    List<BulletinSummaryResponse> listerMesBulletins(String emailUtilisateurConnecte);
}
