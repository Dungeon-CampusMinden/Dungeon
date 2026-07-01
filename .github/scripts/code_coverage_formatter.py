#!/usr/bin/env python3

import argparse
import glob
import os
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


DEFAULT_PATTERN = "**/build/JacocoReports/test/jacocoTestReport.xml"

METRIC_ORDER = [
    "LINE",
    "BRANCH",
    "METHOD",
    "CLASS",
    "INSTRUCTION",
    "COMPLEXITY",
]


def parse_args():
    parser = argparse.ArgumentParser(
        description="Format JaCoCo XML reports as a GitHub Actions summary."
    )

    parser.add_argument(
        "--pattern",
        default=DEFAULT_PATTERN,
        help=f"Glob pattern for JaCoCo XML reports. Default: {DEFAULT_PATTERN}",
    )

    parser.add_argument(
        "--summary-file",
        default=os.environ.get("GITHUB_STEP_SUMMARY"),
        help="GitHub step summary file. Defaults to GITHUB_STEP_SUMMARY.",
    )

    parser.add_argument(
        "--min-line-coverage",
        type=float,
        default=None,
        help="Optional minimum line coverage percentage. Fails if below this value.",
    )

    parser.add_argument(
        "--warning-line-coverage",
        type=float,
        default=60.0,
        help="Line coverage below this value is shown as critical.",
    )

    parser.add_argument(
        "--good-line-coverage",
        type=float,
        default=80.0,
        help="Line coverage at or above this value is shown as good.",
    )

    return parser.parse_args()


def collect_reports(pattern):
    return sorted(glob.glob(pattern, recursive=True))


def module_name_from_report_path(report_file):
    path = Path(report_file)
    parts = path.parts

    if "build" in parts:
        build_index = parts.index("build")

        if build_index == 0:
            return "root"

        return parts[build_index - 1]

    return str(path.parent)


def parse_report(report_file):
    tree = ET.parse(report_file)
    root = tree.getroot()

    counters = {}

    for counter in root.findall("counter"):
        counter_type = counter.attrib["type"]
        missed = int(counter.attrib["missed"])
        covered = int(counter.attrib["covered"])

        counters[counter_type] = {
            "missed": missed,
            "covered": covered,
        }

    return counters


def add_counter(totals, counter_type, missed, covered):
    if counter_type not in totals:
        totals[counter_type] = {
            "missed": 0,
            "covered": 0,
        }

    totals[counter_type]["missed"] += missed
    totals[counter_type]["covered"] += covered


def percentage(covered, missed):
    total = covered + missed

    if total == 0:
        return None

    return covered / total * 100


def format_percentage(value):
    if value is None:
        return "n/a"

    return f"{value:.2f}%"


def coverage_bar(value, width=20):
    if value is None:
        return "n/a"

    filled = round((value / 100) * width)
    empty = width - filled

    return "█" * filled + "░" * empty


def status_for_coverage(value, warning_threshold, good_threshold):
    if value is None:
        return "⚪ Unbekannt"

    if value >= good_threshold:
        return "🟢 Gut"

    if value >= warning_threshold:
        return "🟡 Ausbaufähig"

    return "🔴 Kritisch"


def metric_label(metric):
    labels = {
        "LINE": "Lines",
        "BRANCH": "Branches",
        "METHOD": "Methods",
        "CLASS": "Classes",
        "INSTRUCTION": "Instructions",
        "COMPLEXITY": "Complexity",
    }

    return labels.get(metric, metric.title())


def build_total_table(totals):
    lines = []

    lines.append("## Gesamtübersicht")
    lines.append("")
    lines.append("| Metrik | Coverage | Balken | Covered | Missed |")
    lines.append("|---|---:|---|---:|---:|")

    for metric in METRIC_ORDER:
        if metric not in totals:
            continue

        covered = totals[metric]["covered"]
        missed = totals[metric]["missed"]
        coverage = percentage(covered, missed)

        lines.append(
            f"| {metric_label(metric)} "
            f"| **{format_percentage(coverage)}** "
            f"| `{coverage_bar(coverage)}` "
            f"| {covered} "
            f"| {missed} |"
        )

    return lines


def build_module_table(module_results):
    lines = []

    lines.append("## Coverage pro Modul")
    lines.append("")
    lines.append("| Modul | Line Coverage | Branch Coverage | Method Coverage | Class Coverage |")
    lines.append("|---|---:|---:|---:|---:|")

    for module_name, counters in module_results:
        line = percentage(
            counters.get("LINE", {}).get("covered", 0),
            counters.get("LINE", {}).get("missed", 0),
        )
        branch = percentage(
            counters.get("BRANCH", {}).get("covered", 0),
            counters.get("BRANCH", {}).get("missed", 0),
        )
        method = percentage(
            counters.get("METHOD", {}).get("covered", 0),
            counters.get("METHOD", {}).get("missed", 0),
        )
        clazz = percentage(
            counters.get("CLASS", {}).get("covered", 0),
            counters.get("CLASS", {}).get("missed", 0),
        )

        lines.append(
            f"| `{module_name}` "
            f"| {format_percentage(line)} "
            f"| {format_percentage(branch)} "
            f"| {format_percentage(method)} "
            f"| {format_percentage(clazz)} |"
        )

    return lines


def build_report_paths_section(report_files):
    lines = []

    lines.append("<details>")
    lines.append("<summary>Gefundene JaCoCo-Reports anzeigen</summary>")
    lines.append("")
    lines.append("")

    for report_file in report_files:
        lines.append(f"- `{report_file}`")

    lines.append("")
    lines.append("</details>")

    return lines


def build_summary(report_files, totals, module_results, warning_threshold, good_threshold):
    line_coverage = None

    if "LINE" in totals:
        line_coverage = percentage(
            totals["LINE"]["covered"],
            totals["LINE"]["missed"],
        )

    status = status_for_coverage(
        line_coverage,
        warning_threshold,
        good_threshold,
    )

    lines = []

    lines.append("# JaCoCo Code Coverage")
    lines.append("")
    lines.append(f"**Status:** {status}")
    lines.append("")
    lines.append(f"**Line Coverage:** `{format_percentage(line_coverage)}`")
    lines.append("")
    lines.append(f"`{coverage_bar(line_coverage, width=30)}`")
    lines.append("")

    if line_coverage is not None and line_coverage < warning_threshold:
        lines.append("> [!WARNING]")
        lines.append(
            f"> Die Line Coverage liegt bei **{format_percentage(line_coverage)}** "
            f"und damit unter dem Warnwert von **{warning_threshold:.2f}%**."
        )
        lines.append("")
    elif line_coverage is not None and line_coverage >= good_threshold:
        lines.append("> [!TIP]")
        lines.append(
            f"> Die Line Coverage liegt bei **{format_percentage(line_coverage)}** "
            f"und erfüllt den Zielwert von **{good_threshold:.2f}%**."
        )
        lines.append("")
    else:
        lines.append("> [!NOTE]")
        lines.append(
            "> Die Coverage ist sichtbar, aber noch ausbaufähig. "
            "Für Details den HTML-Report aus den Artefakten herunterladen."
        )
        lines.append("")

    lines.extend(build_total_table(totals))
    lines.append("")
    lines.extend(build_module_table(module_results))
    lines.append("")
    lines.extend(build_report_paths_section(report_files))
    lines.append("")

    return "\n".join(lines)


def main():
    args = parse_args()

    report_files = collect_reports(args.pattern)

    if not report_files:
        print(f"No JaCoCo XML reports found for pattern: {args.pattern}", file=sys.stderr)
        return 1

    totals = {}
    module_results = []

    for report_file in report_files:
        counters = parse_report(report_file)
        module_name = module_name_from_report_path(report_file)

        module_results.append((module_name, counters))

        for counter_type, values in counters.items():
            add_counter(
                totals,
                counter_type,
                values["missed"],
                values["covered"],
            )

    summary = build_summary(
        report_files=report_files,
        totals=totals,
        module_results=module_results,
        warning_threshold=args.warning_line_coverage,
        good_threshold=args.good_line_coverage,
    )

    if args.summary_file:
        with open(args.summary_file, "a", encoding="utf-8") as file:
            file.write(summary)
            file.write("\n")
    else:
        print(summary)

    print("JaCoCo coverage summary written.")

    if args.min_line_coverage is not None and "LINE" in totals:
        line_coverage = percentage(
            totals["LINE"]["covered"],
            totals["LINE"]["missed"],
        )

        if line_coverage is not None and line_coverage < args.min_line_coverage:
            print(
                f"Line coverage {line_coverage:.2f}% is below required minimum "
                f"of {args.min_line_coverage:.2f}%.",
                file=sys.stderr,
            )
            return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())