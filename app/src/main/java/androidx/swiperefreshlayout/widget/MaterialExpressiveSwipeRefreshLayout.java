package androidx.swiperefreshlayout.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.loadingindicator.LoadingIndicator;

/**
 * SwipeRefreshLayout does not expose a drawable hook for its progress view. This class lives in
 * the AndroidX package so it can replace the package-private progress ImageView drawable while
 * keeping the library's nested scrolling and refresh behavior.
 */
public class MaterialExpressiveSwipeRefreshLayout extends SwipeRefreshLayout {
    private static final int INDICATOR_CONTAINER_PADDING_DP = 5;

    private LoadingIndicator materialIndicator;

    public MaterialExpressiveSwipeRefreshLayout(Context context) {
        super(context);
        useMaterialIndicator(context);
    }

    public MaterialExpressiveSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        useMaterialIndicator(context);
    }

    @Override
    public void setSize(int size) {
        super.setSize(size);
        useMaterialIndicator(getContext());
    }

    @Override
    public void setColorSchemeColors(int... colors) {
        super.setColorSchemeColors(colors);
        if (materialIndicator != null && colors.length > 0) {
            materialIndicator.setIndicatorColor(colors);
        }
    }

    @Override
    public void setProgressBackgroundColorSchemeColor(int color) {
        super.setProgressBackgroundColorSchemeColor(color);
        if (materialIndicator != null) {
            materialIndicator.setContainerColor(color);
            mCircleView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void useMaterialIndicator(Context context) {
        materialIndicator = new LoadingIndicator(context);
        int containerSize = getProgressCircleDiameter();
        int indicatorPadding = Math.round(INDICATOR_CONTAINER_PADDING_DP * getResources().getDisplayMetrics().density);
        int indicatorSize = Math.max(0, containerSize - (indicatorPadding * 2));

        materialIndicator.setIndicatorSize(indicatorSize);
        materialIndicator.setContainerWidth(containerSize);
        materialIndicator.setContainerHeight(containerSize);
        materialIndicator.setIndicatorColor(MaterialColors.getColor(context, androidx.appcompat.R.attr.colorPrimary, 0));
        materialIndicator.setContainerColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurfaceContainerHigh, 0));

        mCircleView.setImageDrawable(materialIndicator.getDrawable());
        mCircleView.setBackgroundColor(Color.TRANSPARENT);
    }
}
