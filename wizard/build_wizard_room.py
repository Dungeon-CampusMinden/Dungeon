#!/usr/bin/env python3
"""Temporärer Dialoghelfer zum Bauen einer WizardRoom.jar."""

from __future__ import annotations

import json
import os
from pathlib import Path
import stat
import subprocess
import sys
from typing import Any

try:
    import tkinter as tk
    from tkinter import filedialog, messagebox
except ImportError as error:
    tk = None
    filedialog = None
    messagebox = None
    TK_IMPORT_ERROR: Exception | None = error
else:
    TK_IMPORT_ERROR = None


REPO_ROOT = Path(__file__).resolve().parent.parent
OUTPUT_JAR = REPO_ROOT / "wizard" / "build" / "libs" / "WizardRoom.jar"
REPORT_KEYS = {
    "valid",
    "runnerVersion",
    "rawDeerSha256",
    "hostInputSha256",
    "issues",
}


def _is_report_issue(value: Any) -> bool:
    return (
        isinstance(value, dict)
        and isinstance(value.get("severity"), str)
        and value["severity"] in {"error", "warning"}
        and isinstance(value.get("phase"), str)
        and bool(value["phase"].strip())
        and isinstance(value.get("code"), str)
        and bool(value["code"].strip())
        and isinstance(value.get("path"), str)
    )


def _is_report_envelope(value: Any) -> bool:
    if not isinstance(value, dict) or set(value) != REPORT_KEYS:
        return False
    issues = value["issues"]
    if not (
        isinstance(value["valid"], bool)
        and isinstance(value["runnerVersion"], str)
        and bool(value["runnerVersion"].strip())
        and (value["rawDeerSha256"] is None or isinstance(value["rawDeerSha256"], str))
        and (value["hostInputSha256"] is None or isinstance(value["hostInputSha256"], str))
        and isinstance(issues, list)
        and all(_is_report_issue(issue) for issue in issues)
    ):
        return False
    has_error = any(issue["severity"] == "error" for issue in issues)
    return value["valid"] is (not has_error)


def extract_validation_report(output: str) -> dict[str, Any] | None:
    """Find the validation report in otherwise mixed Gradle output."""
    decoder = json.JSONDecoder()
    report = None
    position = 0
    while True:
        start = output.find("{", position)
        if start < 0:
            return report
        try:
            value, end = decoder.raw_decode(output, start)
        except json.JSONDecodeError:
            position = start + 1
            continue
        if _is_report_envelope(value):
            report = value
        position = max(start + 1, end)


def _build_command(project_folder: Path) -> list[str]:
    wrapper = REPO_ROOT / ("gradlew.bat" if os.name == "nt" else "gradlew")
    return [
        str(wrapper),
        ":wizard:buildWizardRoomJar",
        f"-PwizardProject={project_folder.resolve()}",
        "--console=plain",
    ]


def run_build(deer_file: Path) -> dict[str, Any]:
    project_folder = deer_file.parent.resolve()
    command = _build_command(project_folder)
    print(f"\n[Wizard-Helfer] Projekt: {project_folder}")
    print(f"[Wizard-Helfer] Befehl: {subprocess.list2cmdline(command)}\n")

    chunks: list[str] = []
    try:
        with subprocess.Popen(
            command,
            cwd=REPO_ROOT,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            encoding="utf-8",
            errors="replace",
            bufsize=1,
            shell=False,
        ) as process:
            assert process.stdout is not None
            for chunk in iter(process.stdout.readline, ""):
                chunks.append(chunk)
                sys.stdout.write(chunk)
                sys.stdout.flush()
            exit_code = process.wait()
    except OSError as error:
        print(f"[Wizard-Helfer] Wrapper-Startfehler: {error}", file=sys.stderr)
        print("[Wizard-Helfer] Exit-Code: <nicht gestartet>")
        return {
            "exit_code": None,
            "output": "",
            "report": None,
            "jar_exists": OUTPUT_JAR.is_file(),
            "launch_error": str(error),
        }

    output = "".join(chunks)
    if output and not output.endswith(("\n", "\r")):
        print()
    print(f"[Wizard-Helfer] Exit-Code: {exit_code}")
    return {
        "exit_code": exit_code,
        "output": output,
        "report": extract_validation_report(output),
        "jar_exists": OUTPUT_JAR.is_file(),
        "launch_error": None,
    }


def _issue_summary(report: dict[str, Any]) -> str:
    issues = report["issues"]
    lines = []
    for issue in issues[:4]:
        location = issue["path"]
        entity = issue.get("entity")
        if isinstance(entity, dict) and entity.get("kind") and entity.get("id"):
            location = f"{entity['kind']} {entity['id']}"
        suffix = f" – {location}" if location else ""
        lines.append(f"• {issue['phase']}: {issue['code']}{suffix}")
    if len(issues) > 4:
        lines.append(f"• … und {len(issues) - 4} weitere")
    return "\n".join(lines) or "Der Report enthält keine einzelnen Hinweise."


def failure_message(result: dict[str, Any]) -> tuple[str, str]:
    report = result["report"]
    exit_code = result["exit_code"]
    terminal_hint = "\n\nDie vollständigen Details stehen im Terminal."

    if result["launch_error"]:
        return (
            "Technischer Fehler",
            "Der Gradle-Wrapper konnte nicht gestartet werden:\n"
            + result["launch_error"]
            + terminal_hint,
        )
    if report is None:
        return (
            "Technischer Fehler",
            "Der Build lieferte keinen verwertbaren ProjectValidationReport."
            + terminal_hint,
        )
    if exit_code not in (None, 0) and report["valid"]:
        return (
            "Technischer Fehler",
            "Das Projekt ist gültig, aber Gradle oder das Packaging ist danach mit "
            f"Exit-Code {exit_code} fehlgeschlagen."
            + terminal_hint,
        )
    if exit_code == 0 and report["valid"] and not result["jar_exists"]:
        return (
            "Technischer Fehler",
            f"Der Build war erfolgreich, aber {OUTPUT_JAR} fehlt." + terminal_hint,
        )
    if exit_code == 0 and not report["valid"]:
        return (
            "Technischer Fehler",
            "Der Validierungsreport meldet Fehler, obwohl Gradle Exit-Code 0 geliefert hat."
            + terminal_hint,
        )
    return (
        "Projekt konnte nicht gebaut werden",
        _issue_summary(report) + terminal_hint,
    )


def _selected_deer_file(root: Any, initial_dir: Path | None) -> Path | None:
    options: dict[str, Any] = {
        "parent": root,
        "title": "deer.json auswählen",
        "filetypes": (("JSON-Dateien", "*.json"), ("Alle Dateien", "*.*")),
    }
    if initial_dir is not None:
        options["initialdir"] = str(initial_dir)
    filename = filedialog.askopenfilename(**options)
    return Path(filename) if filename else None


def main() -> int:
    if tk is None:
        print(
            f"Wizard-Helfer: Tkinter ist nicht verfügbar: {TK_IMPORT_ERROR}",
            file=sys.stderr,
        )
        return 1
    try:
        root = tk.Tk()
        root.withdraw()
    except tk.TclError as error:
        print(
            f"Wizard-Helfer: Die grafischen Dateidialoge sind nicht verfügbar: {error}",
            file=sys.stderr,
        )
        return 1

    initial_dir = None
    try:
        while True:
            deer_file = _selected_deer_file(root, initial_dir)
            if deer_file is None:
                return 0
            initial_dir = deer_file.parent
            try:
                regular_file = stat.S_ISREG(deer_file.stat().st_mode)
            except OSError:
                regular_file = False
            if deer_file.name != "deer.json" or not regular_file:
                messagebox.showerror(
                    "Ungültige Auswahl",
                    "Bitte eine reguläre Datei mit dem exakten Namen deer.json auswählen.",
                    parent=root,
                )
                continue

            result = run_build(deer_file.resolve())
            report = result["report"]
            if (
                result["exit_code"] == 0
                and report is not None
                and report["valid"]
                and result["jar_exists"]
            ):
                warning_count = sum(
                    issue["severity"] == "warning" for issue in report["issues"]
                )
                warning_text = (
                    f"\n\nWarnungen: {warning_count}. Details stehen im Terminal."
                    if warning_count
                    else ""
                )
                messagebox.showinfo(
                    "WizardRoom.jar erstellt",
                    f"Die JAR wurde erstellt:\n\n{OUTPUT_JAR}{warning_text}",
                    parent=root,
                )
                return 0

            title, text = failure_message(result)
            messagebox.showerror(title, text, parent=root)
    finally:
        root.destroy()


if __name__ == "__main__":
    raise SystemExit(main())
