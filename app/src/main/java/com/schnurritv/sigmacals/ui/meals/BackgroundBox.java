package com.schnurritv.sigmacals.ui.meals;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.schnurritv.sigmacals.R;
import com.schnurritv.sigmacals.storage.Preferences;
import com.schnurritv.sigmacals.util.Easing;
import com.schnurritv.sigmacals.util.Util;

public class BackgroundBox extends View {

    static final float FADE_OUT_ROTATION_MAX_DEGREE = 75;
    static final float BOX_RADIUS = 60;
    static final float FONZ_SIZE = 60;
    static final float MINIMUM_ICON_SIZE = 0.5f;
    static final float DAILY_INTAKE_DIVISOR = 3f;
    static final long ICON_SIZE_TRANSITION_TIME_MILLIS = 666;


    final Bitmap bicepsPNG = BitmapFactory.decodeResource(getResources(), R.drawable.biceps);
    final Bitmap calsPNG = BitmapFactory.decodeResource(getResources(), R.drawable.kcal);
    final Rect pngSrc, proteinsMax, calsMax, proteinsNew, calsNew, proteinsLast, calsLast, proteinsCurrent, calsCurrent;
    final RectF boxSize;
    final Paint boxPaint, pngPaint, calsTextPaint, proteinsTextPaint, textPaint;
    final MealUi parent;

    int textX = 0;
    long changedValuesTime = 0;
    int width = 0;


    public BackgroundBox(Context context, MealUi parent) {
        super(context);

        this.parent = parent;
        this.boxSize = new RectF(MealUi.MARGIN, 0, 0, MealUi.BOX_HEIGHT);

        this.pngSrc = new Rect(0, 0, calsPNG.getWidth(), calsPNG.getHeight());

        this.proteinsMax = new Rect((int) (1.5f* MealUi.MARGIN), MealUi.PROTEINS_HEIGHT - 2* MealUi.MARGIN, (int) (2.5f* MealUi.MARGIN), MealUi.PROTEINS_HEIGHT  - MealUi.MARGIN);
        this.calsMax = new Rect((int) (1.5f* MealUi.MARGIN), MealUi.CALS_HEIGHT - 2*MealUi.MARGIN, (int) (2.5f* MealUi.MARGIN), MealUi.CALS_HEIGHT - MealUi.MARGIN);

        this.proteinsLast = cloneRect(proteinsMax);
        this.proteinsCurrent = cloneRect(proteinsMax);
        this.proteinsNew = cloneRect(proteinsMax);
        this.calsLast = cloneRect(calsMax);
        this.calsCurrent = cloneRect(calsMax);
        this.calsNew = cloneRect(calsMax);

        this.pngPaint = new Paint();
        this.pngPaint.setAntiAlias(true);
        this.pngPaint.setFilterBitmap(true);
        this.pngPaint.setDither(true);

        this.boxPaint = new Paint();
        this.boxPaint.setColor(ContextCompat.getColor(getContext(), R.color.background));

        this.calsTextPaint = new Paint();
        this.calsTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.cals));
        this.calsTextPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonzie.ttf"));
        this.calsTextPaint.setTextSize(FONZ_SIZE);

        this.proteinsTextPaint = new Paint();
        this.proteinsTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.proteins));
        this.proteinsTextPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonzie.ttf"));
        this.proteinsTextPaint.setTextSize(FONZ_SIZE);

        this.textPaint = new Paint();
        this.textPaint.setColor(ContextCompat.getColor(getContext(), R.color.text));
        this.textPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonzie.ttf"));
        this.textPaint.setTextSize(FONZ_SIZE);


    }

    void resizeIcons(float cals, float proteins, boolean editedCals) {
        // value is basically the relation to the goal divided by 3 - 3 because i guess most peps eat 3 meals a day, so peps would have to eat 3 such chonkers to get their goal
        // we dont resize if the value is 0 and we haven't even changed it

        if(!(cals == 0 && !editedCals))
            Util.resizeRect(calsNew, calsMax, Util.clamp(cals / (Preferences.CALORIES.getValue() / DAILY_INTAKE_DIVISOR), MINIMUM_ICON_SIZE, 1));

        if(!(proteins == 0 && editedCals))
            Util.resizeRect(proteinsNew, proteinsMax, Util.clamp(proteins / (Preferences.getDailyProteinsGoal() / DAILY_INTAKE_DIVISOR), MINIMUM_ICON_SIZE, 1));

        changedValuesTime = System.currentTimeMillis();
        invalidate();
    }

    public void resetCalsIcon() {
        cloneRect(calsLast, calsMax);
        cloneRect(calsCurrent, calsMax);
        cloneRect(calsNew, calsMax);

    }

    Rect cloneRect(Rect ref) {
        Rect target = new Rect();

        target.top = ref.top;
        target.left = ref.left;
        target.right = ref.right;
        target.bottom = ref.bottom;

        return target;
    }

    void cloneRect(Rect target, Rect ref) {
        target.top = ref.top;
        target.left = ref.left;
        target.right = ref.right;
        target.bottom = ref.bottom;
    }



    @Override
    public void layout(int left, int top, int right, int bottom) {
        super.layout(left, top, right, bottom);

        this.boxSize.right = right - MealUi.MARGIN;
        this.textX = (int) (2.5f * MealUi.MARGIN + right * MealUi.NUMBER_WIDTH_FACTOR);
        this.width = right;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        float xOffset = -boxSize.right * (1 - parent.getFadeInP());
        if( xOffset != 0) {
            canvas.translate(xOffset, 0);
            invalidate();
        }

        if(parent.timeWhenRemoved != -1) {
            float animP = Easing.easeInSine(Math.min(MealUi.FADE_OUT_TIME_MILLIS, System.currentTimeMillis() - parent.timeWhenRemoved) / MealUi.FADE_OUT_TIME_MILLIS);

            canvas.translate(animP * width/2f, animP * (MealUi.MARGIN + MealUi.BOX_HEIGHT));
            canvas.rotate(FADE_OUT_ROTATION_MAX_DEGREE * animP, width/2f, (MealUi.MARGIN + MealUi.BOX_HEIGHT) / 2f);
            canvas.scale(1 - animP, 1 - animP);
            canvas.saveLayerAlpha(0, 0, width, MealUi.BOX_HEIGHT + MealUi.MARGIN, (int)((1 - animP) * 255f));
            invalidate();
        }

        // draw background
        canvas.drawRoundRect(boxSize, BOX_RADIUS, BOX_RADIUS, boxPaint);

        canvas.drawText("Kcal", textX + FONZ_SIZE/4f, calsMax.bottom - FONZ_SIZE/2f, calsTextPaint);
        canvas.drawText("grams", textX + FONZ_SIZE/4f, proteinsMax.bottom - FONZ_SIZE/2f, proteinsTextPaint);
        canvas.drawText("Name:", calsMax.left + FONZ_SIZE /4f, MealUi.NAME_HEIGHT - MealUi.MARGIN - FONZ_SIZE*0.75f, textPaint);

        long animTime = System.currentTimeMillis() - changedValuesTime;


        if(animTime > ICON_SIZE_TRANSITION_TIME_MILLIS) {
            cloneRect(proteinsCurrent, proteinsNew);
            cloneRect(calsCurrent, calsNew);

            cloneRect(calsLast, calsNew);
            cloneRect(proteinsLast, proteinsNew);
        }
        else {
            float animP = Easing.easeOutBack((float) animTime / ICON_SIZE_TRANSITION_TIME_MILLIS);
            Util.lerpScale(proteinsCurrent, proteinsLast, proteinsNew, animP);
            Util.lerpScale(calsCurrent, calsLast, calsNew, animP);
            invalidate();
        }

        canvas.drawBitmap(bicepsPNG, pngSrc, proteinsCurrent, pngPaint);
        canvas.drawBitmap(calsPNG, pngSrc, calsCurrent, pngPaint);

        canvas.restore();
    }
}
