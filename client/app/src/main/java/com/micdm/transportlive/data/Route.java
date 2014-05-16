package com.micdm.transportlive.data;

import java.util.ArrayList;
import java.util.List;

public class Route {

    public final int number;
    public final List<Direction> directions = new ArrayList<Direction>();

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
        return directions.get(0).getStart();
    }

    public String getFinish() {
        return directions.get(0).getFinish();
    }
}
