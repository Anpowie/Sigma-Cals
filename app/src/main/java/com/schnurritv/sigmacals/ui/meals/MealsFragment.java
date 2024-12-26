package com.schnurritv.sigmacals.ui.meals;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.schnurritv.sigmacals.R;
import com.schnurritv.sigmacals.databinding.FragmentMealsBinding;
import com.schnurritv.sigmacals.storage.Day;
import com.schnurritv.sigmacals.storage.Meal;
import com.schnurritv.sigmacals.storage.Storage;
import com.schnurritv.sigmacals.util.Debug;
import com.schnurritv.sigmacals.util.Easing;
import com.schnurritv.sigmacals.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MealsFragment extends Fragment {

    public FragmentMealsBinding binding = null;
    int width;
    public boolean hasRemovalAnimationPlaying = false;

    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        if(binding != null)
            return binding.getRoot();

        this.binding = FragmentMealsBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    ViewGroup layout;

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layout = view.findViewById(R.id.meals_container);
        Button addButton = view.findViewById(R.id.add_meal_button);

        layout.post(() -> {
            width = layout.getWidth();
            Day today = Storage.getToday();
            for(Meal meal : today.getMeals())
                createMealBlock(layout, addButton, meal);
        });

        addButton.setOnClickListener((v) -> {
            if(hasRemovalAnimationPlaying)
                return;

            createMealBlock(layout, (Button) v, null);

            // wait a bit, then tell the main thread to scroll down
            Util.runAfter(50, () -> ((ScrollView)view.findViewById(R.id.meals_scroll)).fullScroll(View.FOCUS_DOWN));
        });

        layout.addView(new ButtonManagerView(getContext(), addButton, this), layout.getChildCount()-4);

        layout.setOnClickListener((view1) -> {
            Debug.log("tried");
            addButton.invalidate();
        });

    }

    MealUi createMealBlock(ViewGroup layout, Button addButton, @Nullable Meal data) {
        MealUi details = new MealUi(getContext(), addButton, this, data);
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, MealUi.BOX_HEIGHT + MealUi.MARGIN);
        layout.addView(details, layout.getChildCount()-4, params);
        return details;
    }

    public void removeView(MealUi view) {
        hasRemovalAnimationPlaying = true;

        Util.runAfter((int) MealUi.FADE_OUT_TIME_MILLIS, () -> {
            int indexOfRemoved = layout.indexOfChild(view);
            layout.removeView(view);

            if(indexOfRemoved == -1)
                return;

            int childCount = layout.getChildCount();
            int indexOfLastPossibleElement = childCount - 5;

            for(int i = indexOfRemoved; i <= indexOfLastPossibleElement; i++)
                ((MealUi)layout.getChildAt(i)).startMovingDownAnimation();

            ((ButtonManagerView) layout.getChildAt(childCount-1)).startMovingButton(indexOfRemoved);
            trySaveMeals();

        });
    }

    public void trySaveMeals() {

        List<Meal> meals = new ArrayList<>();

        for(int i = 0; i < layout.getChildCount(); i++) {
            View view = layout.getChildAt(i);

            if(!(view instanceof MealUi))
                continue;

            Meal meal = ((MealUi)view).turnIntoMeal();

            if(meal != null)
                meals.add(meal);
        }

        Storage.getToday().setMeals(meals);
        Storage.save(getContext());
    }

    @Override
    public void onDestroyView() {

        trySaveMeals();

        super.onDestroyView();
    }

    static class ButtonManagerView extends View {

        final Button addButton;
        final MealsFragment parent;

        public ButtonManagerView(Context context, Button addButton, MealsFragment parent) {
            super(context);
            this.addButton = addButton;
            this.parent = parent;
            setWillNotDraw(false);


        }

        long timeWhenHadToMoveDown = -1;
        int lastIndexRemoved = 0;

        void startMovingButton(int indexRemoved) {
            if(timeWhenHadToMoveDown != -1)
                return;

            lastIndexRemoved = indexRemoved;
            timeWhenHadToMoveDown = System.currentTimeMillis();
            invalidate();

            // attempt on forcing the button to be redrawn.. idk if this works
            Animation animation = new TranslateAnimation(0,0,0,0,0,0,0,0);
            animation.setDuration((long) MealUi.MOVE_DOWN_ANIMATION_MILLIS);
            startAnimation(animation);
        }

        int lastMoveOffsetY = 0;




        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if(timeWhenHadToMoveDown == -1)
                return;

            float rawP = Math.min(1,  (System.currentTimeMillis() - timeWhenHadToMoveDown) / MealUi.MOVE_DOWN_ANIMATION_MILLIS);
            float animP = Easing.easeOutBounce(rawP);
            int moveOffsetY = (int) ((1-animP) * (MealUi.MARGIN + MealUi.BOX_HEIGHT));

            addButton.offsetTopAndBottom(-lastMoveOffsetY + moveOffsetY);
            lastMoveOffsetY = moveOffsetY;

            Debug.log(rawP);

            if(rawP == 1) {
                lastMoveOffsetY = 0;
                timeWhenHadToMoveDown = -1;
                lastIndexRemoved = 0;

                parent.hasRemovalAnimationPlaying = false;
                Debug.log("removed");
                return;
            }

            invalidate();
        }
    }
}