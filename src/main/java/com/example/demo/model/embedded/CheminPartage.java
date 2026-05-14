package com.example.demo.model.embedded;

import lombok.Data;

@Data
public class CheminPartage {
    private String chemin;
    private Boolean lecture;
    private Boolean modification;
}