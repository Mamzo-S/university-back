package com.universite.controller;

import com.universite.dto.BulletinResponse;
import com.universite.dto.NoteResponse;
import com.universite.dto.NoteSaisieRequest;
import com.universite.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESSEUR')")
    public NoteResponse saisirOuModifier(@RequestBody NoteSaisieRequest request) {
        return noteService.saisirOuModifier(request);
    }

    @GetMapping("/etudiant/{etudiantId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESSEUR')")
    public List<NoteResponse> listerNotesEtudiant(@PathVariable Long etudiantId) {
        return noteService.listerNotesEtudiant(etudiantId);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ETUDIANT')")
    public List<NoteResponse> listerMesNotes(Authentication authentication) {
        return noteService.listerMesNotes(authentication.getName());
    }

    @PostMapping("/bulletins/publier")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESSEUR')")
    public BulletinResponse publierBulletin(
            @RequestParam Long etudiantId,
            @RequestParam String semestre,
            @RequestParam String anneeAcademique
    ) {
        return noteService.publierBulletin(etudiantId, semestre, anneeAcademique);
    }

    @GetMapping("/bulletins")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESSEUR')")
    public BulletinResponse consulterBulletinPublie(
            @RequestParam Long etudiantId,
            @RequestParam String semestre,
            @RequestParam String anneeAcademique
    ) {
        return noteService.consulterBulletinPublie(etudiantId, semestre, anneeAcademique);
    }

    @GetMapping("/bulletins/me")
    @PreAuthorize("hasAuthority('ETUDIANT')")
    public BulletinResponse consulterMonBulletinPublie(
            Authentication authentication,
            @RequestParam String semestre,
            @RequestParam String anneeAcademique
    ) {
        return noteService.consulterMonBulletinPublie(authentication.getName(), semestre, anneeAcademique);
    }
}
