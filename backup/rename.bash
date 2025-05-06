#!/bin/bash

# Set to your target directory
DIR="./"
cd "$DIR" || exit

TMP_SORTED=$(mktemp)

# Step 1: Parse and sort files
for file in chapter*.level; do
    if [[ "$file" =~ chapter([0-9]+)_([0-9]+)\.level ]]; then
        fullnum=${BASH_REMATCH[1]}
        suffixlevel=${BASH_REMATCH[2]}

        # Take the first digit as chapter, the rest as level
        chapter=${fullnum:0:1}
        level_in_chapter=${fullnum:1}

        # Normalize missing digits
        if [[ -z "$level_in_chapter" ]]; then
            level_in_chapter=0
        fi

        # Create sort key and save
        printf "%d %03d %s\n" "$chapter" "$level_in_chapter" "$file" >> "$TMP_SORTED"
    fi
done

# Step 2: Sort and perform renaming
i=1
sort -k1n -k2n "$TMP_SORTED" | while read -r chapter level file; do
    newname="level1_${i}.level"
    echo "Renaming $file -> $newname"
    mv "$
