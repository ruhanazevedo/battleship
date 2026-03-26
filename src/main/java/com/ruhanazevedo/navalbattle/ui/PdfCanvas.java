package com.ruhanazevedo.navalbattle.ui;

import com.ruhanazevedo.navalbattle.model.CoordinatePick;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Custom panel that displays a rendered PDF page image and handles:
 *
 *   - Zoom (Ctrl+scroll, Ctrl+= / Ctrl+-)
 *   - Crosshair cursor
 *   - Click marker (red crosshair drawn at the last clicked point)
 *   - Live coordinate tracking on mouse move
 *   - Correct iText coordinate conversion using actual page dimensions
 *
 * Coordinate conversion (no magic constants):
 *
 *   The rendered BufferedImage has dimensions:
 *     imageW = pageWidthPts  * (RENDER_DPI / 72)
 *     imageH = pageHeightPts * (RENDER_DPI / 72)
 *
 *   At zoom level Z, the displayed image occupies imageW*Z x imageH*Z pixels.
 *
 *   For a click at (screenX, screenY) relative to this panel:
 *     normalizedX = (screenX - offsetX) / (imageW * Z)
 *     normalizedY = (screenY - offsetY) / (imageH * Z)
 *
 *     ptX = normalizedX * pageWidthPts
 *     ptY = (1 - normalizedY) * pageHeightPts    ← iText Y is bottom-up
 */
public class PdfCanvas extends JPanel {

    // Zoom limits
    private static final float ZOOM_MIN  = 0.25f;
    private static final float ZOOM_MAX  = 4.0f;
    private static final float ZOOM_STEP = 0.15f;

    // Marker appearance
    private static final Color MARKER_COLOR     = new Color(220, 50, 50, 200);
    private static final int   MARKER_ARM       = 14;  // half-length of crosshair arms
    private static final int   MARKER_GAP       = 4;   // gap around centre

    private BufferedImage image;
    private float pageWidthPts;
    private float pageHeightPts;

    private float zoom = 1.0f;

    /** Last picked iText coordinate (null if none yet). */
    private CoordinatePick lastPick;

    /** Screen position of the last click (in panel coordinates), for drawing the marker. */
    private Point markerScreenPos;

    /** Listener called when the user picks a coordinate or hovers. */
    public interface PickListener {
        void onPick(CoordinatePick pick);
        void onClickOutside();
        void onHover(float ptX, float ptY);
        void onHoverOutside();
    }

    private PickListener pickListener;

    public PdfCanvas() {
        setBackground(new Color(40, 40, 40));
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                handleHover(e.getX(), e.getY());
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);

        // Zoom on Ctrl+scroll
        addMouseWheelListener(e -> {
            if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                float delta = e.getWheelRotation() > 0 ? -ZOOM_STEP : ZOOM_STEP;
                setZoom(zoom + delta);
            } else {
                // Pass through to parent scroll pane
                getParent().dispatchEvent(e);
            }
        });

        // Keyboard zoom bindings — registered on this panel
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                    if (e.getKeyCode() == KeyEvent.VK_EQUALS || e.getKeyCode() == KeyEvent.VK_PLUS) {
                        setZoom(zoom + ZOOM_STEP);
                    } else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
                        setZoom(zoom - ZOOM_STEP);
                    } else if (e.getKeyCode() == KeyEvent.VK_0) {
                        setZoom(1.0f);
                    }
                }
            }
        });
    }

    public void setPickListener(PickListener listener) {
        this.pickListener = listener;
    }

    /** Load a new page image and its PDF dimensions. Resets the marker. */
    public void setPage(BufferedImage image, float pageWidthPts, float pageHeightPts) {
        this.image         = image;
        this.pageWidthPts  = pageWidthPts;
        this.pageHeightPts = pageHeightPts;
        this.lastPick       = null;
        this.markerScreenPos = null;
        revalidate();
        repaint();
    }

    public float getZoom() { return zoom; }

    public void setZoom(float z) {
        zoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, z));
        revalidate();
        repaint();
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    @Override
    public Dimension getPreferredSize() {
        if (image == null) return new Dimension(600, 800);
        return new Dimension(
            (int) (image.getWidth()  * zoom),
            (int) (image.getHeight() * zoom)
        );
    }

    // ── Painting ──────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int dispW = (int) (image.getWidth()  * zoom);
        int dispH = (int) (image.getHeight() * zoom);
        int offX  = Math.max(0, (getWidth()  - dispW) / 2);
        int offY  = Math.max(0, (getHeight() - dispH) / 2);

        // Draw page
        g2.drawImage(image, offX, offY, dispW, dispH, null);

        // Draw click marker
        if (markerScreenPos != null) {
            drawMarker(g2, markerScreenPos.x, markerScreenPos.y);
        }

        g2.dispose();
    }

    private void drawMarker(Graphics2D g2, int cx, int cy) {
        g2.setColor(MARKER_COLOR);
        g2.setStroke(new BasicStroke(1.5f));

        // Horizontal arms
        g2.drawLine(cx - MARKER_ARM, cy, cx - MARKER_GAP, cy);
        g2.drawLine(cx + MARKER_GAP, cy, cx + MARKER_ARM, cy);
        // Vertical arms
        g2.drawLine(cx, cy - MARKER_ARM, cx, cy - MARKER_GAP);
        g2.drawLine(cx, cy + MARKER_GAP, cx, cy + MARKER_ARM);

        // Small circle at centre
        g2.drawOval(cx - 3, cy - 3, 6, 6);
    }

    // ── Interaction ───────────────────────────────────────────────────────────

    private void handleClick(int screenX, int screenY) {
        float[] pts = screenToPoints(screenX, screenY);
        if (pts == null) {
            if (pickListener != null) pickListener.onClickOutside();
            return;
        }

        markerScreenPos = new Point(screenX, screenY);
        repaint();

        CoordinatePick pick = new CoordinatePick(0 /* set by caller */, pts[0], pts[1]);
        lastPick = pick;

        if (pickListener != null) pickListener.onPick(pick);
    }

    private void handleHover(int screenX, int screenY) {
        if (pickListener == null || image == null) return;
        float[] pts = screenToPoints(screenX, screenY);
        if (pts == null) {
            pickListener.onHoverOutside();
        } else {
            pickListener.onHover(pts[0], pts[1]);
        }
    }

    /**
     * Convert a panel-relative screen position to iText PDF points.
     * Returns null if the position is outside the page area.
     */
    float[] screenToPoints(int screenX, int screenY) {
        if (image == null) return null;

        int dispW = (int) (image.getWidth()  * zoom);
        int dispH = (int) (image.getHeight() * zoom);
        int offX  = Math.max(0, (getWidth()  - dispW) / 2);
        int offY  = Math.max(0, (getHeight() - dispH) / 2);

        float relX = screenX - offX;
        float relY = screenY - offY;

        if (relX < 0 || relX > dispW || relY < 0 || relY > dispH) return null;

        float ptX = (relX / dispW) * pageWidthPts;
        float ptY = (1f - relY / dispH) * pageHeightPts;

        return new float[]{ ptX, ptY };
    }

    public CoordinatePick getLastPick() { return lastPick; }
}
