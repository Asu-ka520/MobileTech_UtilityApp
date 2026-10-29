package au.edu.jcu.assessment.utilityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import au.edu.jcu.assessment.utilityapp.ui.theme.MobileTech_UtilityAppTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = WeatherRepository()
        val factory = WeatherViewModelFactory(repository)
        setContent {
            MobileTech_UtilityAppTheme {
                val weatherViewModel: WeatherViewModel = viewModel(factory = factory)
                UtilityApp(weatherViewModel)
            }
        }
    }
}

@Composable
fun UtilityApp(weatherViewModel: WeatherViewModel) {
    var selectedTab by remember { mutableStateOf("Weather") }
    val state by weatherViewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == "Weather",
                    onClick = { selectedTab = "Weather" },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Weather") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Settings",
                    onClick = { selectedTab = "Settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            "Weather" -> WeatherScreen(state, weatherViewModel::refresh, Modifier.padding(padding))
            else -> SettingsScreen(
                isCelsius = state.isCelsius,
                onUnitChanged = weatherViewModel::setCelsius,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun WeatherScreen(state: WeatherUiState, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("CAIRNS, QUEENSLAND", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("Today", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = onRefresh, enabled = !state.isLoading) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh weather")
            }
        }

        if (state.isLoading && state.weather == null) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("Getting the latest weather…", style = MaterialTheme.typography.bodyMedium)
        }

        state.weather?.let { weather ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(weather.description, style = MaterialTheme.typography.titleLarge)
                    Text("${weather.temperature.display(state.isCelsius)}°", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Light)
                    Text("Feels like ${weather.feelsLike.display(state.isCelsius)}°", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Text("RIGHT NOW", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Humidity", "${weather.humidity}%", Modifier.weight(1f))
                MetricCard("Wind", "${weather.windSpeed.roundToInt()} km/h", Modifier.weight(1f))
            }
            Text("NEXT HOURS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                    weather.hourly.take(6).forEachIndexed { index, hour ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (index == 0) "Now" else hour.time)
                            Text(hour.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${hour.temperature.display(state.isCelsius)}°", fontWeight = FontWeight.SemiBold)
                        }
                        if (index < minOf(weather.hourly.size, 6) - 1) HorizontalDivider()
                    }
                }
            }
            Text("Updated ${weather.updatedAt}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        state.error?.let { message ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = onRefresh) { Text("Try again") }
                }
            }
        }
        Text("Weather data: Open-Meteo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SettingsScreen(isCelsius: Boolean, onUnitChanged: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text("Choose how temperature is shown on your weather screen.", style = MaterialTheme.typography.bodyLarge)
        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Temperature unit", style = MaterialTheme.typography.titleMedium)
                    Text(if (isCelsius) "Celsius (°C)" else "Fahrenheit (°F)", style = MaterialTheme.typography.bodyMedium)
                }
                Switch(checked = isCelsius, onCheckedChange = onUnitChanged)
            }
        }
        Text("Location is set to Cairns, Queensland.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Unit preference lasts while the app is open.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun Double.display(isCelsius: Boolean): Int = if (isCelsius) roundToInt() else (this * 9 / 5 + 32).roundToInt()

@Suppress("UNCHECKED_CAST")
private class WeatherViewModelFactory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) return WeatherViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
