package net.splodgebox.monthlycrates.utils;

import lombok.NonNull;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class RandomCollection<E> {
    private final NavigableMap<Double, E> map = new TreeMap<>();
    private final Random random = new Random();
    private double total = 0;

    public void add(double weight, @NonNull E value) {
        if (weight > 0) {
            total += weight;
            map.put(total, value);
        }
    }

    @NonNull
    public E getRandomValue() {
        if (total == 0) throw new RuntimeException("Trying to get a random value from an empty RandomCollection");
        final double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }

    public void clear(){
        map.clear();
    }
}
