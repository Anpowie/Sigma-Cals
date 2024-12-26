package com.schnurritv.sigmacals.ui.overview;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.schnurritv.sigmacals.R;
import com.schnurritv.sigmacals.storage.Preferences;
import com.schnurritv.sigmacals.storage.Stats;
import com.schnurritv.sigmacals.util.DateUtil.Weekday;
import com.schnurritv.sigmacals.util.Easing;
import com.schnurritv.sigmacals.util.Util;

public class ProgressBar extends View {

    // from worst to best
    static final String[] EMOJIS = new String[]{"💀","😵","😵‍💫","😱","😭","😰","😥","😓","😞","😬","🫣","😦","😶","😐","🙂","🤓","😜","🤪","😎","🤩"};
    static final float LINE_OFFSET_PIXELS = 2;
    static final float SMALL_EMOJI_SIZE_FACTOR = 1.8f;
    static final float BIG_EMOJI_SIZE_FACTOR = 0.25f;
    static final float MONTH_SIZE_FACTOR = 0.45f;
    static final float MONTH_PNG_Y_INSIDE_OFFSET_FACTOR = 0.675f;
    static final int MONTH_PNG_Y_OUTSIDE_OFFSET_PIXELS = -225;
    static final int MONTH_OUTSIDE_SIZE_PIXELS = 68;

    static final int MONTH_DAY_Y_OFFSET_PIXELS = -3;
    static final float FADE_IN_TIME_MILLIS = 1000;
    static final float MIN_PROGRESS = 0.01f;
    static final int FONT_SIZE = 40;
    static final float PNG_WIDTH_P = 4/5f;
    static final float LINE_WIDTH_P = .8f;
    static final float ICONS_Y_OFFSET_P = 0.15f;
    final Bitmap biceps = BitmapFactory.decodeResource(getResources(), R.drawable.biceps), cals = BitmapFactory.decodeResource(getResources(), R.drawable.kcal);
    Bitmap calendar;
    final Paint paintPNG;
    long initTime = 0;

    Paint paintBackground, paintProteins, paintCalories, paintInnerCircle, paintText, paintSmallEmoji, paintBigEmoji;

    int currentProteins = 0, lastCals = 0, currentCals = 0, lastProteins = 0;
    float radiusProteinsP = 0,  radiusCaloriesP = 0, radiusInnerCircleP = 0, size = 0;
    boolean emojiOnlyMode = false, isPlaceHolder;
    Weekday weekday = Weekday.MONDAY;

    public ProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        paintPNG = new Paint();
        paintPNG.setAntiAlias(true);
        paintPNG.setFilterBitmap(true);
        paintPNG.setDither(true);

    }

    public ProgressBar(Context context) {
        super(context);

        paintPNG = new Paint();
        paintPNG.setAntiAlias(true);
        paintPNG.setFilterBitmap(true);
        paintPNG.setDither(true);

    }


    public void init(Context context, float radiusInnerCircleP, float radiusCaloriesP, float radiusProteinsP, boolean emojiOnlyMode, Weekday weekday, boolean isPlaceholder) {
        this.radiusInnerCircleP = radiusInnerCircleP;
        this.radiusCaloriesP = radiusCaloriesP;
        this.radiusProteinsP = radiusProteinsP;
        this.emojiOnlyMode = emojiOnlyMode;
        this.weekday = weekday;
        this.initTime = System.currentTimeMillis();
        this.isPlaceHolder = isPlaceholder;

        paintBackground = new Paint();
        paintBackground.setColor(ContextCompat.getColor(context, R.color.background));
        paintBackground.setStyle(Paint.Style.FILL);

        paintInnerCircle = new Paint();
        paintInnerCircle.setColor(ContextCompat.getColor(context, R.color.white));
        paintInnerCircle.setStyle(Paint.Style.FILL);

        paintProteins = new Paint();
        paintProteins.setColor(ContextCompat.getColor(context, R.color.proteins));
        paintProteins.setStyle(Paint.Style.STROKE);
        paintProteins.setStrokeCap(Paint.Cap.ROUND);

        paintCalories = new Paint();
        paintCalories.setColor(ContextCompat.getColor(context, R.color.cals));
        paintCalories.setStyle(Paint.Style.STROKE);
        paintCalories.setStrokeCap(Paint.Cap.ROUND);

        paintText = new Paint();
        paintText.setColor(ContextCompat.getColor(context, R.color.text)); // Set text color
        paintText.setTextSize(40); // Set text size in pixels
        paintText.setAntiAlias(true); // Enable anti-aliasing for smoother text
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonzie.ttf"));

        paintSmallEmoji = new Paint();
        paintSmallEmoji.setAntiAlias(true);
        paintSmallEmoji.setTextAlign(Paint.Align.CENTER);

        paintBigEmoji = new Paint();
        paintBigEmoji.setAntiAlias(true);
        paintBigEmoji.setTextAlign(Paint.Align.CENTER);

        // in emoji mode, the calendar is outside the circle and close to the proteins - so it shall be cals color. Otherwise it shall be proteins color cuz its close to the cals
        calendar = BitmapFactory.decodeResource(getResources(), emojiOnlyMode ? R.drawable.calender_cals_colored : R.drawable.calender_proteine_colored);
        setPaintSize();


    }

    public void init(Context context, float radiusInnerCircleP, float radiusCaloriesP, float radiusProteinsP, boolean emojiOnlyMode, Weekday weekday) {
        this.init(context, radiusInnerCircleP, radiusCaloriesP, radiusProteinsP, emojiOnlyMode, weekday, false);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        applyScaling();
    }

    public float applyScaling() {
        size = getWidth();
        setPaintSize();
        invalidate();

        return size;
    }

    void setPaintSize() {
        float fullRadius = size/2;

        if(paintBackground == null)
            return;

        paintBackground.setStrokeWidth(fullRadius);
        paintProteins.setStrokeWidth(getProteinsRadius());
        paintCalories.setStrokeWidth(getCaloriesRadius());
        setEmojiSize();
    }

    void setEmojiSize() {

        paintSmallEmoji.setTextSize(applyEmojiJump(getInnerCircleRadius() * SMALL_EMOJI_SIZE_FACTOR));
        paintBigEmoji.setTextSize(applyEmojiJump(getInnerCircleRadius() * BIG_EMOJI_SIZE_FACTOR));
    }

    float applyEmojiJump(float normalSize) {
        if(initTime == 0)
            return normalSize;


        long time = System.currentTimeMillis();
        float animP =  (time - initTime) / FADE_IN_TIME_MILLIS;

        if(animP >= 1)
            return normalSize;

        animP = Easing.easeOutBack(animP);

        invalidate();
        return (int) (normalSize * animP);
    }


    float getProteinsRadius() {
        return size /2 * radiusProteinsP;
    }

    float getCaloriesRadius() {
        return size /2 * radiusCaloriesP;
    }

    float getInnerCircleRadius() {
        return size /2 * radiusInnerCircleP;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // hasn't innited yet
        if(paintBackground == null)
            return;


        if(isPlaceHolder) {
            drawPlaceHolder(canvas);
            drawDate(canvas, false);
            return;
        }

        drawBars(canvas);
        setEmojiSize();

        drawEmoji(canvas, emojiOnlyMode);
        drawDate(canvas, !emojiOnlyMode);

        if(!emojiOnlyMode) {

            drawIcons(canvas);
            drawValues(canvas);
        }

    }

    void drawPlaceHolder(Canvas canvas) {

        float fullRadius = size / 2f;

        // Draw the circular background
        canvas.drawCircle(fullRadius, fullRadius, fullRadius, paintBackground);

        // draw inner circle for spacing
        canvas.drawCircle(fullRadius, fullRadius, fullRadius * radiusInnerCircleP, paintInnerCircle);

        float textSize = paintText.getTextSize();
        canvas.drawText("No data for", fullRadius, fullRadius, paintText);
        canvas.drawText(weekday.name().toLowerCase(), fullRadius, fullRadius+textSize, paintText);
    }


    void drawDate(Canvas canvas, boolean drawInMiddle) {
        int center = (int) (size /2f);

        int srcWidth = calendar.getWidth();
        int srcHeight = calendar.getHeight();

        Rect src = new Rect(0, 0, srcWidth, srcHeight);

        if(drawInMiddle) {
            float radius = getInnerCircleRadius();
            int destWidth = (int) ((radius * MONTH_SIZE_FACTOR) / 2f);
            Rect dest = new Rect(center - destWidth, center - destWidth, center + destWidth, center + destWidth);
            dest.offset(0, (int) (-radius * MONTH_PNG_Y_INSIDE_OFFSET_FACTOR));

            canvas.drawBitmap(calendar, src, dest, paintPNG);
            canvas.drawText(Weekday.getAbb(weekday), Util.lerp(dest.left, dest.right, 0.5f), dest.bottom - destWidth /2f + MONTH_DAY_Y_OFFSET_PIXELS, paintText);
            return;
        }

        Rect dest = new Rect(center - MONTH_OUTSIDE_SIZE_PIXELS, center - MONTH_OUTSIDE_SIZE_PIXELS, center + MONTH_OUTSIDE_SIZE_PIXELS, center + MONTH_OUTSIDE_SIZE_PIXELS);
        dest.offset(0, MONTH_PNG_Y_OUTSIDE_OFFSET_PIXELS);

        canvas.drawBitmap(calendar, src, dest, paintPNG);
        canvas.drawText(Weekday.getAbb(weekday), Util.lerp(dest.left, dest.right, 0.5f), dest.bottom - MONTH_OUTSIDE_SIZE_PIXELS /2f + MONTH_DAY_Y_OFFSET_PIXELS, paintText);

    }

    void drawEmoji(Canvas canvas, boolean drawInMiddle) {
        float center = size /2;

        if(drawInMiddle)
            canvas.drawText(getFittingEmoji(), center, center + (getInnerCircleRadius() * SMALL_EMOJI_SIZE_FACTOR)/3, paintSmallEmoji);
        else
            canvas.drawText(getFittingEmoji(), center, center + getInnerCircleRadius() - getInnerCircleRadius() / 5, paintBigEmoji);
    }

    void drawValues(Canvas canvas) {
        float center = size /2;
        float maxWidth = getInnerCircleRadius();
        float pngWidth = maxWidth * PNG_WIDTH_P;

        float yOffset = center * ICONS_Y_OFFSET_P;

        // proteins
        canvas.drawText(getShownProteins() + " g", center - pngWidth/2, center + FONT_SIZE - 10 + yOffset, paintText);
        canvas.drawText(Preferences.getDailyProteinsGoal() + " g", center - pngWidth/2, center + FONT_SIZE + FONT_SIZE + yOffset, paintText);
        canvas.drawLine(center - pngWidth * LINE_WIDTH_P, center + FONT_SIZE + yOffset + LINE_OFFSET_PIXELS, center  - pngWidth * (1 - LINE_WIDTH_P), center+ FONT_SIZE  + yOffset + LINE_OFFSET_PIXELS, paintText);

        // cals
        canvas.drawText(getShownCals() + " Kcal", center + pngWidth/2, center + FONT_SIZE - 10  + yOffset, paintText);
        canvas.drawText(Preferences.CALORIES.getValue() + " Kcal", center + pngWidth/2, center + FONT_SIZE + FONT_SIZE  + yOffset, paintText);
        canvas.drawLine(center - pngWidth * LINE_WIDTH_P + pngWidth, center + FONT_SIZE  + yOffset + LINE_OFFSET_PIXELS, center  - pngWidth * (1 - LINE_WIDTH_P) + pngWidth, center+ FONT_SIZE  + yOffset + LINE_OFFSET_PIXELS, paintText);

    }

    int getShownCals() {
        if(initTime == 0)
            return currentCals;

        long time = System.currentTimeMillis();
        float animP =  (time - initTime) / FADE_IN_TIME_MILLIS;

        if(animP >= 1)
            return currentCals;

        animP = Easing.easeOutQuad(animP);

        invalidate();
        return (int) (Util.lerp(Math.min(lastCals, Preferences.CALORIES.getValue()), Math.min(currentCals, Preferences.CALORIES.getValue()), animP));
    }

    int getShownProteins() {
        if(initTime == 0)
            return currentProteins;

        long time = System.currentTimeMillis();
        float animP =  (time - initTime) / FADE_IN_TIME_MILLIS;

        if(animP >= 1)
            return currentProteins;

        animP = Easing.easeOutQuad(animP);

        invalidate();
        return (int) (Util.lerp(Math.min(lastProteins, Preferences.getDailyProteinsGoal()), Math.min(currentProteins, Preferences.getDailyProteinsGoal()), animP));
    }

    void drawIcons(Canvas canvas) {

        float center = size /2;

        float maxWidth = getInnerCircleRadius();
        float pngWidth = maxWidth * PNG_WIDTH_P;

        float pngPosY = center - pngWidth + center * ICONS_Y_OFFSET_P;
        float proteinsPos = center - pngWidth;

        Rect pngSize = new Rect(0, 0, biceps.getWidth(), biceps.getHeight());

        Rect bicepsCoords = new Rect((int) proteinsPos, (int) pngPosY, (int) (proteinsPos + pngWidth), (int) (pngWidth + pngPosY));
        Rect caloriesCoords = new Rect((int) center, (int) pngPosY, (int) (center + pngWidth), (int) (pngWidth + pngPosY));


        canvas.drawBitmap(biceps, pngSize, bicepsCoords, paintPNG);
        canvas.drawBitmap(cals, pngSize, caloriesCoords, paintPNG);

    }
    void drawBars(Canvas canvas) {

        float fullRadius = size / 2f;

        // Draw the circular background
        canvas.drawCircle(fullRadius, fullRadius, fullRadius, paintBackground);

        // Calculate the angles for progress bars
        float angleProteins = 360f * Math.max(MIN_PROGRESS, (float) getShownProteins() / Preferences.getDailyProteinsGoal());
        float angleCalories = 360f * Math.max(MIN_PROGRESS, (float) getShownCals() / Preferences.CALORIES.getValue());


        float proteinsRadius = getProteinsRadius();
        float caloriesRadius = getCaloriesRadius();


        // draw protein bar  proteinsRadius + (caloriesRadius /2)
        float proteinsOffset = proteinsRadius /2;
        canvas.drawArc(proteinsOffset,  proteinsOffset, size - proteinsOffset, size - proteinsOffset, -90, angleProteins, false, paintProteins);

        // draw calories bar
        float CaloriesOffset = proteinsRadius + caloriesRadius / 2;
        canvas.drawArc(CaloriesOffset,  CaloriesOffset, size - CaloriesOffset, size - CaloriesOffset, -90, angleCalories, false, paintCalories);

        // draw inner circle for spacing
        canvas.drawCircle(fullRadius, fullRadius, fullRadius * radiusInnerCircleP, paintInnerCircle);
    }

    String getFittingEmoji() {

        float calsP = Util.clamp(currentCals / (float) Preferences.CALORIES.getValue(), 0, 1);
        float proteinsP = Util.clamp(currentProteins / (float) Preferences.getDailyProteinsGoal(), 0, 1);

        float totalP = (calsP + proteinsP) / 2f;
        int emojiIndex = (int) (totalP * (EMOJIS.length-1));

        return EMOJIS[emojiIndex];
    }

    public void updateValues(Stats stats) {
        this.lastCals = currentCals;
        this.lastProteins = currentProteins;
        this.currentProteins = stats.proteins;
        this.currentCals = stats.calories;
        this.initTime = System.currentTimeMillis();
        invalidate();
    }
}
