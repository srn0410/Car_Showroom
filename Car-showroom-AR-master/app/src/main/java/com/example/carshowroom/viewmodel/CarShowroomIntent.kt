package com.example.carshowroom.viewmodel

import com.example.carshowroom.data.models.CarColor
import com.example.carshowroom.data.models.CarModel
import com.example.carshowroom.data.models.WheelStyle

sealed class CarShowroomIntent {
    data class SelectCar(val car: CarModel) : CarShowroomIntent()
    data class SelectColor(val color: CarColor) : CarShowroomIntent()
    data class SelectWheel(val wheel: WheelStyle) : CarShowroomIntent()
    data class SetRevving(val isRevving: Boolean) : CarShowroomIntent()
    object Initialize : CarShowroomIntent()
}
