package com.quizeria.controller;

import com.quizeria.model.Quiz;
import com.quizeria.model.Question;
import com.quizeria.model.Feedback;
import com.quizeria.model.Note;
import com.quizeria.repository.QuizRepository;
import com.quizeria.repository.QuestionRepository;
import com.quizeria.repository.FeedbackRepository;
import com.quizeria.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private NoteRepository noteRepository;

    // User panel: list all quizzes and notes
    @GetMapping("/user")
    public String userPanel(Model model) {
        List<Quiz> quizzes = quizRepository.findAll();
        List<Note> notes = noteRepository.findAllByOrderByCreatedDateDesc();
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("notes", notes);
        return "user";
    }

    // Show quiz questions
    @GetMapping("/user/quiz/{quizId}")
    public String takeQuiz(@PathVariable Long quizId, Model model) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) return "redirect:/user";
        List<Question> questions = questionRepository.findByQuizId(quizId);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        return "take-quiz";
    }

    // Handle quiz submission and show result
    @PostMapping("/user/quiz/{quizId}/submit")
    public String submitQuiz(@PathVariable Long quizId, @RequestParam Map<String, String> params, Model model) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        List<Question> questions = questionRepository.findByQuizId(quizId);
        int score = 0;
        for (Question q : questions) {
            String answer = params.get("question_" + q.getId());
            if (answer != null && answer.equalsIgnoreCase(q.getCorrectOption())) {
                score++;
            }
        }
        model.addAttribute("quiz", quiz);
        model.addAttribute("score", score);
        model.addAttribute("total", questions.size());
        model.addAttribute("isSuccess", false);
        return "result";
    }

    // Handle feedback submission
    @PostMapping("/user/quiz/{quizId}/feedback")
    public String submitFeedback(@PathVariable Long quizId, @RequestParam("feedback") String feedback, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName(); // Get the logged-in user's name
        
        Feedback fb = new Feedback(quizId, feedback);
        fb.setUserName(userName); // Set the user name
        feedbackRepository.save(fb);
        model.addAttribute("message", "Thank you for your feedback!");
        model.addAttribute("isSuccess", true);
        return "result";
    }

    // View individual study note
    @GetMapping("/user/notes/{noteId}")
    public String viewNote(@PathVariable Long noteId, Model model) {
        Note note = noteRepository.findById(noteId).orElse(null);
        if (note == null) {
            return "redirect:/user";
        }
        model.addAttribute("note", note);
        return "user-notes";
    }

    // List all study notes
    @GetMapping("/user/notes")
    public String listNotes(Model model) {
        List<Note> notes = noteRepository.findAllByOrderByCreatedDateDesc();
        model.addAttribute("notes", notes);
        return "user-notes";
    }
}
