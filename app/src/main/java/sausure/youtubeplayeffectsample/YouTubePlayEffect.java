package sausure.youtubeplayeffectsample;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

/**
 * Created by JOJO on 2015/11/29.
 */
public class YouTubePlayEffect extends ViewGroup {
    public static final int NONE = 1 << 0;
    public static final int HORIZONTAL = 1 << 1;
    public static final int VERTICAL = 1 << 2;

    public static final int SLIDE_RESTORE_ORIGINAL = 1 << 0;
    public static final int SLIDE_TO_LEFT = 1 << 1;
    public static final int SLIDE_TO_RIGHT = 1 << 2;

    private static final float MIN_ALPHA = 0.1f;

    private static final float PLAYER_RATIO = 0.5f;
    private static final float VIDEO_RATIO = 16f / 9f;
    private static final float ORIGINAL_MIN_OFFSET = 1f / (1f + PLAYER_RATIO);
    private static final float LEFT_DRAG_DISAPPEAR_OFFSET = (4f - PLAYER_RATIO) / (4f + 4f * PLAYER_RATIO);
    private static final float RIGHT_DRAG_DISAPPEAR_OFFSET = (4f + PLAYER_RATIO) / (4f + 4f * PLAYER_RATIO);

    private CustomViewDragHelper mDragHelper;
    private View mPlayer;
    private View mDesc;
    private boolean mIsFinishInit = false;
    private boolean mIsMinimum = true;
    private int mVerticalRange;
    private int mHorizontalRange;
    private int mMinTop;
    private int mTop;
    private int mLeft;
    private int mPlayerMaxWidth;
    private int mPlayerMinWidth;
    private int mDragDirect = NONE;
    private float mVerticalOffset = 1f;
    private float mHorizontalOffset = ORIGINAL_MIN_OFFSET;
    private WeakReference<Callback> mCallback;
    private int mLastX;
    private int mLastY;
    private int mDisappearDirect = SLIDE_RESTORE_ORIGINAL;

    public YouTubePlayEffect(Context context) {
        this(context, null);
    }

    public YouTubePlayEffect(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public YouTubePlayEffect(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        mDragHelper = CustomViewDragHelper.create(this, 1f, new MyHelperCallback());
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void restorePosition(){
        this.setAlpha(0f);
        mLeft = mHorizontalRange - mPlayerMinWidth;
        mTop = mVerticalRange;
        mIsMinimum = true;
        mVerticalOffset = 1f;
    }

    public void show(){
        this.setAlpha(1f);
        mDragDirect = VERTICAL;
        maximize();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mDragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isHit = mDragHelper.isViewUnder(mPlayer,(int)event.getX(),(int)event.getY());

        if(isHit) {
            switch (MotionEventCompat.getActionMasked(event)){
                case MotionEvent.ACTION_DOWN: {
                    mLastX = (int) event.getX();
                    mLastY = (int) event.getY();
                }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if(mDragDirect == NONE){
                        int dx = Math.abs(mLastX - (int)event.getX());
                        int dy = Math.abs(mLastY - (int)event.getY());
                        int slop = mDragHelper.getTouchSlop();

                        if(Math.sqrt(dx * dx + dy * dy) >= slop) {
                            if (dy >= dx)
                                mDragDirect = VERTICAL;
                            else
                                mDragDirect = HORIZONTAL;
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:{
                    if(mDragDirect == NONE) {
                        int dx = Math.abs(mLastX - (int) event.getX());
                        int dy = Math.abs(mLastY - (int) event.getY());
                        int slop = mDragHelper.getTouchSlop();

                        if (Math.sqrt(dx * dx + dy * dy) < slop){
                            mDragDirect = VERTICAL;

                            if(mIsMinimum)
                                maximize();
                            else
                                minimize();
                        }
                    }
                }
                    break;

                default:
                    break;
            }
        }

        mDragHelper.processTouchEvent(event);
        return isHit;
    }

    private void maximize() {
        mIsMinimum = false;
        slideVerticalTo(0f);
    }

    private void minimize() {
        mIsMinimum = true;
        slideVerticalTo(1f);
    }

    private boolean slideVerticalTo(float slideOffset) {
        int topBound = mMinTop;
        int y = (int) (topBound + slideOffset * mVerticalRange);

        if (mDragHelper.smoothSlideViewTo(mPlayer, mIsMinimum ?
                (int)(mPlayerMaxWidth * (1 - PLAYER_RATIO)) : getPaddingLeft(), y)) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
    }

    private void slideToLeft(){
        slideHorizontalTo(0f);
        mDisappearDirect = SLIDE_TO_LEFT;
    }

    private void slideToRight(){
        slideHorizontalTo(1f);
        mDisappearDirect = SLIDE_TO_RIGHT;
    }

    private void slideToOriginalPosition(){
        slideHorizontalTo(ORIGINAL_MIN_OFFSET);
        mDisappearDirect = SLIDE_RESTORE_ORIGINAL;
    }

    private boolean slideHorizontalTo(float slideOffset){
        int leftBound = -mPlayer.getWidth();
        int x = (int)(leftBound + slideOffset * mHorizontalRange);

        if(mDragHelper.smoothSlideViewTo(mPlayer, x, mTop)){
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
    }

    private class MyHelperCallback extends CustomViewDragHelper.Callback{
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mPlayer;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if(state == CustomViewDragHelper.STATE_IDLE){

                if(mIsMinimum && mDragDirect == HORIZONTAL && mDisappearDirect != SLIDE_RESTORE_ORIGINAL){
                    if(mCallback != null && mCallback.get() != null)
                        mCallback.get().onDisappear(mDisappearDirect);

                    mDisappearDirect = SLIDE_RESTORE_ORIGINAL;
                    restorePosition();
                    requestLayoutLightly();
                }

                mDragDirect = NONE;
            }
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            int range = 0;

            if(child == mPlayer && mDragDirect == VERTICAL){
                range = mVerticalRange;
            }
//            Log.i("debug","getViewVerticalDragRange-range:"+range);
            return range;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            int range = 0;

            if(child == mPlayer && mIsMinimum && mDragDirect == HORIZONTAL){
                range = mHorizontalRange;
            }
//            Log.i("debug","getViewHorizontalDragRange-range:"+range);
            return range;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            int newTop = mTop;
//            Log.i("debug","clampViewPositionVertical:" + top + "," + dy);
            if(child == mPlayer && mDragDirect == VERTICAL) {
                int topBound = mMinTop;
                int bottomBound = topBound + mVerticalRange;
                newTop = Math.min(Math.max(top, topBound), bottomBound);
            }
//            Log.i("debug","clampViewPositionVertical:newTop-"+newTop);
            return newTop;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int newLeft = mLeft;
//            Log.i("debug","clampViewPositionHorizontal:" + left + "," + dx);
            if(child == mPlayer && mIsMinimum && mDragDirect == HORIZONTAL){
                int leftBound = -mPlayer.getWidth();
                int rightBound = leftBound + mHorizontalRange;
                newLeft = Math.min(Math.max(left,leftBound),rightBound);
            }
//            Log.i("debug","clampViewPositionHorizontal:newLeft-"+newLeft+",mLeft-"+mLeft);
            return newLeft;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
//            Log.i("debug", "onViewPositionChanged:" + "mDragDirect-" + mDragDirect + ",left-" + left + ",top-" + top + ",mLeft-" + mLeft);
//            Log.i("debug","onViewPositionChanged-mPlayer:left-"+mPlayer.getLeft()+",top-"+mPlayer.getTop());
            if(mDragDirect == VERTICAL) {
               mTop = top;
               mVerticalOffset = (float) (mTop - mMinTop) / mVerticalRange;

               mDesc.setAlpha(1 - mVerticalOffset);
            }else if(mIsMinimum && mDragDirect == HORIZONTAL){
                mLeft = left;
                mHorizontalOffset = Math.abs((float)(mLeft + mPlayerMinWidth) / mHorizontalRange);

//            Log.i("debug","mHorizontalOffset="+mHorizontalOffset+",mLeft="+mLeft+",mHorizontalRange="+mHorizontalRange);
            //SurfaceView设置alpha会直接消失，郁闷
//                float alpha = Math.min(
//                        Math.max(1 - Math.abs(mHorizontalOffset - ORIGINAL_MIN_OFFSET),MIN_ALPHA)
//                        ,1);
//                mPlayer.setAlpha(alpha);
            }

            requestLayoutLightly();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if(mDragDirect == VERTICAL)
            {
                if(yvel > 0 || (yvel == 0 && mVerticalOffset >= 0.5f))
                    minimize();
                else if(yvel < 0 || (yvel == 0 && mVerticalOffset < 0.5f))
                    maximize();
            }else if (mIsMinimum && mDragDirect == HORIZONTAL){
                if((mHorizontalOffset < LEFT_DRAG_DISAPPEAR_OFFSET && xvel < 0))
                    slideToLeft();
                else if((mHorizontalOffset > RIGHT_DRAG_DISAPPEAR_OFFSET && xvel > 0))
                    slideToRight();
                else
                    slideToOriginalPosition();
            }
        }
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if(getChildCount() != 2)
            throw new RuntimeException("this ViewGroup must only contains 2 views");

        mPlayer = getChildAt(0);
        mDesc = getChildAt(1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        customMeasure(widthMeasureSpec, heightMeasureSpec);

        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));

        if(!mIsFinishInit){
            mMinTop = getPaddingTop();
            mPlayerMinWidth = mPlayer.getMeasuredWidth();
//            mPlayerMaxWidth = (int)(mPlayerMinWidth / PLAYER_RATIO);
            mHorizontalRange =mPlayerMaxWidth + mPlayerMinWidth;
            mVerticalRange = getMeasuredHeight() - getPaddingTop() - getPaddingBottom()
                    - mPlayer.getMeasuredHeight();

            restorePosition();
            mIsFinishInit = true;
        }
    }

    private void customMeasure(int widthMeasureSpec, int heightMeasureSpec){
        measurePlayer(widthMeasureSpec, heightMeasureSpec);
        measureDesc(widthMeasureSpec, heightMeasureSpec);
    }

    private void measurePlayer(int widthMeasureSpec, int heightMeasureSpec){
        final LayoutParams lp = mPlayer.getLayoutParams();

        if(!mIsFinishInit) {
            int measureWidth = getChildMeasureSpec(widthMeasureSpec,
                getPaddingLeft() + getPaddingRight(), lp.width);

            mPlayerMaxWidth = MeasureSpec.getSize(measureWidth);
        }

        justMeasurePlayer();
    }

    private void measureDesc(int widthMeasureSpec, int heightMeasureSpec){
        measureChild(mDesc, widthMeasureSpec, heightMeasureSpec);
    }

    private void justMeasurePlayer(){
        int widthCurSize =(int)(mPlayerMaxWidth * (1 - mVerticalOffset * PLAYER_RATIO));
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthCurSize, MeasureSpec.EXACTLY);

        int heightSize =(int)(MeasureSpec.getSize(childWidthMeasureSpec) / VIDEO_RATIO);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize,MeasureSpec.EXACTLY);

        mPlayer.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        onLayoutLightly();
    }

    private void onLayoutLightly(){
        if(mDragDirect != HORIZONTAL) {
            mLeft = this.getWidth() - this.getPaddingRight() - this.getPaddingLeft()
                    - mPlayer.getMeasuredWidth();

            mDesc.layout(mLeft, mTop + mPlayer.getMeasuredHeight(),
                    mLeft + mDesc.getMeasuredWidth(), mTop + mDesc.getMeasuredHeight());
        }

        mPlayer.layout(mLeft, mTop, mLeft + mPlayer.getMeasuredWidth(), mTop + mPlayer.getMeasuredHeight());
    }

    private void requestLayoutLightly(){
        justMeasurePlayer();
        onLayoutLightly();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setCallback(Callback callback){
        mCallback = new WeakReference<>(callback);
    }

    public interface Callback{
        void onDisappear(int direct);
    }
}
