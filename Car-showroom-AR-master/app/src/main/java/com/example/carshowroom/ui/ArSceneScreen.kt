package com.example.carshowroom.ui

import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carshowroom.ui.components.*
import com.example.carshowroom.viewmodel.CarShowroomIntent
import com.example.carshowroom.viewmodel.CarShowroomViewModel
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArSceneScreen(
    viewModel: CarShowroomViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showCarSheet by remember { mutableStateOf(value = false) }
    var showColorSheet by remember { mutableStateOf(value = false) }
    var showWheelSheet by remember { mutableStateOf(value = false) }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val environmentLoader = io.github.sceneview.rememberEnvironmentLoader(engine)
    val environment = io.github.sceneview.rememberEnvironment(environmentLoader)
    val childNodes = rememberNodes()
    var currentAnchorNode by remember { mutableStateOf<AnchorNode?>(null) }
    var currentModelNode by remember { mutableStateOf<ModelNode?>(null) }
    var lastTapEvent by remember { mutableStateOf<MotionEvent?>(null) }

    // Handle Model Loading/Switching
    LaunchedEffect(uiState.selectedCar) {
        uiState.selectedCar?.let { car ->
            modelLoader.loadModelInstanceAsync(car.modelPath) { modelInstance ->
                modelInstance?.let { instance ->
                    val newModelNode = ModelNode(
                        modelInstance = instance,
                        scaleToUnits = 1.0f,
                        centerOrigin = io.github.sceneview.math.Position(0f, 0f, 0f),
                    )

                    currentModelNode = newModelNode
                    
                    // If we already have an anchor, update its child
                    currentAnchorNode?.let { anchorNode ->
                        anchorNode.childNodes.forEach { it.destroy() }
                        anchorNode.childNodes = setOf(newModelNode)
                    }
                }
            }
        }
    }

    // Handle Color Change
    LaunchedEffect(uiState.selectedColor, currentModelNode) {
        uiState.selectedColor?.let { carColor ->
            currentModelNode?.let { modelNode ->
                val car = uiState.selectedCar
                val paintMaterials = car?.paintMaterialNames ?: emptyList()
                modelNode.modelInstance.materialInstances.forEach { material ->
                    val materialName = material.name ?: ""
                    val isPaint = paintMaterials.isEmpty() || paintMaterials.any { 
                        materialName.contains(it, ignoreCase = true) || it.contains(materialName, ignoreCase = true)
                    }
                    if (isPaint) {
                        val c = carColor.color
                        material.setParameter("baseColorFactor", c.red, c.green, c.blue, c.alpha)
                        // Add some emissive so the color is visible even if the AR lighting/environment is completely dark
                        material.setParameter("emissiveFactor", c.red * 0.4f, c.green * 0.4f, c.blue * 0.4f)
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            materialLoader = materialLoader,
            environment = environment,
            childNodes = childNodes,
            planeRenderer = true,
            sessionConfiguration = { session, config ->
                config.lightEstimationMode = com.google.ar.core.Config.LightEstimationMode.AMBIENT_INTENSITY
            },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { e, node ->
                    if (node == null) {
                        lastTapEvent = e
                    }
                }
            ),
            onSessionUpdated = { _, frame ->
                if (frame.camera.trackingState == TrackingState.TRACKING) {
                    lastTapEvent?.let { event ->
                        val hitResults = frame.hitTest(event)
                        val hitResult = hitResults.firstOrNull { hit ->
                            val trackable = hit.trackable
                            (trackable is Plane) && 
                            trackable.isPoseInPolygon(hit.hitPose) && 
                            (trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING)
                        }
                        
                        hitResult?.let { hit ->
                            val anchor = hit.createAnchor()
                            val anchorNode = AnchorNode(engine, anchor)
                            
                            currentModelNode?.let { modelNode ->
                                anchorNode.addChildNode(modelNode)
                            }
                            
                            childNodes.forEach { it.destroy() }
                            childNodes.clear()
                            childNodes.add(anchorNode)
                            currentAnchorNode = anchorNode
                        }
                        lastTapEvent = null
                    }
                }
            }
        )

        // Top Bar
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            color = Color.Black.copy(alpha = 0.6f),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "AR Showroom", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(text = "Tap floor to place car", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
            }
        }

        // Rev Button
        RevButton(
            onRevvingChanged = { viewModel.handleIntent(CarShowroomIntent.SetRevving(it)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 24.dp)
        )

        // Bottom Controls
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            color = Color.Black.copy(alpha = 0.4f),
            shape = MaterialTheme.shapes.large
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { showCarSheet = true }) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Cars", tint = Color.White)
                }
                IconButton(onClick = { showColorSheet = true }) {
                    Icon(Icons.Default.Build, contentDescription = "Paint", tint = Color.White)
                }
                IconButton(onClick = { showWheelSheet = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Wheels", tint = Color.White)
                }
            }
        }

        // Sheets
        if (showCarSheet) {
            ModalBottomSheet(onDismissRequest = { showCarSheet = false }) {
                CarSelectorSheet(
                    cars = uiState.cars,
                    selectedCar = uiState.selectedCar,
                    onCarSelected = { car ->
                        viewModel.handleIntent(CarShowroomIntent.SelectCar(car))
                        showCarSheet = false
                    }
                )
            }
        }

        if (showColorSheet) {
            ModalBottomSheet(onDismissRequest = { showColorSheet = false }) {
                ColorPickerSheet(
                    colors = uiState.colors,
                    selectedColor = uiState.selectedColor,
                    onColorSelected = { color ->
                        viewModel.handleIntent(CarShowroomIntent.SelectColor(color))
                        showColorSheet = false
                    }
                )
            }
        }

        if (showWheelSheet) {
            ModalBottomSheet(onDismissRequest = { showWheelSheet = false }) {
                WheelPickerSheet(
                    wheels = uiState.wheels,
                    selectedWheel = uiState.selectedWheel,
                    onWheelSelected = { wheel ->
                        viewModel.handleIntent(CarShowroomIntent.SelectWheel(wheel))
                        showWheelSheet = false
                    }
                )
            }
        }
    }
}
