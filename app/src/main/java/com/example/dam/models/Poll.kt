package com.example.dam.models


import com.google.gson.annotations.SerializedName

data class CreatePollDto(
    val question: String,
    val options: List<String>,
    val allowMultiple: Boolean = false,
    val closesAt: String? = null
)

data class PollOption(
    val optionId: String,
    val text: String,
    val votes: Int
)

data class Poll(
    @SerializedName("_id")
    val id: String,
    val chatId: String,
    val creatorId: String,
    val question: String,
    val options: List<PollOption>,
    val allowMultiple: Boolean,
    val closesAt: String?,
    val closed: Boolean,
    val userVotedOptionIds: List<String>,
    val totalVotes: Int,
    val createdAt: String,
    val updatedAt: String
)

data class PollResponse(
    val polls: List<Poll>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

data class VoteDto(
    val optionIds: List<String>
)