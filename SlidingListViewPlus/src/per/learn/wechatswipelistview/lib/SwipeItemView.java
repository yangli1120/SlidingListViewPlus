package per.learn.wechatswipelistview.lib;

import per.learn.wechatswipelistview.R;
import per.learn.wechatswipelistview.util.LogUtil;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class SwipeItemView extends ViewGroup {

    private int mPrimaryViewID = -1, mSlidingViewID = -1;
    private boolean mEnableSliding = true;

    private View mPrimaryView, mSlidingView;

    private Scroller mScroller;

    public SwipeItemView(Context context) {
        super(context);
        init(context, null);
    }

    public SwipeItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SwipeItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mScroller = new Scroller(context);
        mScroller.forceFinished(false);

        setClickable(true);

        if(attrs != null) {
            TypedArray styled = getContext().obtainStyledAttributes(
                    attrs, R.styleable.SwipeItemView);

            mPrimaryViewID = styled.getResourceId(
                    R.styleable.SwipeItemView_primaryView, -1);
            mSlidingViewID = styled.getResourceId(
                    R.styleable.SwipeItemView_slidingView, -1);
            mEnableSliding = styled.getBoolean(
                    R.styleable.SwipeItemView_enableSliding, true);
        }

        if(mPrimaryViewID == -1)
            throw new RuntimeException(
                    "Illegal attribute 'primaryView', make sure you have set it");

        mPrimaryView = LayoutInflater.from(getContext()).inflate(
                mPrimaryViewID, null);
        mPrimaryView.setClickable(false);
        addView(mPrimaryView, 0);
        if(mSlidingViewID != -1) {
            mSlidingView = LayoutInflater.from(getContext()).inflate(
                    mSlidingViewID, null);
            mSlidingView.setClickable(false);
            addView(mSlidingView, 1);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        /*final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if(widthMode != MeasureSpec.EXACTLY)
            throw new IllegalStateException(
                    "widthMode, only run at MeasureSpec.EXACTLY mode");
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if(heightMode != MeasureSpec.EXACTLY)
            throw new IllegalStateException(
                    "heightMode, only run at MeasureSpec.EXACTLY mode");*/

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        if(heightSize != heightMeasureSpec) {
            mPrimaryView.measure(MeasureSpec.makeMeasureSpec(widthSize, widthMode),
                    MeasureSpec.makeMeasureSpec(heightSize, heightMode));

            if(mSlidingView != null) {
                mSlidingView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                        MeasureSpec.makeMeasureSpec(heightSize, heightMode));
            }

            LogUtil.Log("SwipeItemView.onMeasure(), heightSize = " + heightSize
                    + ", widthSize = " + widthSize
                    + ", heightMode = " + (heightMode == MeasureSpec.AT_MOST ? "AT_MOST"
                            : (heightMode == MeasureSpec.EXACTLY ? "EXACTLY" : "UNSPECIFIED"))
                    + ", widthMode = " + (widthMode == MeasureSpec.AT_MOST ? "AT_MOST"
                            : (widthMode == MeasureSpec.EXACTLY ? "EXACTLY" : "UNSPECIFIED")));
        } else {
            mPrimaryView.measure(widthMeasureSpec, heightMeasureSpec);
            if(mSlidingView != null)
                mSlidingView.measure(widthMeasureSpec, heightMeasureSpec);

            LogUtil.Log("SwipeItemView.onMeasure()"
                    + ", widthMeasureSpec = " + widthMeasureSpec
                    + ", heightMeasureSpec = " + heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mPrimaryView.layout(l, t, r, b);
        if(mSlidingView != null)
            mSlidingView.layout(r, t, r + mSlidingView.getMeasuredWidth(), b);
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
        }
    }

    @Override
    public void scrollBy(int x, int y) {
        if(mEnableSliding)
            super.scrollBy(x, y);
    }

    @Override
    public void scrollTo(int x, int y) {
        if(mEnableSliding)
            super.scrollTo(x, y);
    }

    /**
     * just like scrollTo(), but with animation :D
     * */
    public void scrollToWithAnimation(int scrollX, int scrollY) {
        mScroller.startScroll(getScrollX(), getScrollY(),
                scrollX - getScrollX(), getScrollY() - scrollY, 300);
    }

    /**
     * return if scroller is finished or not
     * */
    public boolean isScrollerFinished() {
        return mScroller.isFinished();
    }

    /**
     * return scroller.getCurrX()
     * */
    public int getCurrentScrollX() {
        return mScroller.getCurrX();
    }

    /**
     * return the mSlidingView
     * */
    public View getSlidingView() {
        return mSlidingView;
    }

    /**
     * set the SwipeItemView should enable sliding or not
     * */
    public void setEnableSliding(boolean enable) {
        mEnableSliding = enable;
    }
}
