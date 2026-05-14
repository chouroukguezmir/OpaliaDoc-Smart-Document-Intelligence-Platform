package com.example.demo.model.embedded;

import lombok.Data;

@Data
public class VpnEntry {
    private String vpnAccount;
    private String date;
    private String from;
    private String to;
    private String provisionalAllTime;
    private String why;
}