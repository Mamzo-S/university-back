package com.universite.export;

import com.universite.entity.Etudiant;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class ExcelExporter {

    public static ByteArrayInputStream exportEtudiants(
            List<Etudiant> etudiants
    ) {

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out =
                     new ByteArrayOutputStream()) {

            Sheet sheet =
                    workbook.createSheet("Etudiants");

            // =========================
            // HEADER
            // =========================

            Row headerRow = sheet.createRow(0);

            headerRow.createCell(0)
                    .setCellValue("INE");

            headerRow.createCell(1)
                    .setCellValue("Nom");

            headerRow.createCell(2)
                    .setCellValue("Prenom");

            headerRow.createCell(3)
                    .setCellValue("Genre");

            headerRow.createCell(4)
                    .setCellValue("Formation");

            headerRow.createCell(5)
                    .setCellValue("Année Début");

            headerRow.createCell(6)
                    .setCellValue("Année Sortie");

            // =========================
            // DATA
            // =========================

            int rowIdx = 1;

            for (Etudiant etudiant : etudiants) {

                Row row = sheet.createRow(rowIdx++);

                row.createCell(0)
                        .setCellValue(etudiant.getIne());

                row.createCell(1)
                        .setCellValue(etudiant.getNom());

                row.createCell(2)
                        .setCellValue(etudiant.getPrenom());

                row.createCell(3)
                        .setCellValue(etudiant.getGenre());

                row.createCell(4)
                        .setCellValue(
                                etudiant.getFormation() != null
                                        ? etudiant.getFormation().getNom()
                                        : ""
                        );

                row.createCell(5)
                        .setCellValue(
                                etudiant.getAnneeDebut()
                        );

                row.createCell(6)
                        .setCellValue(
                                etudiant.getAnneeSortie()
                        );
            }

            workbook.write(out);

            return new ByteArrayInputStream(
                    out.toByteArray()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Erreur export Excel"
            );
        }
    }
}