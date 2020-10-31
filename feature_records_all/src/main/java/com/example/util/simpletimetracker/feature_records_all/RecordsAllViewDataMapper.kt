package com.example.util.simpletimetracker.feature_records_all

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordViewData
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import javax.inject.Inject

class RecordsAllViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper
) {

    fun map(
        record: Record,
        recordType: RecordType,
        isDarkTheme: Boolean
    ): ViewHolderType {
        val (timeStarted, timeEnded) = record.timeStarted to record.timeEnded

        return RecordViewData.Tracked(
            id = record.id,
            name = recordType.name,
            timeStarted = timeStarted
                .let(timeMapper::formatTime),
            timeFinished = timeEnded
                .let(timeMapper::formatTime),
            duration = (timeEnded - timeStarted)
                .let(timeMapper::formatInterval),
            iconId = recordType.icon
                .let(iconMapper::mapToDrawableResId),
            color = recordType.color
                .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                .let(resourceRepo::getColor)
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.records_empty.let(resourceRepo::getString)
        )
    }
}