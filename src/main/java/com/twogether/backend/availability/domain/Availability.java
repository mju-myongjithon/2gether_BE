package com.twogether.backend.availability.domain;

import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "availabilities")
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "repeat_type",
            nullable = false
    )
    private AvailabilityRepeatType repeatType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "day_of_week",
            nullable = false
    )
    private DayOfWeek dayOfWeek;

    @Column(
            name = "start_time",
            nullable = false
    )
    private LocalTime startTime;

    @Column(
            name = "end_time",
            nullable = false
    )
    private LocalTime endTime;

    protected Availability() {
    }

    public Availability(
            User user,
            AvailabilityRepeatType repeatType,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
        validateTime(
                startTime,
                endTime
        );

        this.user = user;
        this.repeatType = repeatType;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private void validateTime(
            LocalTime startTime,
            LocalTime endTime
    ) {
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException(
                    ErrorCode.INVALID_AVAILABILITY_TIME_ORDER
            );
        }

        if (!isThirtyMinuteUnit(startTime)
                || !isThirtyMinuteUnit(endTime)) {
            throw new BusinessException(
                    ErrorCode.INVALID_AVAILABILITY_TIME_UNIT
            );
        }
    }

    private boolean isThirtyMinuteUnit(
            LocalTime time
    ) {
        return time.getSecond() == 0
                && time.getNano() == 0
                && (
                time.getMinute() == 0
                        || time.getMinute() == 30
        );
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public AvailabilityRepeatType getRepeatType() {
        return repeatType;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}