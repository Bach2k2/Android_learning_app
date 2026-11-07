package com.example.japaneseapp.progress;

import com.example.japaneseapp.progress.dto.ProgressDto;
import com.example.japaneseapp.progress.dto.UpdateProgressRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping
    public List<ProgressDto> listMine() {
        return progressService.listMyProgress();
    }

    @PutMapping("/{lessonId}")
    public ProgressDto upsert(
            @PathVariable Long lessonId,
            @Valid @RequestBody UpdateProgressRequest request
    ) {
        return progressService.upsertMyProgress(lessonId, request.status(), request.quizScore());
    }
}
