package com.twogether.backend.availability.service;

import com.twogether.backend.availability.domain.Availability;
import com.twogether.backend.availability.dto.request.AvailabilitySlotRequest;
import com.twogether.backend.availability.dto.request.AvailabilityUpdateRequest;
import com.twogether.backend.availability.dto.response.AvailabilityResponse;
import com.twogether.backend.availability.repository.AvailabilityRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    public AvailabilityService(
            AvailabilityRepository availabilityRepository,
            UserRepository userRepository
    ) {
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
    }

    public List<AvailabilityResponse> getMyAvailabilities(
            String authUserId
    ) {
        return availabilityRepository
                .findAllByUserAuthUserId(authUserId)
                .stream()
                .sorted(
                        Comparator
                                .comparing(
                                        Availability::getDayOfWeek
                                )
                                .thenComparing(
                                        Availability::getStartTime
                                )
                )
                .map(AvailabilityResponse::from)
                .toList();
    }

    @Transactional
    public void updateMyAvailabilities(
            String authUserId,
            AvailabilityUpdateRequest request
    ) {
        User user = userRepository
                .findByAuthUserId(authUserId)
                .orElseThrow(
                        () -> new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        validateDuplicateSlots(
                request.availabilities()
        );

        availabilityRepository.deleteAllByUserAuthUserId(
                authUserId
        );

        List<Availability> availabilities =
                request.availabilities()
                        .stream()
                        .map(
                                slot -> toEntity(
                                        user,
                                        slot
                                )
                        )
                        .toList();

        availabilityRepository.saveAll(
                availabilities
        );
    }

    private Availability toEntity(
            User user,
            AvailabilitySlotRequest slot
    ) {
        return new Availability(
                user,
                slot.repeatType(),
                slot.dayOfWeek(),
                slot.startTime(),
                slot.endTime()
        );
    }

    private void validateDuplicateSlots(
            List<AvailabilitySlotRequest> slots
    ) {
        long distinctCount = slots.stream()
                .distinct()
                .count();

        if (distinctCount != slots.size()) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_AVAILABILITY
            );
        }
    }

    public List<AvailabilityResponse> getUserAvailabilities(
            Long userId
    ) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        return availabilityRepository
                .findAllByUserId(userId)
                .stream()
                .sorted(
                        Comparator
                                .comparing(
                                        Availability::getDayOfWeek
                                )
                                .thenComparing(
                                        Availability::getStartTime
                                )
                )
                .map(AvailabilityResponse::from)
                .toList();
    }
}