#!/usr/bin/env python3
"""Rendert die DesigniteJava-CSVs als GitHub Step Summary und wertet ein
optionales Quality Gate aus (Exit 1 bei Ueberschreitung).

Aufruf:  designite_report.py <designite-output-ordner>

Env (alle optional, -1/ungesetzt = wird nicht geprueft):
  MAX_SMELLS         -> Grenze fuer die Gesamtzahl aller Smells
  MAX_ARCHITECTURE   -> Grenze fuer Architecture Smells
  MAX_DESIGN         -> Grenze fuer Design Smells
  MAX_IMPLEMENTATION -> Grenze fuer Implementation Smells
  MAX_TESTABILITY    -> Grenze fuer Testability Smells
  MAX_TEST           -> Grenze fuer Test Smells
"""
import csv
import os
import sys
from collections import Counter, defaultdict

# Smell-CSV -> Kategorie-Anzeigename
SMELL_FILES = {
    "ArchitectureSmells.csv": "Architecture Smells",
    "DesignSmells.csv": "Design Smells",
    "ImplementationSmells.csv": "Implementation Smells",
    "TestabilitySmells.csv": "Testability Smells",
    "TestSmells.csv": "Test Smells",
}

# Kategorie -> Env-Variable fuer die optionale Grenze
CATEGORY_ENV = {
    "Architecture Smells": "MAX_ARCHITECTURE",
    "Design Smells": "MAX_DESIGN",
    "Implementation Smells": "MAX_IMPLEMENTATION",
    "Testability Smells": "MAX_TESTABILITY",
    "Test Smells": "MAX_TEST",
}

MAX_DETAIL_ROWS = 50  # pro Smell-Typ, damit die Summary nicht ausufert


def int_env(name):
    """Liest eine Env-Grenze; ungesetzt/leer -> -1 (= nicht pruefen)."""
    raw = os.environ.get(name, "").strip()
    if raw == "":
        return -1
    try:
        return int(raw)
    except ValueError:
        return -1


def location(row):
    """Baut aus den vorhandenen Spalten eine kompakte Fundstelle."""
    parts = [row.get(c, "") for c in ("Package", "Class", "Method")]
    where = ".".join(p for p in parts if p)
    line = row.get("Method start line no") or row.get("Line no") or ""
    fname = os.path.basename(row.get("File", "") or "")
    loc = fname + (f":{line}" if line else "")
    return where, loc


def main(out_dir):
    summary_path = os.environ.get("GITHUB_STEP_SUMMARY")

    category_counts = Counter()          # Kategorie -> Anzahl
    smell_counts = Counter()             # (Kategorie, Smell) -> Anzahl
    details = defaultdict(list)          # (Kategorie, Smell) -> [(where, loc, desc)]
    total = 0

    for fname, label in SMELL_FILES.items():
        path = os.path.join(out_dir, fname)
        if not os.path.isfile(path):
            continue
        with open(path, newline="", encoding="utf-8") as fh:
            for row in csv.DictReader(fh):
                smell = (row.get("Smell") or "").strip()
                if not smell:
                    continue
                total += 1
                category_counts[label] += 1
                smell_counts[(label, smell)] += 1
                where, loc = location(row)
                details[(label, smell)].append((where, loc, row.get("Description", "")))

    # --- Quality Gate auswerten ---
    gates = []  # (Name, Anzahl, Grenze)
    if int_env("MAX_SMELLS") >= 0:
        gates.append(("Gesamt", total, int_env("MAX_SMELLS")))
    for label in SMELL_FILES.values():
        limit = int_env(CATEGORY_ENV[label])
        if limit >= 0:
            gates.append((label, category_counts.get(label, 0), limit))
    failed = [(n, c, lim) for (n, c, lim) in gates if c > lim]

    # --- Report bauen ---
    lines = ["# 🔍 DesigniteJava Report", ""]
    if total == 0:
        lines.append("✅ Keine Code Smells gefunden.")
    else:
        lines.append(f"**Gesamt: {total} Smells**")
        lines.append("")
        lines.append("| Kategorie | Anzahl |")
        lines.append("|---|---|")
        for label in SMELL_FILES.values():
            if category_counts.get(label):
                lines.append(f"| {label} | {category_counts[label]} |")
        lines.append("")

    if gates:
        lines.append("## 🚦 Quality Gate")
        lines.append("")
        lines.append("| Grenze | Anzahl | Limit | Status |")
        lines.append("|---|---|---|---|")
        for name, count, limit in gates:
            status = "❌ Fail" if count > limit else "✅ Pass"
            lines.append(f"| {name} | {count} | {limit} | {status} |")
        lines.append("")

    if total > 0:
        for label in SMELL_FILES.values():
            cat_smells = {s: n for (l, s), n in smell_counts.items() if l == label}
            if not cat_smells:
                continue
            lines.append(f"## {label}")
            lines.append("")
            lines.append("| Smell | Fundstelle | Beschreibung |")
            lines.append("|---|---|---|")
            for smell in sorted(cat_smells):
                rows = details[(label, smell)]
                for where, loc, desc in rows[:MAX_DETAIL_ROWS]:
                    desc = desc.replace("|", "\\|").replace("\n", " ")
                    lines.append(f"| {smell} | `{loc}` {where} | {desc} |")
                if len(rows) > MAX_DETAIL_ROWS:
                    lines.append(f"| {smell} | … | _+{len(rows) - MAX_DETAIL_ROWS} weitere_ |")
            lines.append("")

    report = "\n".join(lines)
    if summary_path:
        with open(summary_path, "a", encoding="utf-8") as fh:
            fh.write(report + "\n")
    print(report)

    # --- Exit-Status ---
    if failed:
        for name, count, limit in failed:
            print(f"::error::Lokales Quality Gate '{name}': {count} Smells > erlaubtes Maximum {limit}")
        return 1
    if gates:
        print(f"::notice::Lokales Quality Gate bestanden ({len(gates)} Grenze(n) geprueft).")
    return 0


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("usage: designite_report.py <designite-output-ordner>", file=sys.stderr)
        sys.exit(2)
    sys.exit(main(sys.argv[1]))
