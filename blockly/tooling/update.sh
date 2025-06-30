#!/bin/bash

# Arrays zum Speichern der Nachrichten für die Zusammenfassung
summary_success=()
summary_failure=()

# Funktion zum Protokollieren von Erfolgen
log_success() {
  local message="$1"
  summary_success+=("ERFOLG: $message")
  echo "INFO: $message"
}

# Funktion zum Protokollieren von Fehlern
log_failure() {
  local message="$1"
  summary_failure+=("FEHLER: $message")
  echo "FEHLER: $message"
}

# Funktion zum Herunterladen einer Datei und Überschreiben einer existierenden Datei
# Parameter: $1 URL, $2 Zielverzeichnis, $3 Zieldateiname, $4 Dateibeschreibung
download_file_overwrite() {
  local file_url="$1"
  local target_dir="$2"
  local target_filename="$3"
  local file_description="$4"
  local target_path="$target_dir/$target_filename"
  local tmp_target_path="$target_dir/$target_filename.tmp"

  echo "INFO: Lade $file_description von $file_url nach $target_path herunter (überschreibt existierende Datei)..."
  if wget -q -O "$tmp_target_path" -- "$file_url"; then
    if mv "$tmp_target_path" "$target_path"; then
      log_success "$file_description erfolgreich von $file_url nach $target_path heruntergeladen und verschoben."
    else
      log_failure "Verschieben der temporären Datei $tmp_target_path nach $target_path für $file_description fehlgeschlagen."
      rm -f "$tmp_target_path" # cleanup temp file
    fi
  else
    log_failure "Download von $file_description ($file_url) mit wget fehlgeschlagen. Wget Exit-Code: $?."
    rm -f "$tmp_target_path" # cleanup temp file if it exists
  fi
}

# Funktion zur Ausgabe der endgültigen Zusammenfassung
print_summary() {
  echo -e "\n--- ENDGÜLTIGE ZUSAMMENFASSUNG ---"

  echo -e "\n--- Erfolgreiche Operationen ---"
  if [ ${#summary_success[@]} -eq 0 ]; then
    echo "Keine Operationen wurden erfolgreich abgeschlossen."
  else
    for msg in "${summary_success[@]}"; do
      echo "$msg"
    done
  fi

  echo -e "\n--- Fehlgeschlagene Operationen ---"
  if [ ${#summary_failure[@]} -eq 0 ]; then
    echo "Alle gemeldeten Operationen waren erfolgreich oder wurden aufgrund vorheriger Fehler übersprungen."
  else
    for msg in "${summary_failure[@]}"; do
      echo "$msg"
    done
  fi
}

create_vscode_desktop_entry() {
  local profile_name="$1"
  local target_path="$2" # Der Path der in VS-Code geöffnet werden soll
  local desktop_file_path="/$HOME/Desktop/Visual Studio Code ($profile_name).desktop"

  echo "INFO: Erstelle Desktop-Eintrag für VS-Code Profil '$profile_name'..."
  # Überprüfe, ob der Desktop-Eintrag bereits existiert
  if [ -f "$desktop_file_path" ]; then
    echo "INFO: Desktop-Eintrag für VS-Code Profil '$profile_name' existiert bereits. Überspringe Erstellung."
    return 0
  fi

  cat <<EOF > "$desktop_file_path"
[Desktop Entry]
Name=Visual Studio Code ($profile_name)
Comment=Code Editing. Redefined.
GenericName=Text Editor
Exec=/usr/share/code/code --profile "$profile_name" $target_path
Icon=vscode
Type=Application
StartupNotify=false
StartupWMClass=Code
Categories=TextEditor;Development;IDE;
MimeType=application/x-code-workspace;
Keywords=vscode;
EOF

  # Setze die Berechtigungen für die Desktop-Datei
  chmod +x "$desktop_file_path"

  if [ -f "$desktop_file_path" ]; then
    log_success "Desktop-Eintrag für VS-Code Profil '$profile_name' erfolgreich erstellt unter $desktop_file_path."
  else
    log_failure "Erstellung des Desktop-Eintrags für VS-Code Profil '$profile_name' fehlgeschlagen."
  fi
  return 0
}

# --- Basis-URL für Downloads ---
BASE_DOWNLOAD_URL="<URL HERE>"

# --- Definition der Hauptverzeichnisse ---
# Basisverzeichnis für den Workshop
workshop_base_dir="$HOME/Desktop/Workshop"
# Quellverzeichnis
source_dir="$workshop_base_dir/Source"
# Blockly-Verzeichnis (eine Ebene höher als Source)
blockly_dir_parent="$workshop_base_dir" # Wird als Basis für Blockly-Pfade verwendet

echo "Skript gestartet. Arbeitsverzeichnis: $(pwd)"
echo "Ziel-Workshop-Basisverzeichnis: $workshop_base_dir"

# --- Überprüfe Abhängigkeiten ---
echo -e "\n--- Überprüfe Abhängigkeiten ---"
dependencies_met=true
if ! command -v git >/dev/null 2>&1; then
  log_failure "Git ist nicht installiert. Bitte installieren Sie Git (z.B. 'sudo apt install git')."
  dependencies_met=false
fi
if ! command -v wget >/dev/null 2>&1; then
  log_failure "wget ist nicht installiert. Bitte installieren Sie wget (z.B. 'sudo apt install wget')."
  dependencies_met=false
fi
if ! java -version >/dev/null 2>&1; then
  log_failure "Java ist nicht installiert oder nicht im PATH. Bitte installieren Sie Java (wird für Gradle benötigt)."
  dependencies_met=false
fi
# Hier könnten weitere Abhängigkeitsprüfungen hinzugefügt werden (z.B. für java)

if [ "$dependencies_met" = true ]; then
    log_success "Alle grundlegenden Abhängigkeiten (git, wget, java) sind vorhanden."
else
    log_failure "Eine oder mehrere Abhängigkeiten fehlen. Breche Skript ab."
    print_summary
    exit 1
fi

# --- Schritt 1: Navigiere zu ~/Desktop/Workshop/Source ---
echo -e "\n--- Schritt 1: Navigiere zum Quellverzeichnis ---"
if [ -d "$source_dir" ]; then
  if cd "$source_dir"; then
    log_success "Erfolgreich zu $source_dir navigiert."
  else
    log_failure "Navigation zu $source_dir fehlgeschlagen. Breche Skript ab."
    print_summary
    exit 1
  fi
else
  log_failure "Quellverzeichnis $source_dir nicht gefunden. Breche Skript ab."
  print_summary
  exit 1
fi

# --- Schritt 2: Git-Operationen (lokale Änderungen verwerfen und pullen) ---
echo -e "\n--- Schritt 2: Git-Operationen ---"
echo "INFO: Verwerfe lokale Änderungen (git reset --hard HEAD && git clean -fd)..."
if git reset --hard HEAD && git clean -fd; then
  log_success "Lokale Änderungen erfolgreich verworfen."
  echo "INFO: Führe git pull aus..."
  if git pull; then
    log_success "Git pull erfolgreich abgeschlossen."
  else
    log_failure "Git pull fehlgeschlagen."
  fi
else
  log_failure "Verwerfen der lokalen Änderungen fehlgeschlagen."
fi

# --- Schritt 3: ./gradlew buildBlocklyJar aufrufen ---
echo -e "\n--- Schritt 3: Blockly JAR erstellen ---"
if [ -f "./gradlew" ]; then
  echo "INFO: Führe ./gradlew buildBlocklyJar aus..."
  if ./gradlew buildBlocklyJar; then
    log_success "./gradlew buildBlocklyJar erfolgreich abgeschlossen."
  else
    log_failure "./gradlew buildBlocklyJar fehlgeschlagen."
  fi
else
  log_failure "Gradle Wrapper ./gradlew nicht im Verzeichnis $(pwd) gefunden."
fi

# --- Schritt 4: JAR-Datei verschieben ---
echo -e "\n--- Schritt 4: Blockly JAR verschieben ---"
jar_source_path="./blockly/build/libs/Blockly.jar" # Relativ zu $source_dir
jar_dest_dir="$blockly_dir_parent/Blockly/content"
jar_dest_file="$jar_dest_dir/blockly.jar"

if [ -f "$jar_source_path" ]; then
  echo "INFO: Zielverzeichnis für JAR: $jar_dest_dir"
  if [ ! -d "$jar_dest_dir" ]; then
    echo "INFO: Zielverzeichnis $jar_dest_dir existiert nicht. Versuche es zu erstellen..."
    if mkdir -p "$jar_dest_dir"; then
      log_success "Zielverzeichnis $jar_dest_dir erfolgreich erstellt."
    else
      log_failure "Erstellung des Zielverzeichnisses $jar_dest_dir fehlgeschlagen. JAR kann nicht verschoben werden."
    fi
  fi

  if [ -d "$jar_dest_dir" ]; then
    echo "INFO: Verschiebe $jar_source_path nach $jar_dest_file..."
    if mv "$jar_source_path" "$jar_dest_file"; then
      log_success "JAR-Datei erfolgreich nach $jar_dest_file verschoben."
    else
      log_failure "Verschieben der JAR-Datei von $jar_source_path nach $jar_dest_file fehlgeschlagen."
    fi
  fi
else
  log_failure "Quelldatei $jar_source_path nicht gefunden. Build möglicherweise fehlgeschlagen oder Pfad ist falsch."
fi

# --- Schritt 5: Einen Ordner höher gehen (zum Workshop-Basisverzeichnis) ---
echo -e "\n--- Schritt 5: Navigiere zum Workshop-Basisverzeichnis ---"
if cd "$blockly_dir_parent"; then
  log_success "Erfolgreich zu $blockly_dir_parent navigiert."
else
  log_failure "Navigation zu $blockly_dir_parent fehlgeschlagen. Weitere Schritte könnten fehlschlagen."
  print_summary
  exit 1
fi

# --- Schritt 6: kill_server.sh kopieren ---
echo -e "\n--- Schritt 6: kill_server.sh kopieren ---"
kill_server_path="$blockly_dir_parent/Source/blockly/toolings/kill_server.sh"
kill_server_target_path="$blockly_dir_parent/kill_server.sh"

if [ -f "$kill_server_target_path" ]; then
  echo "INFO: kill_server.sh existiert bereits unter $kill_server_target_path. Überspringe Kopieren."
else
  echo "INFO: kill_server.sh existiert nicht unter $kill_server_target_path. Versuche es zu kopieren..."
fi
if [ ! -d "$kill_server_path" ]; then
  log_failure "kill_server.sh nicht gefunden unter $kill_server_path. Bitte überprüfen Sie den Pfad."
else
  if cp "$kill_server_path" "$kill_server_target_path"; then
    log_success "kill_server.sh erfolgreich nach $kill_server_target_path kopiert."
    # make it executable
    if chmod +x "$kill_server_target_path"; then
      log_success "$kill_server_target_path erfolgreich ausführbar gemacht."
    else
      log_failure "Konnte $kill_server_target_path nicht ausführbar machen."
    fi
  else
    log_failure "Kopieren von kill_server.sh nach $kill_server_target_path fehlgeschlagen."
  fi
fi

# --- Schritt 7: blockly herunterladen ---
echo -e "\n--- Schritt 7: Blockly Dateien herunterladen ---"
blockly_bin_url="$BASE_DOWNLOAD_URL/blockly.bin"
blockly_bin_target_filename="blockly.bin"
blockly_html_url="$BASE_DOWNLOAD_URL/blockly_index"
blockly_html_target_filename="index.html"
download_common_target_dir="./Blockly" # Relativ zu $blockly_dir_parent

if [ -d "$download_common_target_dir" ]; then # Nur fortfahren, wenn das Verzeichnis existiert
  download_file_overwrite "$blockly_bin_url" "$download_common_target_dir" "$blockly_bin_target_filename" "blockly.bin"
  # make executable
  blockly_bin_target_path="$download_common_target_dir/$blockly_bin_target_filename"
  if [ -f "$blockly_bin_target_path" ]; then
    if chmod +x "$blockly_bin_target_path"; then
      log_success "$blockly_bin_target_path erfolgreich ausführbar gemacht."
    else
      log_failure "$blockly_bin_target_path konnte nicht ausführbar gemacht werden."
    fi
  else
    log_failure "Download von $blockly_bin_url fehlgeschlagen oder Datei existiert nicht: $blockly_bin_target_path"
  fi
  # check if content folder exists, if not create it
  if [ ! -d "$download_common_target_dir/content" ]; then
    echo "INFO: Erstelle content Verzeichnis in $download_common_target_dir..."
    mkdir -p "$download_common_target_dir/content"
  fi
  download_file_overwrite "$blockly_html_url" "$download_common_target_dir/content" "$blockly_html_target_filename" "index.html"
  # make sure the index.html is executable
  blockly_html_target_path="$download_common_target_dir/content/$blockly_html_target_filename"
  if [ -f "$blockly_html_target_path" ]; then
    if chmod +x "$blockly_html_target_path"; then
      log_success "$blockly_html_target_path erfolgreich ausführbar gemacht."
    else
      log_failure "$blockly_html_target_path konnte nicht ausführbar gemacht werden."
    fi
  else
    log_failure "Download von $blockly_html_url fehlgeschlagen oder Datei existiert nicht: $blockly_html_target_path"
  fi
else
  log_failure "Download-Zielverzeichnis $download_common_target_dir nicht verfügbar. Überspringe Download von blockly.bin."
fi

# --- Schritt 9: blockly-runner Vs-Code Extension installieren/aktualisieren ---
echo -e "\n--- Schritt 9: blockly-runner Vs-Code Extension installieren/aktualisieren ---"
vs_extension_url="$BASE_DOWNLOAD_URL/blockly-code-runner.vsix"
vs_extension_target_filename="blockly-code-runner.vsix"
vs_extension_target_path="$download_common_target_dir/$vs_extension_target_filename" # Selbes Verzeichnis wie kill_server.sh
vscode_blockly_profile_name="Blockly"
vscode_java_profile_name="Java"

if [ -d "$download_common_target_dir" ]; then # Nur fortfahren, wenn das Verzeichnis existiert
  # Download der Extension mit der neuen Funktion
  download_file_overwrite "$vs_extension_url" "$download_common_target_dir" "$vs_extension_target_filename" "VS-Code Extension blockly-code-runner.vsix"
  
  # Überprüfe, ob die Datei nach dem Download-Versuch existiert, bevor die Installation versucht wird
  if [ -f "$vs_extension_target_path" ]; then
    # Installiere die VS-Code Extension, wenn sie heruntergeladen wurde
    if command -v code >/dev/null 2>&1; then
      echo "INFO: Installiere die VS-Code Extension $vs_extension_target_filename..."
      if code --install-extension "$vs_extension_target_path" --force --profile "$vscode_blockly_profile_name"; then
        log_success "VS-Code Extension $vs_extension_target_filename erfolgreich installiert/aktualisiert."
      else
        log_failure "Installation der VS-Code Extension $vs_extension_target_filename fehlgeschlagen."
      fi
    else
      log_failure "VS-Code (Befehl 'code') ist nicht im PATH. Bitte stellen Sie sicher, dass es installiert und im PATH ist, um die Extension zu installieren."
    fi
  else
    log_failure "VS-Code Extension $vs_extension_target_filename wurde nicht erfolgreich heruntergeladen. Installation übersprungen."
  fi
else
  log_failure "Download-Zielverzeichnis $download_common_target_dir nicht verfügbar. Überspringe Download und Installation von $vs_extension_target_filename."
fi
# --- Schritt 9.2: Java Vs-Code Extension installieren/aktualisieren ---
echo -e "\n--- Schritt 9.2: Java Vs-Code Extension installieren/aktualisieren ---"
java_extension_ids=("redhat.java" "vscjava.vscode-gradle")

for java_extension_id in "${java_extension_ids[@]}"; do
  if code --install-extension "$java_extension_id" --force --profile "$vscode_java_profile_name"; then
    log_success "VS-Code Extension '$java_extension_id' erfolgreich installiert/aktualisiert."
  else
    log_failure "Installation der VS-Code Extension '$java_extension_id' fehlgeschlagen."
  fi
done

# --- Schritt 10: clear user folder ---
echo -e "\n--- Schritt 10: User-Skripts werden gelöscht ---"
user_scripts_dir="$workshop_base_dir/user"

# Überprüfe, ob das User-Skripts Verzeichnis existiert, wenn nicht, erstelle es
if [ ! -d "$user_scripts_dir" ]; then
  echo "INFO: User-Skripts Verzeichnis $user_scripts_dir existiert nicht. Versuche es zu erstellen..."
  if mkdir -p "$user_scripts_dir"; then
    log_success "User-Skripts Verzeichnis $user_scripts_dir erfolgreich erstellt."
  else
    log_failure "Erstellung des User-Skripts Verzeichnisses $user_scripts_dir fehlgeschlagen."
  fi
fi

read -p "Möchten Sie wirklich alle User-Skripts im Verzeichnis $user_scripts_dir löschen? (j/n): " confirm
if [[ "$confirm" == [jJyY] ]]; then
  if rm -rf "$user_scripts_dir/*"; then
    log_success "User-Skripts im Verzeichnis $user_scripts_dir wurden erfolgreich gelöscht."
  else
    log_failure "Löschen der User-Skripts im Verzeichnis $user_scripts_dir fehlgeschlagen."
  fi
  log_success "User-Skripts im Verzeichnis $user_scripts_dir wurden gelöscht."
else
  echo "INFO: User-Skripts im Verzeichnis $user_scripts_dir wurden nicht gelöscht."
fi

# --- Endgültige Zusammenfassung ---
print_summary

echo -e "\nSkript beendet."
