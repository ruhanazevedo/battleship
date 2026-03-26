package com.ruhanazevedo.navalbattle.export;

import com.ruhanazevedo.navalbattle.model.CoordinatePick;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportServiceTest {

    private static final List<CoordinatePick> PICKS = List.of(
        new CoordinatePick(1, 72.5f,  144.0f),
        new CoordinatePick(2, 100.0f, 200.5f)
    );

    @Test
    void csvHasHeaderAndTwoRows() {
        String csv = ExportService.toCsv(PICKS);
        String[] lines = csv.split("\n");
        assertEquals("page,x,y", lines[0]);
        assertEquals(3, lines.length); // header + 2 rows
        assertTrue(lines[1].startsWith("1,72.50,144.00"));
        assertTrue(lines[2].startsWith("2,100.00,200.50"));
    }

    @Test
    void javaSnippetsContainSetFixedPosition() {
        String java = ExportService.toJavaSnippets(PICKS);
        assertTrue(java.contains("setFixedPosition(1, 72.5f, 144.0f)"));
        assertTrue(java.contains("setFixedPosition(2, 100.0f, 200.5f)"));
    }

    @Test
    void jsonContainsBothPicks() throws Exception {
        String json = ExportService.toJson(PICKS);
        assertTrue(json.contains("\"page\" : 1"));
        assertTrue(json.contains("\"x\" : 72.5"));
        assertTrue(json.contains("\"page\" : 2"));
        assertTrue(json.contains("\"x\" : 100.0"));
    }

    @Test
    void emptyListProducesEmptyJson() throws Exception {
        String json = ExportService.toJson(List.of());
        assertEquals("[ ]", json.trim());
    }

    @Test
    void emptyListProducesHeaderOnlyCsv() {
        String csv = ExportService.toCsv(List.of());
        assertEquals("page,x,y\n", csv);
    }
}
