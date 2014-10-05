package com.micdm.transportlive.misc;

import java.util.List;
import java.util.Random;

public class RandomItemSelector {

    private static final Random generator = new Random();

    public static <ItemType> ItemType get(List<ItemType> items) {
        int index = generator.nextInt(items.size());
        return items.get(index);
    }
}
