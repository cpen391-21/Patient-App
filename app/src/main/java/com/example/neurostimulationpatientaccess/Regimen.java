package com.example.neurostimulationpatientaccess;

import android.util.Pair;

import java.util.ArrayList;

public class Regimen {
    public String regimenName;
    //String is wave category, pair is of <frequency, amplitude>
    public ArrayList<Pair<String, Pair<Double, Double>>> regimenWaves;
    public double duration;
    public double offset;
    public ArrayList<String> commands;

    public Regimen() {}

    public Regimen(String regimenName, ArrayList<Pair<String, Pair<Double, Double>>> regimenWaves, double duration, double offset) {
        this.regimenName = regimenName;
        this.regimenWaves = regimenWaves;
        this.duration = duration;
        this.offset = offset;
        //types of waves "square", "sine", "triangle", "random", "offset"
        ArrayList<String> commands = new ArrayList<>();

        commands.add("EN+NEW_WAVE,"+ Math.round(duration*10.0)/10.0 +"\r");
        for (int i = 0; i < regimenWaves.size(); i++) {
            String waveCategory = regimenWaves.get(i).first;
            double frequency = Math.round(regimenWaves.get(i).second.first * 10.0) / 10.0;
            double amplitude = Math.round(regimenWaves.get(i).second.second * 10.0) / 10.0;

            if (waveCategory.equals("square")) {
                commands.add("EN+ADD_SQUARE," + frequency + "," + amplitude + "\r");
            } else if (waveCategory.equals("sine")) {
                commands.add("EN+ADD_SINE," + frequency + "," + amplitude + "\r");
            } else if (waveCategory.equals("triangle")) {
                commands.add("EN+ADD_TRIANGLE," + frequency + "," + amplitude + "\r");
            } else if (waveCategory.equals("random")) {
                commands.add("EN+ADD_RANDOM," + amplitude + "\r");
            }
        }
        commands.add("EN+ADD_OFFSET," + offset + "\r");
        commands.add("EN+START_WAVE\r");
        commands.add("EN+STOP_WAVE\r");
        this.commands = commands;
    }
}
