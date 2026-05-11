package com.taxiapp.notification.repository;

import com.taxiapp.notification.entity.NotificationTask;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    List<NotificationTask> findByTripId(Long tripId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM NotificationTask t WHERE t.status = 'PENDING' AND t.attempts < 3 ORDER BY t.createdAt ASC LIMIT 1")
    Optional<NotificationTask> findNextPendingTask();
}