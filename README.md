# VoiceNote

This application will transcribe speech to text using the Google Speech Recognizer API.

## Installation

Install the `VoiceNote.apk` package located in the Releases section.

## Usage

Upon opening the app, it will ask for microphone permissions. Allow this for the app to function properly.


A brief usage example of this application would be as follows:

1. Accept permissions
2. Press the record button
3. Speak
4. View transcription
5. Stop recording or press **SAVE** (or **CLEAR** if desired)
6. Enter desired title or leave blank for “Untitled #”
7. Press **HISTORY** to view the saved note and previous notes

## Features & Design

All layouts use dynamic constraints and work in both portrait and landscape orientations. Text view variables are saved through rotation change. 

The theme is inspired by the Material design found on modern Android devices.

All assets (microphone icon, settings icon, launcher icon) are default vector assets in Android Studio.

### 1. Main Activity

The main activity page has the following elements and features:

| Element | Description |
| --- | --- |
| Transcription text view | Holds the transcribed text in a selectable window. |
| Record button | Toggles recording status on and off. |
| Current recording timer | Show the elapsed time and is tinted red while recording. |
| Clear button | Clears the transcribed text and stops recording. |
| Save button | Saves the contents of the text view, the timestamp, and a user provided title to the notes database. |
| State text view | Indicates the current state of the SpeechRecognizer: Ready, Stopped, Listening, Saving, Cleared/Initialized |
| History button | Opens the note history activity. |
| Settings button | Opens the app preferences. |

### 2. History Activity

The history activity will show a list of all the notes in the database. It is a bottom stack view for improved reachability.

- Long pressing on a card will show the note contents in a alert dialog and show the exact timestamp.
- The timestamp in the history feed uses a more readable “3 minutes ago” or ”3 days ago” format.
- The text in the cards is also selectable for copying and pasting.

### 3. Settings Activity

The settings for this application are:

- Dark mode/light mode toggle
- Clear notes database with confirmation dialog
