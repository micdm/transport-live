package com.micdm.transportlive.data;

import java.util.ArrayList;

public class Route {

    public int number;
    public ArrayList<Direction> directions = new ArrayList<Direction>();
    public boolean isChecked;

    public Route(int number, boolean isChecked) {
        this.number = number;
        this.isChecked = isChecked;
    }

    public Route(int number) {
        this(number, false);
    }
}
