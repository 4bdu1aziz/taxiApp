package com.taxiapp.user.repository;

import com.taxiapp.user.entity.Driver;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findByStatus(Driver.DriverStatus status);

    // PESSIMISTIC_WRITE блокирует строку в БД — другой запрос не может взять того же водителя
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Driver d WHERE d.status = 'AVAILABLE' ORDER BY function('RANDOM') LIMIT 1")
    Optional<Driver> findAvailableDriverWithLock();
}