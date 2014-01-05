package per.learn.wechatswipelistview.lib;

import per.learn.wechatswipelistview.R;
import per.learn.wechatswipelistview.util.LogUtil;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;

public class SwipeListView extends ListView{

    public static final int DIRECTION_UNKNOW = 0;
    public static final int DIRECTION_HORIZONTAL = 1;
    public static final int DIRECTION_VERTICAL = 2;

    public static final int MAX_DISTANCE = 100;
    public static final int MIN_VELOCITY = ViewConfiguration.getMinimumFlingVelocity() * 10;
    public static final int TOUCH_SLOP = ViewConfiguration.getTouchSlop();

    private float mActionDownX, mActionDownY, mLastMotionX, mLastMotionY;
    private int mLastShowingPos = -1;

    private int mScrollDirection = DIRECTION_UNKNOW;

    private VelocityTracker mTracker;
    private View mItemView;
    private SwipeItemView mSwipeItemView;
    private int mSwipeItemViewID = -1;

    private boolean mCancelMotionEvent = false;

    public SwipeListView(Context context) {
        super(context);
        init(null);
    }

    public SwipeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SwipeListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if(attrs != null) {
            TypedArray styled = getContext().obtainStyledAttributes(
                    attrs, R.styleable.SwipeListView);

            mSwipeItemViewID = styled.getResourceId(
                    R.styleable.SwipeListView_swipeItemViewID, -1);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        //if user had not set mSwipeItemViewID, not handle any touch event
        if(mSwipeItemViewID == -1)
            return super.onTouchEvent(ev);

        if(mCancelMotionEvent && ev.getAction() == MotionEvent.ACTION_DOWN) {
            //ev.setAction(MotionEvent.ACTION_CANCEL);
            LogUtil.Log("SwipeListView.onTouchEvent(), cancel ACTION_DOWN");
            hideShowingItem();

            return true;
        } else if(mCancelMotionEvent && ev.getAction() == MotionEvent.ACTION_MOVE) {
            //why I use scrollBy() but not scrollToWithAnimation() here? I had tried
            //scrollToWithAnimation() first, but I found when the View was handling
            //the touch event, the Scroller.startScroll() can not work, even
            //Scroller.computeScrollOffset() return true... I don't know why,
            //so I only can use the scrollBy() the hide the showing item of the
            //ListView :(, anyway, the scrollBy() can work anytime, thank to google.  - 2014.01.02
            //Then I find just call computeScroll(), the scroller's animation will
            //go on working. - 2014.01.04
            if(mSwipeItemView.getCurrentScrollX() > 0) {
                LogUtil.Log("SwipeListView.onTouchEvent(), cancel ACTION_MOVE");
                mSwipeItemView.computeScroll();
                //mSwipeItemView.scrollBy(-1, 0);
            }

            return true;
        } else if(mCancelMotionEvent && ev.getAction() == MotionEvent.ACTION_UP) {
            LogUtil.Log("SwipeListView.onTouchEvent(), cancel ACTION_UP"
                    + ", getCurrentScrollX() = " + mSwipeItemView.getCurrentScrollX());
            mCancelMotionEvent = false;

            return true;
        }

        switch(ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                LogUtil.Log("ACTION_DOWN");
                if(mTracker == null) {
                    mTracker = VelocityTracker.obtain();
                } else {
                    mTracker.clear();
                }

                mActionDownX = ev.getX();
                mActionDownY = ev.getY();
                mLastMotionX = ev.getX();
                mLastMotionY = ev.getY();
            }break;

            case MotionEvent.ACTION_MOVE: {
                //if the scroll distance at X-axis or Y-axis less than the
                //TOUCH_SLOP, do not handle the event MotionEvent.ACTION_MOVE
                if(Math.abs(ev.getX() - mActionDownX) < TOUCH_SLOP
                        || Math.abs(ev.getY() - mActionDownY) < TOUCH_SLOP)
                    break;

                float curX = ev.getX();
                float curY = ev.getY();
                int distanceX = (int)(mLastMotionX - curX);
                int distanceY = (int)(mLastMotionY - curY);
                if(mScrollDirection == DIRECTION_UNKNOW)
                    /*LogUtil.Log("abs(distanceX) = " + Math.abs(distanceX)
                            + ", abs(distanceY) = " + Math.abs(distanceY)
                            + ", TOUCH_SLOP = " + TOUCH_SLOP)*/;
                if(mScrollDirection == DIRECTION_UNKNOW
                        && Math.abs(distanceY) <= Math.abs(distanceX))
                    mScrollDirection = DIRECTION_HORIZONTAL;
                else if(mScrollDirection == DIRECTION_UNKNOW
                        && Math.abs(distanceY) > Math.abs(distanceX))
                    mScrollDirection = DIRECTION_VERTICAL;

                //if ListView is scrolling vertical, do not handle the touch event
                if(mScrollDirection == DIRECTION_VERTICAL)
                    break;

                int lastPos = pointToPosition((int)mActionDownX, (int)mActionDownY);
                int firstVisibleItemPos = getFirstVisiblePosition()
                        - getHeaderViewsCount();
                int factPos = lastPos - firstVisibleItemPos;
                mItemView = getChildAt(factPos);
                if(mItemView != null) {
                    mSwipeItemView = (SwipeItemView)mItemView.findViewById(mSwipeItemViewID);
                    if(mSwipeItemView.getSlidingView() != null
                            && mSwipeItemView.getScrollX()
                                    <= mSwipeItemView.getSlidingView().getWidth()
                            && mSwipeItemView.getScrollX() >= 0) {
                        if(mSwipeItemView.getScrollX() + distanceX
                                > mSwipeItemView.getSlidingView().getWidth())
                            distanceX = mSwipeItemView.getSlidingView().getWidth()
                                    - mSwipeItemView.getScrollX();
                        else if(mSwipeItemView.getScrollX() + distanceX < 0)
                            distanceX = -mSwipeItemView.getScrollX();

                        mSwipeItemView.scrollBy(distanceX, 0);
                    }

                    mLastShowingPos = lastPos;

                    ev.setAction(MotionEvent.ACTION_CANCEL);
                }

                mLastMotionX = curX;
                mLastMotionY = curY;
            }break;

            case MotionEvent.ACTION_UP: {
                LogUtil.Log("ACTION_UP");
                if(mTracker != null) {
                    mTracker.clear();
                    mTracker.recycle();
                    mTracker = null;
                }

                //reset the mScrollDirection to DIRECTION_UNKNOW
                mScrollDirection = DIRECTION_UNKNOW;

                //reset the mCancelMotionEvent to false
                mCancelMotionEvent = false;

                //ensure if the showing item need open or hide
                if(mLastShowingPos != -1)
                    ensureIfItemOpenOrHide();

                //because we had handle this touch event circle, we had scroll the item
                //at MotionEvent.ACTION_MOVE, change MotionEvent.ACTION_UP to
                //MotionEvent.ACTION_CANCEL so that the this custom ListView will not
                //handle this touch event circle in AdapterView.OnItemClickListener or
                //AdaperView.OnItemLongClickListener.
                /*if(mLastShowingPos != -1)
                    ev.setAction(MotionEvent.ACTION_CANCEL);*/
            }break;

            case MotionEvent.ACTION_CANCEL: {
                hideShowingItem();
            }break;
        }

        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //if user had not set mSwipeItemViewID, not handle any touch event
        if(mSwipeItemViewID == -1)
            return super.onInterceptTouchEvent(ev);

        if(mLastShowingPos != -1
                && ev.getAction() == MotionEvent.ACTION_DOWN
                && !isClickChildView(ev)) {
            LogUtil.Log("SwipeListView.onInterceptTouchEvent(), intercept ACTION_DOWN");
            mCancelMotionEvent = true;

            return true;
        } else if(mLastShowingPos == -1
                && ev.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }

        return super.onInterceptTouchEvent(ev);
    }

    /**
     * hide the current sliding item with animation
     * */
    private void hideShowingItem() {
        if(mLastShowingPos != -1) {
            LogUtil.Log("SwipeListView.hideShowingItem(), 1");
            int firstVisibleItemPos = getFirstVisiblePosition()
                    - getHeaderViewsCount();
            int factPos = mLastShowingPos - firstVisibleItemPos;
            mItemView = getChildAt(factPos);
            if(mItemView != null) {
                LogUtil.Log("SwipeListView.hideShowingItem(), 2");
                mSwipeItemView = (SwipeItemView)mItemView.findViewById(mSwipeItemViewID);
                mSwipeItemView.scrollToWithAnimation(0, 0);
            }

            mLastShowingPos = -1;
        }
    }

    /**
     * open current sliding item with animation
     * */
    private void openShowingItem() {
        if(mLastShowingPos != -1) {
            int firstVisibleItemPos = getFirstVisiblePosition()
                    - getHeaderViewsCount();
            int factPos = mLastShowingPos - firstVisibleItemPos;
            mItemView = getChildAt(factPos);
            if(mItemView != null) {
                mSwipeItemView = (SwipeItemView)mItemView.findViewById(mSwipeItemViewID);
                if(mSwipeItemView.getSlidingView() != null)
                    mSwipeItemView.scrollToWithAnimation(
                            mSwipeItemView.getSlidingView().getWidth(), 0);
            }
        }
    }

    /**
     * make sure that the current sliding item need open or hide
     * */
    private void ensureIfItemOpenOrHide() {
        if(mLastShowingPos != -1) {
            int firstVisibleItemPos = getFirstVisiblePosition()
                    - getHeaderViewsCount();
            int factPos = mLastShowingPos - firstVisibleItemPos;
            mItemView = getChildAt(factPos);
            if(mItemView != null) {
                mSwipeItemView = (SwipeItemView)mItemView.findViewById(mSwipeItemViewID);
                if(mSwipeItemView.getSlidingView() != null &&
                        mSwipeItemView.getScrollX() >=
                                mSwipeItemView.getSlidingView().getWidth() / 2) {
                    openShowingItem();
                } else if(mSwipeItemView.getSlidingView() != null) {
                    hideShowingItem();
                }
            }
        }
    }

    /**
     * make sure if user is click showing item's sliding part
     * */
    private boolean isClickChildView(MotionEvent event) {
        if(mLastShowingPos != -1) {
            int firstVisibleItemPos = getFirstVisiblePosition()
                    - getHeaderViewsCount();
            int factPos = mLastShowingPos - firstVisibleItemPos;
            mItemView = getChildAt(factPos);
            if(mItemView != null) {
                mSwipeItemView = (SwipeItemView)mItemView.findViewById(mSwipeItemViewID);
                View slidingView = mSwipeItemView.getSlidingView();
                if(slidingView != null) {
                    int[] slidingViewLocation = new int[2];
                    slidingView.getLocationOnScreen(slidingViewLocation);

                    int left = slidingViewLocation[0];
                    int right = slidingViewLocation[0] + slidingView.getWidth();
                    int top = slidingViewLocation[1];
                    int bottom = slidingViewLocation[1] + slidingView.getHeight();

                    return (event.getRawX() > left && event.getRawX() < right
                            && event.getRawY() > top && event.getRawY() < bottom);
                }
            }
        }

        return false;
    }

}
