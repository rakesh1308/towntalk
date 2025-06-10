package com.pixelsface.towntalk.features.auth.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.*

@Composable
fun PhoneNumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Phone Number",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var showCountryPicker by remember { mutableStateOf(false) }
    var selectedCountryCode by remember { mutableStateOf(getDefaultCountryCode()) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country code button
            OutlinedButton(
                onClick = { showCountryPicker = true },
                modifier = Modifier.width(80.dp)
            ) {
                Text("+$selectedCountryCode")
            }
            
            // Phone number input
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    // Only allow digits
                    if (newValue.all { it.isDigit() }) {
                        onValueChange(newValue)
                    }
                },
                label = { Text(label) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.weight(1f),
                enabled = enabled,
                isError = isError,
                singleLine = true
            )
        }
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
    
    if (showCountryPicker) {
        CountryCodePickerDialog(
            onDismiss = { showCountryPicker = false },
            onCountrySelected = { code ->
                selectedCountryCode = code
                showCountryPicker = false
            }
        )
    }
}

@Composable
private fun CountryCodePickerDialog(
    onDismiss: () -> Unit,
    onCountrySelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Country Code") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Common country codes
                listOf(
                    "1" to "United States/Canada",
                    "44" to "United Kingdom",
                    "91" to "India",
                    "81" to "Japan",
                    "86" to "China",
                    "49" to "Germany",
                    "33" to "France",
                    "61" to "Australia"
                ).forEach { (code, country) ->
                    TextButton(
                        onClick = { onCountrySelected(code) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+$code - $country")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getDefaultCountryCode(): String {
    return Locale.getDefault().country?.let { country ->
        when (country) {
            "US" -> "1"
            "GB" -> "44"
            "IN" -> "91"
            else -> "1" // Default to US
        }
    } ?: "1" // Default to US if country code is not available
} 