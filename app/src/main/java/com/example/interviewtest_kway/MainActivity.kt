package com.example.interviewtest_kway

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.example.interviewtest_kway.api.Service
import com.example.interviewtest_kway.chart.MyFillFormatter
import com.example.interviewtest_kway.chart.MyLineLegendRenderer
import com.example.interviewtest_kway.databinding.ActivityMainBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val dataArrayList: ArrayList<String> = ArrayList()
    private val colorArrayList: ArrayList<Int> = arrayListOf(Color.parseColor("#36BF36")
        ,Color.parseColor("#6495ED"),Color.parseColor("#87CEEB")
        ,Color.parseColor("#FFE5B4"),Color.parseColor("#FFB366")
        ,Color.parseColor("#F08080"),Color.RED)
    private val pArrayList:ArrayList<String> = ArrayList()
    private val xData: ArrayList<String> = ArrayList()
    val showCount = 12 //預設顯示1年
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.Main).launch {
            val service: Service = Service.getInstance()
            val data = LineData()
            service.index().enqueue(object : Callback<Service.Data> {/**抓資料*/
            override fun onResponse(call: Call<Service.Data>, response: Response<Service.Data>) {
                val chartData = response.body()?.data!![0].chart_Data
                var count = 0
                for (i in showCount - 1 downTo 0) {
                    val x = chartData[i].year_Month.substring(0,4) + "/" + chartData[i].year_Month.substring(4, 6)
                    xData.add(x)
                    val df = DecimalFormat("0.00")
                    val showData = chartData[i].pe_ee //本益比
                    for (j in showData.indices) {
                        val y = showData[j]
                        dataArrayList.add(y)
                        val p = df.format(showData[j].toFloat() / chartData[i].four_EPS.toFloat()).toString()
                        pArrayList.add(p)
                    }
                    val stock = chartData[i].monthAvg
                    dataArrayList.add(stock)
                    count += 1
                }
                for(i in 0 until 7)
                {
                    val set = LineDataSet(dataArrayList, i)
                    set.color = colorArrayList[i]
                    set.mode = LineDataSet.Mode.LINEAR
                    set.setDrawCircles(false)
                    set.setDrawValues(false)
                    if(i in 1..5)//
                    {
                        val preSet = LineDataSet(dataArrayList, i - 1)
                        set.setDrawFilled(true)
                        set.fillColor = colorArrayList[i]
                        set.fillFormatter = MyFillFormatter(preSet)
                        binding.lineChart.renderer = MyLineLegendRenderer(binding.lineChart
                            ,binding.lineChart.animator,binding.lineChart.viewPortHandler)
                    }
                    else
                    {
                        set.lineWidth = 3f
                    }
                    data.addDataSet(set)
                }
                initX(xData)
                initY()
                binding.lineChart.data = data
                initChartFormat()
                binding.lineChart.invalidate() //繪製圖表
            }

                override fun onFailure(call: Call<Service.Data>, t: Throwable) {
                    val tag = service::class.java.name
                    Log.d(tag, "error: ${t.message}")
                }
            })
        }
    }

    private fun getChartData(arrayList: ArrayList<String>, index: Int): ArrayList<Entry> {
        val chartData: ArrayList<Entry> = ArrayList()
        for (i in 0 until showCount) {
            chartData.add(Entry(i.toFloat(), arrayList[i * 7 + index].toFloat()))
        }
        return chartData
    }

    private fun LineDataSet(arrayList: ArrayList<String>, index: Int): LineDataSet {
        return LineDataSet(getChartData(arrayList, index), "")
    }

    private fun initChartFormat() {
        val description = binding.lineChart.description
        description.text = "(元)"
        description.textSize = 12f
        description.textColor = Color.GRAY
        description.setPosition((binding.lineChart.width - 12).toFloat(),(binding.lineChart.height - 12).toFloat())

        val legend = binding.lineChart.legend
        legend.isEnabled = false

        val x = xData.size - 1
        val txtView :ArrayList<Int> = arrayListOf(R.id.txtView9,R.id.txtView8,R.id.txtView7,R.id.txtView6,R.id.txtView5,R.id.txtView4)
        val view : ArrayList<Int> = arrayListOf(R.id.view6,R.id.view5,R.id.view4,R.id.view3,R.id.view2,R.id.view1)

        setText(binding.txtView1, xData[x], Color.WHITE)
        setText(binding.txtView2, "股價 ${getChartData(dataArrayList,6)[x].y}元", Color.WHITE)
        binding.vStock.setBackgroundColor(colorArrayList[6])
        for(i in 0 until txtView.size)
        {
            setText(findViewById(txtView[i]),"${pArrayList[x * 6 + i]}倍 " +
                    "${getChartData(dataArrayList,i)[x].y}", Color.WHITE)
            findViewById<View>(view[i]).setBackgroundColor(colorArrayList[i])
        }

        binding.lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                legend.resetCustom()
                val x = e.x
                setText(binding.txtView1, xData[x.toInt()], Color.WHITE)
                setText(binding.txtView2, "股價 ${getChartData(dataArrayList,6)[x.toInt()].y}元", Color.WHITE)
                for(i in 0 until txtView.size)
                {
                    setText(findViewById(txtView[i]),"${pArrayList[x.toInt() * 6 + i]}倍 " +
                            "${getChartData(dataArrayList,i)[x.toInt()].y}", Color.WHITE)
                }
            }
            override fun onNothingSelected() {
            }
        })
    }

    private fun setText(textView: TextView, text: String, textColor: Int) {
        textView.text = text
        textView.setTextColor(textColor)
    }
    //x軸相關設定
    private fun initX(dataList: ArrayList<String>) {
        val xAxis: XAxis = binding.lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.GRAY
        xAxis.textSize = 12f

        xAxis.labelCount = 5
        xAxis.spaceMin = 0.5f
        xAxis.spaceMax = 0.5f
        xAxis.setDrawAxisLine(false)
        xAxis.valueFormatter = IndexAxisValueFormatter(dataList)
    }
    //y軸相關設定
    private fun initY() {
        val rightAxis: YAxis = binding.lineChart.axisRight
        rightAxis.setDrawAxisLine(false)
        rightAxis.labelCount = 5
        rightAxis.textColor = Color.GRAY
        rightAxis.textSize = 10f
        val leftAxis: YAxis = binding.lineChart.axisLeft
        leftAxis.setDrawAxisLine(false)
        leftAxis.setDrawLabels(false)
    }
}