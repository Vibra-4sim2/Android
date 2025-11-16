// remote/AdventureApi.kt
package com.example.dam.remote

import com.example.dam.models.SortieResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface AdventureApi {
    @Multipart
    @POST("sorties")
    suspend fun createSortie(
        @Header("Authorization") token: String,
        @Part photo: MultipartBody.Part?,
        @Part("titre") titre: RequestBody,
        @Part("description") description: RequestBody,
        @Part("date") date: RequestBody,
        @Part("type") type: RequestBody,
        @Part("option_camping") optionCamping: RequestBody,
        @Part("lieu") lieu: RequestBody,
        @Part("difficulte") difficulte: RequestBody,
        @Part("niveau") niveau: RequestBody,
        @Part("capacite") capacite: RequestBody,
        @Part("prix") prix: RequestBody,
//        @Part("campingId") campingId: RequestBody?,
        @Part("itineraire") itineraire: RequestBody,
        @Part("camping") camping: RequestBody?
    ): Response<Any>




    // ============== NEW GET ENDPOINTS ==============

    @GET("sorties")
    suspend fun getAllSorties(): Response<List<SortieResponse>>

    @GET("sorties/{id}")
    suspend fun getSortieById(
        @Path("id") id: String
    ): Response<SortieResponse>


}