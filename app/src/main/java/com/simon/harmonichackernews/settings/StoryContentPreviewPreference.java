package com.simon.harmonichackernews.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.simon.harmonichackernews.R;
import com.simon.harmonichackernews.utils.SettingsUtils;

public class StoryContentPreviewPreference extends Preference implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final long PREVIEW_ANIMATION_DURATION_MS = 180;
    private static final long PREVIEW_TEXT_FADE_DURATION_MS = 90;

    private ViewGroup previewRoot;
    private ViewGroup previewItemContainer;
    private View metaContainer;
    private View storyLinkLayout;
    private View commentLayout;
    private ImageView favicon;
    private ImageView commentsIcon;
    private TextView storyIndex;
    private TextView storyMetaPoints;
    private TextView storyMeta;
    private TextView comments;
    private boolean leftAligned;
    private int commentsIconResId = R.drawable.ic_action_comment;

    public StoryContentPreviewPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_story_content_preview);
        setSelectable(false);
    }

    public StoryContentPreviewPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        holder.itemView.setClickable(false);
        holder.itemView.setFocusable(false);

        View itemView = holder.itemView;
        View root = itemView.findViewById(R.id.story_content_preview_root);
        previewRoot = root instanceof ViewGroup
                ? (ViewGroup) root
                : itemView instanceof ViewGroup
                ? (ViewGroup) itemView
                : null;
        previewItemContainer = itemView.findViewById(R.id.story_content_preview_item_container);
        leftAligned = SettingsUtils.shouldUseLeftAlign(getContext());
        inflatePreviewItem(leftAligned);
        updatePreview(false);
    }

    public void updateThumbnails(boolean showThumbnails) {
        updatePreview(showThumbnails, null, null, null, null, null, null, true);
    }

    public void updatePoints(boolean showPoints) {
        updatePreview(null, showPoints, null, null, null, null, null, true);
    }

    public void updateCommentsCount(boolean showCommentsCount) {
        updatePreview(null, null, showCommentsCount, null, null, null, null, true);
    }

    public void updateShowIndex(boolean showIndex) {
        updatePreview(null, null, null, showIndex, null, null, null, true);
    }

    public void updateLeftAlign(boolean leftAlign) {
        updatePreview(null, null, null, null, leftAlign, null, null, true);
    }

    public void updateCompact(boolean compact) {
        updatePreview(null, null, null, null, null, compact, null, true);
    }

    public void updateHotness(String hotnessValue) {
        updatePreview(null, null, null, null, null, null, parseHotness(hotnessValue), true);
    }

    @Override
    public void onAttached() {
        super.onAttached();
        SharedPreferences preferences = getSharedPreferences();
        if (preferences != null) {
            preferences.registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onDetached() {
        SharedPreferences preferences = getSharedPreferences();
        if (preferences != null) {
            preferences.unregisterOnSharedPreferenceChangeListener(this);
        }
        super.onDetached();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("pref_thumbnails".equals(key)
                || "pref_show_points".equals(key)
                || "pref_show_comments_count".equals(key)
                || "pref_show_index".equals(key)
                || "pref_left_align".equals(key)
                || "pref_hotness".equals(key)
                || "pref_compact_view".equals(key)) {
            updatePreview(true);
        }
    }

    @SuppressLint("SetTextI18n")
    private void inflatePreviewItem(boolean leftAlign) {
        if (previewItemContainer == null) {
            return;
        }

        previewItemContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        int layout = leftAlign ? R.layout.story_list_item_left : R.layout.story_list_item;
        View itemView = inflater.inflate(layout, previewItemContainer, false);
        previewItemContainer.addView(itemView);

        storyLinkLayout = itemView.findViewById(R.id.story_link_layout);
        commentLayout = itemView.findViewById(R.id.story_comment_layout);
        metaContainer = itemView.findViewById(R.id.story_meta_container);
        favicon = itemView.findViewById(R.id.story_meta_favicon);
        commentsIcon = itemView.findViewById(R.id.story_comments_icon);
        storyIndex = itemView.findViewById(R.id.story_index);
        storyMetaPoints = itemView.findViewById(R.id.story_meta_points);
        storyMeta = itemView.findViewById(R.id.story_meta);
        comments = itemView.findViewById(R.id.story_comments);

        bindStaticPreviewContent();
    }

    @SuppressLint("SetTextI18n")
    private void bindStaticPreviewContent() {
        if (storyLinkLayout != null) {
            storyLinkLayout.setClickable(false);
            storyLinkLayout.setFocusable(false);
        }
        if (commentLayout != null) {
            commentLayout.setClickable(false);
            commentLayout.setFocusable(false);
            commentLayout.setContentDescription("Preview comment button");
        }
        if (favicon != null) {
            favicon.setImageResource(R.drawable.quanta);
        }
        if (commentsIcon != null) {
            commentsIconResId = R.drawable.ic_action_comment;
            commentsIcon.setImageResource(commentsIconResId);
            commentsIcon.setAlpha(1f);
            commentsIcon.setScaleX(1f);
            commentsIcon.setScaleY(1f);
        }
        if (storyIndex != null) {
            storyIndex.setText("3.");
            storyIndex.setContentDescription("Story 3");
            storyIndex.setVisibility(View.GONE);
        }
        if (storyMetaPoints != null) {
            storyMetaPoints.setText("53 points \u2022 ");
            storyMetaPoints.setVisibility(View.VISIBLE);
        }
        if (storyMeta != null) {
            storyMeta.setText("quantamagazine.org \u2022 2 hrs");
        }
        if (comments != null) {
            comments.setText("18");
            comments.setVisibility(View.VISIBLE);
        }
    }

    private void updatePreview(boolean animate) {
        updatePreview(null, null, null, null, null, null, null, animate);
    }

    private void updatePreview(
            Boolean showThumbnailsOverride,
            Boolean showPointsOverride,
            Boolean showCommentsCountOverride,
            Boolean showIndexOverride,
            Boolean leftAlignOverride,
            Boolean compactOverride,
            Integer hotnessOverride,
            boolean animate) {
        if (previewRoot == null) {
            return;
        }

        boolean compact = compactOverride != null
                ? compactOverride
                : SettingsUtils.shouldUseCompactView(getContext());
        boolean showThumbnails = showThumbnailsOverride != null
                ? showThumbnailsOverride
                : SettingsUtils.shouldShowThumbnails(getContext());
        boolean showPoints = (showPointsOverride != null
                ? showPointsOverride
                : SettingsUtils.shouldShowPoints(getContext())) && !compact;
        boolean showCommentsCount = showCommentsCountOverride != null
                ? showCommentsCountOverride
                : SettingsUtils.shouldShowCommentsCount(getContext());
        boolean showIndex = showIndexOverride != null
                ? showIndexOverride
                : SettingsUtils.shouldShowIndex(getContext());
        boolean shouldLeftAlign = leftAlignOverride != null
                ? leftAlignOverride
                : SettingsUtils.shouldUseLeftAlign(getContext());
        int hotness = hotnessOverride != null
                ? hotnessOverride
                : SettingsUtils.getPreferredHotness(getContext());
        if (animate) {
            beginPreviewTransition();
        }

        if (shouldLeftAlign != leftAligned) {
            leftAligned = shouldLeftAlign;
            inflatePreviewItem(shouldLeftAlign);
        }

        if (metaContainer != null) {
            metaContainer.setVisibility(compact ? View.GONE : View.VISIBLE);
        }
        if (favicon != null) {
            favicon.setVisibility(showThumbnails ? View.VISIBLE : View.GONE);
        }

        updateStoryIndex(showIndex);
        updatePoints(showPoints, animate);
        updateCommentCount(showCommentsCount, compact, animate);
        updateHotnessIcon(hotness, animate);
    }

    private void beginPreviewTransition() {
        if (previewRoot == null || !ViewCompat.isLaidOut(previewRoot)) {
            return;
        }

        AutoTransition transition = new AutoTransition();
        transition.setOrdering(TransitionSet.ORDERING_TOGETHER);
        transition.setDuration(PREVIEW_ANIMATION_DURATION_MS);
        transition.setInterpolator(new PathInterpolator(0.2f, 0f, 0f, 1f));
        TransitionManager.beginDelayedTransition(previewRoot, transition);
    }

    private void updatePoints(boolean showPoints, boolean animate) {
        if (storyMetaPoints == null) {
            if (storyMeta != null) {
                storyMeta.setText(showPoints ? "53 points \u2022 quantamagazine.org \u2022 2 hrs" : "quantamagazine.org \u2022 2 hrs");
            }
            return;
        }

        storyMetaPoints.animate().cancel();
        int targetVisibility = showPoints ? View.VISIBLE : View.GONE;
        if (!animate) {
            storyMetaPoints.setVisibility(targetVisibility);
            storyMetaPoints.setAlpha(1f);
            return;
        }

        if (showPoints && storyMetaPoints.getVisibility() != View.VISIBLE) {
            storyMetaPoints.setAlpha(0f);
            storyMetaPoints.setVisibility(View.VISIBLE);
            storyMetaPoints.animate()
                    .alpha(1f)
                    .setDuration(PREVIEW_TEXT_FADE_DURATION_MS)
                    .setInterpolator(new PathInterpolator(0.2f, 0f, 0f, 1f))
                    .start();
        } else {
            storyMetaPoints.setVisibility(targetVisibility);
            storyMetaPoints.setAlpha(1f);
        }
    }

    private void updateStoryIndex(boolean showIndex) {
        if (storyIndex == null) {
            return;
        }

        storyIndex.setVisibility(showIndex ? View.VISIBLE : View.GONE);
    }

    private void updateCommentCount(boolean showCommentsCount, boolean compact, boolean animate) {
        if (comments == null) {
            return;
        }

        String targetText = showCommentsCount ? "18" : "\u2022";
        int targetVisibility = compact ? View.GONE : View.VISIBLE;
        comments.animate().cancel();

        if (!animate) {
            comments.setText(targetText);
            comments.setVisibility(targetVisibility);
            comments.setAlpha(1f);
            return;
        }

        if (comments.getVisibility() != targetVisibility) {
            comments.setVisibility(targetVisibility);
            comments.setAlpha(1f);
        }

        CharSequence currentText = comments.getText();
        if (currentText != null && targetText.contentEquals(currentText)) {
            comments.setAlpha(1f);
            return;
        }

        if (targetVisibility != View.VISIBLE) {
            comments.setText(targetText);
            return;
        }

        comments.animate()
                .alpha(0f)
                .setDuration(PREVIEW_TEXT_FADE_DURATION_MS / 2)
                .setInterpolator(new PathInterpolator(0.2f, 0f, 0f, 1f))
                .withEndAction(() -> {
                    comments.setText(targetText);
                    comments.animate()
                            .alpha(1f)
                            .setDuration(PREVIEW_TEXT_FADE_DURATION_MS)
                            .setInterpolator(new PathInterpolator(0.2f, 0f, 0f, 1f))
                            .start();
                })
                .start();
    }

    private void updateHotnessIcon(int hotness, boolean animate) {
        if (commentsIcon == null) {
            return;
        }

        int targetIconResId = hotness > 0 ? R.drawable.ic_action_whatshot : R.drawable.ic_action_comment;
        commentsIcon.animate().cancel();

        if (targetIconResId == commentsIconResId) {
            commentsIcon.setAlpha(1f);
            commentsIcon.setScaleX(1f);
            commentsIcon.setScaleY(1f);
            return;
        }

        if (!animate) {
            commentsIconResId = targetIconResId;
            commentsIcon.setImageResource(commentsIconResId);
            commentsIcon.setAlpha(1f);
            commentsIcon.setScaleX(1f);
            commentsIcon.setScaleY(1f);
            return;
        }

        commentsIcon.animate()
                .alpha(0f)
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(PREVIEW_TEXT_FADE_DURATION_MS / 2)
                .setInterpolator(new PathInterpolator(0.2f, 0f, 0f, 1f))
                .withEndAction(() -> {
                    commentsIconResId = targetIconResId;
                    commentsIcon.setImageResource(commentsIconResId);
                    commentsIcon.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(PREVIEW_TEXT_FADE_DURATION_MS)
                            .setInterpolator(new PathInterpolator(0.2f, 0f, 0f, 1f))
                            .start();
                })
                .start();
    }

    private int parseHotness(String hotnessValue) {
        try {
            return Integer.parseInt(hotnessValue);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
