package com.example.jautograder;

import java.io.*;
import java.net.*;

public class Client {
    public String strClient = "1 1 1 -1", strServer, pic;
    public Socket socketClient;
    public DataInputStream fromServer;
    public DataOutputStream toServer;

    public void input (double longitude, double latitude, double bearing, double speed) throws UnsupportedEncodingException {
        strClient = String.valueOf(longitude) + " " + String.valueOf(latitude) + " " + String.valueOf(bearing) + " " + String.valueOf(speed);
    }
}
