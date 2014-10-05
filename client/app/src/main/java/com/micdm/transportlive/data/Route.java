package com.micdm.transportlive.data;

import java.util.List;

public class Route {

    private final int number;
    private final List<Direction> directions;

    public Route(int number, List<Direction> directions) {
        this.number = number;
        this.directions = directions;
    }

    public int getNumber() {
        return number;
    }

    public List<Direction> getDirections() {
        return directions;
    }

    public Direction getDirectionById(int id) {
        for (Direction direction: directions) {
            if (direction.getId() == id) {
                return direction;
            }
        }
        return null;
    }

    public String getStart() {
        return directions.get(0).getStart();
    }

    public String getFinish() {
        return directions.get(0).getFinish();
    }
}
