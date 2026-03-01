package pl.coopsoft.szambelan.domain.model

data class MeterData(
    val currentGarden: Double,
    val currentMain: Double,
    val oldGarden: Double,
    val oldMain: Double,
    val emptyActions: String
)
