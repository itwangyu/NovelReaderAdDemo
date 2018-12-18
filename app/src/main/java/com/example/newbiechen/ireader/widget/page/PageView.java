package com.example.newbiechen.ireader.widget.page;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.example.newbiechen.ireader.model.bean.CollBookBean;
import com.example.newbiechen.ireader.utils.UtilsView;
import com.example.newbiechen.ireader.widget.animation.CoverPageAnim;
import com.example.newbiechen.ireader.widget.animation.HorizonPageAnim;
import com.example.newbiechen.ireader.widget.animation.NonePageAnim;
import com.example.newbiechen.ireader.widget.animation.PageAnimation;
import com.example.newbiechen.ireader.widget.animation.ScrollPageAnim;
import com.example.newbiechen.ireader.widget.animation.SimulationPageAnim;
import com.example.newbiechen.ireader.widget.animation.SlidePageAnim;

/**
 * Created by Administrator on 2016/8/29 0029.
 * 原作者的GitHub Project Path:(https://github.com/PeachBlossom/treader)
 * 绘制页面显示内容的类
 */
public class PageView extends FrameLayout {

    private final static String TAG = "BookPageWidget";

    private int mViewWidth = 0; // 当前View的宽
    private int mViewHeight = 0; // 当前View的高

    private int mStartX = 0;
    private int mStartY = 0;
    private boolean isMove = false;
    // 初始化参数
    private int mBgColor = 0xFFCEC29C;
    private PageMode mPageMode = PageMode.SIMULATION;
    // 是否允许点击
    private boolean canTouch = true;
    // 唤醒菜单的区域
    private RectF mCenterRect = null;
    private boolean isPrepare;
    // 动画类
    public PageAnimation mPageAnim;
    private View mAdView,mCoverPageView;
    // 动画监听类
    private PageAnimation.OnPageChangeListener mPageAnimListener = new PageAnimation.OnPageChangeListener() {
        @Override
        public boolean hasPrev() {
            //左侧点击上一页
            return PageView.this.hasPrevPage();
        }

        @Override
        public boolean hasNext() {
            return PageView.this.hasNextPage();
        }

        @Override
        public void pageCancel() {
            PageView.this.pageCancel();
        }
    };

    //点击监听
    private TouchListener mTouchListener;
    //内容加载器
    private PageLoader mPageLoader;

    public PageView(Context context) {
        this(context, null);
    }

    public PageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        //千万不要关闭硬件加速，否则页面渲染会很卡
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;

        isPrepare = true;

        if (mPageLoader != null) {
            mPageLoader.prepareDisplay(w, h);
        }
        postInvalidate();
    }

    //设置翻页的模式
    void setPageMode(PageMode pageMode) {
        mPageMode = pageMode;
        //视图未初始化的时候，禁止调用
        if (mViewWidth == 0 || mViewHeight == 0) return;

        switch (mPageMode) {
            case SIMULATION:
                mPageAnim = new SimulationPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case COVER:
                mPageAnim = new CoverPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case SLIDE:
                mPageAnim = new SlidePageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case NONE:
                mPageAnim = new NonePageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case SCROLL:
                mPageAnim = new ScrollPageAnim(mViewWidth, mViewHeight, 0,
                        mPageLoader.getMarginHeight(), this, mPageAnimListener);
                break;
            default:
                mPageAnim = new SimulationPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
        }
        if (mPageAnim instanceof HorizonPageAnim) {
            ((HorizonPageAnim) mPageAnim).setScrollAnimListener(new HorizonPageAnim.ScrollAnimListener() {
                @Override
                public void onScrollAnimEnd() {
//                    if (mPageLoader == null || mPageLoader.mCurPage == null) {
//                        return;
//                    }
//                    addAdLayout();
                }

                @Override
                public void onCancelAnimEnd() {
//                    addAdLayout();
                }

                @Override
                public void onAnimAbort() {
                    drawCurPage(false);
                }
            });
        }
    }

    public Bitmap getNextBitmap() {
        if (mPageAnim == null) return null;
        return mPageAnim.getNextBitmap();
    }

    public Bitmap getBgBitmap() {
        if (mPageAnim == null) return null;
        return mPageAnim.getBgBitmap();
    }

    public boolean autoPrevPage() {
        //滚动暂时不支持自动翻页
        if (mPageAnim instanceof ScrollPageAnim) {
            return false;
        } else {
            startPageAnim(PageAnimation.Direction.PRE);
            return true;
        }
    }

    public boolean autoNextPage() {
        if (mPageAnim instanceof ScrollPageAnim) {
            return false;
        } else {
            startPageAnim(PageAnimation.Direction.NEXT);
            return true;
        }
    }

    private void startPageAnim(PageAnimation.Direction direction) {
        if (mTouchListener == null) return;
        //是否正在执行动画
        abortAnimation();
        if (direction == PageAnimation.Direction.NEXT) {
            int x = mViewWidth;
            int y = mViewHeight;
            //初始化动画
            mPageAnim.setStartPoint(x, y);
            //设置点击点
            mPageAnim.setTouchPoint(x, y);
            //设置方向
            Boolean hasNext = hasNextPage();

            mPageAnim.setDirection(direction);
            if (!hasNext) {
                return;
            }
        } else {
            int x = 0;
            int y = mViewHeight;
            //初始化动画
            mPageAnim.setStartPoint(x, y);
            //设置点击点
            mPageAnim.setTouchPoint(x, y);
            mPageAnim.setDirection(direction);
            //设置方向方向
            Boolean hashPrev = hasPrevPage();
            if (!hashPrev) {
                return;
            }
        }
        mPageAnim.startAnim();
        this.postInvalidate();
    }

    public void setBgColor(int color) {
        mBgColor = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制背景
        canvas.drawColor(mBgColor);
        //绘制动画
        mPageAnim.draw(canvas);
    }

    public Bitmap mBitmap;
    private boolean shouldDraw=true;
    @Override
    protected void dispatchDraw(Canvas canvas) {
        try {
            if (mBitmap != null) {
                canvas = new Canvas(mBitmap);
//                canvas.drawColor(Color.YELLOW,PorterDuff.Mode.CLEAR);
            }
            if (mPageLoader==null||mPageLoader.mCurPage==null) {
                return;
            }
            switch (mPageLoader.mCurPage.pageType) {
                case TxtPage.VALUE_STRING_COVER_TYPE:
                    //这里用一个标记位解决透明图片的问题
                    if (shouldDraw) {
                        super.dispatchDraw(canvas);
                        shouldDraw=false;
                    }
                    break;
                case TxtPage.VALUE_STRING_AD_TYPE:
                    super.dispatchDraw(canvas);
                    break;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (!canTouch && event.getAction() != MotionEvent.ACTION_DOWN) return true;

        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = x;
                mStartY = y;
                isMove = false;

                canTouch = mTouchListener.onTouch();
                mPageAnim.onTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                // 判断是否大于最小滑动值。
                int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                if (!isMove) {
                    isMove = Math.abs(mStartX - event.getX()) > slop || Math.abs(mStartY - event.getY()) > slop;
                }
                // 如果滑动了，则进行翻页。
                if (isMove) {
                    mPageAnim.onTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isMove) {
                    if (mPageLoader == null || mPageLoader.mCurPage == null) {
                        return true;
                    }

                    //设置中间区域范围
                    if (mCenterRect == null) {
                        mCenterRect = new RectF(mViewWidth / 4, mViewHeight / 4,
                                mViewWidth * 3 / 4, mViewHeight * 3 / 4);
                    }

                    //是否点击了中间
                    if (mCenterRect.contains(x, y)) {
                        if (mTouchListener != null) {
                            mTouchListener.center();
                        }
                        return true;
                    }
                }
                mPageAnim.onTouchEvent(event);
                break;
        }
        return true;
    }

    /**
     * 判断是否存在上一页
     *
     * @return
     */
    private boolean hasPrevPage() {
        mTouchListener.prePage();
        shouldDraw=true;
        return mPageLoader.prev();
    }

    /**
     * 判断是否下一页存在
     *
     * @return
     */
    private boolean hasNextPage() {
        mTouchListener.nextPage();
        shouldDraw=true;
        return mPageLoader.next();
    }

    private void pageCancel() {
        mTouchListener.cancel();
        mPageLoader.pageCancel();

        //翻页取消的时候，如果当前页是广告页，那么就重新添加
        if (mPageLoader.mCurPage != null && mPageLoader.mCurPage.isCustomView) {
            addAdLayout();
        } else {
            //否则清除
            cleanAdView();
        }
    }



    private void addAdLayout() {
        if (mPageLoader==null||mPageLoader.mCurPage==null||!mPageLoader.mCurPage.isCustomView) {
            return;
        }
        switch (mPageLoader.mCurPage.pageType) {
            case TxtPage.VALUE_STRING_AD_TYPE:
                if (mAdView != null) {
                    UtilsView.removeParent(mAdView);
                    addView(mAdView);
                }
                break;
            case TxtPage.VALUE_STRING_COVER_TYPE:
                if (mAdView != null) {
                    UtilsView.removeParent(mCoverPageView);
                    addView(mCoverPageView);
                }
                break;
        }

    }

    /**
     * 清除添加的所有view
     */
    public void cleanAdView() {
        removeAllViews();
    }

    @Override
    public void computeScroll() {
        //进行滑动
        mPageAnim.scrollAnim();
        super.computeScroll();
    }

    //如果滑动状态没有停止就取消状态，重新设置Anim的触碰点
    public void abortAnimation() {
        mPageAnim.abortAnim();
    }

    public boolean isRunning() {
        if (mPageAnim == null) {
            return false;
        }
        return mPageAnim.isRunning();
    }

    public boolean isPrepare() {
        return isPrepare;
    }

    public void setTouchListener(TouchListener mTouchListener) {
        this.mTouchListener = mTouchListener;
    }

    public void drawNextPage() {
        if (!isPrepare) return;

        if (mPageAnim instanceof HorizonPageAnim) {
            ((HorizonPageAnim) mPageAnim).changePage();
        }
        mPageLoader.drawPage(getNextBitmap(), false);
    }

    public boolean drawCoverPage(Bitmap bitmap) {
        if (!isPrepare) return false;

        if (mReaderAdListener == null) {
            return false;
        }
        mBitmap = bitmap;
        if (mPageLoader.mCurPage.hasDrawAd&&mCoverPageView!=null) {
            addAdLayout();
            return true;
        } else {
            mCoverPageView = mReaderAdListener.getCoverPageView();
        }

        if (mCoverPageView == null) {
            return false;
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        mCoverPageView.setLayoutParams(params);
        addAdLayout();
        mPageLoader.mCurPage.hasDrawAd = true;
        return true;
    }

    public boolean drawAdPage(Bitmap bitmap) {
        if (!isPrepare) return false;

        if (mReaderAdListener == null) {
            return false;
        }
        mBitmap = bitmap;
        if (mPageLoader.mCurPage.hasDrawAd&&mAdView!=null) {
            addAdLayout();
            return true;
        } else {
            mAdView = mReaderAdListener.getAdView();
            mReaderAdListener.onRequestAd();
        }

        if (mAdView == null) {
            return false;
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        mAdView.setLayoutParams(params);
        addAdLayout();
        mPageLoader.mCurPage.hasDrawAd = true;
        return true;
    }

    /**
     * 绘制当前页。
     *
     * @param isUpdate
     */
    public void drawCurPage(boolean isUpdate) {

        if (!isPrepare) return;

        if (!isUpdate) {
            if (mPageAnim instanceof ScrollPageAnim) {
                ((ScrollPageAnim) mPageAnim).resetBitmap();
            }
        }
        mPageLoader.drawPage(getNextBitmap(), isUpdate);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            mPageAnim.abortAnim();
            mPageAnim.clear();

            mPageLoader = null;
            mPageAnim = null;
        } catch (Exception e) {

        }

    }

    /**
     * 获取 PageLoader
     *
     * @param collBook
     * @return
     */
    public PageLoader getPageLoader(CollBookBean collBook) {
        // 判是否已经存在
        if (mPageLoader != null) {
            return mPageLoader;
        }
        // 根据书籍类型，获取具体的加载器
        if (collBook.isLocal()) {
            mPageLoader = new LocalPageLoader(this, collBook);
        } else {
            mPageLoader = new NetPageLoader(this, collBook);
        }
        // 判断是否 PageView 已经初始化完成
        if (mViewWidth != 0 || mViewHeight != 0) {
            // 初始化 PageLoader 的屏幕大小
            mPageLoader.prepareDisplay(mViewWidth, mViewHeight);
        }

        return mPageLoader;
    }

    public interface TouchListener {
        boolean onTouch();

        void center();

        void prePage();

        void nextPage();

        void cancel();
    }

    public void setReaderAdListener(ReaderAdListener readerAdListener) {
        mReaderAdListener = readerAdListener;
    }

    ReaderAdListener mReaderAdListener;

    public interface ReaderAdListener {
        View getAdView();

        void onRequestAd();

        View getCoverPageView();
    }
}
