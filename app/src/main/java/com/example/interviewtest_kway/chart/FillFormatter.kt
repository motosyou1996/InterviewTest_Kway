package com.example.interviewtest_kway.chart

import android.graphics.Canvas
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.renderer.LineChartRenderer
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler

class MyFillFormatter //Pass the dataset of other line in the Constructor
@JvmOverloads constructor(private val boundaryDataSet: ILineDataSet? = null) :
    IFillFormatter {
    override fun getFillLinePosition(
        dataSet: ILineDataSet,
        dataProvider: LineDataProvider,
    ): Float {
        return 0f
    }

    //Define a new method which is used in the LineChartRenderer
    val fillLineBoundary: List<Entry>?
        get() = if (boundaryDataSet != null) {
            (boundaryDataSet as LineDataSet).values
        } else null
}

class MyLineLegendRenderer(
    chart: LineDataProvider?,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler?,
) :
    LineChartRenderer(chart, animator, viewPortHandler), Parcelable {
    constructor(parcel: Parcel) : this(
        TODO("chart"),
        TODO("animator"),
        TODO("viewPortHandler")
    ) {
    }

    //This method is same as it's parent implemntation
    override fun drawLinearFill(
        c: Canvas?,
        dataSet: ILineDataSet,
        trans: Transformer,
        bounds: XBounds,
    ) {
        val filled: Path = mGenerateFilledPathBuffer
        val startingIndex = bounds.min
        val endingIndex = bounds.range + bounds.min
        val indexInterval = 128
        var currentStartIndex = 0
        var currentEndIndex = indexInterval
        var iterations = 0

        // Doing this iteratively in order to avoid OutOfMemory errors that can happen on large bounds sets.
        do {
            currentStartIndex = startingIndex + iterations * indexInterval
            currentEndIndex = currentStartIndex + indexInterval
            currentEndIndex =
                if (currentEndIndex > endingIndex) endingIndex else currentEndIndex
            if (currentStartIndex <= currentEndIndex) {
                generateFilledPath(dataSet, currentStartIndex, currentEndIndex, filled)
                trans.pathValueToPixel(filled)
                val drawable = dataSet.fillDrawable
                if (drawable != null) {
                    drawFilledPath(c, filled, drawable)
                } else {
                    drawFilledPath(c, filled, dataSet.fillColor, /**dataSet.fillAlpha*/255)
                }
            }
            iterations++
        } while (currentStartIndex <= currentEndIndex)
    }

    //This is where we define the area to be filled.
    private fun generateFilledPath(
        dataSet: ILineDataSet,
        startIndex: Int,
        endIndex: Int,
        outputPath: Path,
    ) {
        //Call the custom method to retrieve the dataset for other line
        val boundaryEntry = (dataSet.fillFormatter as MyFillFormatter).fillLineBoundary
        val phaseY = mAnimator.phaseY
        val filled: Path = outputPath
        filled.reset()
        val entry = dataSet.getEntryForIndex(startIndex)
        filled.moveTo(entry.x, boundaryEntry!![0].y)
        filled.lineTo(entry.x, entry.y * phaseY)

        // create a new path
        var currentEntry: Entry? = null
        var previousEntry: Entry? = null
        for (x in startIndex + 1..endIndex) {
            currentEntry = dataSet.getEntryForIndex(x)
            filled.lineTo(currentEntry.x, currentEntry.y * phaseY)
        }

        // close up
        if (currentEntry != null && previousEntry != null) {
            filled.lineTo(currentEntry.x, previousEntry.y)
        }

        //Draw the path towards the other line
        for (x in endIndex downTo startIndex + 1) {
            previousEntry = boundaryEntry[x]
            filled.lineTo(previousEntry.x, previousEntry.y * phaseY)
        }
        filled.close()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyLineLegendRenderer> {
        override fun createFromParcel(parcel: Parcel): MyLineLegendRenderer {
            return MyLineLegendRenderer(parcel)
        }

        override fun newArray(size: Int): Array<MyLineLegendRenderer?> {
            return arrayOfNulls(size)
        }
    }
}