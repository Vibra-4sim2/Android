package com.example.dam.remote

import com.example.dam.models.CreatePollDto
import com.example.dam.models.Poll
import com.example.dam.models.PollResponse
import com.example.dam.models.VoteDto
import retrofit2.Response
import retrofit2.http.*

interface PollApiService {

    /**
     * Create a new poll for a sortie (using sortieId)
     */
    @POST("polls/sortie/{sortieId}")
    suspend fun createPollForSortie(
        @Header("Authorization") token: String,
        @Path("sortieId") sortieId: String,
        @Body createPollDto: CreatePollDto
    ): Response<Poll>

    /**
     * Get all polls for a sortie
     */
    @GET("polls/sortie/{sortieId}")
    suspend fun getSortiePolls(
        @Header("Authorization") token: String,
        @Path("sortieId") sortieId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<PollResponse>

    /**
     * Vote on a poll
     */
    @POST("polls/{pollId}/vote")
    suspend fun voteOnPoll(
        @Header("Authorization") token: String,
        @Path("pollId") pollId: String,
        @Body voteDto: VoteDto
    ): Response<Poll>

    /**
     * Get a single poll by ID
     */
    @GET("polls/{pollId}")
    suspend fun getPoll(
        @Header("Authorization") token: String,
        @Path("pollId") pollId: String
    ): Response<Poll>

    /**
     * Close a poll (creator only)
     */
    @PATCH("polls/{pollId}/close")
    suspend fun closePoll(
        @Header("Authorization") token: String,
        @Path("pollId") pollId: String
    ): Response<Poll>
}

