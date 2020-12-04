package com.technology.mycow.kotlinpractice

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitEndpoint {
    @GET(ConstantCollection.RETROFIT_POPULAR_MOVIE)
    fun getMovies(@Query("api_key") key: String): Call<PopularMovies>
}