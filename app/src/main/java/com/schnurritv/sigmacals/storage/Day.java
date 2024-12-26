package com.schnurritv.sigmacals.storage;


import com.schnurritv.sigmacals.util.ByteUtil;
import com.schnurritv.sigmacals.util.Debug;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Day {

    private final List<Meal> meals = new ArrayList<>();

    public Day() {}
    public Day(List<Meal> meals) {
        this.meals.addAll(meals);
    }



    public void addMeal(Meal meal) {
        meals.add(meal);
    }

    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals.clear();
        this.meals.addAll(meals);
    }

    public void removeMeal(int index) {
        meals.remove(index);
    }

    public static Day deserialize(byte[] data) {
        List<byte[]> collectedMealData = new ArrayList<>();
        List<Byte> currentMealData = new ArrayList<>();

        int indexWhenStringStarts = Integer.BYTES + Integer.BYTES; // the string starts after the cals(int) and proteins(int)

        for(int i = 0; i < data.length; i++) {

            byte currentByte = data[i];

            if(i > indexWhenStringStarts && currentByte == ByteUtil.STOP_SIGN) {

                // filthy way of turning a List<Byte> into a byte[] but it's also the easiest.
                byte[] mealData = new byte[currentMealData.size()];
                for(int i1 = 0; i1 < mealData.length; i1++)
                    mealData[i1] = currentMealData.get(i1);

                collectedMealData.add(mealData);
                currentMealData.clear();
                continue;
            }

            currentMealData.add(currentByte);
        }

        Day day = new Day();

        for(byte[] mealData : collectedMealData)
            day.addMeal(Meal.deserialize(mealData));

        return day;
    }

    public byte[] serialize() {

        List<byte[]> mealData = new ArrayList<>();
        int mealDataBytesAmount = 0;

        for(Meal meal : meals) {
            byte[] bytes = meal.serialize();
            mealData.add(bytes);
            mealDataBytesAmount += bytes.length;
        }

        byte[] dayData = new byte[
                    mealDataBytesAmount +
                    mealData.size() // After every meal, we place a stop sign and each stop sign takes 1 byte as well
                ];


        int index = 0;

        for(byte[] meal : mealData) {
            int amountOfBytesPerMeal = meal.length;
            System.arraycopy(meal, 0, dayData, index, amountOfBytesPerMeal); // save amountMeals
            index += amountOfBytesPerMeal;
            dayData[index] = ByteUtil.STOP_SIGN;
            index++;
        }

        return dayData;
    }

    public Stats getStats() {
        int calories = 0, proteins = 0;

        for(Meal meal : meals) {
            calories += meal.getCalories();
            proteins += meal.getProteins();
        }

        return new Stats(calories, proteins);
    }
}
