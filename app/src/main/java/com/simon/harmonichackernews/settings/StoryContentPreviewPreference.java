package com.simon.harmonichackernews.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.AttributeSet;
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

public class StoryContentPreviewPreference extends Preference {

    private static final long PREVIEW_ANIMATION_DURATION_MS = 180;
    private static final long PREVIEW_TEXT_FADE_DURATION_MS = 90;

    private ViewGroup previewRoot;
    private View metaContainer;
    private View storyLinkLayout;
    private View commentLayout;
    private ImageView favicon;
    private TextView storyMetaPoints;
    private TextView storyMeta;
    private TextView comments;

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
        previewRoot = itemView instanceof ViewGroup
                ? (ViewGroup) itemView
                : itemView.findViewById(R.id.story_content_preview_root);
        storyLinkLayout = itemView.findViewById(R.id.story_link_layout);
        commentLayout = itemView.findViewById(R.id.story_comment_layout);
        metaContainer = itemView.findViewById(R.id.story_meta_container);
        favicon = itemView.findViewById(R.id.story_meta_favicon);
        storyMetaPoints = itemView.findViewById(R.id.story_meta_points);
        storyMeta = itemView.findViewById(R.id.story_meta);
        comments = itemView.findViewById(R.id.story_comments);

        bindStaticPreviewContent();
        updatePreview(false);
    }

    public void updateThumbnails(boolean showThumbnails) {
        updatePreview(showThumbnails, null, null, null, true);
    }

    public void updatePoints(boolean showPoints) {
        updatePreview(null, showPoints, null, null, true);
    }

    public void updateCommentsCount(boolean showCommentsCount) {
        updatePreview(null, null, showCommentsCount, null, true);
    }

    public void updateCompact(boolean compact) {
        updatePreview(null, null, null, compact, true);
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
        updatePreview(null, null, null, null, animate);
    }

    private void updatePreview(
            Boolean showThumbnailsOverride,
            Boolean showPointsOverride,
            Boolean showCommentsCountOverride,
            Boolean compactOverride,
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
        if (animate) {
            beginPreviewTransition();
        }

        if (metaContainer != null) {
            metaContainer.setVisibility(compact ? View.GONE : View.VISIBLE);
        }
        if (favicon != null) {
            favicon.setVisibility(showThumbnails ? View.VISIBLE : View.GONE);
        }

        updatePoints(showPoints, animate);
        updateCommentCount(showCommentsCount, compact);
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

    private void updateCommentCount(boolean showCommentsCount, boolean compact) {
        if (comments == null) {
            return;
        }

        comments.animate().cancel();
        comments.setText(showCommentsCount ? "18" : "\u2022");
        comments.setVisibility(compact ? View.GONE : View.VISIBLE);
        comments.setAlpha(1f);
    }
}
