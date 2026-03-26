package com.ruhanazevedo.navalbattle.model;

/**
 * A single coordinate pick: iText PDF point coordinates on a given page.
 *
 * iText coordinate system has (0,0) at the bottom-left of the page.
 * Units are PDF points (1 pt = 1/72 inch).
 */
public class CoordinatePick {

    private final int page;       // 1-based page number
    private final float x;        // iText X in PDF points
    private final float y;        // iText Y in PDF points

    public CoordinatePick(int page, float x, float y) {
        this.page = page;
        this.x    = x;
        this.y    = y;
    }

    public int   getPage() { return page; }
    public float getX()    { return x; }
    public float getY()    { return y; }

    /** Human-readable label shown in the history list. */
    @Override
    public String toString() {
        return String.format("p%d  X: %.1f  Y: %.1f", page, x, y);
    }

    /** iText 7 Java snippet for setFixedPosition. */
    public String toJavaSnippet() {
        return String.format(
            "element.setFixedPosition(%d, %.1ff, %.1ff);",
            page, x, y
        );
    }

    /** iText 5 style (legacy). */
    public String toJavaSnippetItextFive() {
        return String.format(
            "// iText 5: new ColumnText(writer).setSimpleColumn(%.1ff, %.1ff, ...)",
            x, y
        );
    }

    /** CSV row: page,x,y */
    public String toCsvRow() {
        return String.format("%d,%.2f,%.2f", page, x, y);
    }
}
