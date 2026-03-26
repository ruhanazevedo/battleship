package com.ruhanazevedo.navalbattle.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Renders a single PDF page to a BufferedImage.
 *
 * Rendering is done at a configurable DPI. The caller chooses whether to
 * scale the result for display — PdfRenderer only renders, it does not
 * scale. This keeps the rendering pipeline simple and testable.
 *
 * Coordinate conversion formula (no magic constants):
 *
 *   ptX = (clickX / renderedImageWidth)  * pageWidthPts
 *   ptY = ((renderedImageHeight - clickY) / renderedImageHeight) * pageHeightPts
 *
 * This is exact because PDFBox renders at exactly [dpi/72] pixels per point.
 */
public class PdfRenderer {

    /** DPI used for rendering. Higher = sharper image, more memory. */
    public static final float RENDER_DPI = 144f;

    private PdfRenderer() {}

    /**
     * Render page {@code pageIndex} (0-based) of the given PDF file.
     *
     * @throws IOException          if the file cannot be read or rendered
     * @throws IndexOutOfBoundsException if pageIndex is out of range
     */
    public static PageRenderResult render(File pdfFile, int pageIndex) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            int totalPages = document.getNumberOfPages();
            if (pageIndex < 0 || pageIndex >= totalPages) {
                throw new IndexOutOfBoundsException(
                    "Page index " + pageIndex + " is out of range (0–" + (totalPages - 1) + ")");
            }

            PDPage page = document.getPage(pageIndex);
            PDRectangle mediaBox = page.getMediaBox();

            float pageWidthPts  = mediaBox.getWidth();
            float pageHeightPts = mediaBox.getHeight();

            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image  = renderer.renderImageWithDPI(pageIndex, RENDER_DPI, ImageType.RGB);

            return new PageRenderResult(image, totalPages, pageWidthPts, pageHeightPts);
        }
    }
}
