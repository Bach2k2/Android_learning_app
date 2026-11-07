package com.example.japaneseapp.ai;

import com.example.japaneseapp.common.ApiException;
import com.example.japaneseapp.config.AppProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
public class OllamaClient {

    private final WebClient webClient;
    private final AppProperties.AiProperties props;

    public OllamaClient(AppProperties appProperties, WebClient.Builder builder) {
        this.props = appProperties.ai();
        this.webClient = builder
                .baseUrl(props.ollamaBaseUrl())
                .build();
    }

    public String generate(String prompt) {
        try {
            OllamaGenerateResponse res = webClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new OllamaGenerateRequest(props.model(), prompt, false))
                    .retrieve()
                    .bodyToMono(OllamaGenerateResponse.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();

            if (res == null || res.response() == null) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "AI model returned empty response");
            }
            return res.response();
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI model is unavailable. Ensure Ollama is running and OLLAMA_BASE_URL is correct.");
        }
    }

    public record OllamaGenerateRequest(String model, String prompt, boolean stream) {
    }

    public record OllamaGenerateResponse(String response) {
    }
}
