package com.schnurritv.sigmacals.ui.overview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.schnurritv.sigmacals.R;
import com.schnurritv.sigmacals.databinding.FragmentOverviewBinding;
import com.schnurritv.sigmacals.storage.Day;
import com.schnurritv.sigmacals.storage.Stats;
import com.schnurritv.sigmacals.storage.Storage;
import com.schnurritv.sigmacals.util.DateUtil;
import com.schnurritv.sigmacals.util.DateUtil.Weekday;
import com.schnurritv.sigmacals.util.Util;

import java.util.ArrayList;
import java.util.List;

public class OverviewFragment extends Fragment {

    static final float INNER_CIRCLE_RADIUS_TODAY = 0.7f;
    static final float CALORIES_CIRCLE_RADIUS_TODAY = 0.1f;
    static final float PROTEINS_CIRCLE_RADIUS_TODAY = 0.2f;

    static final float INNER_CIRCLE_RADIUS_PAST = 0.3f;
    static final float CALORIES_CIRCLE_RADIUS_PAST = 0.3f;
    static final float PROTEINS_CIRCLE_RADIUS_PAST = 0.4f;

    static final float INNER_CIRCLE_RADIUS_PLACEHOLDER = 0.9f;
    static final float CALORIES_CIRCLE_RADIUS_PLACEHOLDER = 0.05f;
    static final float PROTEINS_CIRCLE_RADIUS_PLACEHOLDER = 0.05f;

    private FragmentOverviewBinding binding = null;

    Stats lastTodayStats;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(binding != null) {
            // load up new Values
            View view = binding.getRoot();

            // onCreateView is sadly called before onDestroyView. So values are being loaded up before they are saved
            // -> gotta wait a bit before we load up the values properly
            Util.runAfter(200, () -> {
                ProgressBar progressToday = view.findViewById(R.id.todayStats);
                Stats stats = Storage.getToday().getStats();
                progressToday.updateValues(stats);
            });
            return view;
        }


        // create everything

        binding = FragmentOverviewBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        Weekday today = DateUtil.getCurrentWeekday();
        ProgressBar progressToday = view.findViewById(R.id.todayStats);
        progressToday.init(getContext(), INNER_CIRCLE_RADIUS_TODAY, CALORIES_CIRCLE_RADIUS_TODAY, PROTEINS_CIRCLE_RADIUS_TODAY,  false, today);
        progressToday.updateValues(Storage.getToday().getStats());

        List<ProgressBar> launchedBars = new ArrayList<>();

        Day yesterday = Storage.getYesterDay();
        ProgressBar yesterdayStats = view.findViewById(R.id.yesterdayStats);

        if(yesterday == null)
            yesterdayStats.init(getContext(), INNER_CIRCLE_RADIUS_PLACEHOLDER, CALORIES_CIRCLE_RADIUS_PLACEHOLDER, PROTEINS_CIRCLE_RADIUS_PLACEHOLDER,  true, Weekday.yesterdayOf(today), true);
        else
        {
            yesterdayStats.init(getContext(), INNER_CIRCLE_RADIUS_PAST, CALORIES_CIRCLE_RADIUS_PAST, PROTEINS_CIRCLE_RADIUS_PAST,  true, Weekday.yesterdayOf(today));
            yesterdayStats.updateValues(yesterday.getStats());
            launchedBars.add(yesterdayStats);
        }

        Day twoDaysAgo = Storage.getTwoDaysAgo();
        ProgressBar twoDaysAgoStats = view.findViewById(R.id.twoDaysAgoStats);

        if(twoDaysAgo == null)
            twoDaysAgoStats.init(getContext(), INNER_CIRCLE_RADIUS_PLACEHOLDER, CALORIES_CIRCLE_RADIUS_PLACEHOLDER, PROTEINS_CIRCLE_RADIUS_PLACEHOLDER, true, Weekday.yesterdayOf(Weekday.yesterdayOf(today)), true);
        else {
            twoDaysAgoStats.init(getContext(), INNER_CIRCLE_RADIUS_PAST, CALORIES_CIRCLE_RADIUS_PAST, PROTEINS_CIRCLE_RADIUS_PAST, true, Weekday.yesterdayOf(Weekday.yesterdayOf(today)));
            twoDaysAgoStats.updateValues(twoDaysAgo.getStats());
            launchedBars.add(twoDaysAgoStats);
        }


        Day threeDaysAgo = Storage.getThreeDaysAgo();
        ProgressBar theeDaysAgoStats = view.findViewById(R.id.theeDaysAgoStats);

        if(threeDaysAgo == null)
            theeDaysAgoStats.init(getContext(), INNER_CIRCLE_RADIUS_PLACEHOLDER, CALORIES_CIRCLE_RADIUS_PLACEHOLDER, PROTEINS_CIRCLE_RADIUS_PLACEHOLDER, true, Weekday.yesterdayOf(Weekday.yesterdayOf(Weekday.yesterdayOf(today))), true);
        else {
            theeDaysAgoStats.init(getContext(), INNER_CIRCLE_RADIUS_PAST, CALORIES_CIRCLE_RADIUS_PAST, PROTEINS_CIRCLE_RADIUS_PAST, true, Weekday.yesterdayOf(Weekday.yesterdayOf(Weekday.yesterdayOf(today))));
            theeDaysAgoStats.updateValues(threeDaysAgo.getStats());
            launchedBars.add(theeDaysAgoStats);
        }

        /*
            We gotta wait for at least 1 frame to be rendered, until we can receive to correct display scale.
            Since we cannot know how long it will take to make a frame, we just wait until we get an actual size
         */
        new Thread(() -> {
            while (progressToday.applyScaling() == 0)
                try{Thread.sleep(10);}catch (Exception ignored) {}

            for(ProgressBar bar : launchedBars)
                bar.applyScaling();
        }).start();



        return view;
    }
}