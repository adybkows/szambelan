package pl.coopsoft.szambelan.models

data class DataModel(
    val prevMainMeter: Double,
    val prevGardenMeter: Double,
    val currentMainMeter: Double,
    val currentGardenMeter: Double,
    val emptyActions: List<MeterStates>
)