package com.pixelsface.towntalk.features.events.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.pixelsface.towntalk.features.events.domain.model.Event
import com.pixelsface.towntalk.ui.theme.TownTalkColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    viewModel: EventsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onEventCreatedSuccessfully: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    var selectedStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedStartTime by remember { mutableStateOf<LocalTime?>(null) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEndTime by remember { mutableStateOf<LocalTime?>(null) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val eventOperationState by viewModel.eventOperationUiState.collectAsState()
    val selectedImageUris by viewModel.selectedImageUris.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isProcessingImage by remember { mutableStateOf(false) }

    // --- Image Picking and Camera ---
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var permissionAction by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                isProcessingImage = true
                coroutineScope.launch {
                    val resizedUris = uris.mapNotNull { uri ->
                        resizeImageAsync(context, uri, 1280, 1280)
                    }
                    viewModel.addImageUris(resizedUris)
                    isProcessingImage = false
                }
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success: Boolean ->
            if (success) {
                tempImageUri?.let { uri ->
                    isProcessingImage = true
                    coroutineScope.launch {
                        val resizedUri = resizeImageAsync(context, uri, 1280, 1280)
                        resizedUri?.let { viewModel.addImageUri(it) }
                        isProcessingImage = false
                    }
                }
            }
        }
    )

    val readStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[readStoragePermission] == true
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true

        when (permissionAction) {
            "gallery" -> {
                if (readGranted) {
                    imagePickerLauncher.launch("image/*")
                } else {
                    viewModel.setEventOperationError("Storage permission is required to select images from gallery.")
                }
            }
            "camera" -> {
                if (cameraGranted && readGranted) {
                    val uri = createImageUri(context)
                    tempImageUri = uri
                    cameraLauncher.launch(uri)
                } else if (!cameraGranted && !readGranted) {
                     viewModel.setEventOperationError("Camera and Storage permissions are required to take photos.")
                } else if (!cameraGranted) {
                    viewModel.setEventOperationError("Camera permission is required to take photos.")
                } else { 
                    viewModel.setEventOperationError("Storage permission is required to save photos.")
                }
            }
        }
        permissionAction = null
    }
    // --- End Image Picking and Camera ---

    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val timeFormatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }

    LaunchedEffect(eventOperationState) {
        if (eventOperationState is EventOperationUiState.Success && (eventOperationState as EventOperationUiState.Success).eventId != null) {
            viewModel.resetEventOperationState()
            onEventCreatedSuccessfully()
        }
    }

    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedStartDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
            ?: System.currentTimeMillis()
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedEndDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
            ?: selectedStartDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
            ?: System.currentTimeMillis()
    )

    val startTimePickerState = rememberTimePickerState(
        initialHour = selectedStartTime?.hour ?: LocalTime.now().hour,
        initialMinute = selectedStartTime?.minute ?: LocalTime.now().minute,
        is24Hour = false
    )
    val endTimePickerState = rememberTimePickerState(
        initialHour = selectedEndTime?.hour ?: selectedStartTime?.hour ?: LocalTime.now().hour,
        initialMinute = selectedEndTime?.minute ?: selectedStartTime?.minute ?: LocalTime.now().minute,
        is24Hour = false
    )

    Scaffold(
        containerColor = TownTalkColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("Create New Event", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TownTalkColors.OnPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TownTalkColors.Primary,
                    titleContentColor = TownTalkColors.OnPrimary,
                    navigationIconContentColor = TownTalkColors.OnPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TownTalkColors.Primary,
                unfocusedBorderColor = TownTalkColors.OnSurface.copy(alpha = 0.3f),
                focusedLabelColor = TownTalkColors.Primary,
                cursorColor = TownTalkColors.Primary,
                errorBorderColor = TownTalkColors.Error,
                errorLabelColor = TownTalkColors.Error,
                disabledTextColor = TownTalkColors.OnSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium),
                disabledBorderColor = TownTalkColors.OnSurface.copy(alpha = 0.3f)
            )

            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors, textStyle = MaterialTheme.typography.bodyLarge.copy(color = TownTalkColors.OnBackground))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().height(120.dp), colors = textFieldColors, textStyle = MaterialTheme.typography.bodyLarge.copy(color = TownTalkColors.OnBackground))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g., Music, Workshop)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors, textStyle = MaterialTheme.typography.bodyLarge.copy(color = TownTalkColors.OnBackground))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address / Venue") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors, textStyle = MaterialTheme.typography.bodyLarge.copy(color = TownTalkColors.OnBackground))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors, textStyle = MaterialTheme.typography.bodyLarge.copy(color = TownTalkColors.OnBackground))
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = selectedStartDate?.format(dateFormatter) ?: "",
                onValueChange = { },
                label = { Text("Start Date") },
                placeholder = { Text("Select start date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStartDatePicker = true },
                readOnly = true,
                enabled = false,
                colors = textFieldColors,
                trailingIcon = { Icon(Icons.Filled.CalendarToday, "Select Start Date") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = selectedStartTime?.format(timeFormatter) ?: "",
                onValueChange = { },
                label = { Text("Start Time") },
                placeholder = { Text("Select start time") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStartTimePicker = true },
                readOnly = true,
                enabled = false,
                colors = textFieldColors,
                trailingIcon = { Icon(Icons.Filled.Schedule, "Select Start Time") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = selectedEndDate?.format(dateFormatter) ?: "",
                onValueChange = { },
                label = { Text("End Date (Optional)") },
                placeholder = { Text("Select end date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEndDatePicker = true },
                readOnly = true,
                enabled = false,
                colors = textFieldColors,
                trailingIcon = { Icon(Icons.Filled.CalendarToday, "Select End Date") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = selectedEndTime?.format(timeFormatter) ?: "",
                onValueChange = { },
                label = { Text("End Time (Optional)") },
                placeholder = { Text("Select end time") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEndTimePicker = true },
                readOnly = true,
                enabled = false,
                colors = textFieldColors,
                trailingIcon = { Icon(Icons.Filled.Schedule, "Select End Time") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Event Images", style = MaterialTheme.typography.titleMedium, color = TownTalkColors.OnBackground)
            Spacer(modifier = Modifier.height(8.dp))

            if (selectedImageUris.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    items(selectedImageUris) { uri ->
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Selected image",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { viewModel.removeImageUri(uri) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove image", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { 
                        permissionAction = "gallery"
                        permissionLauncher.launch(arrayOf(readStoragePermission)) 
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TownTalkColors.Secondary),
                    enabled = !isProcessingImage
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = "Gallery", tint = TownTalkColors.OnSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery", color = TownTalkColors.OnSecondary)
                }
                Button(
                    onClick = { 
                        permissionAction = "camera"
                        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, readStoragePermission)) 
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TownTalkColors.Secondary),
                    enabled = !isProcessingImage
                ) {
                     Icon(Icons.Filled.PhotoCamera, contentDescription = "Camera", tint = TownTalkColors.OnSecondary)
                     Spacer(modifier = Modifier.width(8.dp))
                     Text("Camera", color = TownTalkColors.OnSecondary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isProcessingImage) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TownTalkColors.Primary, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing images...", style = MaterialTheme.typography.bodyMedium, color = TownTalkColors.OnBackground)
                }
            }

            if (eventOperationState is EventOperationUiState.Loading) {
                CircularProgressIndicator(color = TownTalkColors.Primary)
                val loadingMessage = "Processing..."
                Text(loadingMessage, color = TownTalkColors.Primary, style = MaterialTheme.typography.bodyMedium)
            }
            if (eventOperationState is EventOperationUiState.Error) {
                Text(
                    (eventOperationState as EventOperationUiState.Error).message,
                    color = TownTalkColors.Error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank() && city.isNotBlank() && address.isNotBlank() && category.isNotBlank()) {
                        if (selectedStartDate == null || selectedStartTime == null) {
                            viewModel.setEventOperationError("Please select a valid start date and time.")
                            return@Button
                        }

                        val startDateTime = LocalDateTime.of(selectedStartDate, selectedStartTime)
                        val startTimestamp = Timestamp(startDateTime.atZone(ZoneId.systemDefault()).toInstant().epochSecond, startDateTime.nano)

                        var endTimestamp: Timestamp? = null
                        if (selectedEndDate != null && selectedEndTime != null) {
                            val endDateTime = LocalDateTime.of(selectedEndDate, selectedEndTime)
                            if(endDateTime.isBefore(startDateTime)) {
                                viewModel.setEventOperationError("End time cannot be before start time.")
                                return@Button
                            }
                            endTimestamp = Timestamp(endDateTime.atZone(ZoneId.systemDefault()).toInstant().epochSecond, endDateTime.nano)
                        } else if (selectedEndDate != null || selectedEndTime != null) {
                            viewModel.setEventOperationError("Please select both end date and end time, or leave both empty.")
                            return@Button
                        }

                        val newEvent = Event(
                            title = title.trim(),
                            description = description.trim(),
                            category = category.trim(),
                            address = address.trim(),
                            city = city.trim(),
                            startTime = startTimestamp,
                            endTime = endTimestamp,
                            imageUrls = null
                        )
                        viewModel.createEvent(newEvent)
                    } else {
                         viewModel.setEventOperationError("Please fill all required non-optional fields.")
                    }
                },
                enabled = eventOperationState !is EventOperationUiState.Loading && !isProcessingImage,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TownTalkColors.Primary)
            ) {
                Text("Create Event", style = MaterialTheme.typography.labelLarge, color = TownTalkColors.OnPrimary)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let { millis ->
                        selectedStartDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            title = "Select Start Time",
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedStartTime = LocalTime.of(startTimePickerState.hour, startTimePickerState.minute)
                    showStartTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
            }
        ) {
            TimeInput(state = startTimePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let { millis ->
                        selectedEndDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            title = "Select End Time",
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedEndTime = LocalTime.of(endTimePickerState.hour, endTimePickerState.minute)
                    showEndTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
            }
        ) {
            TimeInput(state = endTimePickerState)
        }
    }
}

fun createImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = context.getExternalFilesDir("event_images_temp_camera")
    if (storageDir != null && !storageDir.exists()) {
        storageDir.mkdirs()
    }
    val imageFile = File.createTempFile(
        imageFileName,  
        ".jpg",         
        storageDir      
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider", 
        imageFile
    )
}

@Composable
fun TimePickerDialog(
    title: String = "Select Time", 
    onDismissRequest: () -> Unit,
    confirmButton: @Composable (() -> Unit),
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = { 
            content()
        },
        confirmButton = confirmButton,
        dismissButton = dismissButton,
    )
}

suspend fun resizeImageAsync(
    context: Context,
    uri: Uri,
    maxWidth: Int,
    maxHeight: Int,
    quality: Int = 85 // Default JPEG quality
): Uri? = withContext(Dispatchers.IO) {
    try {
        var inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null

        // Check orientation
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close() // Close it as it has been consumed

        inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null // Reopen for exif
        val exif = inputStream.use { ExifInterface(it) }
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        var srcWidth = originalBitmap.width
        var srcHeight = originalBitmap.height

        val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, srcWidth, srcHeight, matrix, true)
        if (rotatedBitmap != originalBitmap) { // recycle original if new one was created
            originalBitmap.recycle()
        }
        
        srcWidth = rotatedBitmap.width
        srcHeight = rotatedBitmap.height


        var newWidth = srcWidth
        var newHeight = srcHeight

        if (newWidth > maxWidth) {
            newHeight = (newHeight * (maxWidth.toFloat() / newWidth)).toInt()
            newWidth = maxWidth
        }

        if (newHeight > maxHeight) {
            newWidth = (newWidth * (maxHeight.toFloat() / newHeight)).toInt()
            newHeight = maxHeight
        }
        
        if (newWidth <= 0 || newHeight <= 0) { // Safety check for invalid dimensions
            rotatedBitmap.recycle()
            return@withContext null
        }

        val scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap, newWidth, newHeight, true)
        if (scaledBitmap != rotatedBitmap) { // recycle rotated if new one was created
             rotatedBitmap.recycle()
        }


        val outputDir = File(context.cacheDir, "resized_event_images")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val outputFile = File.createTempFile("resized_", ".jpg", outputDir)
        FileOutputStream(outputFile).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
        scaledBitmap.recycle()
        
        return@withContext Uri.fromFile(outputFile)
    } catch (e: Exception) {
        // Log error e.printStackTrace() or use a proper logger
        e.printStackTrace()
        return@withContext null
    }
}

// Need to add this to EventsViewModel:
// fun setEventOperationError(message: String) {
// _eventOperationUiState.value = EventOperationUiState.Error(message)
// } 