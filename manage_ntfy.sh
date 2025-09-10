#!/bin/zsh

# set -x
# exec &>> /tmp/ntfy_notifier.log

# ntfy.sh Notification Manager
# Manages the ntfy notifier script
# Usage: ./manage_ntfy.sh <directory_containing_ntfy_notifier.sh> [command]
# Example: ./manage_ntfy.sh /Users/mpardhu/Documents/Personal_Projects/SMSSyncer restart


LOCK_FILE="/tmp/ntfy_notifier.lock"
LOG_FILE="/tmp/ntfy_notifier.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Function to check if notifier is running
is_running() {
    if [ -f "$LOCK_FILE" ]; then
        local pid=$(cat "$LOCK_FILE" 2>/dev/null)
        if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
            return 0  # Running
        fi
    fi
    return 1  # Not running
}

# Function to get PID if running
get_pid() {
    if is_running; then
        cat "$LOCK_FILE" 2>/dev/null
    else
        echo ""
    fi
}

display_notification() {
    local message="$1"
    local title="$2"
    # check if osascript is installed
    if ! command -v osascript &> /dev/null; then
        print_status "${title}: ${message}"
    fi
    osascript -e "display notification \"$message\" with title \"$title\" sound name \"Submarine\""
}

# Get the directory containing ntfy_notifier.sh from first argument
SCRIPT_DIR="$1"
print_status "Running ${SCRIPT_DIR}/ntfy_notifier.sh"
if [ -z "$SCRIPT_DIR" ]; then
    print_error "Error: Directory containing ntfy_notifier.sh must be provided as first argument"
    print_error "Usage: $0 <directory_containing_ntfy_notifier.sh> [command]"
    print_error "Example: $0 /Users/mpardhu/Documents/Personal_Projects/SMSSyncer restart"
    exit 1
fi

# Set the notifier script path
NOTIFIER_SCRIPT="$SCRIPT_DIR/ntfy_notifier.sh"

# Verify the notifier script exists
if [ ! -f "$NOTIFIER_SCRIPT" ]; then
    print_error "ntfy_notifier.sh not found in directory: $SCRIPT_DIR"
    exit 1
fi

# Function to start the notifier
start_notifier() {
    if is_running; then
        local pid=$(get_pid)
        print_warning "Notifier is already running (PID: $pid)"
        return 1
    fi
    
    print_status "Starting ntfy notifier..."
    print_status "Notifier script: $NOTIFIER_SCRIPT"
    print_status "Log file: $LOG_FILE"

    # read the file /Users/Shared/SMS_Syncer_Listener/config.json and get the topic name and password
    # first check if the file is there
    if [ -f "/Users/Shared/SMS_Syncer_Listener/config.json" ]; then
        local topic_name=$(jq -r '.topic_name' "/Users/Shared/SMS_Syncer_Listener/config.json")
        local encryption_password=$(jq -r '.encryption_password' "/Users/Shared/SMS_Syncer_Listener/config.json")
        
        # if the field is "null" or empty, then print an error
        # note that jq will return as "null" if the field is not present
        if [ "$topic_name" = "null" ] || [ -z "$topic_name" ]; then
            print_error "Error: topic_name field is null or empty in config.json. Set it by running 'echo '{\"topic_name\":\"<your_topic_name>\",\"encryption_password\":\"<your_password>\"}' > /Users/Shared/SMS_Syncer_Listener/config.json'"
            exit 1
        fi
        
        if [ "$encryption_password" = "null" ] || [ -z "$encryption_password" ]; then
            print_error "Error: encryption_password field is null or empty in config.json. Set it by running 'echo '{\"topic_name\":\"<your_topic_name>\",\"encryption_password\":\"<your_password>\"}' > /Users/Shared/SMS_Syncer_Listener/config.json'"
            exit 1
        fi
        
        print_status "Topic name: $topic_name"
        print_status "Encryption password: [HIDDEN]"
    else
        print_error "Error: config.json file not found. Create it by running 'echo '{\"topic_name\":\"<your_topic_name>\",\"encryption_password\":\"<your_password>\"}' > /Users/Shared/SMS_Syncer_Listener/config.json'"
        exit 1
    fi
    
    # Start in background and redirect output
    nohup "$NOTIFIER_SCRIPT" "$topic_name" "$encryption_password" "$LOG_FILE"  >> /dev/null 2>&1 &
    
    # Wait a moment and check if it started successfully
    sleep 2
    if is_running; then
        local pid=$(get_pid)
        print_status "Notifier started successfully (PID: $pid)"
        display_notification "Notifier started successfully (PID: $pid)" "SMSSyncer"
        return 0
    else
        print_error "Failed to start notifier"
        display_notification "Failed to start notifier" "SMSSyncer"
        return 1
    fi
}

# Function to stop the notifier
stop_notifier() {
    if ! is_running; then
        print_warning "Notifier is not running"
        return 1
    fi
    
    local pid=$(get_pid)
    print_status "Stopping notifier (PID: $pid)..."
    
    # Send SIGTERM to the process
    kill -TERM "$pid" 2>/dev/null
    
    # Wait for graceful shutdown
    local count=0
    while is_running && [ $count -lt 10 ]; do
        sleep 1
        ((count++))
    done
    
    # Force kill if still running
    if is_running; then
        print_warning "Force killing notifier..."
        kill -KILL "$pid" 2>/dev/null
        sleep 1
    fi
    
    # Clean up lock file
    rm -f "$LOCK_FILE"
    
    if ! is_running; then
        print_status "Notifier stopped successfully"
        return 0
    else
        print_error "Failed to stop notifier"
        return 1
    fi
}

# Function to restart the notifier
restart_notifier() {
    print_status "Restarting notifier..."
    stop_notifier
    sleep 0.1
    start_notifier
}

# Function to show status
show_status() {
    echo -e "${BLUE}=== ntfy.sh Notifier Status ===${NC}"
    
    if is_running; then
        local pid=$(get_pid)
        echo -e "${GREEN}Status:${NC} Running (PID: $pid)"
        echo -e "${GREEN}Lock file:${NC} $LOCK_FILE"
        echo -e "${GREEN}Log file:${NC} $LOG_FILE"
        
        # Show recent log entries
        if [ -f "$LOG_FILE" ]; then
            echo -e "\n${BLUE}Recent log entries:${NC}"
            tail -n 5 "$LOG_FILE" 2>/dev/null | while read line; do
                echo "  $line"
            done
        fi
    else
        echo -e "${RED}Status:${NC} Not running"
        echo -e "${GREEN}Lock file:${NC} $LOCK_FILE"
        echo -e "${GREEN}Log file:${NC} $LOG_FILE"
    fi
}

# Function to show logs
show_logs() {
    if [ -f "$LOG_FILE" ]; then
        echo -e "${BLUE}=== ntfy.sh Notifier Logs ===${NC}"
        tail -n 20 "$LOG_FILE" 2>/dev/null
    else
        print_warning "Log file not found: $LOG_FILE"
    fi
}

# Function to show help
show_help() {
    echo -e "${BLUE}ntfy.sh Notification Manager${NC}"
    echo ""
    echo "Usage: $0 <directory_containing_ntfy_notifier.sh> [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  start     Start the ntfy notifier"
    echo "  stop      Stop the ntfy notifier"
    echo "  restart   Restart the ntfy notifier (default)"
    echo "  status    Show current status"
    echo "  logs      Show recent logs"
    echo "  help      Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 /Users/mpardhu/Documents/Personal_Projects/SMSSyncer start    # Start the notifier"
    echo "  $0 /Users/mpardhu/Documents/Personal_Projects/SMSSyncer status   # Check if running"
    echo "  $0 /Users/mpardhu/Documents/Personal_Projects/SMSSyncer logs     # View recent logs"
    echo "  $0 /Users/mpardhu/Documents/Personal_Projects/SMSSyncer          # Restart (default)"
}

# Main execution
case "${2:-restart}" in
    start)
        start_notifier
        ;;
    stop)
        stop_notifier
        ;;
    restart)
        restart_notifier
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Unknown command: $2"
        echo ""
        show_help
        exit 1
        ;;
esac
