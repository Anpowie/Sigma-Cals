package com.schnurritv.sigmacals.util;

public class Easing {

    // all stolen from https://easings.net/

    public static float easeOutElastic(float x) {
        final double c4 = (2 * Math.PI) / 3;

        if (x == 0) {
            return 0;
        } else if (x == 1) {
            return 1;
        } else {
            return (float) (Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1);
        }
    }

    public static float easeOutBack(float x) {
        final double c1 = 1.70158;
        final double c3 = c1 + 1;

        return (float) (1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2));
    }
    public static float easeInBack(float x) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;

        return c3 * x * x * x - c1 * x * x;
    }

    public static float easeOutQuad(float x) {
        return 1 - (1 - x) * (1 - x);
    }
    public static float easeInSine(float x) {
        return (float) (1 - Math.cos((x * Math.PI) / 2));
    }
    public static float easeOutSine(float x) {
        return (float) Math.sin((x * Math.PI) / 2);
    }
    public static float easeInOutSine(float x) {
        return (float) (-(Math.cos(Math.PI * x) - 1) / 2);
    }

    public static float easeOutBounce(float x) {
        float n1 = 7.5625f;
        float d1 = 2.75f;

        if (x < 1 / d1) {
            return n1 * x * x;
        } else if (x < 2 / d1) {
            return n1 * (x -= 1.5f / d1) * x + 0.75f;
        } else if (x < 2.5 / d1) {
            return n1 * (x -= 2.25f / d1) * x + 0.9375f;
        } else {
            return n1 * (x -= 2.625f / d1) * x + 0.984375f;
        }

    }
}
