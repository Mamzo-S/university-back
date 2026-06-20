package com.universite.export;

import com.universite.entity.Etudiant;
import com.universite.entity.Utilisateur;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class ExcelExporter {

    public static ByteArrayInputStream exportEtudiants(List<Etudiant> etudiants) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Etudiants");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("INE");
            headerRow.createCell(1).setCellValue("Nom");
            headerRow.createCell(2).setCellValue("Prenom");
            headerRow.createCell(3).setCellValue("Email");
            headerRow.createCell(4).setCellValue("Promotion");
            headerRow.createCell(5).setCellValue("Formation");
            headerRow.createCell(6).setCellValue("Année Entrée");
            headerRow.createCell(7).setCellValue("Année Sortie");

            int rowIdx = 1;

            for (Etudiant etudiant : etudiants) {
                Row row = sheet.createRow(rowIdx++);
                Utilisateur utilisateur = etudiant.getUtilisateur();

                row.createCell(0).setCellValue(etudiant.getIne());
                row.createCell(1).setCellValue(utilisateur != null ? utilisateur.getNom() : "");
                row.createCell(2).setCellValue(utilisateur != null ? utilisateur.getPrenom() : "");
                row.createCell(3).setCellValue(utilisateur != null ? utilisateur.getEmail() : "");
                row.createCell(4).setCellValue(
                        etudiant.getPromotion() != null ? etudiant.getPromotion().getNom() : ""
                );
                row.createCell(5).setCellValue(
                        etudiant.getPromotion() != null
                                && etudiant.getPromotion().getFormation() != null
                                ? etudiant.getPromotion().getFormation().getNom()
                                : ""
                );
                row.createCell(6).setCellValue(
                        etudiant.getAnneeEntree() != null ? etudiant.getAnneeEntree() : 0
                );
                row.createCell(7).setCellValue(
                        etudiant.getAnneeSortie() != null ? etudiant.getAnneeSortie() : 0
                );
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Erreur export Excel");
        }
    }
}
