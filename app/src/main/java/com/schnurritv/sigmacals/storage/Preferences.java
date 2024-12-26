package com.schnurritv.sigmacals.storage;

import android.content.Context;
import android.content.SharedPreferences;
import com.schnurritv.sigmacals.util.Debug;

public class Preferences<T> {

    public static final int MIN_CALORIE_GOAL = 1500;
    public static final int MAX_CALORIE_GOAL = 4000;
    public static final float MIN_PROTEIN_GOAL = 1.6f;
    public static final float MAX_PROTEIN_GOAL = 2.2f;

    public static final Preferences<Integer> WEIGHT = new Preferences<>(79, "weight");
    public static final Preferences<Float> PROTEINS = new Preferences<>(2f, "proteins");
    public static final Preferences<Integer> CALORIES = new Preferences<>(2700, "calories");

    static final String PREFERENCES_KEY = "settings";
    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;

    private T value;
    private final String key;


    public Preferences(T defaultValue, String key) {
        this.value = defaultValue;
        this.key = key;
    }
    public static void load() {
        WEIGHT.value = preferences.getInt(WEIGHT.key, WEIGHT.value);
        PROTEINS.value = preferences.getFloat(PROTEINS.key, PROTEINS.value);
        CALORIES.value = preferences.getInt(CALORIES.key, CALORIES.getValue());
    }

    public static void saveAllValues() {
        editor.putInt(WEIGHT.key, WEIGHT.value);
        editor.putFloat(PROTEINS.key, PROTEINS.value);
        editor.putInt(CALORIES.key, CALORIES.value);
        editor.apply();
    }

    public static void init(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        editor = preferences.edit();

        load();
    }

    public static void saveValue(Preferences preference, Object value) {
        if(value instanceof Integer)
            editor.putInt(preference.key, (Integer) value);
        if(value instanceof Float)
            editor.putFloat(preference.key, (Float) value);
        if(value instanceof Boolean)
            editor.putBoolean(preference.key, (Boolean) value);
        if(value instanceof String)
            editor.putString(preference.key, (String) value);

        preference.value = value;
        editor.apply();
    }

    public T getValue() {
        return value;
    }

    public static int getDailyProteinsGoal() {
        return (int) (WEIGHT.value * PROTEINS.value);
    }

}
