# 1. Beende Chromium (WICHTIG: Sonst werden die Daten beim Schließen wieder erstellt)
pkill -f chromium

# 2. Lösche den Inhalt des leveldb Ordners
rm -rf ~/.config/chromium/Default/Local\ Storage/leveldb/*
