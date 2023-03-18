package com.example.sprcontrollerkt.joystick

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.sprcontrollerkt.R
import kotlin.math.*

/**
 * Created by kqw on 2016/8/30.
 * 摇杆控件
 */
class RockerView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val mAreaBackgroundPaint: Paint
    private val mRockerPaint: Paint
    private val mCenterPoint: Point
    private var mRockerPosition: Point
    private var mAreaRadius = 0
    private var mRockerRadius = 0
    private var mCallBackMode = CallBackMode.CALL_BACK_MODE_MOVE
    private var mOnAngleChangeListener: OnAngleChangeListener? = null
    private var mAreaBackgroundMode = AREA_BACKGROUND_MODE_DEFAULT
    private var mAreaBitmap: Bitmap? = null
    private var mAreaColor = 0
    private var mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_DEFAULT
    private var mRockerBitmap: Bitmap? = null
    private var mRockerColor = 0

    init {

        // 获取自定义属性
        initAttribute(context, attrs)
        if (isInEditMode) {
            Log.i(TAG, "RockerView: isInEditMode")
        }

        // 移动区域画笔
        mAreaBackgroundPaint = Paint()
        mAreaBackgroundPaint.isAntiAlias = true

        // 摇杆画笔
        mRockerPaint = Paint()
        mRockerPaint.isAntiAlias = true

        // 中心点
        mCenterPoint = Point()
        // 摇杆位置
        mRockerPosition = Point()
    }

    /**
     * 获取属性
     *
     * @param context context
     * @param attrs   attrs
     */
    private fun initAttribute(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RockerView)

        // 可移动区域背景
        val areaBackground = typedArray.getDrawable(R.styleable.RockerView_areaBackground)
        if (null != areaBackground) {
            // 设置了背景
            when(areaBackground){
                // 设置了一张图片
                is BitmapDrawable -> {
                    mAreaBackgroundMode = AREA_BACKGROUND_MODE_PIC
                    mAreaBitmap = areaBackground.bitmap
                }
                // XML
                is GradientDrawable -> {
                    mAreaBackgroundMode = AREA_BACKGROUND_MODE_XML
                    mAreaBitmap = drawable2Bitmap(areaBackground)
                }
                // 设置了颜色
                is ColorDrawable -> {
                    mAreaBackgroundMode = AREA_BACKGROUND_MODE_COLOR
                    mAreaColor = areaBackground.color
                }
                else -> {
                    mAreaBackgroundMode = AREA_BACKGROUND_MODE_DEFAULT
                }
            }
        } else {
            // 没有设置背景
            mAreaBackgroundMode = AREA_BACKGROUND_MODE_DEFAULT
        }
        // 摇杆背景
        val rockerBackground = typedArray.getDrawable(R.styleable.RockerView_rockerBackground)
        if (null != rockerBackground) {
            // 设置了摇杆背景
            when(rockerBackground){
                is BitmapDrawable -> {
                    // 设置了一张图片
                    mRockerBitmap = (rockerBackground as BitmapDrawable).bitmap
                    mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_PIC
                }
                is GradientDrawable -> {
                    // XML
                    mRockerBitmap = drawable2Bitmap(rockerBackground as GradientDrawable)
                    mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_XML
                }
                is ColorDrawable -> {
                    // 色值
                    mRockerColor = (rockerBackground as ColorDrawable).color
                    mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_COLOR
                }
                else -> {
                    // 其他形式
                    mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_DEFAULT
                }
            }
        } else {
            // 没有设置摇杆背景
            mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_DEFAULT
        }

        // 摇杆半径
        mRockerRadius = typedArray.getDimensionPixelOffset(
            R.styleable.RockerView_rockerRadius, DEFAULT_ROCKER_RADIUS
        )
        Log.i(
            TAG,
            "initAttribute: mAreaBackground = $areaBackground   mRockerBackground = $rockerBackground  mRockerRadius = $mRockerRadius"
        )
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureWidth: Int
        val measureHeight: Int
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        measureWidth = if (widthMode == MeasureSpec.EXACTLY) {
            // 具体的值和match_parent
            widthSize
        } else {
            // wrap_content
            DEFAULT_SIZE
        }
        measureHeight = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else {
            DEFAULT_SIZE
        }
        Log.i(TAG, "onMeasure: --------------------------------------")
        Log.i(
            TAG,
            "onMeasure: widthMeasureSpec = $widthMeasureSpec heightMeasureSpec = $heightMeasureSpec"
        )
        Log.i(
            TAG, "onMeasure: widthMode = $widthMode  measureWidth = $widthSize"
        )
        Log.i(
            TAG, "onMeasure: heightMode = $heightMode  measureHeight = $widthSize"
        )
        Log.i(
            TAG, "onMeasure: measureWidth = $measureWidth measureHeight = $measureHeight"
        )
        setMeasuredDimension(measureWidth, measureHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val measuredWidth = measuredWidth
        val measuredHeight = measuredHeight
        val cx = measuredWidth / 2
        val cy = measuredHeight / 2
        // 中心点
        mCenterPoint[cx] = cy
        // 可移动区域的半径
        mAreaRadius = if (measuredWidth <= measuredHeight) cx else cy
        mAreaRadius -= mRockerRadius

        // 摇杆位置
        if (0 == mRockerPosition.x || 0 == mRockerPosition.y) {
            mRockerPosition[mCenterPoint.x] = mCenterPoint.y
        }

        // 画可移动区域
        if (AREA_BACKGROUND_MODE_PIC == mAreaBackgroundMode || AREA_BACKGROUND_MODE_XML == mAreaBackgroundMode) {
            // 图片
            @SuppressLint("DrawAllocation") val src =
                Rect(0, 0, mAreaBitmap!!.width, mAreaBitmap!!.height)
            @SuppressLint("DrawAllocation") val dst = Rect(
                mCenterPoint.x - mAreaRadius,
                mCenterPoint.y - mAreaRadius,
                mCenterPoint.x + mAreaRadius,
                mCenterPoint.y + mAreaRadius
            )
            canvas.drawBitmap(mAreaBitmap!!, src, dst, mAreaBackgroundPaint)
        } else if (AREA_BACKGROUND_MODE_COLOR == mAreaBackgroundMode) {
            // 色值
            mAreaBackgroundPaint.color = mAreaColor
            canvas.drawCircle(
                mCenterPoint.x.toFloat(),
                mCenterPoint.y.toFloat(),
                mAreaRadius.toFloat(),
                mAreaBackgroundPaint
            )
        } else {
            // 其他或者未设置
            mAreaBackgroundPaint.color = Color.GRAY
            canvas.drawCircle(
                mCenterPoint.x.toFloat(),
                mCenterPoint.y.toFloat(),
                mAreaRadius.toFloat(),
                mAreaBackgroundPaint
            )
        }

        // 画摇杆
        if (ROCKER_BACKGROUND_MODE_PIC == mRockerBackgroundMode || ROCKER_BACKGROUND_MODE_XML == mRockerBackgroundMode) {
            // 图片
            @SuppressLint("DrawAllocation") val src =
                Rect(0, 0, mRockerBitmap!!.width, mRockerBitmap!!.height)
            @SuppressLint("DrawAllocation") val dst = Rect(
                mRockerPosition.x - mRockerRadius,
                mRockerPosition.y - mRockerRadius,
                mRockerPosition.x + mRockerRadius,
                mRockerPosition.y + mRockerRadius
            )
            canvas.drawBitmap(mRockerBitmap!!, src, dst, mRockerPaint)
        } else if (ROCKER_BACKGROUND_MODE_COLOR == mRockerBackgroundMode) {
            // 色值
            mRockerPaint.color = mRockerColor
            canvas.drawCircle(
                mRockerPosition.x.toFloat(),
                mRockerPosition.y.toFloat(),
                mRockerRadius.toFloat(),
                mRockerPaint
            )
        } else {
            // 其他或者未设置
            mRockerPaint.color = Color.RED
            canvas.drawCircle(
                mRockerPosition.x.toFloat(),
                mRockerPosition.y.toFloat(),
                mRockerRadius.toFloat(),
                mRockerPaint
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 回调 开始
                callBackStart()
                val moveX = event.x
                val moveY = event.y
                mRockerPosition = getRockerPositionPoint(
                    mCenterPoint,
                    Point(moveX.toInt(), moveY.toInt()),
                    mAreaRadius.toFloat(),
                    mRockerRadius.toFloat()
                )
                moveRocker(mRockerPosition.x.toFloat(), mRockerPosition.y.toFloat())
            }
            MotionEvent.ACTION_MOVE -> {
                val moveX = event.x
                val moveY = event.y
                mRockerPosition = getRockerPositionPoint(
                    mCenterPoint,
                    Point(moveX.toInt(), moveY.toInt()),
                    mAreaRadius.toFloat(),
                    mRockerRadius.toFloat()
                )
                moveRocker(mRockerPosition.x.toFloat(), mRockerPosition.y.toFloat())
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 回调 结束
                callBackFinish()
                val upX = event.x
                val upY = event.y
                moveRocker(mCenterPoint.x.toFloat(), mCenterPoint.y.toFloat())
                Log.i(TAG, "onTouchEvent: 抬起位置 : x = $upX y = $upY")
            }
        }
        return true
    }

    /**
     * 获取摇杆实际要显示的位置（点）
     *
     * @param centerPoint  中心点
     * @param touchPoint   触摸点
     * @param regionRadius 摇杆可活动区域半径
     * @param rockerRadius 摇杆半径
     * @return 摇杆实际显示的位置（点）
     */
    private fun getRockerPositionPoint(
        centerPoint: Point, touchPoint: Point, regionRadius: Float, rockerRadius: Float
    ): Point {
        // 两点在X轴的距离
        val lenX = (touchPoint.x - centerPoint.x).toFloat()
        Log.i(TAG, "getRockerPositionPoint: X :$lenX")
        // 两点在Y轴距离
        val lenY = (touchPoint.y - centerPoint.y).toFloat()
        Log.i(TAG, "getRockerPositionPoint: Y :$lenY")
        // 两点距离
        val lenXY = sqrt((lenX * lenX + lenY * lenY).toDouble()).toFloat()
        Log.i(TAG, "getRockerPositionPoint: 两点距离 :$lenXY")
        // 计算弧度
        val radian =
            acos((lenX / lenXY).toDouble()) * if (touchPoint.y < centerPoint.y) -1 else 1
        Log.i(TAG, "getRockerPositionPoint: 弧度 :$radian")
        // 计算角度
        val angle = radian2Angle(radian)

        // 回调 返回参数
        callBack(angle)
        Log.i(TAG, "getRockerPositionPoint: 角度 :$angle")
        //        if (lenXY + rockerRadius <= regionRadius) { // 触摸位置在可活动范围内
//            return touchPoint;
//        } else { // 触摸位置在可活动范围以外
//            // 计算要显示的位置
//            int showPointX = (int) (centerPoint.x + (regionRadius - rockerRadius) * Math.cos(radian));
//            int showPointY = (int) (centerPoint.y + (regionRadius - rockerRadius) * Math.sin(radian));
//            return new Point(showPointX, showPointY);
//        }
        return if (lenXY <= regionRadius) { // 触摸位置在可活动范围内
            touchPoint
        } else { // 触摸位置在可活动范围以外
            // 计算要显示的位置
            val showPointX = (centerPoint.x + regionRadius * cos(radian)).toInt()
            val showPointY = (centerPoint.y + regionRadius * sin(radian)).toInt()
            Point(showPointX, showPointY)
        }
    }

    /**
     * 移动摇杆到指定位置
     *
     * @param x x坐标
     * @param y y坐标
     */
    private fun moveRocker(x: Float, y: Float) {
        mRockerPosition[x.toInt()] = y.toInt()
        Log.i(TAG, "onTouchEvent: 移动位置 : x = " + mRockerPosition.x + " y = " + mRockerPosition.y)
        invalidate()
    }

    /**
     * 弧度转角度
     *
     * @param radian 弧度
     * @return 角度[0, 360)
     */
    private fun radian2Angle(radian: Double): Double {
        val tmp = (radian / Math.PI * 180).roundToInt().toDouble()
        return if (tmp >= 0) tmp else 360 + tmp
    }

    /**
     * Drawable 转 Bitmap
     *
     * @param drawable Drawable
     * @return Bitmap
     */
    private fun drawable2Bitmap(drawable: Drawable): Bitmap {
        // 取 drawable 的长宽
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        // 取 drawable 的颜色格式
        val config =
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        // 建立对应 bitmap
        val bitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * 回调
     * 开始
     */
    private fun callBackStart() {
        if (null != mOnAngleChangeListener) {
            mOnAngleChangeListener!!.onStart()
        }
    }

    /**
     * 回调
     * 返回参数
     *
     * @param angle 摇动角度
     */
    private fun callBack(angle: Double) {
        if (null != mOnAngleChangeListener) {
            mOnAngleChangeListener!!.angle(angle)
            mOnAngleChangeListener!!.value(
                (mRockerPosition.x - mCenterPoint.x).toFloat(),
                (mCenterPoint.y - mRockerPosition.y).toFloat(),
                mAreaRadius.toFloat()
            )
        }
    }

    /**
     * 回调
     * 结束
     */
    private fun callBackFinish() {
        if (null != mOnAngleChangeListener) {
            mOnAngleChangeListener!!.onFinish()
        }
    }

    /**
     * 设置回调模式
     *
     * @param mode 回调模式
     */
    fun setCallBackMode(mode: CallBackMode) {
        mCallBackMode = mode
    }

    /**
     * 添加摇杆摇动角度的监听
     *
     * @param listener 回调接口
     */
    fun setOnAngleChangeListener(listener: OnAngleChangeListener?) {
        mOnAngleChangeListener = listener
    }


    /**
     * 回调模式
     */
    enum class CallBackMode {
        // 有移动就立刻回调
        CALL_BACK_MODE_MOVE,
    }

    /**
     * 摇动角度的监听接口
     */
    interface OnAngleChangeListener {
        // 开始
        fun onStart()

        /**
         * 摇杆角度变化
         *
         * @param angle 角度[0,360)
         */
        fun angle(angle: Double)

        /**
         * 摇动值
         *
         * @param x x轴摇动值
         * @param y y轴摇动值
         */
        fun value(x: Float, y: Float, regionRadius: Float)

        // 结束
        fun onFinish()
    }

    companion object {
        private const val TAG = "RockerView"
        private const val DEFAULT_SIZE = 400
        private const val DEFAULT_ROCKER_RADIUS = DEFAULT_SIZE / 8

        // 摇杆可移动区域背景
        private const val AREA_BACKGROUND_MODE_PIC = 0
        private const val AREA_BACKGROUND_MODE_COLOR = 1
        private const val AREA_BACKGROUND_MODE_XML = 2
        private const val AREA_BACKGROUND_MODE_DEFAULT = 3

        // 摇杆背景
        private const val ROCKER_BACKGROUND_MODE_PIC = 4
        private const val ROCKER_BACKGROUND_MODE_COLOR = 5
        private const val ROCKER_BACKGROUND_MODE_XML = 6
        private const val ROCKER_BACKGROUND_MODE_DEFAULT = 7
    }
}
