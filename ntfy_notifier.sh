#!/bin/zsh

# ntfy.sh SSE Notification Script
# Connects to ntfy.sh topic and displays messages as system notifications

# Configuration
TOPIC_NAME="$1"  # Replace with your actual topic name
LOCK_FILE="/tmp/ntfy_notifier.lock"
LOG_FILE="$2"

LOG_THRESHOLD=1000
LOG_LINES_TO_REMOVE=100

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

if [ -z "$LOG_FILE" ]; then
    echo "Log file is not set. Using echo to print to console."
fi

rotate_log() {
    local file="$1"
    local threshold="$2"
    local lines_to_remove="$3"

    if [ ! -f "$file" ]; then
        echo "Error: File '$file' not found."
        return 0
    fi

    local total_lines=$(wc -l < "$file")

    if [ "$total_lines" -gt "$threshold" ]; then
        tail -n +$((lines_to_remove + 1)) "$file" > "$file.tmp"
        mv "$file.tmp" "$file"
        echo "File updated: Removed the first $lines_to_remove lines."
    fi
}


# Function to log messages
log_message() {
    # check if log_file variable is set. If not set, then use echo to print to console
    # take the first argument as color and then use that color to print the message
    local color="$1"
    local type="$2"
    local message="$3"

    if [ -z "$LOG_FILE" ]; then
        echo -e "${color}$(date '+%Y-%m-%d %H:%M:%S') - ${type}: ${message}${NC}"
    else
        rotate_log "$LOG_FILE" "$LOG_THRESHOLD" "$LOG_LINES_TO_REMOVE"
        printf "${color}$(date '+%Y-%m-%d %H:%M:%S') - %7s: %s${NC}\n" "$type" "$message" >> "$LOG_FILE"
    fi
}

log_info() {
    log_message "${NC}" "INFO" "$1"
}

log_success() {
    log_message "${GREEN}" "SUCCESS" "$1"
}

# Function to log errors
log_error() {
    log_message "${RED}" "ERROR" "$1"
}

# Function to log warnings
log_warning() {
    log_message "${YELLOW}" "WARNING" "$1"
}

# check if the first argument is a valid topic name
if [ -z "$TOPIC_NAME" ]; then
    log_error "Error: Topic name is not set.\nRun the script with the topic name as the first argument.\nExample: ./ntfy_notifier.sh <topic_name> <log_file_path>"
    exit 1
else
    log_info "Topic name: $TOPIC_NAME"
fi

# Dependency checks
if ! command -v jq &> /dev/null; then
    log_error "Error: jq is not installed. Install it by running 'brew install jq'"
    exit 1
fi


# Function to cleanup on exit
cleanup() {
    log_info "Shutting down ntfy notifier..."
    rm -f "$LOCK_FILE"
    exit 0
}

# Function to check if another instance is running
check_singleton() {
    if [ -f "$LOCK_FILE" ]; then
        local pid=$(cat "$LOCK_FILE" 2>/dev/null)
        if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
            log_error "Another instance is already running (PID: $pid)"
            exit 1
        else
            log_warning "Stale lock file found, removing..."
            rm -f "$LOCK_FILE"
        fi
    fi
    
    # Create lock file with current PID
    echo $$ > "$LOCK_FILE"
    log_success "Started ntfy notifier (PID: $$)"
}

# Use jq to get fields from json input
get_field_from_json() {
    local json="$1"
    local field="$2"
    # use printf so that new line characters don't cause any problem
    local value=$(printf "%s" "$json" | jq -r "$field")
    echo "$value"
}

# Function to display notification
show_notification() {
    local message="$1"
    local title="$2"
    # Limit message length for notification (macOS has limits)
    if [ ${#message} -gt 200 ]; then
        message="${message:0:197}..."
    fi
    
    # Escape quotes for osascript
    message=$(echo "$message" | sed 's/"/\\"/g')
    
    # Show notification
    osascript -e "display notification \"$message\" with title \"$title\" sound name \"Glass\"" 2>/dev/null
    
    if [ $? -eq 0 ]; then
        log_success "Notification displayed: ${message}"
    else
        log_error "Failed to display notification"
    fi
}

# Function to process SSE stream
process_sse() {
    local url="https://ntfy.sh/$TOPIC_NAME/json"
    
    log_info "Connecting to ntfy.sh topic: $TOPIC_NAME"
    log_info "SSE URL: $url"
    
    # Connect to SSE stream
    curl -s -N --no-buffer --max-time 180 "$url" | while IFS= read -r line; do
        # Skip empty lines
        [ -z "$line" ] && continue
        
        # Check if line contains JSON data
        if [[ "$line" == *"{"* ]]; then
            # Parse the JSON
            local event=$(get_field_from_json "$line" ".event")
            local message=$(get_field_from_json "$line" ".message")
            local title=$(get_field_from_json "$line" ".title")
            # Log all events for debugging
            log_info "Received event: $event"            
            # Process message events
            if [ "$event" = "message" ] && [ -n "$message" ]; then
                log_info "Processing message: ${message}"
                show_notification "$message" "$title"
            fi
        fi
    done
    
    # If we reach here, the connection was lost
    log_warning "Will reconnect in 0.1 second..."
    sleep 0.1
}

# Main execution
main() {
    # Set up signal handlers
    trap cleanup SIGINT SIGTERM
    
    # Check for singleton instance
    check_singleton
    
    # Create log file if it doesn't exist
    touch "$LOG_FILE"
    
    log_info "ntfy.sh notification script started"
    log_info "Log file: $LOG_FILE"
    log_info "Lock file: $LOCK_FILE"
    
    # Main loop with reconnection logic
    while true; do
        process_sse
    done
}

# Check if running on macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
    echo "Error: This script is designed for macOS"
    exit 1
fi

# Check if required tools are available
if ! command -v curl &> /dev/null; then
    echo "Error: curl is not installed"
    exit 1
fi

if ! command -v osascript &> /dev/null; then
    echo "Error: osascript is not available"
    exit 1
fi

# Run main function
main
