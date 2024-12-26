package com.schnurritv.sigmacals.ui.meals;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.expression.parser.Parser;
import com.schnurritv.sigmacals.R;
import com.schnurritv.sigmacals.storage.Meal;
import com.schnurritv.sigmacals.util.Debug;
import com.schnurritv.sigmacals.util.Easing;

public class MealUi extends ViewGroup {

    static final float FADE_IN_TIME_MILLIS = 666;
    static final float MOVE_DOWN_ANIMATION_MILLIS = 666;
    static final float FADE_OUT_TIME_MILLIS = 666;
    public static final int REMOVE_BUTTON_WIDTH = 150;
    public static final int NAME_HEIGHT = 275;
    public static final int PROTEINS_HEIGHT = 400;
    public static final int CALS_HEIGHT = 525;
    public static final int BOX_HEIGHT = 500;
    public static final int MARGIN = 125;
    public static final float NUMBER_WIDTH_FACTOR = 0.3f;
    final String ALLOWED_INPUT_FOR_CALCULATION = "[0123456789+*./]";

    final BackgroundBox box;
    final EditText nameEdit, calsEdit, proteinsEdit;
    final Button addButton, removeThisButton;
    final long timeWhenCreated;
    public long timeWhenRemoved = -1;
    long timeWhenHadToMoveDown = -1;
    boolean skipSlideAnimation;
    final MealsFragment parent;

    public MealUi(Context context, Button addButton, MealsFragment parent, @Nullable Meal meal) {
        super(context);

        this.parent = parent;

        timeWhenCreated = meal == null ? System.currentTimeMillis() : 0; // skip animation if its being loaded up
        setClipChildren(false);

        this.addButton = addButton;
        this.skipSlideAnimation = meal != null;
        box = new BackgroundBox(getContext(), this);
        nameEdit = new EditText(context);
        proteinsEdit = new EditText(context);
        calsEdit = new EditText(context);
        removeThisButton = new Button(context);

        proteinsEdit.setInputType(InputType.TYPE_CLASS_PHONE);
        calsEdit.setInputType(InputType.TYPE_CLASS_PHONE);

        nameEdit.setSingleLine();
        proteinsEdit.setSingleLine();
        calsEdit.setSingleLine();

        proteinsEdit.setGravity(Gravity.CENTER);
        calsEdit.setGravity(Gravity.CENTER);

        nameEdit.setHint("optional");

        // remove the 0 once one start typing - add it in case its left empty
        // oh and calculate the result if there is
        proteinsEdit.setOnFocusChangeListener(makeFocusThread(proteinsEdit));
        calsEdit.setOnFocusChangeListener(makeFocusThread(calsEdit));

        // only allow stuff needed for calculation
        proteinsEdit.setFilters(createIllegalChecker());
        calsEdit.setFilters(createIllegalChecker());

        Drawable icon = ContextCompat.getDrawable(context, R.drawable.minus);
        removeThisButton.setPadding(0,0,0,0);
        removeThisButton.setBackground(icon);
        removeThisButton.setOnClickListener((v) -> {
            if(parent.hasRemovalAnimationPlaying)
                return;

            parent.removeView(this);
            timeWhenRemoved = System.currentTimeMillis();
            removeView(nameEdit);
            removeView(proteinsEdit);
            removeView(calsEdit);
            removeView(removeThisButton);
            box.invalidate();
        });

        addView(box);
        addView(nameEdit);
        addView(proteinsEdit);
        addView(calsEdit);
        addView(removeThisButton);


        if(meal != null) {
            String name = meal.getName();

            if(!Meal.NO_NAME.equals(name))
                nameEdit.setText(name);

            int proteins = meal.getProteins();
            int cals = meal.getCalories();
            proteinsEdit.setText(String.valueOf(proteins));
            calsEdit.setText(String.valueOf(cals));
            box.resizeIcons(cals, proteins, false);
        }

    }


    void startMovingDownAnimation() {
        timeWhenHadToMoveDown = System.currentTimeMillis();
        invalidate();
    }

    OnFocusChangeListener makeFocusThread(EditText edit) {
        return (v, hasFocus) -> {

            edit.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), hasFocus ? (edit == calsEdit ? R.color.cals : R.color.proteins) : R.color.text)));

            if(hasFocus)
                return;

            // calculate the field input
            try {
                edit.setText(String.valueOf(calculateInput(edit)));
                edit.setTextColor(ContextCompat.getColor(getContext(), R.color.text));
                parent.trySaveMeals();

            }
            catch (Exception e) {
                edit.setTextColor(ContextCompat.getColor(getContext(), R.color.error));
                return;
            }

            // adjust icon size
            try {
                box.resizeIcons(calculateInput(calsEdit), calculateInput(proteinsEdit), edit == calsEdit);
            }
            catch (Exception ignored) {}


        };
    }

    int calculateInput(EditText edit) throws Exception /*error in calculation. Can happen for inputs like "5..56 + 8"*/ {
        return (int)Parser.simpleEval(edit.getText().toString());
    }


    InputFilter[] createIllegalChecker() {
        return new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            StringBuilder filteredStringBuilder = new StringBuilder();

            // Iterate through the characters in the source and only keep allowed characters
            for (int i = 0; i < source.length(); i++) {
                char c = source.charAt(i);

                if (String.valueOf(c).matches(ALLOWED_INPUT_FOR_CALCULATION))
                    filteredStringBuilder.append(c);
            }

            return filteredStringBuilder.toString();
        }};
    }

    public Meal turnIntoMeal() {

        try {
            String proteinsText = proteinsEdit.getText().toString();
            String calsText = calsEdit.getText().toString();

            if("".equals(proteinsText))
                proteinsText = "0";

            if("".equals(calsText))
                calsText = "0";

            int proteins = Integer.parseInt(proteinsText);
            int calories = Integer.parseInt(calsText);

            if(calories == 0 && proteins == 0)
                return null;

            String name = nameEdit.getText().toString();

            if("".equals(name))
                return new Meal(calories, proteins);

            return new Meal(calories, proteins, name);

        }catch (Exception ignored) {}

        return null;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        box.layout(0, MARGIN, right, MARGIN + BOX_HEIGHT);
        int lineHeight = get1LineHeight();

        nameEdit.layout(3*MARGIN, NAME_HEIGHT - lineHeight, right - 2*MARGIN, NAME_HEIGHT);
        proteinsEdit.layout((int) (2.5f*MARGIN), PROTEINS_HEIGHT - lineHeight, (int) (2.5f*MARGIN + right * NUMBER_WIDTH_FACTOR), PROTEINS_HEIGHT);
        calsEdit.layout((int) (2.5f*MARGIN), CALS_HEIGHT - lineHeight, (int) (2.5f*MARGIN + right * NUMBER_WIDTH_FACTOR), CALS_HEIGHT);
        removeThisButton.layout(right - MARGIN - REMOVE_BUTTON_WIDTH, MARGIN, right - MARGIN, MARGIN + REMOVE_BUTTON_WIDTH);
        invalidate();
    }

    int lastXOffset = 0;
    int lastYOffset = 0;
    boolean setFocus = false;

    void manageSlideInAnimation() {
        boolean animOver = getRawP() >= 1;

        if(animOver && !setFocus) {
            proteinsEdit.requestFocus();
            setFocus = true;
            calsEdit.setText("");
            box.resetCalsIcon();
        }

        if(animOver)
            return;

        float animP = getFadeInP();
        int xOffset = (int) (-box.getRight() * (1f-animP));

        nameEdit.offsetLeftAndRight(-lastXOffset + xOffset);
        calsEdit.offsetLeftAndRight(-lastXOffset + xOffset);
        proteinsEdit.offsetLeftAndRight(-lastXOffset + xOffset);
        removeThisButton.offsetLeftAndRight(-lastXOffset + xOffset);

        int yOffset = (int) (-(BOX_HEIGHT + MARGIN) * (1-animP));
        addButton.offsetTopAndBottom(-lastYOffset + yOffset);

        lastXOffset = xOffset;
        lastYOffset = yOffset;
        invalidate();

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if(!skipSlideAnimation)
            manageSlideInAnimation();
        manageMoveDownAnimation();
    }

    int lastMoveOffsetY = 0;

    void manageMoveDownAnimation() {
        if(timeWhenHadToMoveDown == -1)
            return;

        float animP = Easing.easeOutBounce(Math.min(1, (System.currentTimeMillis() - timeWhenHadToMoveDown) / MOVE_DOWN_ANIMATION_MILLIS));
        int moveOffsetY = (int) ((1-animP) * (MARGIN + BOX_HEIGHT));

        box.offsetTopAndBottom(-lastMoveOffsetY + moveOffsetY);
        nameEdit.offsetTopAndBottom(-lastMoveOffsetY + moveOffsetY);
        calsEdit.offsetTopAndBottom(-lastMoveOffsetY + moveOffsetY);
        proteinsEdit.offsetTopAndBottom(-lastMoveOffsetY + moveOffsetY);
        removeThisButton.offsetTopAndBottom(-lastMoveOffsetY + moveOffsetY);

        invalidate();
        lastMoveOffsetY = moveOffsetY;

        if(animP == 1) {
            lastMoveOffsetY = 0;
            timeWhenHadToMoveDown = -1;
        }
    }

    int get1LineHeight() {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                48,
                getResources().getDisplayMetrics()
        );
    }

    public float getRawP() {
        return (System.currentTimeMillis() - timeWhenCreated) / FADE_IN_TIME_MILLIS;
    }

    public float getFadeInP() {
        if(skipSlideAnimation)
            return 1;

        return Easing.easeOutBack(Math.min(1, (System.currentTimeMillis() - timeWhenCreated) / FADE_IN_TIME_MILLIS));
    }
}
