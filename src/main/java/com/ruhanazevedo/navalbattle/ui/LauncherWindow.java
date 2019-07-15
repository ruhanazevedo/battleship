package com.ruhanazevedo.navalbattle.ui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Launch screen shown when the app starts.
 *
 * Offers two ways to open a PDF:
 *   1. "Open PDF..." button → JFileChooser
 *   2. Drag-and-drop a PDF onto the window
 *
 * Remembers the last used directory across sessions via java.util.prefs.
 */
public class LauncherWindow extends JFrame {

    private static final String PREF_LAST_DIR = "lastDir";
    private final Preferences prefs = Preferences.userNodeForPackage(LauncherWindow.class);

    public LauncherWindow() {
        buildUI();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void buildUI() {
        setTitle("Battleship");
        setSize(420, 260);
        setResizable(false);
        setLocationRelativeTo(null);
        UIUtil.applyIcon(this);

        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBorder(BorderFactory.createEmptyBorder(36, 48, 32, 48));

        // Title block
        JLabel title    = new JLabel("Battleship", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));

        JLabel subtitle = new JLabel("PDF Coordinate Picker for iText Developers", SwingConstants.CENTER);
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subtitle.setForeground(UIManager.getColor("Label.disabledForeground"));

        JLabel hint = new JLabel("Drag a PDF here or click Open", SwingConstants.CENTER);
        hint.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        hint.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel header = new JPanel(new GridLayout(3, 1, 0, 6));
        header.setOpaque(false);
        header.add(title);
        header.add(subtitle);
        header.add(hint);

        // Open button
        JButton openBtn = new JButton("Open PDF\u2026");
        openBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        openBtn.setPreferredSize(new Dimension(160, 38));
        openBtn.addActionListener(e -> choosePdf());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(openBtn);

        content.add(header, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(content);

        // Drag-and-drop
        new DropTarget(content, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                handleDrop(dtde);
            }
        });
    }

    private void choosePdf() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select a PDF file");
        fc.setFileFilter(new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf"));

        String lastDir = prefs.get(PREF_LAST_DIR, null);
        if (lastDir != null) fc.setCurrentDirectory(new File(lastDir));

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File selected = fc.getSelectedFile();
        prefs.put(PREF_LAST_DIR, selected.getParent());
        openPdf(selected);
    }

    private void handleDrop(DropTargetDropEvent dtde) {
        try {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            @SuppressWarnings("unchecked")
            List<File> files = (List<File>) dtde.getTransferable()
                .getTransferData(DataFlavor.javaFileListFlavor);
            if (!files.isEmpty()) {
                File f = files.get(0);
                if (f.getName().toLowerCase().endsWith(".pdf")) {
                    openPdf(f);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Please drop a PDF file.", "Not a PDF", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Could not open dropped file:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openPdf(File file) {
        new ViewerWindow(file);
    }
}
