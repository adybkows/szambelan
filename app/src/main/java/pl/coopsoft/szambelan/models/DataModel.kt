package pl.coopsoft.szambelan.models

data class DataModel(
    val prevMainMeter: Double,
    val prevGardenMeter: Double,
    val currentMainMeter: Double,
    val currentGardenMeter: Double,
    val emptyActions: List<MeterStates>
) {
    constructor() : this(0.0, 0.0, 0.0, 0.0, emptyList())
}