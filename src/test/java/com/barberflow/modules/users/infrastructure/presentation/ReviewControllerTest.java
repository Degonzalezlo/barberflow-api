package com.barberflow.modules.users.infrastructure.presentation;


import com.barberflow.modules.users.application.dtos.ReviewDTOs.CreateReviewRequest;
import com.barberflow.modules.users.application.dtos.ReviewDTOs.ReviewResponse;
import com.barberflow.modules.users.application.services.ReviewService;
import com.barberflow.modules.users.infrastructure.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter; 
    @Nested
    @DisplayName("Create Review Endpoint Tests")
    class CreateReviewEndpointTests {

        @Test
        @WithMockUser
        @DisplayName("POST /api/v1/reviews - Should create review and return 201 Created")
        void createReview_Success() throws Exception {
            CreateReviewRequest request = new CreateReviewRequest(
                    10L, 
                    5, 
                    "Excellent haircut and service!"
            );

            ReviewResponse response = new ReviewResponse(
                    100L,
                    1L,
                    10L,
                    "Daniel Gonzalez",
                    "Carlos Barber",
                    5,
                    "Excellent haircut and service!",
                    LocalDateTime.now()
            );

            when(reviewService.createReview(any(CreateReviewRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.reviewId").value(100))
                    .andExpect(jsonPath("$.barbershopId").value(1))
                    .andExpect(jsonPath("$.appointmentId").value(10))
                    .andExpect(jsonPath("$.clientName").value("Daniel Gonzalez"))
                    .andExpect(jsonPath("$.barberName").value("Carlos Barber"))
                    .andExpect(jsonPath("$.rating").value(5))
                    .andExpect(jsonPath("$.comment").value("Excellent haircut and service!"));

            verify(reviewService).createReview(any(CreateReviewRequest.class));
        }

        @Test
        @WithMockUser
        @DisplayName("POST /api/v1/reviews - Should return 400 Bad Request when validation fails")
        void createReview_ValidationError() throws Exception {
            // Rating 6 violates @Max(5) constraint
            CreateReviewRequest invalidRequest = new CreateReviewRequest(
                    10L, 
                    6, 
                    "Invalid rating test"
            );

            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Get Reviews Endpoint Tests")
    class GetReviewsEndpointTests {

        @Test
        @WithMockUser
        @DisplayName("GET /api/v1/reviews - Should return review list for barbershop and 200 OK")
        void getReviews_Success() throws Exception {
            ReviewResponse r1 = new ReviewResponse(1L, 5L, 10L, "Daniel G.", "Carlos B.", 5, "Awesome", LocalDateTime.now());
            ReviewResponse r2 = new ReviewResponse(2L, 5L, 11L, "Andres M.", "Carlos B.", 4, "Good", LocalDateTime.now());

            when(reviewService.getReviewsByBarbershop(eq(5L)))
                    .thenReturn(List.of(r1, r2));

            mockMvc.perform(get("/api/v1/reviews")
                            .param("barbershopId", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(2))
                    .andExpect(jsonPath("$[0].reviewId").value(1))
                    .andExpect(jsonPath("$[0].rating").value(5))
                    .andExpect(jsonPath("$[1].reviewId").value(2))
                    .andExpect(jsonPath("$[1].rating").value(4));

            verify(reviewService).getReviewsByBarbershop(eq(5L));
        }
    }
}
