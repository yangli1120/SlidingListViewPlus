package per.learn.wechatswipelistview.lib;

import per.learn.wechatswipelistview.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class SwipeItemView extends ViewGroup {

    private int mPrimaryViewID = -1, mSlidingViewID = -1;
    private boolean mEnableSliding = true;

    private float mLastMotionX, mLastMotionY;

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
        addView(mPrimaryView, 0);
        if(mSlidingViewID != -1) {
            mSlidingView = LayoutInflater.from(getContext()).inflate(
                    mSlidingViewID, null);
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

        mPrimaryView.measure(widthMeasureSpec, heightMeasureSpec);
        if(mSlidingView != null) {
            int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            mSlidingView.measure(width, height);
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
            Log.i("Young Lee", "computeScroll(), getCurrX() = " + mScroller.getCurrX());
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
        }
    }

    /**
     * just like scrollTo(), but with animation :D
     * */
    public void scrollToWithAnimation(int scrollX, int scrollY) {
        mScroller.startScroll(getScrollX(), getScrollY(),
                scrollX - getScrollX(), getScrollY() - scrollY, 300);
    }

    public boolean isScrollerFinished() {
        return mScroller.isFinished();
    }

    public int getCurrentScrollX() {
        return mScroller.getCurrX();
    }

    public View getSlidingView() {
        return mSlidingView;
    }
}
