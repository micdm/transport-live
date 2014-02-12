package com.micdm.transportlive.data;

import java.util.ArrayList;

public class Route {

    public int number;
    public boolean isChecked;
    public ArrayList<Direction> directions = new ArrayList<Direction>();

    public Route(int number, boolean isChecked) {
        this.number = number;
        this.isChecked = isChecked;
    }

    public Route(int number) {
        this(number, false);
    }
}
