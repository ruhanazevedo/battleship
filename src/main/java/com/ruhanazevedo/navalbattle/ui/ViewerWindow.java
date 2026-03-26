package com.ruhanazevedo.navalbattle.ui;

import com.ruhanazevedo.navalbattle.export.ExportService;
import com.ruhanazevedo.navalbattle.model.CoordinatePick;
import com.ruhanazevedo.navalbattle.pdf.PageRenderResult;
import com.ruhanazevedo.navalbattle.pdf.PdfRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Main PDF viewer window.
 *
 * Layout:
 *   NORTH   — toolbar (prev/next, page label, zoom controls)
 *   CENTER  — PdfCanvas inside JScrollPane (left) + history panel (right)
 *   SOUTH   — status bar (live coords on hover / last pick + copy button)
 *
 * Key bindings:
 *   Left / Right arrows  — navigate pages
 *   Ctrl+scroll          — zoom (handled by PdfCanvas)
 *   Ctrl+=, Ctrl+-       — zoom in/out
 *   Ctrl+0               — reset zoom
 */
public class ViewerWindow extends JFrame {

    private final File pdfFile;

    // State
    private int pageIndex  = 0;
    private int totalPages = 0;
    private float pageWidthPts;
    private float pageHeightPts;

    // Coordinate history for this session
    private final List<CoordinatePick> history = new ArrayList<>();
    private final DefaultListModel<CoordinatePick> historyModel = new DefaultListModel<>();

    // UI components
    private JButton  prevBtn;
    private JButton  nextBtn;
    private JLabel   pageLabel;
    private JButton  zoomInBtn;
    private JButton  zoomOutBtn;
    private JLabel   zoomLabel;
    private JLabel   statusLabel;
    private JButton  copyBtn;
    private JButton  exportBtn;
    private PdfCanvas canvas;
    private JScrollPane scrollPane;

    public ViewerWindow(File pdfFile) {
        this.pdfFile = pdfFile;
        buildUI();
        registerKeyBindings();
        loadPage(false);
        setVisible(true);
    }

    // ── Build UI ──────────────────────────────────────────────────────────────

    private void buildUI() {
        setTitle("Naval Battle \u2014 " + pdfFile.getName());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        UIUtil.applyIcon(this);

        // ── Toolbar ───────────────────────────────────────────────────────────
        prevBtn = new JButton("\u2190 Prev");
        prevBtn.setEnabled(false);
        prevBtn.addActionListener(e -> navigate(-1));

        nextBtn = new JButton("Next \u2192");
        nextBtn.setEnabled(false);
        nextBtn.addActionListener(e -> navigate(1));

        pageLabel = new JLabel("Loading\u2026", SwingConstants.CENTER);
        pageLabel.setPreferredSize(new Dimension(120, 24));

        zoomOutBtn = new JButton("\u2212");
        zoomOutBtn.setToolTipText("Zoom out (Ctrl+\u2212)");
        zoomOutBtn.addActionListener(e -> changeZoom(-0.15f));

        zoomInBtn = new JButton("+");
        zoomInBtn.setToolTipText("Zoom in (Ctrl+=)");
        zoomInBtn.addActionListener(e -> changeZoom(+0.15f));

        zoomLabel = new JLabel("100%", SwingConstants.CENTER);
        zoomLabel.setPreferredSize(new Dimension(52, 24));
        zoomLabel.setToolTipText("Current zoom (Ctrl+0 to reset)");

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(new EmptyBorder(6, 10, 6, 10));
        toolbar.add(prevBtn);
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(pageLabel);
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(nextBtn);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.addSeparator();
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(zoomOutBtn);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(zoomLabel);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(zoomInBtn);

        // ── Canvas ────────────────────────────────────────────────────────────
        canvas = new PdfCanvas();
        canvas.setPickListener(new PdfCanvas.PickListener() {
            @Override
            public void onPick(CoordinatePick pick) {
                // Attach real page number (canvas doesn't know it)
                CoordinatePick real = new CoordinatePick(pageIndex + 1, pick.getX(), pick.getY());
                onCoordinatePicked(real);
            }

            @Override
            public void onHover(float ptX, float ptY) {
                statusLabel.setText(String.format(
                    "Cursor \u2014 X: %.1f  Y: %.1f  \u2014  page %d",
                    ptX, ptY, pageIndex + 1));
                statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
            }

            @Override
            public void onHoverOutside() {
                if (history.isEmpty()) {
                    statusLabel.setText("Click anywhere on the PDF to get iText coordinates");
                    statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
                }
            }
        });

        scrollPane = new JScrollPane(canvas);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(40, 40, 40));
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);

        // ── History sidebar ───────────────────────────────────────────────────
        JList<CoordinatePick> historyList = new JList<>(historyModel);
        historyList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        historyList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane historySP = new JScrollPane(historyList);
        historySP.setBorder(BorderFactory.createTitledBorder("History"));
        historySP.setPreferredSize(new Dimension(200, 0));

        // Right-click context menu on history list
        JPopupMenu histCtx = new JPopupMenu();
        JMenuItem copyJava = new JMenuItem("Copy as Java snippet");
        JMenuItem copyJson = new JMenuItem("Copy as JSON");
        JMenuItem clearAll = new JMenuItem("Clear history");
        histCtx.add(copyJava);
        histCtx.add(copyJson);
        histCtx.addSeparator();
        histCtx.add(clearAll);

        copyJava.addActionListener(e -> {
            List<CoordinatePick> sel = historyList.getSelectedValuesList();
            if (!sel.isEmpty()) copyToClipboard(ExportService.toJavaSnippets(sel));
        });
        copyJson.addActionListener(e -> {
            List<CoordinatePick> sel = historyList.getSelectedValuesList();
            if (!sel.isEmpty()) {
                try { copyToClipboard(ExportService.toJson(sel)); }
                catch (Exception ex) { showError("Export failed: " + ex.getMessage()); }
            }
        });
        clearAll.addActionListener(e -> {
            history.clear();
            historyModel.clear();
            exportBtn.setEnabled(false);
            copyBtn.setEnabled(false);
            statusLabel.setText("Click anywhere on the PDF to get iText coordinates");
            statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        });

        historyList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) histCtx.show(historyList, e.getX(), e.getY());
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) histCtx.show(historyList, e.getX(), e.getY());
            }
        });

        // ── Split: canvas + history ────────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, historySP);
        split.setResizeWeight(1.0);
        split.setDividerSize(5);
        split.setContinuousLayout(true);

        // ── Status bar ────────────────────────────────────────────────────────
        statusLabel = new JLabel("Click anywhere on the PDF to get iText coordinates");
        statusLabel.setBorder(new EmptyBorder(6, 12, 6, 8));
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        copyBtn = new JButton("Copy");
        copyBtn.setEnabled(false);
        copyBtn.setToolTipText("Copy last coordinate to clipboard");
        copyBtn.addActionListener(e -> {
            if (!history.isEmpty()) {
                CoordinatePick last = history.get(history.size() - 1);
                copyToClipboard(last.toJavaSnippet());
            }
        });

        exportBtn = new JButton("Export\u2026");
        exportBtn.setEnabled(false);
        exportBtn.setToolTipText("Export all picks as JSON, CSV, or Java code");
        exportBtn.addActionListener(e -> showExportDialog());

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
            UIManager.getColor("Separator.foreground")));
        statusBar.add(statusLabel, BorderLayout.CENTER);

        JPanel statusButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        statusButtons.setOpaque(false);
        statusButtons.add(copyBtn);
        statusButtons.add(exportBtn);
        statusBar.add(statusButtons, BorderLayout.EAST);

        // ── Window layout ─────────────────────────────────────────────────────
        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
        add(split,   BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (screen.width * 0.72), (int) (screen.height * 0.84));
        setLocationRelativeTo(null);
    }

    // ── Key bindings ──────────────────────────────────────────────────────────

    private void registerKeyBindings() {
        JRootPane root = getRootPane();
        InputMap  im   = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am   = root.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,  0), "prevPage");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "nextPage");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK), "zoomIn");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,  InputEvent.CTRL_DOWN_MASK), "zoomOut");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_0,      InputEvent.CTRL_DOWN_MASK), "zoomReset");

        am.put("prevPage",  new AbstractAction() {
            public void actionPerformed(ActionEvent e) { if (pageIndex > 0) navigate(-1); }
        });
        am.put("nextPage",  new AbstractAction() {
            public void actionPerformed(ActionEvent e) { if (pageIndex < totalPages - 1) navigate(1); }
        });
        am.put("zoomIn",    new AbstractAction() {
            public void actionPerformed(ActionEvent e) { changeZoom(+0.15f); }
        });
        am.put("zoomOut",   new AbstractAction() {
            public void actionPerformed(ActionEvent e) { changeZoom(-0.15f); }
        });
        am.put("zoomReset", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { setZoom(1.0f); }
        });
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void navigate(int delta) {
        pageIndex += delta;
        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        pageLabel.setText("Loading\u2026");
        loadPage(true);
    }

    private void loadPage(boolean scrollToTop) {
        new SwingWorker<PageRenderResult, Void>() {
            @Override
            protected PageRenderResult doInBackground() throws Exception {
                return PdfRenderer.render(pdfFile, pageIndex);
            }

            @Override
            protected void done() {
                try {
                    PageRenderResult result = get();
                    totalPages    = result.totalPages;
                    pageWidthPts  = result.pageWidthPts;
                    pageHeightPts = result.pageHeightPts;

                    canvas.setPage(result.image, pageWidthPts, pageHeightPts);
                    updateNavigation();

                    if (scrollToTop) {
                        SwingUtilities.invokeLater(() ->
                            scrollPane.getViewport().setViewPosition(new Point(0, 0)));
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    showError("Failed to render PDF:\n" +
                        (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
                }
            }
        }.execute();
    }

    private void updateNavigation() {
        prevBtn.setEnabled(pageIndex > 0);
        nextBtn.setEnabled(pageIndex < totalPages - 1);
        pageLabel.setText("Page " + (pageIndex + 1) + " / " + totalPages);
    }

    // ── Zoom ──────────────────────────────────────────────────────────────────

    private void changeZoom(float delta) {
        setZoom(canvas.getZoom() + delta);
    }

    private void setZoom(float z) {
        canvas.setZoom(z);
        zoomLabel.setText(Math.round(canvas.getZoom() * 100) + "%");
    }

    // ── Coordinate pick ───────────────────────────────────────────────────────

    private void onCoordinatePicked(CoordinatePick pick) {
        history.add(pick);
        historyModel.addElement(pick);

        statusLabel.setText(String.format(
            "X: %.1ff,  Y: %.1ff  \u2014  page %d  (iText coordinates)",
            pick.getX(), pick.getY(), pick.getPage()));
        statusLabel.setForeground(UIManager.getColor("Label.foreground"));

        copyBtn.setEnabled(true);
        exportBtn.setEnabled(true);
    }

    // ── Export dialog ─────────────────────────────────────────────────────────

    private void showExportDialog() {
        if (history.isEmpty()) return;

        String[] options = {"JSON", "CSV", "Java snippet", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Export " + history.size() + " coordinate pick(s) as:",
            "Export Coordinates",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null, options, options[0]);

        if (choice == 3 || choice < 0) return;

        String output;
        String ext;
        try {
            switch (choice) {
                case 0: output = ExportService.toJson(history);         ext = "json"; break;
                case 1: output = ExportService.toCsv(history);          ext = "csv";  break;
                case 2: output = ExportService.toJavaSnippets(history); ext = "java"; break;
                default: return;
            }
        } catch (Exception ex) {
            showError("Export failed: " + ex.getMessage());
            return;
        }

        // Show in text dialog with copy button
        JTextArea ta = new JTextArea(output);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ta.setEditable(false);
        ta.setRows(Math.min(history.size() + 3, 20));
        ta.setColumns(60);

        JScrollPane sp = new JScrollPane(ta);

        int copy = JOptionPane.showConfirmDialog(
            this, sp, "Export — " + ext.toUpperCase(),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (copy == JOptionPane.OK_OPTION) {
            copyToClipboard(output);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard()
            .setContents(new StringSelection(text), null);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
