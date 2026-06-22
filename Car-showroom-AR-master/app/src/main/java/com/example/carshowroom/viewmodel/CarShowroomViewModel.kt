package com.example.carshowroom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carshowroom.audio.SoundEngine
import com.example.carshowroom.data.CarRepository
import com.example.carshowroom.data.models.CarColor
import com.example.carshowroom.data.models.CarModel
import com.example.carshowroom.data.models.WheelStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

enum class InteractionMode {
    TRANSLATE,
    ZOOM,
    ROTATE,
    LOCKED
}

data class CarShowroomUiState(
    val cars: List<CarModel> = emptyList(),
    val colors: List<CarColor> = emptyList(),
    val wheels: List<WheelStyle> = emptyList(),
    val selectedCar: CarModel? = null,
    val selectedColor: CarColor? = null,
    val selectedWheel: WheelStyle? = null,
    val isRevving: Boolean = false,
    val isMuted: Boolean = false,
    val interactionMode: InteractionMode = InteractionMode.TRANSLATE,
)

@HiltViewModel
class CarShowroomViewModel @Inject constructor(
    private val repository: CarRepository,
    private val soundEngine: SoundEngine,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CarShowroomUiState())
    val uiState: StateFlow<CarShowroomUiState> = _uiState.asStateFlow()

    private var revJob: Job? = null
    private var currentPitch = 1.0f
    private var wasPlayingBeforePause = true

    init {
        handleIntent(CarShowroomIntent.Initialize)
    }

    fun handleIntent(intent: CarShowroomIntent) {
        when (intent) {
            is CarShowroomIntent.Initialize -> {
                val cars = repository.getCars()
                val colors = repository.getColors()
                val wheels = repository.getWheelStyles()
                _uiState.value = _uiState.value.copy(
                    cars = cars,
                    colors = colors,
                    wheels = wheels,
                    selectedCar = cars.firstOrNull(),
                    selectedColor = colors.firstOrNull(),
                    selectedWheel = wheels.firstOrNull()
                )
                soundEngine.initialize()
                soundEngine.start()
            }
            is CarShowroomIntent.SelectCar -> {
                _uiState.value = _uiState.value.copy(selectedCar = intent.car)
            }
            is CarShowroomIntent.SelectColor -> {
                _uiState.value = _uiState.value.copy(selectedColor = intent.color)
            }
            is CarShowroomIntent.SelectWheel -> {
                _uiState.value = _uiState.value.copy(selectedWheel = intent.wheel)
            }
            is CarShowroomIntent.SetRevving -> {
                _uiState.value = _uiState.value.copy(isRevving = intent.isRevving)
                handleRevving(intent.isRevving)
            }
            is CarShowroomIntent.ToggleMute -> {
                val newMutedState = !_uiState.value.isMuted
                _uiState.value = _uiState.value.copy(isMuted = newMutedState)
                soundEngine.setVolume(if (newMutedState) 0f else 1f)
            }
            is CarShowroomIntent.SetInteractionMode -> {
                _uiState.value = _uiState.value.copy(interactionMode = intent.mode)
            }
            is CarShowroomIntent.PauseAudio -> {
                soundEngine.pause()
            }
            is CarShowroomIntent.ResumeAudio -> {
                soundEngine.start()
            }
        }
    }

    private fun handleRevving(isRevving: Boolean) {
        revJob?.cancel()
        revJob = viewModelScope.launch {
            if (isRevving) {
                // 1.0 -> 2.5 over 800ms
                val startPitch = currentPitch
                val targetPitch = 2.5f
                val duration = 800L
                val steps = 20
                val pitchStep = (targetPitch - startPitch) / steps
                val timeStep = duration / steps

                repeat(steps) {
                    currentPitch += pitchStep
                    soundEngine.setPitch(currentPitch)
                    delay(timeStep.milliseconds)
                }
            } else {
                // 2.5 -> 1.0 over 1200ms
                val startPitch = currentPitch
                val targetPitch = 1.0f
                val duration = 1200L
                val steps = 30
                val pitchStep = (targetPitch - startPitch) / steps
                val timeStep = duration / steps

                repeat(steps) {
                    currentPitch += pitchStep
                    soundEngine.setPitch(currentPitch)
                    delay(timeStep.milliseconds)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.release()
    }
}
