package com.ruhanazevedo.navalbattle.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruhanazevedo.navalbattle.model.CoordinatePick;

import java.util.List;

/**
 * Converts a list of CoordinatePick objects into various export formats.
 *
 * All methods are pure (no I/O) — callers decide where to write the output.
 */
public class ExportService {

    private static final ObjectMapper JSON = new ObjectMapper();

    private ExportService() {}

    // ── JSON ──────────────────────────────────────────────────────────────────

    /**
     * Produces a JSON array:
     * [{"page":1,"x":72.5,"y":144.0}, ...]
     */
    public static String toJson(List<CoordinatePick> picks) throws Exception {
        ArrayNode array = JSON.createArrayNode();
        for (CoordinatePick p : picks) {
            ObjectNode node = JSON.createObjectNode();
            node.put("page", p.getPage());
            node.put("x",    p.getX());
            node.put("y",    p.getY());
            array.add(node);
        }
        return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(array);
    }

    // ── CSV ───────────────────────────────────────────────────────────────────

    /**
     * Produces a CSV string with header row.
     *
     * page,x,y
     * 1,72.50,144.00
     */
    public static String toCsv(List<CoordinatePick> picks) {
        StringBuilder sb = new StringBuilder("page,x,y\n");
        for (CoordinatePick p : picks) {
            sb.append(p.toCsvRow()).append('\n');
        }
        return sb.toString();
    }

    // ── Java snippet ──────────────────────────────────────────────────────────

    /**
     * Produces iText 7 Java code snippets, one per pick.
     *
     * // pick 1
     * element.setFixedPosition(1, 72.5f, 144.0f);
     */
    public static String toJavaSnippets(List<CoordinatePick> picks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < picks.size(); i++) {
            CoordinatePick p = picks.get(i);
            sb.append("// pick ").append(i + 1).append('\n');
            sb.append(p.toJavaSnippet()).append('\n');
            if (i < picks.size() - 1) sb.append('\n');
        }
        return sb.toString();
    }
}
