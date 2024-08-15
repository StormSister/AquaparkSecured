package com.example.aquaparksecured.reservation;

import com.example.aquaparksecured.user.AppUser;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class ReservationPdfService {

    public String generateReservationPdf(Reservation reservation) throws IOException {

        String directoryPath = "src/main/resources/reservations/";

        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String pdfPath = directoryPath + "reservation_" + reservation.getId() + ".pdf";

        try {
            PdfWriter writer = new PdfWriter(pdfPath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Use DateTimeFormatter to format dates without time
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            String logoPath = "src/main/resources/logo/logo.png";  // Set the path to the logo
            Image logoImage = new Image(ImageDataFactory.create(logoPath));
            document.add(logoImage);

            AppUser user = reservation.getUser();
            document.add(new Paragraph("Reservation ID: " + reservation.getId()));
            document.add(new Paragraph("Client Name: " + user.getFirstName() + " " + user.getLastName()));
            document.add(new Paragraph("Client Email: " + user.getEmail()));
            document.add(new Paragraph("Phone Number: " + user.getPhoneNumber()));
            document.add(new Paragraph("Room: " + reservation.getRoom().getType()));
            document.add(new Paragraph("Start Date: " + reservation.getStartDate().format(dateFormatter) + " (Check-in at 13:00)"));
            document.add(new Paragraph("End Date: " + reservation.getEndDate().format(dateFormatter) + " (Check-out at 11:00)"));

            document.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Failed to generate PDF: " + e.getMessage());
        }

        return pdfPath;
    }
}
