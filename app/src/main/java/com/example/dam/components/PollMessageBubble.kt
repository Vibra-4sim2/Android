package com.example.dam.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dam.models.Poll
import com.example.dam.models.PollOption

@Composable
fun PollMessageBubble(
    poll: Poll,
    onVote: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOptions by remember(poll.id, poll.userVotedOptionIds) {
        mutableStateOf(poll.userVotedOptionIds.toSet())
    }
    var hasVoted by remember(poll.id, poll.userVotedOptionIds) {
        mutableStateOf(poll.userVotedOptionIds.isNotEmpty())
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF2d4a3e),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header avec icône
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 Sondage",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF25D366)
                )
                if (poll.closed) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Fermé",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Fermé",
                            fontSize = 10.sp,
                            color = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Question
            Text(
                text = poll.question,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Options
            poll.options.forEach { option ->
                PollOptionItem(
                    option = option,
                    isSelected = selectedOptions.contains(option.optionId),
                    totalVotes = poll.totalVotes,
                    isMultipleChoice = poll.allowMultiple,
                    isClosed = poll.closed,
                    hasVoted = hasVoted,
                    onSelect = {
                        if (!poll.closed) {
                            selectedOptions = if (poll.allowMultiple) {
                                if (selectedOptions.contains(option.optionId)) {
                                    selectedOptions - option.optionId
                                } else {
                                    selectedOptions + option.optionId
                                }
                            } else {
                                setOf(option.optionId)
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Info et bouton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${poll.totalVotes} vote${if (poll.totalVotes > 1) "s" else ""}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    if (poll.allowMultiple) {
                        Text(
                            text = "Choix multiples",
                            fontSize = 10.sp,
                            color = Color(0xFF25D366).copy(alpha = 0.8f)
                        )
                    }
                }

                if (!poll.closed && selectedOptions.isNotEmpty() && selectedOptions != poll.userVotedOptionIds.toSet()) {
                    Button(
                        onClick = {
                            onVote(selectedOptions.toList())
                            hasVoted = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (hasVoted) "Modifier" else "Voter",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PollOptionItem(
    option: PollOption,
    isSelected: Boolean,
    totalVotes: Int,
    isMultipleChoice: Boolean,
    isClosed: Boolean,
    hasVoted: Boolean,
    onSelect: () -> Unit
) {
    val percentage = if (totalVotes > 0) {
        (option.votes.toFloat() / totalVotes.toFloat() * 100).toInt()
    } else {
        0
    }

    val showResults = hasVoted || isClosed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isClosed) { onSelect() }
            .background(
                color = Color(0xFF1a3a2e),
                shape = RoundedCornerShape(8.dp)
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = Color(0xFF25D366),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
    ) {
        // Barre de progression (si résultats visibles)
        if (showResults) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .matchParentSize()
                    .background(
                        color = Color(0xFF25D366).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicateur de sélection
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Sélectionné",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = if (isMultipleChoice) RoundedCornerShape(3.dp) else RoundedCornerShape(50)
                            )
                    )
                }

                Text(
                    text = option.text,
                    fontSize = 13.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }

            // Résultats
            if (showResults) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${option.votes}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "$percentage%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF25D366)
                    )
                }
            }
        }
    }
}