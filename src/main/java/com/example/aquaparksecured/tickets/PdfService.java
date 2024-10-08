package com.example.aquaparksecured.tickets;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
//import com.itextpdf.layout.property.HorizontalAlignment;
//import com.itextpdf.layout.property.VerticalAlignment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    public String generateTicketPdf(Ticket ticket) throws IOException {
        String directoryPath = "src/main/resources/tickets/";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String pdfPath = directoryPath + "ticket_" + ticket.getId() + ".pdf";

        try {
            PdfWriter writer = new PdfWriter(pdfPath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            String logoPath = "src/main/resources/logo/logo.png";
            Image logoImage = new Image(ImageDataFactory.create(logoPath));
            document.add(logoImage);

            document.add(new Paragraph("Ticket ID: " + ticket.getId()));
            document.add(new Paragraph("Email: " + ticket.getEmail()));
            document.add(new Paragraph("Type: " + ticket.getType()));
            document.add(new Paragraph(String.format("Price: %.2f", ticket.getPrice())));
            document.add(new Paragraph("Purchase Date: " + ticket.getPurchaseDate().format(formatter)));
            document.add(new Paragraph("Expiration Date: " + ticket.getExpirationDate().format(formatter)));
            document.add(new Paragraph("Adults: " + ticket.getAdults()));
            document.add(new Paragraph("Seniors: " + ticket.getSeniors()));

            String qrCodeData = "Ticket ID: " + ticket.getId() + "\n" +
                    "Email: " + ticket.getEmail() + "\n" +
                    "Type: " + ticket.getType() + "\n" +
                    "Price: " + ticket.getPrice() + "\n" +
                    "Purchase Date: " + ticket.getPurchaseDate().format(formatter) + "\n" +
                    "Expiration Date: " + ticket.getExpirationDate().format(formatter) + "\n" +
                    "Adults: " + ticket.getAdults() + "\n" +
                    "Seniors: " + ticket.getSeniors();

            File qrCodeFile = new File(directoryPath + "qr_" + ticket.getId() + ".png");
            generateQRCode(qrCodeData, qrCodeFile.getAbsolutePath(), 200, 200);
            Image qrCodeImage = new Image(ImageDataFactory.create(qrCodeFile.getAbsolutePath()));
            document.add(qrCodeImage);

            document.close();

            qrCodeFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Failed to generate PDF: " + e.getMessage());
        }

        return pdfPath;
    }

    private void generateQRCode(String qrCodeData, String filePath, int width, int height) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(qrCodeData, BarcodeFormat.QR_CODE, width, height);
            Path path = Paths.get(filePath);
            MatrixToImageWriter.writeToPath(matrix, "PNG", path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}