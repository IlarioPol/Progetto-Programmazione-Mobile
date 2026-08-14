package com.example.progettoprogrammazionemobile.data.model

import com.google.firebase.firestore.PropertyName

data class TimeRange(
    val startTime: String = "09:00",
    val endTime: String = "18:00"
)

data class DayAvailability(
    val dayOfWeek: Int = 1,
    @get:PropertyName("workDay")
    val workDay: Boolean = true,
    val timeRanges: List<TimeRange> = listOf(TimeRange())
)

data class ProviderAvailability(
    val providerId: String = "",
    val weeklyAvailability: List<DayAvailability> = emptyList()
) {
    companion object {
        fun getDefault(providerId: String): ProviderAvailability {
            return ProviderAvailability(
                providerId = providerId,
                weeklyAvailability = (1..7).map { DayAvailability(dayOfWeek = it) }
            )
        }
    }
}
