package com.barberflow.modules.users.infrastructure.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barberflow.modules.users.application.dtos.ReviewDTOs.CreateReviewRequest;
import com.barberflow.modules.users.application.dtos.ReviewDTOs.ReviewResponse;
import com.barberflow.modules.users.application.services.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(request));
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviews(@RequestParam Long barbershopId) {
        return ResponseEntity.ok(reviewService.getReviewsByBarbershop(barbershopId));
    }
}
