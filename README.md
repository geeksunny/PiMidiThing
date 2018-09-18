# PiMidiThing
Turn a *Raspberry Pi 3* running *Android Things* into a USB MIDI host, configurable MIDI router with added features, and (eventually) more!

## Features
#### Implemented
* **MIDI Routing** - Route MIDI messages from one or more inputs to one or more outputs.
* **Channel Filter** - Specify which MIDI channels to listen for, using either a whitelist or blacklist. Map traffic from one channel to another.
* **Chord Filter** - Add additional notes for on-the-fly chords.
* **Message Type Filter** - Filter out messaged based on MessageType, using either a whitelist or blacklist.
* **Velocity Filter** - Enforce a static or scaled velocity to incoming notes, or drop notes entirely if they do not fall within a specified value range.
* **Transpose Filter** - Transpose notes received to another octave.
#### Planned / In Progress
* **MIDI-CC maping** - Map MIDI-CC control messages to controlling software features.
* **MIDI Clock Master** - Control synchronized playback for one or more output devices.
* **Analog Clock Sync** - Synchronize output devices using an analog-click signal. *(Teenage Engineering Pocket Operators, Korg Volcas)*
* **Config Sync** - Automatic configuration sync & reload to/from USB storage for quick & easy updates from your computer.
* **Sysex file support** - Parse and transmit .sysex files to output devices.
* **Sense HAT UI** - Interact with the software without the need for an external display. Use the 8x8 LED matrix to see messages and menus, and use the 5-button joystick to navigate.
