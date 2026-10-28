package com.mohammad.booking_service.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mohammad.booking_service.dto.EventSummaryDto;
import com.mohammad.booking_service.entity.Booking;
import com.mohammad.booking_service.entity.BookingItem;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class TicketPdfGenerator {

    // Standard Arial font definitions
    private static final Font TITLE_FONT = FontFactory.getFont("Arial", 18, Font.BOLD);
    private static final Font SECTION_FONT = FontFactory.getFont("Arial", 12, Font.BOLD);
    private static final Font BOLD_FONT = FontFactory.getFont("Arial", 10, Font.BOLD);
    private static final Font TEXT_FONT = FontFactory.getFont("Arial", 10, Font.NORMAL);

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' hh:mm a");

    public static byte[] generateTicketPdf(Booking booking, EventSummaryDto event, String attendeeEmail) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. Header & Booking Info
            Paragraph title = new Paragraph("TAZKARA EVENT TICKET", TITLE_FONT);
            title.setSpacingAfter(10);
            document.add(title);

            document.add(new Paragraph("Booking Reference: " + booking.getReference(), BOLD_FONT));
            document.add(new Paragraph("Status: " + booking.getStatus(), TEXT_FONT));
            document.add(new Paragraph("Attendee: " + (attendeeEmail != null ? attendeeEmail : "User #" + booking.getUserId()), TEXT_FONT));

            // 2. Event Details Section
            Paragraph eventHeading = new Paragraph("Event Details", SECTION_FONT);
            eventHeading.setSpacingBefore(15);
            eventHeading.setSpacingAfter(5);
            document.add(eventHeading);

            String eventTitle = (event != null && event.title() != null) ? event.title() : "Event #" + booking.getEventId();
            document.add(new Paragraph("Event: " + eventTitle, TEXT_FONT));

            if (event != null) {
                if (event.dateTime() != null) {
                    document.add(new Paragraph("Date & Time: " + event.dateTime().format(DATE_FORMATTER), TEXT_FONT));
                }
                String location = (event.venue() != null ? event.venue() : "TBA")
                        + (event.city() != null ? ", " + event.city() : "");
                document.add(new Paragraph("Location: " + location, TEXT_FONT));
            }

            // 3. Ticket Items Table
            Paragraph ticketsHeading = new Paragraph("Tickets", SECTION_FONT);
            ticketsHeading.setSpacingBefore(15);
            ticketsHeading.setSpacingAfter(5);
            document.add(ticketsHeading);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 1, 2, 2});

            // Table Header
            table.addCell(createCell("Ticket Type", BOLD_FONT, Element.ALIGN_LEFT));
            table.addCell(createCell("Qty", BOLD_FONT, Element.ALIGN_RIGHT));
            table.addCell(createCell("Unit Price", BOLD_FONT, Element.ALIGN_RIGHT));
            table.addCell(createCell("Total", BOLD_FONT, Element.ALIGN_RIGHT));

            // Table Rows
            if (booking.getItems() != null) {
                for (BookingItem item : booking.getItems()) {
                    BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                    BigDecimal total = price.multiply(BigDecimal.valueOf(item.getQuantity()));

                    table.addCell(createCell(item.getTicketTypeName(), TEXT_FONT, Element.ALIGN_LEFT));
                    table.addCell(createCell(String.valueOf(item.getQuantity()), TEXT_FONT, Element.ALIGN_RIGHT));
                    table.addCell(createCell("$" + price.toPlainString(), TEXT_FONT, Element.ALIGN_RIGHT));
                    table.addCell(createCell("$" + total.toPlainString(), TEXT_FONT, Element.ALIGN_RIGHT));
                }
            }

            // Total Amount Row
            BigDecimal grandTotal = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
            PdfPCell totalLabelCell = createCell("Total Amount", BOLD_FONT, Element.ALIGN_RIGHT);
            totalLabelCell.setColspan(3);
            table.addCell(totalLabelCell);
            table.addCell(createCell("$" + grandTotal.toPlainString(), BOLD_FONT, Element.ALIGN_RIGHT));

            document.add(table);

            // 4. Footer Note
            Paragraph footer = new Paragraph("Please present this ticket or booking reference at the entrance.", TEXT_FONT);
            footer.setSpacingBefore(15);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate ticket PDF: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private static PdfPCell createCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(6);
        return cell;
    }
}
