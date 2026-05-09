package com.simon.harmonichackernews.settings;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.card.MaterialCardView;
import com.simon.harmonichackernews.R;
import com.simon.harmonichackernews.utils.SettingsUtils;
import com.simon.harmonichackernews.utils.Utils;

public class StoryDisplayStylePreference extends Preference {

    private static final int SELECTION_ANIMATION_DURATION_MS = 180;

    public StoryDisplayStylePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(getDisplayStyleLayoutResource());
        setSelectable(false);
    }

    public StoryDisplayStylePreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        holder.itemView.setClickable(false);
        holder.itemView.setFocusable(false);

        String style = getPersistedString(getDefaultStyle());
        boolean cardSelected = getCardStyle().equals(style);

        MaterialCardView standardCard = (MaterialCardView) holder.findViewById(getStandardCardId());
        MaterialCardView cardCard = (MaterialCardView) holder.findViewById(getCardCardId());

        bindOptionCard(
                standardCard,
                !cardSelected,
                "Standard " + getDisplayStyleName() + " display style");
        bindOptionCard(
                cardCard,
                cardSelected,
                "Card " + getDisplayStyleName() + " display style");

        if (standardCard != null) {
            standardCard.setOnClickListener(v -> selectStyle(
                    getStandardStyle(),
                    standardCard,
                    cardCard));
        }
        if (cardCard != null) {
            cardCard.setOnClickListener(v -> selectStyle(
                    getCardStyle(),
                    cardCard,
                    standardCard));
        }
    }

    private void bindOptionCard(MaterialCardView optionCard, boolean selected, String label) {
        if (optionCard == null) {
            return;
        }

        setOptionCardState(optionCard, selected, false);
        optionCard.setContentDescription(label + (selected ? ", selected" : ""));
        optionCard.setChecked(selected);
    }

    private void selectStyle(
            String style,
            MaterialCardView selectedCard,
            MaterialCardView unselectedCard) {
        String oldStyle = getPersistedString(getDefaultStyle());
        if (oldStyle.equals(style)) {
            return;
        }

        if (!callChangeListener(style)) {
            return;
        }

        persistString(style);

        setOptionCardState(selectedCard, true, true);
        setOptionCardState(unselectedCard, false, true);
        if (selectedCard != null) {
            selectedCard.setContentDescription(selectedCard.getContentDescription() + ", selected");
            selectedCard.setChecked(true);
        }
        if (unselectedCard != null) {
            String description = unselectedCard.getContentDescription() == null
                    ? ""
                    : unselectedCard.getContentDescription().toString().replace(", selected", "");
            unselectedCard.setContentDescription(description);
            unselectedCard.setChecked(false);
        }
    }

    private void setOptionCardState(MaterialCardView optionCard, boolean selected, boolean animate) {
        if (optionCard == null) {
            return;
        }

        Context context = optionCard.getContext();
        int targetStrokeWidth = Utils.pxFromDpInt(context.getResources(), selected ? 2 : 1);
        int targetStrokeColor = Utils.getColorViaAttr(
                context,
                selected ? R.attr.commentCountIndicatorColor : R.attr.commentDividerColor);

        if (!animate) {
            optionCard.setStrokeWidth(targetStrokeWidth);
            optionCard.setStrokeColor(targetStrokeColor);
            optionCard.setScaleX(1f);
            optionCard.setScaleY(1f);
            return;
        }

        int startStrokeWidth = optionCard.getStrokeWidth();
        int startStrokeColor = optionCard.getStrokeColor();

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        ArgbEvaluator argbEvaluator = new ArgbEvaluator();
        animator.setDuration(SELECTION_ANIMATION_DURATION_MS);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            int strokeWidth = Math.round(startStrokeWidth + ((targetStrokeWidth - startStrokeWidth) * fraction));
            int strokeColor = (int) argbEvaluator.evaluate(fraction, startStrokeColor, targetStrokeColor);
            optionCard.setStrokeWidth(strokeWidth);
            optionCard.setStrokeColor(strokeColor);
        });
        animator.start();

        if (selected) {
            optionCard.animate()
                    .scaleX(1.015f)
                    .scaleY(1.015f)
                    .setDuration(SELECTION_ANIMATION_DURATION_MS / 2)
                    .withEndAction(() -> optionCard.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(SELECTION_ANIMATION_DURATION_MS / 2)
                            .start())
                    .start();
        } else {
            optionCard.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(SELECTION_ANIMATION_DURATION_MS)
                    .start();
        }
    }

    protected int getDisplayStyleLayoutResource() {
        return R.layout.preference_story_display_style;
    }

    protected int getStandardCardId() {
        return R.id.story_display_style_standard;
    }

    protected int getCardCardId() {
        return R.id.story_display_style_card;
    }

    protected String getDefaultStyle() {
        return SettingsUtils.STORY_DISPLAY_STYLE_STANDARD;
    }

    protected String getStandardStyle() {
        return SettingsUtils.STORY_DISPLAY_STYLE_STANDARD;
    }

    protected String getCardStyle() {
        return SettingsUtils.STORY_DISPLAY_STYLE_CARD;
    }

    protected String getDisplayStyleName() {
        return "story";
    }
}
