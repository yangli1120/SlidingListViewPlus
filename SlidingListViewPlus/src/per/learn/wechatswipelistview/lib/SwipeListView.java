package per.learn.wechatswipelistview.lib;

import per.learn.wechatswipelistview.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
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

    private float mActionDownX,mActionDownY, mLastMotionX, mLastMotionY;
    private int mLastShowingPos = -1;

    private int mScrollDirection = DIRECTION_UNKNOW;

    private VelocityTracker mTracker;
    private View mItemView;
    private SwipeItemView mSwipeItemView;

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
            
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if(mCancelMotionEvent && ev.getAction() == MotionEvent.ACTION_DOWN) {
            ev.setAction(MotionEvent.ACTION_CANCEL);
        } else if(mCancelMotionEvent && ev.getAction() == MotionEvent.ACTION_MOVE) {
            Log.i("Young Lee", "cancel ACTION_MOVE");
            return super.onTouchEvent(ev);
        }

        switch(ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
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
                    Log.i("Young Lee", "abs(distanceX) = " + Math.abs(distanceX)
                            + ", abs(distanceY) = " + Math.abs(distanceY)
                            + ", TOUCH_SLOP = " + TOUCH_SLOP);
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
                    mSwipeItemView = (SwipeItemView)mItemView.findViewById(R.id.swipe_item_view);
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
                Log.i("Young Lee", "ACTION_CANCEL");
                hideShowingItem();
            }break;
        }

        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(mLastShowingPos != -1
                && ev.getAction() == MotionEvent.ACTION_DOWN) {
            mCancelMotionEvent = true;

            return true;
        }

        return super.onInterceptTouchEvent(ev);
    }

    //TODO: hide the showing item
    private void hideShowingItem() {
        if(mLastShowingPos != -1) {
            int firstVisibleItemPos = getFirstVisiblePosition()
                    - getHeaderViewsCount();
            int factPos = mLastShowingPos - firstVisibleItemPos;
            mItemView = getChildAt(factPos);
            if(mItemView != null) {
                mSwipeItemView = (SwipeItemView)mItemView.findViewById(R.id.swipe_item_view);
                mSwipeItemView.scrollToWithAnimation(0, 0);
            }

            mLastShowingPos = -1;
        }
    }

    private void openShowingItem() {
        if(mLastShowingPos != -1) {
            int firstVisibleItemPos = getFirstVisiblePosition()
                    - getHeaderViewsCount();
            int factPos = mLastShowingPos - firstVisibleItemPos;
            mItemView = getChildAt(factPos);
            if(mItemView != null) {
                mSwipeItemView = (SwipeItemView)mItemView.findViewById(R.id.swipe_item_view);
                if(mSwipeItemView.getSlidingView() != null)
                    mSwipeItemView.scrollToWithAnimation(
                            mSwipeItemView.getSlidingView().getWidth(), 0);
            }
        }
    }

    //TODO: ensure if the item need show or hide
    private void ensureIfItemOpenOrHide() {
        if(mLastShowingPos != -1) {
            int firstVisibleItemPos = getFirstVisiblePosition()
                    - getHeaderViewsCount();
            int factPos = mLastShowingPos - firstVisibleItemPos;
            mItemView = getChildAt(factPos);
            if(mItemView != null) {
                mSwipeItemView = (SwipeItemView)mItemView.findViewById(R.id.swipe_item_view);
                if(mSwipeItemView.getSlidingView() != null &&
                        mSwipeItemView.getScrollX() >=
                                mSwipeItemView.getSlidingView().getWidth() / 3) {
                    openShowingItem();
                } else if(mSwipeItemView.getSlidingView() != null) {
                    hideShowingItem();
                }
            }
        }
    }

    //TODO: ensure if this event is click the child view of the showing item
    private boolean isClickChildView(MotionEvent event) {
        return false;
    }

}
