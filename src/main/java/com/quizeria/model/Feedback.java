package com.quizeria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long quizId;

    @Column(length = 1000)
    private String feedbackText;

    // New fields to match template expectations
    private String userName;
    
    private String message;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    public Feedback() {
        this.createdDate = LocalDateTime.now();
    }

    public Feedback(Long quizId, String feedbackText) {
        this();
        this.quizId = quizId;
        this.feedbackText = feedbackText;
        this.message = feedbackText; // Map feedbackText to message for backward compatibility
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }
    
    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { 
        this.feedbackText = feedbackText;
        this.message = feedbackText; // Keep both in sync
    }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getMessage() { return message != null ? message : feedbackText; }
    public void setMessage(String message) { 
        this.message = message;
        if (this.feedbackText == null) {
            this.feedbackText = message; // Keep both in sync
        }
    }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
