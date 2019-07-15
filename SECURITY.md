# Security Policy

## Supported Versions

| Version | Supported |
|---------|-----------|
| 3.x     | ✅ Active  |
| < 3.0   | ❌ No longer supported |

## Reporting a Vulnerability

If you discover a security vulnerability in Battleship, please **do not open a public issue**.

Report it privately by emailing:

**ruhanv.azvdo@gmail.com**

Include in your report:
- A description of the vulnerability
- Steps to reproduce it
- Potential impact
- Any suggested fix (optional)

## Response Timeline

| Stage | Target |
|-------|--------|
| Acknowledgement | Within 48 hours |
| Assessment | Within 7 days |
| Fix / patch release | Within 30 days depending on severity |

## Scope

Battleship is a **desktop tool** that runs entirely offline on the user's machine. It reads local PDF files and does not:

- Connect to any external server
- Transmit data of any kind
- Store credentials or sensitive information

Security concerns most relevant to this project:

- Malicious PDF files crafted to exploit the PDF parsing library (PDFBox)
- Dependency vulnerabilities in third-party libraries

## Dependencies

This project uses [Snyk](https://snyk.io) for automated dependency vulnerability monitoring. Patches are applied as they become available.
