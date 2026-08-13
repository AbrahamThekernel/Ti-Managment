package utng.gtid.jjcm.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.ReportData;

/**
 * Escritor PDF pequeño construido únicamente con clases del JDK.
 *
 * <p>Utiliza Helvetica, que es una de las fuentes estándar de PDF. El contenido
 * se codifica como Windows-1252 para conservar los acentos del español. No se
 * necesita iText, PDFBox ni conexión a Internet.</p>
 */
public final class PdfReportWriter {

    /** Codificación compatible con /WinAnsiEncoding dentro del documento PDF. */
    private static final Charset PDF_TEXT_CHARSET = Charset.forName("windows-1252");

    /** Formato español utilizado para el periodo visible. */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ROOT);

    /** Tamaño horizontal A4 expresado en puntos PDF. */
    private static final int PAGE_WIDTH = 842;

    /** Tamaño vertical A4 expresado en puntos PDF. */
    private static final int PAGE_HEIGHT = 595;

    /** Márgenes laterales usados por encabezados y tabla. */
    private static final int MARGIN = 36;

    /** Espacio vertical reservado por cada fila. */
    private static final int ROW_HEIGHT = 18;

    /** Número máximo de registros que caben en una página. */
    private static final int ROWS_PER_PAGE = 23;

    /** Constructor privado porque todos los métodos son utilidades estáticas. */
    private PdfReportWriter() {
    }

    /**
     * Genera un PDF completo y lo guarda en la ruta indicada.
     */
    public static void write(
            ReportData report,
            LocalDate start,
            LocalDate end,
            Path destination
    ) {
        try {
            Files.write(destination, createBytes(report, start, end));
        } catch (IOException error) {
            throw new DatabaseException("No se pudo escribir el archivo PDF.", error);
        }
    }

    /**
     * Devuelve el PDF en memoria; este método permite agregarlo directamente a
     * un ZIP sin crear archivos temporales.
     */
    public static byte[] createBytes(ReportData report, LocalDate start, LocalDate end) {
        List<List<String>> rows = report.getRows();
        int pageCount = Math.max(1, (rows.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
        List<byte[]> pageStreams = new ArrayList<>();

        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            int firstRow = pageIndex * ROWS_PER_PAGE;
            int lastRow = Math.min(rows.size(), firstRow + ROWS_PER_PAGE);
            List<List<String>> pageRows = rows.subList(firstRow, lastRow);
            pageStreams.add(createPageContent(
                    report,
                    pageRows,
                    start,
                    end,
                    pageIndex + 1,
                    pageCount
            ));
        }

        return assemblePdf(pageStreams);
    }

    /**
     * Construye las instrucciones gráficas de una página: títulos, tabla y pie.
     */
    private static byte[] createPageContent(
            ReportData report,
            List<List<String>> rows,
            LocalDate start,
            LocalDate end,
            int pageNumber,
            int pageCount
    ) {
        ByteArrayOutputStream content = new ByteArrayOutputStream();

        // Encabezado institucional sin colores, siguiendo el diseño monocromático.
        addText(content, "UTNG - Gestión Institucional", MARGIN, PAGE_HEIGHT - 38, 16);
        addText(content, report.getTitle(), MARGIN, PAGE_HEIGHT - 58, 12);
        addText(
                content,
                "Periodo: " + start.format(DATE_FORMAT) + " al " + end.format(DATE_FORMAT),
                MARGIN,
                PAGE_HEIGHT - 75,
                9
        );
        addText(
                content,
                "Registros totales: " + report.getRows().size(),
                PAGE_WIDTH - 175,
                PAGE_HEIGHT - 75,
                9
        );

        int columnCount = Math.max(1, report.getColumns().size());
        double tableWidth = PAGE_WIDTH - (MARGIN * 2.0);
        double columnWidth = tableWidth / columnCount;
        double tableTop = PAGE_HEIGHT - 98;

        // El encabezado se distingue con un fondo gris y texto negro.
        addRectangle(content, MARGIN, tableTop - ROW_HEIGHT, tableWidth, ROW_HEIGHT, 0.90);
        addHorizontalLine(content, MARGIN, PAGE_WIDTH - MARGIN, tableTop, 0.65);
        addHorizontalLine(content, MARGIN, PAGE_WIDTH - MARGIN,
                tableTop - ROW_HEIGHT, 0.65);

        for (int column = 0; column < columnCount; column++) {
            double x = MARGIN + (column * columnWidth);
            addVerticalLine(
                    content,
                    x,
                    tableTop,
                    tableTop - ROW_HEIGHT - (rows.size() * ROW_HEIGHT),
                    0.75
            );
            String heading = column < report.getColumns().size()
                    ? report.getColumns().get(column)
                    : "";
            addText(
                    content,
                    abbreviate(heading.toUpperCase(Locale.ROOT), columnWidth, 7),
                    x + 4,
                    tableTop - 12,
                    7
            );
        }
        addVerticalLine(
                content,
                PAGE_WIDTH - MARGIN,
                tableTop,
                tableTop - ROW_HEIGHT - (rows.size() * ROW_HEIGHT),
                0.75
        );

        // Cada registro se dibuja en su celda y se acorta cuando excede el ancho.
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            List<String> row = rows.get(rowIndex);
            double rowTop = tableTop - ROW_HEIGHT - (rowIndex * ROW_HEIGHT);
            double textY = rowTop - 12;
            for (int column = 0; column < columnCount; column++) {
                String value = column < row.size() ? row.get(column) : "";
                double x = MARGIN + (column * columnWidth) + 4;
                addText(content, abbreviate(value, columnWidth, 7), x, textY, 7);
            }
            addHorizontalLine(
                    content,
                    MARGIN,
                    PAGE_WIDTH - MARGIN,
                    rowTop - ROW_HEIGHT,
                    0.82
            );
        }

        if (report.getRows().isEmpty()) {
            addText(
                    content,
                    "No existen registros para el periodo seleccionado.",
                    MARGIN + 8,
                    tableTop - 48,
                    10
            );
        }

        // Pie de página con número actual y total.
        addText(
                content,
                "Página " + pageNumber + " de " + pageCount,
                PAGE_WIDTH - 105,
                24,
                8
        );
        addText(content, "Documento generado por Gestión Institucional UTNG", MARGIN, 24, 8);
        return content.toByteArray();
    }

    /** Agrega una instrucción de texto con fuente Helvetica. */
    private static void addText(
            ByteArrayOutputStream content,
            String text,
            double x,
            double y,
            int fontSize
    ) {
        writeAscii(content, "BT /F1 " + fontSize + " Tf " + decimal(x) + " "
                + decimal(y) + " Td (");
        writePdfText(content, sanitize(text));
        writeAscii(content, ") Tj ET\n");
    }

    /** Dibuja una franja rectangular usando una escala de grises de 0 a 1. */
    private static void addRectangle(
            ByteArrayOutputStream content,
            double x,
            double y,
            double width,
            double height,
            double gray
    ) {
        writeAscii(content, decimal(gray) + " g " + decimal(x) + " " + decimal(y)
                + " " + decimal(width) + " " + decimal(height) + " re f 0 g\n");
    }

    /** Dibuja una línea horizontal delgada. */
    private static void addHorizontalLine(
            ByteArrayOutputStream content,
            double startX,
            double endX,
            double y,
            double gray
    ) {
        writeAscii(content, decimal(gray) + " G 0.5 w " + decimal(startX) + " "
                + decimal(y) + " m " + decimal(endX) + " " + decimal(y)
                + " l S 0 G\n");
    }

    /** Dibuja una línea vertical delgada. */
    private static void addVerticalLine(
            ByteArrayOutputStream content,
            double x,
            double startY,
            double endY,
            double gray
    ) {
        writeAscii(content, decimal(gray) + " G 0.5 w " + decimal(x) + " "
                + decimal(startY) + " m " + decimal(x) + " " + decimal(endY)
                + " l S 0 G\n");
    }

    /**
     * Arma catálogo, árbol de páginas, fuente, streams y tabla xref del PDF.
     */
    private static byte[] assemblePdf(List<byte[]> pageStreams) {
        int pageCount = pageStreams.size();
        int objectCount = 3 + (pageCount * 2);
        byte[][] objects = new byte[objectCount + 1][];

        // Objeto 1: catálogo raíz del documento.
        objects[1] = ascii("<< /Type /Catalog /Pages 2 0 R >>");

        // Objeto 2: lista de todas las páginas.
        StringBuilder kids = new StringBuilder();
        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            int pageObject = 4 + (pageIndex * 2);
            kids.append(pageObject).append(" 0 R ");
        }
        objects[2] = ascii("<< /Type /Pages /Kids [" + kids + "] /Count "
                + pageCount + " >>");

        // Objeto 3: fuente estándar con codificación para español.
        objects[3] = ascii(
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica "
                + "/Encoding /WinAnsiEncoding >>"
        );

        // Cada página tiene un objeto Page y otro objeto Content.
        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            int pageObject = 4 + (pageIndex * 2);
            int contentObject = pageObject + 1;
            byte[] stream = pageStreams.get(pageIndex);

            objects[pageObject] = ascii(
                    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 "
                    + PAGE_WIDTH + " " + PAGE_HEIGHT + "] "
                    + "/Resources << /Font << /F1 3 0 R >> >> "
                    + "/Contents " + contentObject + " 0 R >>"
            );
            objects[contentObject] = join(
                    ascii("<< /Length " + stream.length + " >>\nstream\n"),
                    stream,
                    ascii("\nendstream")
            );
        }

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        writeAscii(pdf, "%PDF-1.4\n");
        // Cuatro bytes binarios identifican el documento como archivo PDF binario.
        pdf.write(0x25);
        pdf.write(0xE2);
        pdf.write(0xE3);
        pdf.write(0xCF);
        pdf.write(0xD3);
        pdf.write('\n');

        long[] offsets = new long[objectCount + 1];
        for (int object = 1; object <= objectCount; object++) {
            offsets[object] = pdf.size();
            writeAscii(pdf, object + " 0 obj\n");
            writeBytes(pdf, objects[object]);
            writeAscii(pdf, "\nendobj\n");
        }

        long crossReferenceOffset = pdf.size();
        writeAscii(pdf, "xref\n0 " + (objectCount + 1) + "\n");
        writeAscii(pdf, "0000000000 65535 f \n");
        for (int object = 1; object <= objectCount; object++) {
            writeAscii(pdf, String.format(Locale.ROOT, "%010d 00000 n \n", offsets[object]));
        }
        writeAscii(pdf, "trailer\n<< /Size " + (objectCount + 1)
                + " /Root 1 0 R >>\nstartxref\n" + crossReferenceOffset
                + "\n%%EOF\n");
        return pdf.toByteArray();
    }

    /** Acorta el texto según el espacio aproximado disponible. */
    private static String abbreviate(String value, double columnWidth, int fontSize) {
        String normalized = sanitize(value);
        int maximumCharacters = Math.max(4, (int) ((columnWidth - 8) / (fontSize * 0.52)));
        if (normalized.length() <= maximumCharacters) {
            return normalized;
        }
        return normalized.substring(0, maximumCharacters - 3) + "...";
    }

    /** Sustituye saltos de línea para mantener cada registro dentro de una fila. */
    private static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ').trim();
    }

    /** Escribe texto PDF escapando paréntesis y diagonales inversas. */
    private static void writePdfText(ByteArrayOutputStream output, String value) {
        byte[] bytes = value.getBytes(PDF_TEXT_CHARSET);
        for (byte rawByte : bytes) {
            int unsignedByte = rawByte & 0xFF;
            if (unsignedByte == '(' || unsignedByte == ')' || unsignedByte == '\\') {
                output.write('\\');
            }
            output.write(unsignedByte);
        }
    }

    /** Convierte números a una representación corta válida para PDF. */
    private static String decimal(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    /** Convierte comandos estructurales a ASCII. */
    private static byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }

    /** Escribe un comando ASCII sin propagar IOException de memoria. */
    private static void writeAscii(ByteArrayOutputStream output, String value) {
        writeBytes(output, ascii(value));
    }

    /** Escribe bytes en memoria. */
    private static void writeBytes(ByteArrayOutputStream output, byte[] bytes) {
        output.write(bytes, 0, bytes.length);
    }

    /** Une varias secciones binarias en un solo objeto PDF. */
    private static byte[] join(byte[]... sections) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (byte[] section : sections) {
            writeBytes(output, section);
        }
        return output.toByteArray();
    }
}
