package com.micdm.transportlive.data;

import java.util.ArrayList;

public class Route {

    public int number;
    public ArrayList<Point> points = new ArrayList<Point>();
    public ArrayList<Direction> directions = new ArrayList<Direction>();

    public Route(int number) {
        this.number = number;
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
