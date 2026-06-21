package com.example.carshowroom.data

import androidx.compose.ui.graphics.Color
import com.example.carshowroom.data.models.CarColor
import com.example.carshowroom.data.models.CarModel
import com.example.carshowroom.data.models.WheelStyle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarRepository @Inject constructor() {
    fun getCars() = listOf(
        CarModel("car1", "Nissan GTR", "models/1.glb", 0, listOf("Object_2", "Material.001"), listOf("Object_14", "Material.012")),
        CarModel("car2", "Toyota Supra", "models/2.glb", 0, listOf("Object_12", "carpaint"), listOf("Object_19", "wheel_metal.001")),
        CarModel("car3", "Ferrari f12tdf", "models/3.glb", 0, 
            listOf("carpaint", "Ferrari_F12_car:chassis_carpaint_custom01_LOD2_carpaint_0", "Ferrari_F12_car:chassis_carpaint_custom02_LOD2_carpaint_0", "Ferrari_F12_car:chassis_carpaint_LOD2_carpaint_0", "Ferrari_F12_car:detach_bumper_F_5_carpaint_LOD2_carpaint_0"), 
            listOf("rims", "Ferrari_F12_car:wheelFR_rims_LOD2_rims_0", "Ferrari_F12_car:wheelFL_rims_LOD2_rims_0", "Ferrari_F12_car:wheelBR_rims_LOD2_rims_0", "Ferrari_F12_car:wheelBL_rims_LOD2_rims_0")),
        CarModel("car4", "Ferrari f430", "models/4.glb", 0, 
            listOf("f430Vehicle_Exterior_mm_ext1", "f430:LOD_A_BODY_mm_ext_f430:Vehicle_Exterior_mm_ext1_0", "f430:LOD_A_BOOT_mm_ext_f430:Vehicle_Exterior_mm_ext1_0", "f430:LOD_A_DOOR_LEFT_mm_ext_f430:Vehicle_Exterior_mm_ext1_0", "f430:LOD_A_DOOR_RIGHT_mm_ext_f430:Vehicle_Exterior_mm_ext1_0", "f430:LOD_A_FRONTBUMPER_mm_ext_f430:Vehicle_Exterior_mm_ext1_0", "f430:LOD_A_HOOD_mm_ext_f430:Vehicle_Exterior_mm_ext1_0", "f430:LOD_A_MIRROR_LEFT_mm_ext_f430:Vehicle_Exterior_mm_ext1_0", "f430:LOD_A_MIRROR_RIGHT_mm_ext_f430:Vehicle_Exterior_mm_ext1_0", "f430:LOD_A_REARBUMPER_mm_ext_f430:Vehicle_Exterior_mm_ext1_0"), 
            listOf("f430Vehicle_Exterior_mm_wheel1", "f430:LOD_A_WHEEL_mm_wheel_f430:Vehicle_Exterior_mm_wheel1_0", "LOD_A_WHEEL_mm_wheel1_f430:Vehicle_Exterior_mm_wheel1_0", "LOD_A_WHEEL_mm_wheel2_f430:Vehicle_Exterior_mm_wheel1_0", "LOD_A_WHEEL_mm_wheel_f430:Vehicle_Exterior_mm_wheel1_0")),
        CarModel("car5", "Mercedes G-Class", "models/5.glb", 0, listOf("Object_4", "Material.001"), listOf("Object_15", "Object_19", "Object_23", "Object_27", "Material.006"))
    )

    fun getColors() = listOf(
        CarColor("Pearl White", Color.White),
        CarColor("Midnight Black", Color.Black),
        CarColor("Candy Red", Color(0xFFFF0800)),
        CarColor("Ocean Blue", Color(0xFF0077BE)),
        CarColor("British Racing Green", Color(0xFF004225)),
        CarColor("Burnt Orange", Color(0xFFCC5500))
    )

    fun getWheelStyles() = listOf(
        WheelStyle("standard", "Standard", "models/4.glb"),
        WheelStyle("sport", "Sport", "models/5.glb")
    )
}
