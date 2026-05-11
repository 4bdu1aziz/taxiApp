package com.taxiapp.notification.controller;

import com.taxiapp.notification.entity.NotificationTask;
import com.taxiapp.notification.repository.NotificationTaskRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationTaskRepository repository;

    public NotificationController(NotificationTaskRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/notifications")
    public ResponseEntity<NotificationTask> create(@RequestBody Map<String, Object> body) {
        NotificationTask task = new NotificationTask();
        task.setTripId(Long.valueOf(body.get("tripId").toString()));
        task.setRecipientType((String) body.get("recipientType"));
        task.setRecipientId(Long.valueOf(body.get("recipientId").toString()));
        task.setMessage((String) body.get("message"));
        task.setStatus(NotificationTask.NotificationStatus.PENDING);
        task.setAttempts(0);
        return ResponseEntity.ok(repository.save(task));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationTask>> getByTrip(@RequestParam("trip_id") Long tripId) {
        return ResponseEntity.ok(repository.findByTripId(tripId));
    }
}