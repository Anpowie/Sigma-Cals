package com.schnurritv.sigmacals.storage;

import com.schnurritv.sigmacals.util.ByteUtil;
import com.schnurritv.sigmacals.util.Debug;

import java.nio.ByteBuffer;

public class Meal {

    static final int AMMOUNT_BYTES_UNTIL_NAME_STARTS = Integer.BYTES * 2;
    public static final String NO_NAME = "!@#$%";
    static final byte[] NO_NAME_BYTES = NO_NAME.getBytes();

    public String getName() {
        return name;
    }

    private final String name;
    private final int calories, proteins;

    public Meal(int calories, int proteins) {
        this.calories = calories;
        this.proteins = proteins;
        this.name = NO_NAME;
    }

    public Meal(int calories, int proteins, String name) {
        this.calories = calories;
        this.proteins = proteins;
        this.name = name;
    }

    public int getCalories() {
        return calories;
    }

    public int getProteins() {
        return proteins;
    }

    public byte[] serialize() {

        byte[] nameBytes = name.getBytes();
        return ByteBuffer.allocate(AMMOUNT_BYTES_UNTIL_NAME_STARTS + nameBytes.length)
                .putInt(calories)
                .putInt(proteins)
                .put(nameBytes)
                .array();
    }

    public static Meal deserialize(byte[] data) {


        return new Meal(
                ByteUtil.getInt(data[0], data[1], data[2], data[3]),
                ByteUtil.getInt(data[4], data[5], data[6], data[7]),
                ByteUtil.getString(AMMOUNT_BYTES_UNTIL_NAME_STARTS, data)
        );
    }
}
