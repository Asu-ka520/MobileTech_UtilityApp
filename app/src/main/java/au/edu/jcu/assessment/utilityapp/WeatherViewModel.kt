package au.edu.jcu.assessment.utilityapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class WeatherUiState(
    val weather: CurrentWeather? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCelsius: Boolean = true
)

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState(isLoading = true))
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val weather = repository.getWeather()
                _uiState.update { it.copy(weather = weather, isLoading = false, error = null) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = if (it.weather == null) "Couldn't load weather. Check your connection and try again."
                        else "Couldn't refresh. Showing the last weather we loaded."
                    )
                }
            }
        }
    }

    fun setCelsius(isCelsius: Boolean) {
        _uiState.update { it.copy(isCelsius = isCelsius) }
    }
}
