package com.micdm.transportlive.data;

import java.util.ArrayList;

public class Route {

    public int number;
    public boolean isSelected;
    public ArrayList<Point> points = new ArrayList<Point>();
    public ArrayList<Direction> directions = new ArrayList<Direction>();

    public Route(int number, boolean isSelected) {
        this.number = number;
        this.isSelected = isSelected;
    }

    public Route(int number) {
        this(number, false);
    }

    public Direction getDirectionById(int id) {
        for (Direction direction: directions) {
            if (direction.id == id) {
                return direction;
            }
        }
        return null;
    }
}
