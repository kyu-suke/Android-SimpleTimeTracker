package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeCardSizeMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.getFullName
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.GoalTimeType
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_running_records.R
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordTypeAddViewData
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class RunningRecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val recordTypeCardSizeMapper: RecordTypeCardSizeMapper,
) {

    fun map(
        runningRecord: RunningRecord,
        dailyCurrent: Long,
        weeklyCurrent: Long,
        recordType: RecordType,
        recordTags: List<RecordTag>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        showSeconds: Boolean,
    ): RunningRecordViewData {
        val currentDuration = System.currentTimeMillis() - runningRecord.timeStarted
        return RunningRecordViewData(
            id = runningRecord.id,
            name = recordType.name,
            tagName = recordTags
                .getFullName(),
            timeStarted = runningRecord.timeStarted
                .let {
                    timeMapper.formatTime(
                        time = it,
                        useMilitaryTime = useMilitaryTime,
                        showSeconds = showSeconds,
                    )
                },
            timer = currentDuration
                .let {
                    timeMapper.formatInterval(
                        interval = it,
                        forceSeconds = true,
                        useProportionalMinutes = false,
                    )
                },
            goalTime = getGoalTimeString(
                goalTime = recordType.goalTime,
                current = currentDuration,
                type = GoalTimeType.Session
            ),
            goalTime2 = getGoalTimeString(
                goalTime = recordType.dailyGoalTime,
                current = dailyCurrent + currentDuration,
                type = GoalTimeType.Day,
            ),
            goalTime3 = getGoalTimeString(
                goalTime = recordType.weeklyGoalTime,
                current = weeklyCurrent + currentDuration,
                type = GoalTimeType.Week,
            ),
            iconId = recordType.icon
                .let(iconMapper::mapIcon),
            color = recordType.color
                .let { colorMapper.mapToColorInt(it, isDarkTheme) },
            comment = runningRecord.comment
        )
    }

    private fun getGoalTimeString(
        goalTime: Long,
        current: Long,
        type: GoalTimeType,
    ): String {
        if (goalTime <= 0L) return ""

        val typeString = when (type) {
            is GoalTimeType.Session -> R.string.change_record_type_session_goal_time
            is GoalTimeType.Day -> R.string.change_record_type_daily_goal_time
            is GoalTimeType.Week -> R.string.change_record_type_weekly_goal_time
        }.let(resourceRepo::getString).lowercase()

        val durationLeftString = (goalTime - current / 1000)
            .takeIf { it > 0L }
            .orZero()
            .let(timeMapper::formatDuration) // TODO format interval with seconds?

        return "$typeString $durationLeftString"
    }

    fun map(
        recordType: RecordType,
        isFiltered: Boolean,
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RecordTypeViewData {
        return recordTypeViewDataMapper.mapFiltered(
            recordType,
            numberOfCards,
            isDarkTheme,
            isFiltered
        )
    }

    fun mapToTypesEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.running_records_types_empty.let(resourceRepo::getString)
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.running_records_empty.let(resourceRepo::getString),
            hint = R.string.running_records_empty_hint.let(resourceRepo::getString)
        )
    }

    fun mapToAddItem(
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RunningRecordTypeAddViewData {
        return RunningRecordTypeAddViewData(
            name = R.string.running_records_add_type.let(resourceRepo::getString),
            iconId = RecordTypeIcon.Image(R.drawable.add),
            color = colorMapper.toInactiveColor(isDarkTheme),
            width = recordTypeCardSizeMapper.toCardWidth(numberOfCards),
            height = recordTypeCardSizeMapper.toCardHeight(numberOfCards),
            asRow = recordTypeCardSizeMapper.toCardAsRow(numberOfCards)
        )
    }
}