package com.schnurritv.sigmacals.ui.settings;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.schnurritv.sigmacals.R;
import com.schnurritv.sigmacals.databinding.FragmentSettingsBinding;
import com.schnurritv.sigmacals.storage.Preferences;
import com.schnurritv.sigmacals.util.Debug;
import com.schnurritv.sigmacals.util.Util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;

    EditText weightInput;
    SeekBar proteinSlider, calorieSlider;
    TextView proteinValue, calorieValue;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        ConstraintLayout layout = binding.getRoot();

        weightInput = layout.findViewById(R.id.weightInput);
        proteinSlider = layout.findViewById(R.id.proteinsAmountSeekBar);
        calorieSlider = layout.findViewById(R.id.caloriesAmountSeekBar);
        proteinValue = layout.findViewById(R.id.proteinsAmount);
        calorieValue = layout.findViewById(R.id.caloriesAmount);

        // init values
        float porteinsGoal = Preferences.PROTEINS.getValue();
        int calsGoal = Preferences.CALORIES.getValue();
        proteinValue.setText(String.valueOf(porteinsGoal));
        calorieValue.setText(String.valueOf(calsGoal));
        proteinSlider.setProgress((int) (((porteinsGoal - Preferences.MIN_PROTEIN_GOAL) / (Preferences.MAX_PROTEIN_GOAL - Preferences.MIN_PROTEIN_GOAL)) * 100f));
        calorieSlider.setProgress((int) (((float)calsGoal - Preferences.MIN_CALORIE_GOAL) / (Preferences.MAX_CALORIE_GOAL - Preferences.MIN_CALORIE_GOAL) * 100f));

        weightInput.setText(String.valueOf(Preferences.WEIGHT.getValue()));
        weightInput.setOnFocusChangeListener ((v, focus) -> weightInput.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(layout.getContext(), focus ? R.color.cals : R.color.text))));

        weightInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                try {Preferences.saveValue(Preferences.WEIGHT, Math.max(1, Integer.parseInt(editable.toString())));} // has to be at least 1 so we don't divide by 0 later on
                catch (NumberFormatException ignored) {}
            }
        });

        proteinSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                NumberFormat numberFormat = NumberFormat.getInstance();
                numberFormat.setMaximumFractionDigits(1);

                float value = Util.lerp(Preferences.MIN_PROTEIN_GOAL, Preferences.MAX_PROTEIN_GOAL, i / 100f);
                value = Float.parseFloat(numberFormat.format(value).replace(",", ".")); // if i one day catch the european retard that said we shall use , instead of . I will rape his virgin butthole and eat out his dick

                Preferences.saveValue(Preferences.PROTEINS, value);
                proteinValue.setText(String.valueOf(value));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        calorieSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int value = Util.lerp(Preferences.MIN_CALORIE_GOAL, Preferences.MAX_CALORIE_GOAL, i / 100f);
                value = Util.roundToNearestHundred(value);

                Preferences.saveValue(Preferences.CALORIES, value);
                calorieValue.setText(String.valueOf(value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}