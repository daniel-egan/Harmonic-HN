package com.simon.harmonichackernews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.simon.harmonichackernews.databinding.ActivityWelcomeBinding;
import com.simon.harmonichackernews.utils.ThemeUtils;

public class WelcomeActivity extends AppCompatActivity {

    private static final long PREVIEW_ANIMATION_DURATION_MS = 180;
    private static final long PREVIEW_TEXT_FADE_DURATION_MS = 90;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeUtils.setupTheme(this);
        ActivityWelcomeBinding binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        final View root = binding.getRoot();
        setContentView(root);
        final int padLeft = root.getPaddingLeft();
        final int padTop = root.getPaddingTop();
        final int padRight = root.getPaddingRight();
        final int padBottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout());

            view.setPadding(
                    padLeft + Math.max(bars.left, cutout.left),
                    padTop + bars.top,
                    padRight + Math.max(bars.right, cutout.right),
                    padBottom + Math.max(bars.bottom, ime.bottom)
            );
            return insets;
        });
        ViewCompat.requestApplyInsets(root);

        ViewCompat.setAccessibilityHeading(binding.welcomeTitle, true);
        ViewCompat.setAccessibilityHeading(binding.welcomeThemeHeader, true);

        ImageView favicon = binding.storyListItem.storyMetaFavicon;
        favicon.setImageResource(R.drawable.quanta);
        TextView storyMetaPoints = binding.storyListItem.storyMetaPoints;
        TextView storyMeta = binding.storyListItem.storyMeta;
        storyMeta.setText("quantamagazine.org • 2 hrs");
        storyMetaPoints.setVisibility(View.VISIBLE);
        storyMetaPoints.setAlpha(1f);

        binding.welcomeSwitchThumbnails.setOnCheckedChangeListener((@NonNull CompoundButton compoundButton, boolean b) -> {
            beginPreviewTransition(binding);
            favicon.setVisibility(b ? View.VISIBLE : View.GONE);
            setBooleanSetting(compoundButton.getContext(), "pref_thumbnails", b);
        });

        binding.welcomeSwitchIndex.setOnCheckedChangeListener((@NonNull CompoundButton compoundButton, boolean b) -> {
            beginPreviewTransition(binding);
            binding.storyListItem.storyIndex.setVisibility(b ? View.VISIBLE : View.GONE);
            setBooleanSetting(compoundButton.getContext(), "pref_show_index", b);
        });

        binding.welcomeSwitchPoints.setOnCheckedChangeListener((@NonNull CompoundButton compoundButton, boolean b) -> {
            animateStoryMeta(binding, b);
            setBooleanSetting(compoundButton.getContext(), "pref_show_points", b);
        });

        View.OnClickListener buttonClickListener = (View view) -> {
            setSetting(view.getContext(), "pref_theme", (String) view.getTag());
            restartActivity();
        };

        binding.welcomeButtonMaterialDaynight.setOnClickListener(buttonClickListener);
        binding.welcomeButtonMaterialDark.setOnClickListener(buttonClickListener);
        binding.welcomeButtonMaterialLight.setOnClickListener(buttonClickListener);
        binding.welcomeButtonDark.setOnClickListener(buttonClickListener);
        binding.welcomeButtonGray.setOnClickListener(buttonClickListener);
        binding.welcomeButtonBlack.setOnClickListener(buttonClickListener);
        binding.welcomeButtonLight.setOnClickListener(buttonClickListener);
        binding.welcomeButtonHackerNews.setOnClickListener(buttonClickListener);
        binding.welcomeButtonWhite.setOnClickListener(buttonClickListener);
    }

    private void beginPreviewTransition(ActivityWelcomeBinding binding) {
        ViewGroup previewRoot = (ViewGroup) binding.storyListItem.getRoot();
        if (!ViewCompat.isLaidOut(previewRoot)) {
            return;
        }

        AutoTransition transition = new AutoTransition();
        transition.setOrdering(TransitionSet.ORDERING_TOGETHER);
        transition.setDuration(PREVIEW_ANIMATION_DURATION_MS);
        transition.setInterpolator(new PathInterpolator(0.2f, 0f, 0f, 1f));
        TransitionManager.beginDelayedTransition(previewRoot, transition);
    }

    @SuppressLint("SetTextI18n")
    private void animateStoryMeta(ActivityWelcomeBinding binding, boolean showPoints) {
        TextView storyMetaPoints = binding.storyListItem.storyMetaPoints;
        storyMetaPoints.animate().cancel();
        if (showPoints) {
            storyMetaPoints.setAlpha(0f);
            beginPreviewTransition(binding);
            storyMetaPoints.setVisibility(View.VISIBLE);
            storyMetaPoints.animate()
                    .alpha(1f)
                    .setDuration(PREVIEW_TEXT_FADE_DURATION_MS)
                    .setInterpolator(new PathInterpolator(0.2f, 0f, 0f, 1f))
                    .start();
            return;
        }

        beginPreviewTransition(binding);
        storyMetaPoints.setVisibility(View.GONE);
    }

    @SuppressLint("ApplySharedPref")
    private void setSetting(Context ctx, String key, String newTheme) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        prefs.edit().putString(key, newTheme).commit();
    }

    @SuppressLint("ApplySharedPref")
    private void setBooleanSetting(Context ctx, String key, boolean newVal) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        prefs.edit().putBoolean(key, newVal).commit();
    }

    private void restartActivity() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

    public void done(View view) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
