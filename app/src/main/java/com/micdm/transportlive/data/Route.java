package com.micdm.transportlive.data;

import java.util.ArrayList;
import java.util.List;

public class Route {

    public int number;
    public List<Point> points = new ArrayList<Point>();
    public List<Direction> directions = new ArrayList<Direction>();

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

    public String getStart() {
        return directions.get(0).getStart().name;
    }

    public String getFinish() {
        return directions.get(0).getFinish().name;
    }
}
