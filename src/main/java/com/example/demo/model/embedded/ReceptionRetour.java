package com.example.demo.model.embedded;

import lombok.Data;

@Data
public class ReceptionRetour {
    private String modeleOrdinateur;
    private String modeleTelephone;
    private String modeleInternet;
    private String modeleAutre;
    private String nSerieOrdinateur;
    private String nSerieTelephone;
    private String nSerieChargeurOrdinateur;
    private String sacocheEtuiOrdinateur;
    private String nPuceInternet;
    private String dateReceptionVisa;
    private String dateRetourVisa;
}