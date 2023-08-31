package com.example.interviewtest_kway.api

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface Service {
    /**建立資料*/
    data class PostRequest(
        @SerializedName("年月") val year_Month: String,
        @SerializedName("月平均收盤價") val monthAvg: String,
        @SerializedName("近四季EPS") val four_EPS: String,
        @SerializedName("月近四季本益比") val four_PE: String,
        @SerializedName("本益比股價基準") val pe_ee: List<String>,
        @SerializedName("近一季BPS") val one_BPS: String,
        @SerializedName("月近一季本淨比") val one_PB: String,
        @SerializedName("本淨比股價基準") val pb_bb: List<String>,
        @SerializedName("平均本益比") val chart_PE_Avg: String,
        @SerializedName("平均本淨比") val chart_PB_Avg: String,
        @SerializedName("近3年年複合成長") val three_Year: String,
    )

    data class PostResponse(
        @SerializedName("股票代號") val id: String,
        @SerializedName("股票名稱") val name: String,
        @SerializedName("本益比基準") val pe_Ratio: List<String>,
        @SerializedName("本淨比基準") val pb_Ratio: List<String>,
        @SerializedName("河流圖資料") val chart_Data: List<PostRequest>,
        @SerializedName("目前本益比") val pe_Now: String,
        @SerializedName("目前本淨比") val pb_Now: String,
        @SerializedName("同業本益比中位數") val pe_Median: String,
        @SerializedName("同業本淨比中位數") val pb_Median: String,
        @SerializedName("本益比股價評估") val pe_Evaluate: String,
        @SerializedName("本淨比股價評估") val pb_Evaluate: String,
        @SerializedName("平均本益比") val pe_Avg_Ratio: String,
        @SerializedName("平均本淨比") val pb_Avg_Ratio: String,
        @SerializedName("本益成長比") val pe_Growth_Ratio: String,
    )
    data class Data(
        val data : List<PostResponse>
    )
    @GET("/v2/per-river/interview?stock_id=2330")/**相對路徑*/
    fun index(): Call<Data>

    companion object {/**產生Retrofit物件*/
    private const val URL = "https://api.nstock.tw"
        var service: Service? = null
        fun getInstance(): Service {
            if (service == null) {
                val client = OkHttpClient.Builder().build()
                val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory((GsonConverterFactory.create()))
                    .client(client)
                    .build()
                service = retrofit.create(Service::class.java)
            }
            return service!!
        }
    }
}