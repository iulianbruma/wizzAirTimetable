package com.iulian.wizzair;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream("WizzAirFlights.pdf"));

        document.open();

        Paragraph titleParagraph = new Paragraph();
        Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
        Chunk chunk = new Chunk("Wizz Air Flights", font);
        titleParagraph.setAlignment(Element.ALIGN_LEFT);
        titleParagraph.add(chunk);

        Paragraph logoParagraph = new Paragraph();
        Path path = Paths.get("wizz-logo.png");
        Image img = Image.getInstance(path.toAbsolutePath().toString());
        img.scalePercent(10);

        logoParagraph.setAlignment(Element.ALIGN_RIGHT);
        logoParagraph.add(img);
        document.add(chunk);
        document.add(img);

        Paragraph paragraph = new Paragraph();
        addEmptyLine(paragraph, 3);
        document.add(paragraph);

        PdfPTable table = new PdfPTable(5);
        addTableHeader(table);
        addRows(table);
        addCustomRows(table);

        document.add(table);
        document.close();
    }

    private static void addTableHeader(PdfPTable table) {
        Stream.of("Departure", "Arrival", "Departure Date", "Day", "Price")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
    }

    private static void addRows(PdfPTable table) {
        table.addCell("row 1, col 1");
        table.addCell("row 1, col 2");
        table.addCell("row 1, col 3");
    }

    private static void addCustomRows(PdfPTable table) throws URISyntaxException, BadElementException, IOException {
        Path path = Paths.get("wizz-logo.png");
        Image img = Image.getInstance(path.toAbsolutePath().toString());
        img.scalePercent(10);

        PdfPCell imageCell = new PdfPCell(img);
        table.addCell(imageCell);

        PdfPCell horizontalAlignCell = new PdfPCell(new Phrase("row 2, col 2"));
        horizontalAlignCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(horizontalAlignCell);

        PdfPCell verticalAlignCell = new PdfPCell(new Phrase("row 2, col 3"));
        verticalAlignCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        table.addCell(verticalAlignCell);
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}
