package com.radicalninja.pimidithing.midi.router.filter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.radicalninja.pimidithing.midi.MidiMessage;
import com.radicalninja.pimidithing.util.MathUtils;

import java.util.HashMap;
import java.util.Map;

public class ChordFilter extends BaseFilter {

    private static final String KEY_CHORD = "chord";

    private static final int NOTE_MIN = 0;
    private static final int NOTE_MAX = 127;

    static final Map<String, int[]> chords = new HashMap<>(27);
    static {
        chords.put("DISABLED", new int[0]);
        chords.put("MAJOR3", new int[]{0, 4, 7});
        chords.put("MINOR3", new int[]{0, 3, 7});
        chords.put("MAJOR3_LO", new int[]{-5, 0, 4});
        chords.put("MINOR3_LO", new int[]{-5, 0, 3});
        chords.put("MAJOR2", new int[]{0, 4});
        chords.put("MINOR2", new int[]{0, 3});
        chords.put("DIM", new int[]{0, 3, 6, 9});
        chords.put("AUG", new int[]{0, 4, 8, 10});
        chords.put("SUS2", new int[]{0, 2, 7});
        chords.put("SUS4", new int[]{0, 5, 7});
        chords.put("7SUS2", new int[]{0, 2, 7, 10});
        chords.put("7SUS4", new int[]{0, 5, 7, 10});
        chords.put("6TH", new int[]{0, 4, 7, 9});
        chords.put("7TH", new int[]{0, 4, 7, 10});
        chords.put("9TH", new int[]{0, 4, 7, 10, 14});
        chords.put("MAJOR7TH", new int[]{0, 4, 7, 11});
        chords.put("MAJOR9TH", new int[]{0, 4, 7, 11, 14});
        chords.put("MAJOR11TH", new int[]{0, 4, 7, 14, 17});
        chords.put("MINOR6TH", new int[]{0, 3, 7, 9});
        chords.put("MINOR7TH", new int[]{0, 3, 7, 10});
        chords.put("MINOR9TH", new int[]{0, 3, 7, 10, 14});
        chords.put("MINOR11TH", new int[]{0, 3, 7, 14, 17});
        chords.put("POWER2", new int[]{0, 7});
        chords.put("POWER3", new int[]{0, 7, 12});
        chords.put("OCTAVE2", new int[]{0, 12});
        chords.put("OCTAVE3", new int[]{0, 12, 24});
    }

    private String chordName;
    private boolean disabled;
    private int[] offsets;

    public ChordFilter(JsonObject settings) {
        super(settings);
    }

    @Override
    public void onSettings(JsonObject settings) {
        final JsonElement chordJson = settings.get(KEY_CHORD);
        String chordName;
        if (null != chordJson && chordJson.isJsonPrimitive()) {
            chordName = chordJson.getAsString();
            if (!chords.containsKey(chordName)) {
                chordName = "DISABLED";
            }
        } else {
            chordName = "DISABLED";
        }
        setChord(chordName);
    }

    @Override
    public JsonObject getSettings() {
        final JsonObject json = new JsonObject();
        json.addProperty(KEY_CHORD, chordName);
        return json;
    }

    @Override
    Result onProcess(MidiMessage message) {
        if (disabled || !message.hasProperty(MidiMessage.PROPERTY_NAME_NOTE)) {
            return new Result(message);
        }
        final MidiMessage[] result = new MidiMessage[offsets.length];
        final int note = message.getProperty(MidiMessage.PROPERTY_NAME_NOTE);
        for (int i = 0; i < offsets.length; i++) {
            final int offset = offsets[i];
            final int _note = note += offset;
            if (!MathUtils.withinRange(_note, NOTE_MIN, NOTE_MAX)) {
                continue;
            }
            final MidiMessage _message = new MidiMessage(message);
            _message.setProperty(MidiMessage.PROPERTY_NAME_NOTE, _note);
            result[i] = _message;
        }
        return new Result(result);
    }

    void setChord(final String chordName) {
        final int[] offsets = chords.get(chordName);
        if (null != offsets) {
            this.chordName = chordName;
            this.offsets = offsets;
            setDisabled(offsets.length == 0);
        }
    }

    void setDisabled(final boolean disabled) {
        this.disabled = disabled;
    }

}
