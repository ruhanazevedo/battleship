package com.ruhanazevedo.navalbattle;

import com.formdev.flatlaf.FlatDarkLaf;
import com.ruhanazevedo.navalbattle.ui.LauncherWindow;

import javax.swing.*;

/**
 * Naval Battle — PDF Coordinate Picker
 *
 * Entry point. Bootstraps FlatLaf and opens the launcher.
 *
 * @author Ruhan Azevedo
 */
public class App {

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(LauncherWindow::new);
    }
}
