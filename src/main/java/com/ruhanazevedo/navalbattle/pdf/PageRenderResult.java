package com.ruhanazevedo.navalbattle.pdf;

import java.awt.image.BufferedImage;

/**
 * Result of rendering one PDF page.
 *
 * Carries both the display-ready image and the PDF page dimensions in points,
 * which are needed for accurate coordinate conversion.
 */
public class PageRenderResult {

    /** Rendered image at display scale. */
    public final BufferedImage image;

    /** Total pages in the document. */
    public final int totalPages;

    /** Page width in PDF points (from PDFBox PDPage). */
    public final float pageWidthPts;

    /** Page height in PDF points (from PDFBox PDPage). */
    public final float pageHeightPts;

    public PageRenderResult(
            BufferedImage image,
            int totalPages,
            float pageWidthPts,
            float pageHeightPts) {
        this.image        = image;
        this.totalPages   = totalPages;
        this.pageWidthPts = pageWidthPts;
        this.pageHeightPts = pageHeightPts;
    }
}
