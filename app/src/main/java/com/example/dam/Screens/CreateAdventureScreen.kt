package com.example.dam.Screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dam.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAdventureScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // States
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var activityType by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var includeCamping by remember { mutableStateOf(false) }
    var showActivityDropdown by remember { mutableStateOf(false) }

    // Camping fields
    var campingName by remember { mutableStateOf("") }
    var campingDescription by remember { mutableStateOf("") }
    var campingLocation by remember { mutableStateOf("") }
    var campingPrice by remember { mutableStateOf("") }
    var campingStartDate by remember { mutableStateOf("") }
    var campingEndDate by remember { mutableStateOf("") }

    val activityTypes = listOf(
        "Hiking",
        "Cycling",
        "Camping",
        "Climbing",
        "Kayaking",
        "Running",
        "Other"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Create Adventure",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Plan your next outdoor experience",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Title Field
                AdventureFormField(
                    label = "Title",
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Mountain Hiking Adventure"
                )

                // Description Field
                AdventureFormField(
                    label = "Description",
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Describe your adventure...",
                    minLines = 3
                )

                // Date Field
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Date",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E3A30)
                        )
                    }

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        placeholder = { Text("Select date and time", color = Color(0xFF9E9E9E)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E3A30),
                            unfocusedTextColor = Color(0xFF1E3A30),
                            focusedBorderColor = GreenAccent,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            cursorColor = GreenAccent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Activity Type Dropdown
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Activity Type",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E3A30)
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = activityType,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Select activity type", color = Color(0xFF9E9E9E)) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color(0xFF757575)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showActivityDropdown = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF1E3A30),
                                unfocusedTextColor = Color(0xFF1E3A30),
                                focusedBorderColor = GreenAccent,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFF5F5F5)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        DropdownMenu(
                            expanded = showActivityDropdown,
                            onDismissRequest = { showActivityDropdown = false },
                            modifier = Modifier
                                .fillMaxWidth(0.87f)
                                .background(Color.White, RoundedCornerShape(12.dp))
                        ) {
                            activityTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type, color = Color(0xFF1E3A30)) },
                                    onClick = {
                                        activityType = type
                                        showActivityDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Capacity Field
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Capacity",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E3A30)
                        )
                    }

                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { capacity = it },
                        placeholder = { Text("Number of participants", color = Color(0xFF9E9E9E)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E3A30),
                            unfocusedTextColor = Color(0xFF1E3A30),
                            focusedBorderColor = GreenAccent,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            cursorColor = GreenAccent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                // Camping Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeCamping = !includeCamping }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Checkbox(
                        checked = includeCamping,
                        onCheckedChange = { includeCamping = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = GreenAccent,
                            uncheckedColor = Color(0xFF9E9E9E)
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = if (includeCamping) GreenAccent else Color(0xFF757575),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Include camping option",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E3A30)
                    )
                }

                // Camping Details Section (Animated)
                AnimatedVisibility(
                    visible = includeCamping,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFF0F9F4),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Camping Section Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = TealAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Camping Details",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A30)
                            )
                        }

                        HorizontalDivider(color = Color(0xFFD0E8DC), thickness = 1.dp)

                        // Camping Name
                        CampingFormField(
                            label = "Camping Name",
                            value = campingName,
                            onValueChange = { campingName = it },
                            placeholder = "Lake View Camping"
                        )

                        // Camping Description
                        CampingFormField(
                            label = "Description",
                            value = campingDescription,
                            onValueChange = { campingDescription = it },
                            placeholder = "Describe the camping...",
                            minLines = 2
                        )

                        // Camping Location
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = TealAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Location",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E3A30)
                                )
                            }

                            OutlinedTextField(
                                value = campingLocation,
                                onValueChange = { campingLocation = it },
                                placeholder = { Text("Lake Geneva, Switzerland", fontSize = 13.sp, color = Color(0xFF9E9E9E)) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF1E3A30),
                                    unfocusedTextColor = Color(0xFF1E3A30),
                                    focusedBorderColor = TealAccent,
                                    unfocusedBorderColor = Color(0xFFD0E8DC),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    cursorColor = TealAccent
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Camping Price
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = TealAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Price (€)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E3A30)
                                )
                            }

                            OutlinedTextField(
                                value = campingPrice,
                                onValueChange = { campingPrice = it },
                                placeholder = { Text("35", fontSize = 13.sp, color = Color(0xFF9E9E9E)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF1E3A30),
                                    unfocusedTextColor = Color(0xFF1E3A30),
                                    focusedBorderColor = TealAccent,
                                    unfocusedBorderColor = Color(0xFFD0E8DC),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    cursorColor = TealAccent
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Dates Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Start Date
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Start Date",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E3A30)
                                )

                                OutlinedTextField(
                                    value = campingStartDate,
                                    onValueChange = { campingStartDate = it },
                                    placeholder = { Text("mm/dd/yyyy", fontSize = 12.sp, color = Color(0xFF9E9E9E)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF1E3A30),
                                        unfocusedTextColor = Color(0xFF1E3A30),
                                        focusedBorderColor = TealAccent,
                                        unfocusedBorderColor = Color(0xFFD0E8DC),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        cursorColor = TealAccent
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // End Date
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "End Date",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E3A30)
                                )

                                OutlinedTextField(
                                    value = campingEndDate,
                                    onValueChange = { campingEndDate = it },
                                    placeholder = { Text("mm/dd/yyyy", fontSize = 12.sp, color = Color(0xFF9E9E9E)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF1E3A30),
                                        unfocusedTextColor = Color(0xFF1E3A30),
                                        focusedBorderColor = TealAccent,
                                        unfocusedBorderColor = Color(0xFFD0E8DC),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        cursorColor = TealAccent
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }

                // Create Button
                Button(
                    onClick = {
                        // Handle create adventure
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenAccent
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 6.dp
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = "Create Adventure",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun AdventureFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E3A30)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF9E9E9E)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF1E3A30),
                unfocusedTextColor = Color(0xFF1E3A30),
                focusedBorderColor = GreenAccent,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFF5F5F5),
                cursorColor = GreenAccent
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun CampingFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E3A30)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 13.sp, color = Color(0xFF9E9E9E)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF1E3A30),
                unfocusedTextColor = Color(0xFF1E3A30),
                focusedBorderColor = TealAccent,
                unfocusedBorderColor = Color(0xFFD0E8DC),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = TealAccent
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}