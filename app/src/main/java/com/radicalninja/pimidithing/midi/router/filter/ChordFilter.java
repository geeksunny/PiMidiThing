package com.radicalninja.pimidithing.midi.router.filter;

import com.google.gson.JsonObject;
import com.radicalninja.pimidithing.midi.MidiMessage;

import java.util.HashMap;
import java.util.Map;

public class ChordFilter extends BaseFilter {

    static Map<String, Integer[]> chords = new HashMap<>(26);
    static {
        chords.put("MAJOR3", new Integer[]{0, 4, 7});
        chords.put("MINOR3", new Integer[]{0, 3, 7});
        chords.put("MAJOR3_LO", new Integer[]{-5, 0, 4});
        chords.put("MINOR3_LO", new Integer[]{-5, 0, 3});
        chords.put("MAJOR2", new Integer[]{0, 4});
        chords.put("MINOR2", new Integer[]{0, 3});
        chords.put("DIM", new Integer[]{0, 3, 6, 9});
        chords.put("AUG", new Integer[]{0, 4, 8, 10});
        chords.put("SUS2", new Integer[]{0, 2, 7});
        chords.put("SUS4", new Integer[]{0, 5, 7});
        chords.put("7SUS2", new Integer[]{0, 2, 7, 10});
        chords.put("7SUS4", new Integer[]{0, 5, 7, 10});
        chords.put("6TH", new Integer[]{0, 4, 7, 9});
        chords.put("7TH", new Integer[]{0, 4, 7, 10});
        chords.put("9TH", new Integer[]{0, 4, 7, 10, 14});
        chords.put("MAJOR7TH", new Integer[]{0, 4, 7, 11});
        chords.put("MAJOR9TH", new Integer[]{0, 4, 7, 11, 14});
        chords.put("MAJOR11TH", new Integer[]{0, 4, 7, 14, 17});
        chords.put("MINOR6TH", new Integer[]{0, 3, 7, 9});
        chords.put("MINOR7TH", new Integer[]{0, 3, 7, 10});
        chords.put("MINOR9TH", new Integer[]{0, 3, 7, 10, 14});
        chords.put("MINOR11TH", new Integer[]{0, 3, 7, 14, 17});
        chords.put("POWER2", new Integer[]{0, 7});
        chords.put("POWER3", new Integer[]{0, 7, 12});
        chords.put("OCTAVE2", new Integer[]{0, 12});
        chords.put("OCTAVE3", new Integer[]{0, 12, 24});
    }

    public ChordFilter(JsonObject settings) {
        super(settings);
    }

    @Override
    public void onSettings(JsonObject settings) {
        // TODO
    }

    @Override
    public JsonObject getSettings() {
        // TODO
        return null;
    }

    @Override
    Result onProcess(MidiMessage message) {
        // TODO!!!
        return null;
    }

    static class Chord {
        final String name;
        final int[] steps;

        Chord(String name, int[] steps) {
            this.name = name;
            this.steps = steps;
        }
    }

}
