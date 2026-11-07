package com.example.japaneseapp.ai;

import com.example.japaneseapp.ai.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/ai")
@Validated
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/lessons/{lessonId}/generate-quiz")
    public GenerateQuizResponse generateQuiz(
            @PathVariable Long lessonId,
            @RequestParam(defaultValue = "5") @Min(1) @Max(10) int count
    ) {
        var questions = aiService.generateQuizForLesson(lessonId, count);
        return new GenerateQuizResponse(lessonId, questions);
    }

    @PostMapping("/grammar/explain")
    public ExplainResponse explainGrammar(@Valid @RequestBody ExplainRequest request) {
        return new ExplainResponse(aiService.explainGrammarSimply(request.text()));
    }

    @PostMapping("/vocab/review")
    public ExplainResponse reviewVocab(@Valid @RequestBody ExplainRequest request) {
        return new ExplainResponse(aiService.reviewVocabulary(request.text()));
    }
}
