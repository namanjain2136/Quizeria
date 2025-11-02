package com.quizeria.controller;
import org.springframework.security.core.Authentication;
import com.quizeria.model.Quiz;
import com.quizeria.model.Question;
import com.quizeria.model.Feedback;
import com.quizeria.model.Note;
import com.quizeria.repository.QuizRepository;
import com.quizeria.repository.QuestionRepository;
import com.quizeria.repository.FeedbackRepository;
import com.quizeria.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@PreAuthorize("hasRole('ADMIN')")
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private NoteRepository noteRepository;

    @GetMapping
    public String adminPanel(@RequestParam(value = "section", required = false) String section,
                           @RequestParam(value = "success", required = false) String success,
                           @RequestParam(value = "error", required = false) String error,
                           Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authorities: " + auth.getAuthorities());
        List<Quiz> quizzes = quizRepository.findAll();
        List<Note> notes = noteRepository.findAllByOrderByCreatedDateDesc();
        
        // Dashboard statistics
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("notes", notes);
        model.addAttribute("totalQuizzes", quizzes.size());
        model.addAttribute("totalQuestions", questionRepository.count());
        model.addAttribute("totalFeedback", feedbackRepository.count());
        model.addAttribute("totalNotes", notes.size());
        model.addAttribute("feedbackList", feedbackRepository.findAll());
        
        // Add section and message attributes
        if (section != null) {
            model.addAttribute("activeSection", section);
        }
        if (success != null) {
            model.addAttribute("success", success);
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        
        return "admin-dashboard";
    }

    @GetMapping("/add")
    public String showAddQuizForm(Model model) {
        model.addAttribute("quiz", new Quiz());
        model.addAttribute("isEdit", false);
        return "quiz-form";
    }

    @PostMapping("/add")
    public String addQuiz(@ModelAttribute Quiz quiz) {
        quizRepository.save(quiz);
        return "redirect:/admin";
    }
    
    // New quiz creation endpoints for admin-addquiz.html
    @GetMapping("/quiz/add")
    public String showCreateQuizForm(@RequestParam(value = "success", required = false) String success,
                                   @RequestParam(value = "error", required = false) String error,
                                   Model model) {
        model.addAttribute("quiz", new Quiz());
        
        if (success != null) {
            model.addAttribute("success", success);
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        
        return "admin-addquiz";
    }

    @PostMapping("/quiz/add")
    public String createQuiz(@ModelAttribute Quiz quiz, Model model) {
        try {
            Quiz savedQuiz = quizRepository.save(quiz);
            return "redirect:/admin/quiz/" + savedQuiz.getId() + "/questions/add?success=Quiz created successfully!";
        } catch (Exception e) {
            model.addAttribute("quiz", quiz);
            model.addAttribute("error", "Failed to create quiz: " + e.getMessage());
            return "admin-addquiz";
        }
    }

    @GetMapping("/quiz/{quizId}/edit")
    public String showEditQuizForm(@PathVariable Long quizId, Model model) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) return "redirect:/admin";
        model.addAttribute("quiz", quiz);
        model.addAttribute("isEdit", true);
        return "quiz-form";
    }

    @PostMapping("/quiz/{quizId}/edit")
    public String editQuiz(@PathVariable Long quizId, @ModelAttribute Quiz quiz) {
        Quiz existingQuiz = quizRepository.findById(quizId).orElse(null);
        if (existingQuiz != null) {
            existingQuiz.setTitle(quiz.getTitle());
            quizRepository.save(existingQuiz);
        }
        return "redirect:/admin";
    }

    @GetMapping("/quiz/{quizId}/delete")
    public String deleteQuiz(@PathVariable Long quizId) {
        quizRepository.deleteById(quizId);
        return "redirect:/admin";
    }

    @GetMapping("/quiz/{quizId}/questions")
    public String viewQuestions(@PathVariable Long quizId, 
                              @RequestParam(value = "success", required = false) String success,
                              @RequestParam(value = "error", required = false) String error,
                              Model model) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) {
            return "redirect:/admin";
        }
        List<Question> questions = questionRepository.findByQuizId(quizId);
        
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        
        if (success != null) {
            model.addAttribute("success", success);
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        
        return "admin-questions";
    }

    @GetMapping("/quiz/{quizId}/questions/add")
    public String showAddQuestionForm(@PathVariable Long quizId, 
                                    @RequestParam(value = "success", required = false) String success,
                                    @RequestParam(value = "error", required = false) String error,
                                    Model model) {
        Question question = new Question();
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) {
            return "redirect:/admin";
        }
        
        question.setQuiz(quiz);
        long questionCount = questionRepository.countByQuizId(quizId);
        
        model.addAttribute("question", question);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questionCount", questionCount);
        model.addAttribute("quizId", quizId);
        model.addAttribute("isEdit", false);
        
        if (success != null) {
            model.addAttribute("success", success);
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        
        return "admin-addquestions";
    }

    @PostMapping("/quiz/{quizId}/questions/add")
    public String addQuestion(@PathVariable Long quizId, @ModelAttribute Question question, Model model) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) {
            return "redirect:/admin";
        }
        
        try {
            question.setQuiz(quiz);
            questionRepository.save(question);
            
            // Redirect back to add more questions with success message
            return "redirect:/admin/quiz/" + quizId + "/questions/add?success=Question added successfully!";
        } catch (Exception e) {
            long questionCount = questionRepository.countByQuizId(quizId);
            
            model.addAttribute("question", new Question());  // Reset form
            model.addAttribute("quiz", quiz);
            model.addAttribute("questionCount", questionCount);
            model.addAttribute("error", "Failed to add question: " + e.getMessage());
            
            return "admin-addquestions";
        }
    }

    @GetMapping("/quiz/{quizId}/questions/{questionId}/edit")
    public String showEditQuestionForm(@PathVariable Long quizId, @PathVariable Long questionId,
                                     @RequestParam(value = "success", required = false) String success,
                                     @RequestParam(value = "error", required = false) String error,
                                     Model model) {
        Question question = questionRepository.findById(questionId).orElse(null);
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        
        if (question == null || quiz == null) {
            return "redirect:/admin/quiz/" + quizId + "/questions?error=Question not found!";
        }
        
        model.addAttribute("question", question);
        model.addAttribute("quiz", quiz);
        model.addAttribute("quizId", quizId);
        
        if (success != null) {
            model.addAttribute("success", success);
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        
        return "admin-editquestion";
    }

    @PostMapping("/quiz/{quizId}/questions/{questionId}/edit")
    public String editQuestion(@PathVariable Long quizId, @PathVariable Long questionId, @ModelAttribute Question question, Model model) {
        try {
            Question existingQuestion = questionRepository.findById(questionId).orElse(null);
            Quiz quiz = quizRepository.findById(quizId).orElse(null);
            
            if (existingQuestion == null || quiz == null) {
                return "redirect:/admin/quiz/" + quizId + "/questions?error=Question not found!";
            }
            
            existingQuestion.setText(question.getText());
            existingQuestion.setOptionA(question.getOptionA());
            existingQuestion.setOptionB(question.getOptionB());
            existingQuestion.setOptionC(question.getOptionC());
            existingQuestion.setOptionD(question.getOptionD());
            existingQuestion.setCorrectOption(question.getCorrectOption());
            questionRepository.save(existingQuestion);
            
            return "redirect:/admin/quiz/" + quizId + "/questions?success=Question updated successfully!";
        } catch (Exception e) {
            // If there's an error, show the form again with error message
            Question currentQuestion = questionRepository.findById(questionId).orElse(null);
            Quiz quiz = quizRepository.findById(quizId).orElse(null);
            
            model.addAttribute("question", currentQuestion);
            model.addAttribute("quiz", quiz);
            model.addAttribute("error", "Failed to update question: " + e.getMessage());
            
            return "admin-editquestion";
        }
    }

    @GetMapping("/quiz/{quizId}/questions/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long quizId, @PathVariable Long questionId) {
        try {
            questionRepository.deleteById(questionId);
            return "redirect:/admin/quiz/" + quizId + "/questions?success=Question deleted successfully!";
        } catch (Exception e) {
            return "redirect:/admin/quiz/" + quizId + "/questions?error=Failed to delete question: " + e.getMessage();
        }
    }

    @GetMapping("/feedback")
    public String viewFeedback(Model model) {
        List<Quiz> allQuizzes = quizRepository.findAll();
        List<Feedback> feedbackList = feedbackRepository.findAll();
        
        // For the admin dashboard
        model.addAttribute("quizzes", allQuizzes);
        model.addAttribute("feedbackList", feedbackList);
        model.addAttribute("totalQuizzes", allQuizzes.size());
        model.addAttribute("totalQuestions", questionRepository.count());
        model.addAttribute("totalFeedback", feedbackList.size());
        model.addAttribute("totalNotes", noteRepository.count());
        
        return "admin-dashboard";
    }

    // Notes Management
    @GetMapping("/notes/add")
    public String addNoteForm(Model model) {
        model.addAttribute("note", new Note());
        model.addAttribute("action", "add");
        return "note-form";
    }
    
    @PostMapping("/notes/add")
    public String addNote(@ModelAttribute Note note, @RequestParam(value = "file", required = false) MultipartFile file) {
        // Handle file upload
        if (file != null && !file.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                String uploadDir = "uploads/notes/";
                Path uploadPath = Paths.get(uploadDir);
                
                // Create directories if they don't exist
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                
                note.setFilePath(uploadDir + fileName);
                note.setFileName(file.getOriginalFilename());
            } catch (IOException e) {
                // Handle file upload error
                e.printStackTrace();
            }
        }
        
        noteRepository.save(note);
        return "redirect:/admin?section=notes&success=Note added successfully!";
    }
    
    @GetMapping("/notes/{id}/edit")
    public String editNoteForm(@PathVariable Long id, Model model) {
        Note note = noteRepository.findById(id).orElseThrow();
        model.addAttribute("note", note);
        model.addAttribute("action", "edit");
        return "note-form";
    }
    
    @PostMapping("/notes/{id}/edit")
    public String editNote(@PathVariable Long id, @ModelAttribute Note note, @RequestParam(value = "file", required = false) MultipartFile file) {
        Note existingNote = noteRepository.findById(id).orElse(null);
        if (existingNote != null) {
            existingNote.setTitle(note.getTitle());
            existingNote.setTopic(note.getTopic());
            existingNote.setDescription(note.getDescription());
            existingNote.setVideoUrl(note.getVideoUrl());
            
            // Handle file upload
            if (file != null && !file.isEmpty()) {
                try {
                    String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                    String uploadDir = "uploads/notes/";
                    Path uploadPath = Paths.get(uploadDir);
                    
                    // Create directories if they don't exist
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(file.getInputStream(), filePath);
                    
                    existingNote.setFilePath(uploadDir + fileName);
                    existingNote.setFileName(file.getOriginalFilename());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            noteRepository.save(existingNote);
        }
        return "redirect:/admin?section=notes&success=Note updated successfully!";
    }
    
    @GetMapping("/notes/{id}/delete")
    public String deleteNote(@PathVariable Long id) {
        noteRepository.deleteById(id);
        return "redirect:/admin?section=notes&success=Note deleted successfully!";
    }
}
