package com.example.myweatherapp

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WeatherApi.initialize(applicationContext)
        setContent {
            WeatherApp()
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WeatherApp(viewModel: WeatherViewModel = viewModel()) {
    var city by remember { mutableStateOf("") }
    val locationPermissionsState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val contextt: Context = LocalContext.current
    // Get permission status
    val hasLocationPermission = locationPermissionsState.status.isGranted
    val shouldShowRationale = locationPermissionsState.status.shouldShowRationale

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Weather App",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City Name") },
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { viewModel.fetchWeatherByCity(city) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Search")
            }
        }

        Button(
            onClick = {
                if (hasLocationPermission) {  // Changed from locationPermissionsState.hasPermission
                    viewModel.FetchLocation(context  = contextt)
                } else {
                    locationPermissionsState.launchPermissionRequest()
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Use My Location")
        }

        if (!hasLocationPermission && shouldShowRationale) {  // Changed here too
            Text(
                text = "Location permission is required...",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        when {
            viewModel.isLoading.value -> {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            viewModel.error.value != null -> {
                Text(
                    text = viewModel.error.value!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            viewModel.weatherData.value != null -> {
                WeatherInfo(weatherData = viewModel.weatherData.value!!)
            }
        }
    }
}

@Composable
fun WeatherInfo(weatherData: WeatherResponse) {
    val iconUrl = "https://openweathermap.org/img/wn/${weatherData.weather[0].icon}@2x.png"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${weatherData.name}, ${weatherData.sys.country}",
                style = MaterialTheme.typography.headlineSmall
            )

            AsyncImage(
                model = iconUrl,
                contentDescription = weatherData.weather[0].description,
                modifier = Modifier.size(100.dp)
            )

            Text(
                text = "${weatherData.main.temp}°C",
                style = MaterialTheme.typography.displaySmall
            )

            Text(
                text = weatherData.weather[0].description.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetailItem(
                    icon = Icons.Default.Build,
                    label = "Min",
                    value = "${weatherData.main.temp_min}°C"
                )

                WeatherDetailItem(
                    icon = Icons.Default.Build,
                    label = "Max",
                    value = "${weatherData.main.temp_max}°C"
                )

                WeatherDetailItem(
                    icon = Icons.Default.Favorite,
                    label = "Humidity",
                    value = "${weatherData.main.humidity}%"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetailItem(
                    icon = Icons.Filled.Info,
                    label = "Wind",
                    value = "${weatherData.wind.speed} m/s"
                )

                WeatherDetailItem(
                    icon = Icons.Default.Info,
                    label = "Pressure",
                    value = "${weatherData.main.pressure} hPa"
                )
            }
        }
    }
}

@Composable
fun WeatherDetailItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = label)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}


