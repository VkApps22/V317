package br.com.kascosys.vulkanconnectv317.interfaces

import retrofit2.Call
import retrofit2.http.GET

interface TestAPI {

    @GET("/H")
    fun getPingServerH(): Call<APIResponse>

    @GET("/L")
    fun getPingServerL(): Call<APIResponse>
}

class APIResponse(val bool: Boolean = true)