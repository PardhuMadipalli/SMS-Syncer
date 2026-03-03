# MAC Application

## How to create a Mac Application to listen to SMS messages

1. Open Automator application on your mac and choose Document type as `Application`
2. Choose `Run AppleScript` as first step of the application with the following contents
  ```
  on run {input, parameters}
      return POSIX path of (path to me) & "Contents/" & "Resources"
  end run
  ```
3. `Run Shell Script` by choosing "Pass Input" -> "as arguments". Copy paste the contents of [manage_ntfy.sh](./manage_ntfy.sh) here.
4. Create the application by naming it as `SMS Syncer Listener`. You will see an application with the same name in your `Applications`. 
5. Now copy the file [ntfy_notifier.sh](./ntfy_notifier.sh) to the application's relative path `Contents/Resources`.
6. Make the script executable by running `chmod +x ntfy_notifier.sh` in the Resources folder
7. Optionally you can add icon to this application by running
    ```bash
    cp AppIcon.icns /Applications/SMS\ Syncer\ Listener.app/Contents/Resources/ApplicationStub.icns
    ```
8. Start the application after writing the topic_name and password in `/Users/Shared/SMS_Syncer_Listener/config.json`.

## How to run the application
- Install `jq` using 
    ```bash
    brew install jq
    ```
- Double click the application to start it.
- Add the application to login items so that it starts automatically. `System Preferences` -> `Users & Groups` -> `Login Items` -> `+` -> Select the application.

## How to ship the mac application to others by creating a .dmg file

### Prerequisites
- Install `create-dmg` using `brew` by running `brew install create-dmg`

### Steps
- First, clean up the quarantine attributes and self-sign the app:
    ```bash
    xattr -cr /Applications/SMS\ Syncer\ Listener.app
    codesign --force --deep --sign - /Applications/SMS\ Syncer\ Listener.app
    ```
- Create a staging folder
    ```bash
    mkdir -p staging
    ```
- Copy the application to the staging folder
    ```bash
    cp -r "/Applications/SMS Syncer Listener.app" staging/
    ```
- Create the dmg file
    ```bash
    create-dmg \
      --volname "SMS Syncer Listener" \
      --volicon "AppIcon.icns" \
      --window-pos 200 120 \
      --window-size 800 400 \
      --icon-size 100 \
      --icon "SMS Syncer Listener.app" 200 190 \
      --hide-extension "SMS Syncer Listener.app" \
      --app-drop-link 600 185 \
      "sms_syncer_listener.dmg" \
      "staging/"
    ```

