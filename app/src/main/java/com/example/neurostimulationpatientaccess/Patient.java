package com.example.neurostimulationpatientaccess;

public class Patient {

    public String fullName, email, doctorEmail;

    public Patient() {

    }

    public Patient(String fullName, String email, String doctorEmail) {
        this.fullName = fullName;
        this.email = email;
        this.doctorEmail = doctorEmail;
    }
}
