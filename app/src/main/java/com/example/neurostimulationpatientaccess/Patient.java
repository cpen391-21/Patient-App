package com.example.neurostimulationpatientaccess;

import java.util.ArrayList;

public class Patient {

    public String fullName, email, doctorEmail;
    //public ArrayList<Regimen> listOfRegimens;

    public Patient() {}

    public Patient(String fullName, String email, String doctorEmail/*, ArrayList<Regimen> listOfRegimens*/) {
        this.fullName = fullName;
        this.email = email;
        this.doctorEmail = doctorEmail;
        //this.listOfRegimens = listOfRegimens;
    }
}
