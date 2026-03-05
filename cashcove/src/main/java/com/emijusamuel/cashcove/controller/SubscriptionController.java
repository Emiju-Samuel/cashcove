package com.emijusamuel.cashcove.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emijusamuel.cashcove.dto.SubscriptionDTO;
import com.emijusamuel.cashcove.service.SubscriptionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // Get my subscriptions (list)
    // What it does: Returns all active/current subscriptions for the logged-in user

    @GetMapping
    public ResponseEntity<List<SubscriptionDTO>> getMySubscriptions() {
        List<SubscriptionDTO> subscriptions = subscriptionService.getMySubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    // Get a single subscription by ID
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionDTO> getSubscription(@PathVariable Long id) {
        SubscriptionDTO subscription = subscriptionService.getSubscription(id);
        return ResponseEntity.ok(subscription);
    }

    // Get upcoming subscriptions
    @GetMapping("/upcoming")
    public ResponseEntity<List<SubscriptionDTO>> getUpcoming(@RequestParam(defaultValue = "7") int days) {
        List<SubscriptionDTO> upcoming = subscriptionService.getUpcoming(days);
        return ResponseEntity.ok(upcoming);
    }

    // Add a new subscription
    @PostMapping
    public ResponseEntity<SubscriptionDTO> addSubscription(@RequestBody SubscriptionDTO dto) {
        SubscriptionDTO saved = subscriptionService.addSubscription(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Update an existing subscription
    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionDTO> updateSubscription(@PathVariable Long id, @RequestBody SubscriptionDTO dto) {
        SubscriptionDTO updated = subscriptionService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    // Soft delete a subscription
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {
        subscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Optional – Change status explicitly (cancel / pause / reactivate)
    // What it does: Dedicated endpoint for status transitions (cleaner than patch sometimes)

    @PostMapping("/{id}/status")
    public ResponseEntity<SubscriptionDTO> changeStatus(@PathVariable Long id, @RequestParam String status) {
        SubscriptionDTO updated = subscriptionService.changeStatus(id, status);
        return ResponseEntity.ok(updated);
    }
    

}
