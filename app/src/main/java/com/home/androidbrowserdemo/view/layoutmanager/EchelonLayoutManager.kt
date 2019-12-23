package com.home.androidbrowserdemo.view.layoutmanager

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dingmouren.layoutmanagergroup.echelon.ItemViewInfo
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class EchelonLayoutManager : RecyclerView.LayoutManager() {

    private var mItemViewWidth: Int = 0
    private var mItemViewHeight: Int = 0
    private var mItemCount: Int = 0
    private var mScrollOffset = 2147483647
    private val mScale = 0.9f
    private val verticalSpace: Int
        get() = this.height - this.paddingTop - this.paddingBottom
    private val horizontalSpace: Int
        get() = this.width - this.paddingLeft - this.paddingRight

    init {
        this.mItemViewWidth = (this.horizontalSpace.toFloat() * 0.87f).toInt()
        this.mItemViewHeight = (this.mItemViewWidth.toFloat() * 1.46f).toInt()
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(-2, -2)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
        if (state.itemCount != 0 && !state.isPreLayout) {
            this.removeAndRecycleAllViews(recycler!!)
            this.mItemViewWidth = (this.horizontalSpace.toFloat() * 0.87f).toInt()
            this.mItemViewHeight = (this.mItemViewWidth.toFloat() * 1.46f).toInt()
            this.mItemCount = this.itemCount
            this.mScrollOffset = min(
                max(this.mItemViewHeight, this.mScrollOffset),
                this.mItemCount * this.mItemViewHeight
            )
            this.layoutChild(recycler)
        }
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        val pendingScrollOffset = this.mScrollOffset + dy
        this.mScrollOffset = min(
            max(this.mItemViewHeight, this.mScrollOffset + dy),
            this.mItemCount * this.mItemViewHeight
        )
        this.layoutChild(recycler)
        return this.mScrollOffset - pendingScrollOffset + dy
    }

    override fun canScrollVertically(): Boolean {
        return true
    }

    private fun layoutChild(recycler: RecyclerView.Recycler?) {
        if (this.itemCount != 0) {
            var bottomItemPosition =
                floor((this.mScrollOffset / this.mItemViewHeight).toDouble()).toInt()
            var remainSpace = this.verticalSpace - this.mItemViewHeight
            val bottomItemVisibleHeight = this.mScrollOffset % this.mItemViewHeight
            val offsetPercentRelativeToItemView =
                bottomItemVisibleHeight.toFloat() * 1.0f / this.mItemViewHeight.toFloat()
            val layoutInfos = mutableListOf<Any>()
            var layoutCount = bottomItemPosition - 1
            var startPos = 1
            var i: Int
            while (layoutCount >= 0) {
                val maxOffset =
                    ((this.verticalSpace - this.mItemViewHeight) / 2).toDouble() * Math.pow(
                        0.8,
                        startPos.toDouble()
                    )
                i =
                    (remainSpace.toDouble() - offsetPercentRelativeToItemView.toDouble() * maxOffset).toInt()
                val scaleXY =
                    (this.mScale.toDouble().pow((startPos - 1).toDouble()) * (1.0f - offsetPercentRelativeToItemView * (1.0f - this.mScale)).toDouble()).toFloat()
                val layoutPercent = i.toFloat() * 1.0f / this.verticalSpace.toFloat()
                val info = ItemViewInfo(i, scaleXY, offsetPercentRelativeToItemView, layoutPercent)
                layoutInfos.add(0, info)
                remainSpace = (remainSpace.toDouble() - maxOffset).toInt()
                if (remainSpace <= 0) {
                    info.top = (remainSpace.toDouble() + maxOffset).toInt()
                    info.positionOffset = 0.0f
                    info.layoutPercent = (info.top / this.verticalSpace).toFloat()
                    info.scaleXY =
                        this.mScale.toDouble().pow((startPos - 1).toDouble()).toFloat()
                    break
                }
                --layoutCount
                ++startPos
            }
            if (bottomItemPosition < this.mItemCount) {
                layoutCount = this.verticalSpace - bottomItemVisibleHeight
                layoutInfos.add(
                    ItemViewInfo(
                        layoutCount,
                        1.0f,
                        bottomItemVisibleHeight.toFloat() * 1.0f / this.mItemViewHeight.toFloat(),
                        layoutCount.toFloat() * 1.0f / this.verticalSpace.toFloat()
                    ).setIsBottom()
                )
            } else {
                --bottomItemPosition
            }
            layoutCount = layoutInfos.size
            startPos = bottomItemPosition - (layoutCount - 1)
            val endPos = bottomItemPosition
            val childCount = this.childCount
            var view: View?
            i = childCount - 1
            while (i >= 0) {
                view = this.getChildAt(i)
                val pos = this.getPosition(view!!)
                if (pos > endPos || pos < startPos) {
                    this.removeAndRecycleView(view, recycler!!)
                }
                --i
            }
            this.detachAndScrapAttachedViews(recycler!!)
            i = 0
            while (i < layoutCount) {
                view = recycler.getViewForPosition(startPos + i)
                val layoutInfo = layoutInfos.get(i) as ItemViewInfo
                this.addView(view)
                this.measureChildWithExactlySize(view)
                val left = (this.horizontalSpace - this.mItemViewWidth) / 2
                this.layoutDecoratedWithMargins(
                    view,
                    left,
                    layoutInfo.top,
                    left + this.mItemViewWidth,
                    layoutInfo.top + this.mItemViewHeight
                )
                view.pivotX = (view.width / 2).toFloat()
                view.pivotY = 0.0f
                view.scaleX = layoutInfo.scaleXY
                view.scaleY = layoutInfo.scaleXY
                ++i
            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun measureChildWithExactlySize(child: View) {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(this.mItemViewWidth, 1073741824)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(this.mItemViewHeight, 1073741824)
        child.measure(widthSpec, heightSpec)
    }
}