#!/bin/bash

# Raspberry Pi Installation Script
# Updates system and installs development environment

#=============================================================================
# CONFIGURATION
#=============================================================================
JAVA_VERSION="21" # Used for version checking
# Specific Java download URL for Temurin JDK 21 ARM64
TEMURIN_JDK_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.3%2B9/OpenJDK21U-jdk_aarch64_linux_hotspot_21.0.3_9.tar.gz"

PROJECT_TAR_URL="<URL HERE>/Workshop.tar.gz" # Will be prompted if empty
PROJECT_DESKTOP_NAME="Workshop"
VSIX_EXTENSION_NAME="blockly-code-runner-1.0.0.vsix"
BLOCKLY_BIN_NAME="blockly.bin"
BLOCKLY_DIR_NAME="Blockly"
SOURCE_DIR_NAME="Source"
GRADLE_TASK="build"

LOG_FILE="/tmp/pi_setup_$(date +%Y%m%d_%H%M%S).log"
TEMP_DIR="/tmp/pi_setup_temp"
DOWNLOADS_DIR="$HOME/Downloads"
DESKTOP_DIR="$HOME/Desktop"

#=============================================================================
# UTILITY FUNCTIONS
#=============================================================================

log_message() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

error_exit() {
    log_message "ERROR: $1"
    # cleanup_temp_files is called by trap
    exit 1
}

cleanup_temp_files() {
    log_message "Cleaning up temporary files..."
    rm -rf "$TEMP_DIR"
    rm -f "$DOWNLOADS_DIR"/project.tar*
    rm -f /tmp/vscode.deb
    rm -f /tmp/idea.tar.gz
    rm -f /tmp/packages.microsoft.gpg
}

check_sudo_privileges() {
    log_message "Checking for sudo privileges..."
    if ! sudo -n true 2>/dev/null; then
        error_exit "This script requires sudo privileges. Please run with sudo or ensure your user has sudo access."
    fi
    log_message "Sudo privileges verified."
}

check_internet_connection() {
    log_message "Checking internet connection..."
    if ! ping -c 1 8.8.8.8 &>/dev/null; then
        error_exit "No internet connection. Please check your network."
    fi
    log_message "Internet connection verified."
}

prompt_user_confirmation() {
    echo
    echo "==================== INSTALLATION PLAN ===================="
    echo "This script will install:"
    echo "- System updates"
    echo "- Java OpenJDK $JAVA_VERSION (Temurin from URL)"
    echo "- VS Code"
    echo "- Workshop project from TAR file"
    echo "- VS Code extension"
    echo "=========================================================="
    echo
    read -p "Do you want to continue with the installation? [y/N]: " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_message "Installation cancelled by user."
        exit 0
    fi
}

#=============================================================================
# INSTALLATION FUNCTIONS
#=============================================================================

update_system() {
    log_message "Updating system packages..."
    sudo apt update && sudo apt upgrade -y
    sudo apt install -y wget curl gpg software-properties-common apt-transport-https file
}

install_java() {
    log_message "Checking Java OpenJDK $JAVA_VERSION (Temurin) installation..."

    local temurin_archive_name
    temurin_archive_name=$(basename "$TEMURIN_JDK_URL")
    local install_base_dir="/opt"
    local extracted_jdk_dir_name
    local java_home_path

    # Download the archive temporarily to determine the extracted directory name
    log_message "Temporarily downloading Java archive to determine structure..."
    mkdir -p "$TEMP_DIR"
    wget -q -P "$TEMP_DIR" "$TEMURIN_JDK_URL" -O "$TEMP_DIR/$temurin_archive_name"
    if [ $? -ne 0 ]; then
        error_exit "Failed to download Java JDK to determine structure."
    fi

    extracted_jdk_dir_name=$(tar -tf "$TEMP_DIR/$temurin_archive_name" | head -n 1 | cut -f1 -d"/")
    if [ -z "$extracted_jdk_dir_name" ]; then
        rm -f "$TEMP_DIR/$temurin_archive_name" # Clean up temp download
        error_exit "Could not determine JDK directory name from archive $temurin_archive_name."
    fi
    # No need to keep the archive yet, it will be re-downloaded or used if check fails
    rm -f "$TEMP_DIR/$temurin_archive_name"

    java_home_path="$install_base_dir/$extracted_jdk_dir_name"

    log_message "Target Java installation path: $java_home_path"

    # Check if already installed and correct version
    if [ -d "$java_home_path" ]; then
        if [ -x "$java_home_path/bin/java" ]; then
            local installed_version
            installed_version=$("$java_home_path/bin/java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
            if [[ "$installed_version" == "$JAVA_VERSION."* ]]; then
                log_message "Java OpenJDK $JAVA_VERSION (Temurin) already installed at $java_home_path and version matches. Skipping re-installation."
                # Ensure environment variables are correctly set up
                if ! sudo grep -Fxq "export JAVA_HOME=$java_home_path" /etc/profile.d/java_home.sh; then
                    log_message "Setting up environment variables for existing Java installation..."
                    sudo tee /etc/profile.d/java_home.sh > /dev/null <<EOF
export JAVA_HOME=$java_home_path
export PATH=\$PATH:\$JAVA_HOME/bin
EOF
                    sudo chmod +x /etc/profile.d/java_home.sh
                fi
                export JAVA_HOME="$java_home_path"
                export PATH="$PATH:$JAVA_HOME/bin"
                return 0
            else
                log_message "Found Java at $java_home_path but version ($installed_version) does not match target $JAVA_VERSION. Removing old version."
                sudo rm -rf "$java_home_path"
            fi
        else
            log_message "Directory $java_home_path exists but $java_home_path/bin/java not found or not executable. Cleaning up and reinstalling."
            sudo rm -rf "$java_home_path"
        fi
    fi

    log_message "Installing Java OpenJDK $JAVA_VERSION (Temurin) from $TEMURIN_JDK_URL..."
    
    log_message "Downloading $temurin_archive_name to $TEMP_DIR..."
    wget -P "$TEMP_DIR" "$TEMURIN_JDK_URL" -O "$TEMP_DIR/$temurin_archive_name"
    if [ $? -ne 0 ]; then
        error_exit "Failed to download Java JDK."
    fi

    log_message "Creating installation directory $install_base_dir (if it doesn't exist)..."
    sudo mkdir -p "$install_base_dir"

    log_message "Extracting $temurin_archive_name to $install_base_dir..."
    sudo tar -xzf "$TEMP_DIR/$temurin_archive_name" -C "$install_base_dir"
    if [ $? -ne 0 ]; then
        rm -f "$TEMP_DIR/$temurin_archive_name" # Clean up download
        error_exit "Failed to extract Java JDK."
    fi

    # Verify the directory exists after extraction
    if [ ! -d "$java_home_path" ]; then
        rm -f "$TEMP_DIR/$temurin_archive_name" # Clean up download
        error_exit "JDK directory $java_home_path not found after extraction. Extracted name might have been unexpected."
    fi

    log_message "Java JDK extracted to $java_home_path"

    # Setup JAVA_HOME globally
    log_message "Setting up JAVA_HOME environment variable to $java_home_path..."
    sudo tee /etc/profile.d/java_home.sh > /dev/null <<EOF
export JAVA_HOME=$java_home_path
export PATH=\$PATH:\$JAVA_HOME/bin
EOF
    sudo chmod +x /etc/profile.d/java_home.sh

    # Set for current session
    export JAVA_HOME="$java_home_path"
    export PATH="$PATH:$JAVA_HOME/bin"

    # Clean up downloaded archive from TEMP_DIR
    rm -f "$TEMP_DIR/$temurin_archive_name"

    log_message "Java installation completed. JAVA_HOME set to: $java_home_path"
    log_message "Verifying Java installation..."
    if "$java_home_path/bin/java" -version &>/dev/null; then
        log_message "Java version: $("$java_home_path/bin/java" -version 2>&1 | head -n 1)"
    else
        error_exit "Java installation verification failed. '$java_home_path/bin/java -version' did not run successfully."
    fi
}

install_vscode() {
    log_message "Checking VS Code installation..."
    
    if command -v code &>/dev/null; then
        log_message "VS Code already installed. Skipping..."
        return 0
    fi
    
    log_message "Installing VS Code..."
    ARCH=$(dpkg --print-architecture)
    
    if [[ "$ARCH" == "arm64" ]] || [[ "$ARCH" == "armhf" ]]; then
        curl -L "https://code.visualstudio.com/sha/download?build=stable&os=linux-deb-$ARCH" -o /tmp/vscode.deb
        sudo dpkg -i /tmp/vscode.deb
        sudo apt-get install -f -y # Install dependencies
        rm -f /tmp/vscode.deb
    else
        wget -qO- https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor > /tmp/packages.microsoft.gpg
        sudo install -o root -g root -m 644 /tmp/packages.microsoft.gpg /etc/apt/trusted.gpg.d/
        sudo sh -c 'echo "deb [arch=amd64 signed-by=/etc/apt/trusted.gpg.d/packages.microsoft.gpg] https://packages.microsoft.com/repos/code stable main" > /etc/apt/sources.list.d/vscode.list'
        rm -f /tmp/packages.microsoft.gpg
        sudo apt update && sudo apt install -y code
    fi
    
    log_message "VS Code installation completed."
}

setup_project() {
    log_message "Setting up Workshop project..."
    
    if [ -z "$PROJECT_TAR_URL" ]; then
        echo
        read -p "Please provide the TAR download URL: " PROJECT_TAR_URL
        if [ -z "$PROJECT_TAR_URL" ]; then
            error_exit "TAR URL is required"
        fi
    fi
    
    mkdir -p "$TEMP_DIR" # Ensure TEMP_DIR exists
    
    log_message "Downloading Workshop project from: $PROJECT_TAR_URL to $TEMP_DIR/project.tar"
    wget "$PROJECT_TAR_URL" -O "$TEMP_DIR/project.tar"
    
    if [ ! -f "$TEMP_DIR/project.tar" ]; then
        error_exit "Failed to download project tar file"
    fi
    
    file_type=$(file -b "$TEMP_DIR/project.tar")
    log_message "Detected project file type: $file_type"
    
    # Create a subdirectory within TEMP_DIR for extraction to avoid clutter
    EXTRACT_SUBDIR="$TEMP_DIR/project_extracted"
    mkdir -p "$EXTRACT_SUBDIR"
    
    log_message "Extracting project.tar to $EXTRACT_SUBDIR"
    if [[ $file_type == *"gzip"* ]]; then
        tar -xzf "$TEMP_DIR/project.tar" -C "$EXTRACT_SUBDIR"
    elif [[ $file_type == *"bzip2"* ]]; then
        tar -xjf "$TEMP_DIR/project.tar" -C "$EXTRACT_SUBDIR"
    else
        tar -xf "$TEMP_DIR/project.tar" -C "$EXTRACT_SUBDIR"
    fi
    
    # Find the Workshop directory (should be named 'Workshop')
    # It could be directly in EXTRACT_SUBDIR or one level down if tarball had a root folder
    PROJECT_SOURCE_PATH_CANDIDATE1="$EXTRACT_SUBDIR/Workshop"
    PROJECT_SOURCE_PATH_CANDIDATE2=$(find "$EXTRACT_SUBDIR" -maxdepth 2 -type d -name "Workshop" | head -n 1)

    if [ -d "$PROJECT_SOURCE_PATH_CANDIDATE1" ]; then
        PROJECT_SOURCE_PATH="$PROJECT_SOURCE_PATH_CANDIDATE1"
    elif [ -d "$PROJECT_SOURCE_PATH_CANDIDATE2" ]; then
        PROJECT_SOURCE_PATH="$PROJECT_SOURCE_PATH_CANDIDATE2"
    else
        error_exit "Expected 'Workshop' directory not found after extraction in $EXTRACT_SUBDIR"
    fi
    
    log_message "Found Workshop folder at: $PROJECT_SOURCE_PATH"
    
    if [ ! -d "$PROJECT_SOURCE_PATH/$BLOCKLY_DIR_NAME" ]; then
        error_exit "Expected $BLOCKLY_DIR_NAME directory not found in $PROJECT_SOURCE_PATH"
    fi
    
    if [ ! -d "$PROJECT_SOURCE_PATH/$SOURCE_DIR_NAME" ]; then
        error_exit "Expected $SOURCE_DIR_NAME directory not found in $PROJECT_SOURCE_PATH"
    fi
    
    mkdir -p "$DESKTOP_DIR" # Ensure Desktop directory exists
    PROJECT_FINAL_PATH="$DESKTOP_DIR/$PROJECT_DESKTOP_NAME" # Use PROJECT_DESKTOP_NAME
    log_message "Moving Workshop project to Desktop: $PROJECT_FINAL_PATH"
    
    if [ -d "$PROJECT_FINAL_PATH" ]; then
        read -p "$PROJECT_DESKTOP_NAME directory already exists on Desktop. Overwrite? [y/N]: " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            rm -rf "$PROJECT_FINAL_PATH"
        else
            error_exit "Installation cancelled due to existing $PROJECT_DESKTOP_NAME directory"
        fi
    fi
    
    mv "$PROJECT_SOURCE_PATH" "$PROJECT_FINAL_PATH"
    
    BLOCKLY_BIN_PATH="$PROJECT_FINAL_PATH/$BLOCKLY_DIR_NAME/$BLOCKLY_BIN_NAME"
    if [ -f "$BLOCKLY_BIN_PATH" ]; then
        log_message "Setting permissions for $BLOCKLY_BIN_NAME..."
        chmod +x "$BLOCKLY_BIN_PATH"
    else
        log_message "Warning: $BLOCKLY_BIN_NAME not found at $BLOCKLY_BIN_PATH"
    fi
    
    SOURCE_PATH="$PROJECT_FINAL_PATH/$SOURCE_DIR_NAME"
    log_message "Testing build process in $SOURCE_PATH..."
    
    if [ -f "$SOURCE_PATH/gradlew" ]; then
        chmod +x "$SOURCE_PATH/gradlew"
        (cd "$SOURCE_PATH" && ./gradlew "$GRADLE_TASK") # Run in subshell
        if [ $? -eq 0 ]; then
            log_message "Build test completed successfully."
        else
            error_exit "Gradle build task failed in $SOURCE_PATH"
        fi
    else
        error_exit "gradlew not found in $SOURCE_PATH directory"
    fi
    
    VSIX_PATH="$PROJECT_FINAL_PATH/$VSIX_EXTENSION_NAME"
    if [ -f "$VSIX_PATH" ]; then
        log_message "Installing VS Code extension from $VSIX_PATH..."
        if command -v code &>/dev/null; then
            code --install-extension "$VSIX_PATH" --force
            log_message "VS Code extension installed successfully."
        else
            error_exit "Installation of VS Code extension failed."
        fi
    else
        # This was error_exit before, changing to warning as project might not always have it
        log_message "Warning: VS Code extension not found at $VSIX_PATH. Skipping extension install."
    fi
    
    log_message "Workshop project setup completed. Location: $PROJECT_FINAL_PATH"
    # Temp project files are cleaned up by cleanup_temp_files via trap or at the end
}

#=============================================================================
# MAIN EXECUTION
#=============================================================================

main() {
    # Setup logging
    
    # Create log file and ensure it's writable
    touch "$LOG_FILE"
    if [ ! -w "$LOG_FILE" ]; then
        echo "ERROR: Log file $LOG_FILE is not writable. Exiting."
        exit 1
    fi

    log_message "Starting Raspberry Pi setup..."
    log_message "Logfile: $LOG_FILE"
    log_message "Detected architecture: $(dpkg --print-architecture)"
    
    prompt_user_confirmation
    
    check_sudo_privileges # Needs to be after prompt if script is not run with sudo initially
    check_internet_connection
    
    mkdir -p "$TEMP_DIR"
    mkdir -p "$DOWNLOADS_DIR" # Ensure Downloads dir exists
    
    update_system
    install_java
    install_vscode
    setup_project
    
    cleanup_temp_files
    
    echo
    echo "==================== INSTALLATION SUMMARY ===================="
    log_message "Java version: $( (export JAVA_HOME=$JAVA_HOME && $JAVA_HOME/bin/java -version 2>&1 | head -1) || echo 'Java not found or JAVA_HOME not set correctly in script summary' )"
    log_message "VS Code version: $(code --version 2>/dev/null | head -1 || echo 'VS Code installation needs verification')"
    log_message "Workshop project location: $DESKTOP_DIR/$PROJECT_DESKTOP_NAME"
    log_message "Logfile: $LOG_FILE"
    echo "=============================================================="
    echo
    log_message "Installation completed successfully!"
    log_message "Please restart your terminal or run 'source /etc/profile' or 'source /etc/profile.d/java_home.sh' to apply Java environment changes."
    
    echo
    read -p "Would you like to reboot now to ensure all changes take effect? [y/N]: " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log_message "Rebooting system..."
        sudo reboot
    fi
}

# Error handling
set -e # Exit immediately if a command exits with a non-zero status.
trap cleanup_temp_files EXIT # Call cleanup_temp_files on script exit (normal or error)

# Run main function, redirecting all output to log and stdout
main "$@"