# Battleship

PDF coordinate picker for iText developers.

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java 11+](https://img.shields.io/badge/java-11%2B-orange.svg)](https://adoptium.net/)
[![Known Vulnerabilities](https://snyk.io/test/github/ruhanazevedo/battleship/badge.svg)](https://snyk.io/test/github/ruhanazevedo/battleship)
[![Release](https://img.shields.io/github/v/release/ruhanazevedo/battleship)](https://github.com/ruhanazevedo/battleship/releases)

---

## Why this exists

If you've worked with iText, you know the pain. You need to place a text element at a specific position on a PDF, so you open the file in a viewer, eyeball the spot, guess some coordinates, run the code, check the result, it's off, guess again, run again. Over and over.

The issue is that iText uses its own coordinate system: **(0, 0) starts at the bottom-left** of the page, measured in PDF points (1 pt = 1/72 inch). Standard PDF viewers don't show that. They show pixels, or percentages, or nothing at all. There's no quick way to just point at a spot and know where it is in iText terms.

I got tired of the guessing loop while working on a project that generated a lot of PDF documents with precisely placed elements. So I built Battleship: open your PDF, click the exact spot you want, and get the coordinate ready to paste.

---

## Features

| | |
|---|---|
| **Accurate coordinates** | Derived directly from the PDF page dimensions via PDFBox, no approximations or magic constants |
| **Drag and drop** | Drop a `.pdf` onto the launcher to open it |
| **Zoom** | Ctrl+scroll, Ctrl+= / Ctrl+- / Ctrl+0, or toolbar buttons (25% to 400%) |
| **Click marker** | Red crosshair stays on the last picked position so you don't lose track |
| **Live hover** | Status bar shows the iText coordinate under your cursor as you move |
| **Target history** | Every pick in the session is listed in a sidebar |
| **Export** | Copy as JSON, CSV, or ready-to-paste iText 7 Java code |
| **Right-click menu** | Copy selected picks from the sidebar as Java or JSON |
| **Keyboard navigation** | Left/right arrow keys to move between pages |
| **Remembers last folder** | File chooser reopens where you left off |

---

## Download

**[Download the latest release](https://github.com/ruhanazevedo/battleship/releases/latest)**

Requires Java 11 or later. Run with:

```bash
java -jar battleship.jar
```

---

## Build from source

```bash
git clone https://github.com/ruhanazevedo/battleship.git
cd battleship
mvn package -q
java -jar target/battleship.jar
```

Run tests:

```bash
mvn test
```

---

## How to use

**1. Open a PDF**

Click **Open PDF...** on the launcher, or drag and drop a file directly onto the window.

**2. Pick a coordinate**

Click anywhere on the rendered page. The status bar shows the iText coordinates for that spot:

```
Target acquired  ->  X: 72.5f,  Y: 144.0f  (page 1)
```

A red crosshair marks the position on the page.

**3. Copy or export**

- **Copy** copies the last pick as an iText 7 snippet:
  ```java
  element.setFixedPosition(1, 72.5f, 144.0f);
  ```
- **Export...** lets you export everything picked in the session:

  | Format | Output |
  |---|---|
  | JSON | `[{"page":1,"x":72.5,"y":144.0}, ...]` |
  | CSV | `page,x,y` rows |
  | Java | `element.setFixedPosition(page, x, y);` per pick |

- **Right-click** any item in the sidebar to copy individual picks or clear the list.

**Navigation and zoom**

| Action | Shortcut |
|---|---|
| Previous / next page | Left / Right arrow keys or toolbar |
| Zoom in / out | Ctrl+scroll or Ctrl+= / Ctrl+- |
| Reset zoom | Ctrl+0 |

---

## Coordinate system

iText measures from the bottom-left corner of the page. If you click near the top-left of an A4 page, Y will be close to 842 (the full page height), not close to 0.

```
(0, pageH) ────────────── (pageW, pageH)
     |                           |
     |      iText coordinates    |
     |      origin at bottom     |
     |                           |
   (0, 0) ──────────── (pageW, 0)
```

Common page sizes in PDF points:

| Format | Width | Height |
|---|---|---|
| A4 | 595 | 842 |
| US Letter | 612 | 792 |
| US Legal | 612 | 1008 |

---

## Stack

- Java 11
- Apache PDFBox 2 for rendering and page metadata
- FlatLaf for the dark UI
- Jackson for JSON export
- JUnit 5 for coordinate math and export tests

---

## Contributing

Bug reports and pull requests are welcome.

1. Fork the repo
2. Create a branch (`git checkout -b feat/my-feature`)
3. Commit your changes
4. Open a pull request

Run `mvn test` before submitting.

---

## License

[MIT](LICENSE)

---

[Ruhan Azevedo](https://github.com/ruhanazevedo)
