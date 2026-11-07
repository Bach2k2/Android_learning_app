package com.example.japaneseapp.ai;

import com.example.japaneseapp.common.ApiException;
import com.example.japaneseapp.lesson.*;
import com.example.japaneseapp.lesson.dto.QuizQuestionDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AiService {

    private final OllamaClient ollamaClient;
    private final LessonRepository lessonRepository;
    private final VocabularyRepository vocabularyRepository;
    private final GrammarPointRepository grammarPointRepository;
    private final LessonExampleSentenceRepository exampleSentenceRepository;
    private final GeneratedQuizQuestionRepository generatedQuizQuestionRepository;
    private final ObjectMapper objectMapper;

    public AiService(
            OllamaClient ollamaClient,
            LessonRepository lessonRepository,
            VocabularyRepository vocabularyRepository,
            GrammarPointRepository grammarPointRepository,
            LessonExampleSentenceRepository exampleSentenceRepository,
            GeneratedQuizQuestionRepository generatedQuizQuestionRepository,
            ObjectMapper objectMapper
    ) {
        this.ollamaClient = ollamaClient;
        this.lessonRepository = lessonRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.grammarPointRepository = grammarPointRepository;
        this.exampleSentenceRepository = exampleSentenceRepository;
        this.generatedQuizQuestionRepository = generatedQuizQuestionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public List<QuizQuestionDto> generateQuizForLesson(Long lessonId, int count) {
        if (count < 1 || count > 10) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "count must be between 1 and 10");
        }
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lesson not found"));

        var vocab = vocabularyRepository.findAllByLesson_IdOrderByKanaAsc(lessonId);
        var grammar = grammarPointRepository.findAllByLesson_IdOrderByTitleAsc(lessonId);
        var examples = exampleSentenceRepository.findAllByLesson_IdOrderByJapaneseAsc(lessonId);

        String prompt = buildQuizPrompt(lesson, vocab, grammar, examples, count);
        String aiText = ollamaClient.generate(prompt);

        if (aiText == null || aiText.length() > 20000) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI response exceeded the allowed size");
        }
        JsonNode root = extractJson(aiText);
        JsonNode questionsNode = root.get("questions");
        if (questionsNode == null || !questionsNode.isArray() || questionsNode.size() != count) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI response did not contain the requested number of questions");
        }

        List<GeneratedQuizQuestion> generated = new ArrayList<>();
        List<QuizQuestionDto> result = new ArrayList<>();
        for (JsonNode q : questionsNode) {
            JsonNode promptNode = q.get("prompt");
            JsonNode optionsNode = q.path("options");
            JsonNode correctIndexNode = q.get("correctIndex");
            JsonNode explanationNode = q.get("explanation");
            String promptText = promptNode != null && promptNode.isTextual() ? promptNode.asText() : null;
            int correctIndex = correctIndexNode != null && correctIndexNode.isIntegralNumber()
                    ? correctIndexNode.asInt() : -1;
            String explanation = explanationNode != null && explanationNode.isTextual()
                    ? explanationNode.asText() : null;

            if (promptText == null || promptText.isBlank() || promptText.length() > 500
                    || !optionsNode.isArray() || optionsNode.size() < 2 || optionsNode.size() > 6
                    || correctIndex < 0 || correctIndex >= optionsNode.size()
                    || (explanationNode != null && !explanationNode.isNull() && !explanationNode.isTextual())
                    || (explanation != null && explanation.length() > 1000)) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "AI response contained an invalid quiz question");
            }

            List<String> options = new ArrayList<>();
            optionsNode.forEach(n -> {
                String option = n.isTextual() ? n.asText().trim() : "";
                if (option.isEmpty() || option.length() > 300) {
                    throw new ApiException(HttpStatus.BAD_GATEWAY, "AI response contained an invalid quiz option");
                }
                options.add(option);
            });
            if (options.stream().distinct().count() != options.size()) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "AI response contained duplicate quiz options");
            }

            String optionsJson;
            try {
                optionsJson = objectMapper.writeValueAsString(options);
            } catch (Exception e) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "AI response could not be processed");
            }

            var entity = new GeneratedQuizQuestion(
                    UUID.randomUUID(), lesson, promptText.trim(), optionsJson, correctIndex, explanation);
            generated.add(entity);
            result.add(new QuizQuestionDto(entity.getId(), entity.getPrompt(), options));
        }

        generatedQuizQuestionRepository.saveAll(generated);

        return result;
    }

    public String explainGrammarSimply(String text) {
        String prompt = "You are a Japanese tutor. Explain the following grammar point simply for a beginner. " +
                "Keep the explanation focused on this grammar point. Use short sentences. Provide 2 example " +
                "sentences with English translations.\n\nGrammar: " + text;
        return checkedStudyResponse(ollamaClient.generate(prompt));
    }

    public String reviewVocabulary(String text) {
        String prompt = "You are a Japanese tutor. Help the learner review the following vocabulary. " +
                "Keep the review focused on the supplied vocabulary. Return a compact study plan (bullets) " +
                "and 3 quick practice questions.\n\nVocabulary:\n" + text;
        return checkedStudyResponse(ollamaClient.generate(prompt));
    }

    private String checkedStudyResponse(String response) {
        String text = response == null ? "" : response.trim();
        if (text.isEmpty() || text.length() > 5000) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI response did not contain usable study assistance");
        }
        return "AI-generated study assistance (not an authoritative assessment):\n" + text;
    }

    private String buildQuizPrompt(Lesson lesson, List<Vocabulary> vocab, List<GrammarPoint> grammar, List<LessonExampleSentence> examples, int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a Japanese learning assistant. Create exactly ").append(count).append(" multiple-choice questions for this lesson.\n");
        sb.append("Return ONLY valid JSON (no markdown). Format: {\"questions\":[{\"prompt\":string,\"options\":[string,...],\"correctIndex\":number,\"explanation\":string}]}.\n");
        sb.append("Questions should be beginner-friendly. Use lesson vocabulary and grammar; avoid trick questions.\n\n");
        sb.append("LESSON: ").append(lesson.getLessonNumber()).append(" - ").append(lesson.getTitle()).append("\n");
        sb.append("OVERVIEW: ").append(lesson.getOverview()).append("\n\n");

        sb.append("VOCABULARY:\n");
        for (Vocabulary v : vocab) {
            sb.append("- ").append(v.getKana());
            if (v.getKanji() != null && !v.getKanji().isBlank()) {
                sb.append(" (").append(v.getKanji()).append(")");
            }
            sb.append(" = ").append(v.getMeaning()).append("\n");
        }
        sb.append("\nGRAMMAR:\n");
        for (GrammarPoint g : grammar) {
            sb.append("- ").append(g.getTitle()).append(": ").append(g.getExplanation()).append("\n");
        }
        sb.append("\nEXAMPLES:\n");
        for (LessonExampleSentence e : examples) {
            sb.append("- ").append(e.getJapanese()).append(" / ").append(e.getEnglish()).append("\n");
        }

        if (sb.length() > 12000) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Lesson content exceeded the AI prompt limit");
        }
        return sb.toString();
    }

    private JsonNode extractJson(String aiText) {
        try {
            return objectMapper.readTree(aiText);
        } catch (Exception ignored) {
        }

        int first = aiText.indexOf('{');
        int last = aiText.lastIndexOf('}');
        if (first >= 0 && last > first) {
            String candidate = aiText.substring(first, last + 1);
            try {
                return objectMapper.readTree(candidate);
            } catch (Exception ignored) {
            }
        }
        throw new ApiException(HttpStatus.BAD_GATEWAY, "AI response was not valid JSON");
    }
}
