# Naval Battle

> **PDF coordinate picker for iText developers.**
> Click anywhere on a PDF — get the exact iText `X, Y` coordinates instantly.

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java 11+](https://img.shields.io/badge/java-11%2B-orange.svg)](https://adoptium.net/)
[![Known Vulnerabilities](https://snyk.io/test/github/ruhanazevedo/naval-battle/badge.svg)](https://snyk.io/test/github/ruhanazevedo/naval-battle)
[![Release](https://img.shields.io/github/v/release/ruhanazevedo/naval-battle)](https://github.com/ruhanazevedo/naval-battle/releases)

---

## The problem

iText places elements using a coordinate system with **(0, 0) at the bottom-left** of the page, measured in **PDF points** (1 pt = 1/72 inch). Getting those coordinates has always been tedious — standard PDF viewers show pixel positions that mean nothing to iText, so developers resort to trial-and-error or spreadsheet math.

**Naval Battle solves this:** open any PDF, click the exact spot, and get ready-to-paste iText coordinates.

---

## Features

| | |
|---|---|
| **Accurate coordinates** | Derived from actual PDF page dimensions via PDFBox — no approximations, no magic constants |
| **Drag-and-drop** | Drop a `.pdf` onto the launcher to open it immediately |
| **Zoom** | Ctrl+scroll, Ctrl+=/−/0, or toolbar buttons (25%–400%) |
| **Click marker** | Red crosshair persists at the last picked position on the PDF |
| **Live hover tracking** | Status bar updates iText coordinates as you move the cursor |
| **Coordinate history** | Every pick in the session is listed in a collapsible sidebar |
| **Export** | JSON, CSV, or ready-to-paste iText 7 Java code per pick |
| **Right-click history** | Copy selected picks as Java snippet or JSON; clear all |
| **Keyboard navigation** | ← → arrow keys to move between pages |
| **Remembers last directory** | File chooser reopens where you left off |

---

## Download

**[→ Download the latest release JAR](https://github.com/ruhanazevedo/naval-battle/releases/latest)**

Requires Java 11 or later. Double-click the JAR to launch, or:

```bash
java -jar naval-battle.jar
```

---

## Build from source

```bash
git clone https://github.com/ruhanazevedo/naval-battle.git
cd naval-battle
mvn package -q
java -jar target/naval-battle.jar
```

Run tests:

```bash
mvn test
```

---

## Usage

### 1 — Open a PDF
Click **Open PDF…** or drag-and-drop a `.pdf` file onto the launcher.

### 2 — Pick a coordinate
Click anywhere on the rendered page. The status bar shows:

```
X: 72.5f,  Y: 144.0f  —  page 1  (iText coordinates)
```

A red crosshair marks the picked position on the page.

### 3 — Copy or export

- **Copy** — copies the last pick as an iText 7 snippet:
  ```java
  element.setFixedPosition(1, 72.5f, 144.0f);
  ```
- **Export…** — export all picks from the session:

  | Format | Output |
  |---|---|
  | JSON | `[{"page":1,"x":72.5,"y":144.0}, ...]` |
  | CSV | `page,x,y` rows |
  | Java | `element.setFixedPosition(page, x, y);` per pick |

- **Right-click** any item in the history sidebar for per-pick copy options.

### Navigation & zoom

| Action | How |
|---|---|
| Previous / next page | ← → arrow keys, or toolbar buttons |
| Zoom in / out | Ctrl+scroll, Ctrl+= / Ctrl+− |
| Reset zoom | Ctrl+0 |

---

## Coordinate system

iText measures from the **bottom-left corner** of the page.

```
(0, pageH) ────────────────── (pageW, pageH)
     │                               │
     │       iText coordinate        │
     │           space               │
     │                               │
   (0, 0) ──────────────── (pageW, 0)
```

Common page sizes in PDF points:

| Format | Width (pt) | Height (pt) |
|---|---|---|
| A4 | 595 | 842 |
| US Letter | 612 | 792 |
| US Legal | 612 | 1008 |

---

## Tech stack

- **Java 11** — cross-platform desktop
- **Apache PDFBox 2** — PDF rendering and page metadata
- **FlatLaf** — modern dark Swing look-and-feel
- **Jackson** — JSON export
- **JUnit 5** — coordinate math and export tests

---

## Contributing

Bug reports and pull requests are welcome.

1. Fork the repository
2. Create a feature branch (`git checkout -b feat/my-feature`)
3. Commit your changes
4. Open a pull request

Please make sure `mvn test` passes before submitting.

---

## License

[MIT](LICENSE)

---

## Author

[Ruhan Azevedo](https://github.com/ruhanazevedo)

<a href="https://www.buymeacoffee.com/ruhanazevedo" target="_blank">
  <img src="https://cdn.buymeacoffee.com/buttons/v2/default-blue.png" alt="Buy Me A Coffee" height="50">
</a>
