package com.example.dam.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dam.ui.theme.DamTheme

@Composable
fun VerificationCodeDialog(
    onDismiss: () -> Unit,
    onVerify: (String) -> Boolean, // Retourne true si succès, false si erreur
    onResend: () -> Unit
) {
    var code by remember { mutableStateOf(List(6) { "" }) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val greenColor = Color(0xFF4CAF50)
    val redColor = Color(0xFFEF5350)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = "Enter Verification Code",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = "Please enter the 6-digit code sent to your device",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Code Input Boxes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    code.indices.forEach { index ->
                        CodeInputBox(
                            value = code[index],
                            onValueChange = { newValue ->
                                if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                    val newCode = code.toMutableList()
                                    newCode[index] = newValue
                                    code = newCode
                                }
                            },
                            isFocused = code[index].isEmpty() && code.take(index).all { it.isNotEmpty() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Counter
                Text(
                    text = "${code.count { it.isNotEmpty() }}/6 digits entered",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Verify Button
                    Button(
                        onClick = {
                            val fullCode = code.joinToString("")
                            if (fullCode.length == 6) {
                                onVerify(fullCode)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5A5A5A),
                            disabledContainerColor = Color(0xFFCCCCCC)
                        ),
                        enabled = code.all { it.isNotEmpty() }
                    ) {
                        Text(
                            text = "Verify",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Resend Link
                TextButton(onClick = onResend) {
                    Text(
                        text = "Didn't receive the code? ",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "Resend",
                        fontSize = 14.sp,
                        color = greenColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CodeInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    isFocused: Boolean
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .size(50.dp)
            .border(
                width = 2.dp,
                color = if (isFocused) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(12.dp)
            )
            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
        textStyle = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                innerTextField()
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun VerificationCodeDialogPreview() {
    DamTheme {
        VerificationCodeDialog(
            onDismiss = {},
            onVerify = { _ -> true },  // Return Boolean here
            onResend = {}
        )
    }
}