package com.ruhanazevedo.navalbattle.ui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/** Shared UI helpers. */
public final class UIUtil {

    private UIUtil() {}

    public static void applyIcon(JFrame frame) {
        URL url = UIUtil.class.getResource("/icon/boat.png");
        if (url != null) {
            frame.setIconImage(Toolkit.getDefaultToolkit().getImage(url));
        }
    }
}
