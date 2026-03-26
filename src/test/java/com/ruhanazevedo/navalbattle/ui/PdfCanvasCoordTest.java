package com.ruhanazevedo.navalbattle.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests coordinate conversion in PdfCanvas.
 *
 * A standard A4 page is 595.28 x 841.89 PDF points.
 * Rendered at 144 DPI: imageW = 595.28 * 2 = 1190.56 px ≈ 1191 px
 *                      imageH = 841.89 * 2 = 1683.78 px ≈ 1684 px
 * (PDFBox rounds to int pixels)
 *
 * We use a synthetic 1190 x 1684 image to keep the test self-contained.
 */
class PdfCanvasCoordTest {

    // A4 in PDF points
    private static final float PAGE_W = 595.28f;
    private static final float PAGE_H = 841.89f;

    // Matching rendered image dimensions (144 DPI / 72 = factor 2)
    private static final int IMG_W = 1190;
    private static final int IMG_H = 1684;

    private PdfCanvas canvas;

    @BeforeEach
    void setup() {
        canvas = new PdfCanvas() {
            // Override getWidth/getHeight so the panel is exactly the image size
            @Override public int getWidth()  { return IMG_W; }
            @Override public int getHeight() { return IMG_H; }
        };
        BufferedImage img = new BufferedImage(IMG_W, IMG_H, BufferedImage.TYPE_INT_RGB);
        canvas.setPage(img, PAGE_W, PAGE_H);
    }

    @Test
    void topLeftCorner() {
        // Screen (0,0) → iText (0, pageH)
        float[] pts = canvas.screenToPoints(0, 0);
        assertNotNull(pts);
        assertEquals(0f,    pts[0], 1f);
        assertEquals(PAGE_H, pts[1], 1f);
    }

    @Test
    void bottomLeftCorner() {
        // Screen (0, imgH) → iText (0, 0)
        float[] pts = canvas.screenToPoints(0, IMG_H);
        assertNotNull(pts);
        assertEquals(0f, pts[0], 1f);
        assertEquals(0f, pts[1], 1f);
    }

    @Test
    void bottomRightCorner() {
        // Screen (imgW, imgH) → iText (pageW, 0)
        float[] pts = canvas.screenToPoints(IMG_W, IMG_H);
        assertNotNull(pts);
        assertEquals(PAGE_W, pts[0], 1f);
        assertEquals(0f,     pts[1], 1f);
    }

    @Test
    void topRightCorner() {
        // Screen (imgW, 0) → iText (pageW, pageH)
        float[] pts = canvas.screenToPoints(IMG_W, 0);
        assertNotNull(pts);
        assertEquals(PAGE_W, pts[0], 1f);
        assertEquals(PAGE_H, pts[1], 1f);
    }

    @Test
    void centre() {
        // Screen (imgW/2, imgH/2) → iText (pageW/2, pageH/2)
        float[] pts = canvas.screenToPoints(IMG_W / 2, IMG_H / 2);
        assertNotNull(pts);
        assertEquals(PAGE_W / 2, pts[0], 2f);
        assertEquals(PAGE_H / 2, pts[1], 2f);
    }

    @Test
    void outsideReturnsNull() {
        assertNull(canvas.screenToPoints(-1, 100));
        assertNull(canvas.screenToPoints(IMG_W + 1, 100));
        assertNull(canvas.screenToPoints(100, -1));
        assertNull(canvas.screenToPoints(100, IMG_H + 1));
    }

    @Test
    void zoomDoesNotAffectResult() {
        // At zoom 2.0, the canvas panel would need to be 2x larger.
        // We test via a 2x-sized panel.
        PdfCanvas zoomed = new PdfCanvas() {
            @Override public int getWidth()  { return IMG_W * 2; }
            @Override public int getHeight() { return IMG_H * 2; }
        };
        BufferedImage img = new BufferedImage(IMG_W, IMG_H, BufferedImage.TYPE_INT_RGB);
        zoomed.setPage(img, PAGE_W, PAGE_H);
        zoomed.setZoom(2.0f);

        // Centre of the zoomed view → should still be centre of the PDF
        float[] pts = zoomed.screenToPoints(IMG_W, IMG_H); // centre of 2x panel
        assertNotNull(pts);
        assertEquals(PAGE_W / 2, pts[0], 2f);
        assertEquals(PAGE_H / 2, pts[1], 2f);
    }
}
