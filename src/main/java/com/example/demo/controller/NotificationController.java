
package com.example.demo.controller;

import com.example.demo.model.Notification;
import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/pharmacie/{pharmacieId}")
    public List<Notification> getNotifications(@PathVariable Long pharmacieId) {
        return notificationService.getAllNotifications(pharmacieId);
    }

    @GetMapping("/pharmacie/{pharmacieId}/unread")
    public List<Notification> getUnreadNotifications(@PathVariable Long pharmacieId) {
        return notificationService.getUnreadNotifications(pharmacieId);
    }

    @GetMapping("/pharmacie/{pharmacieId}/count")
    public int getUnreadCount(@PathVariable Long pharmacieId) {
        return notificationService.getUnreadCount(pharmacieId);
    }

    @PostMapping("/mark-as-read/{id}")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}
