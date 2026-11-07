package com.example.japaneseapp.roleplay;

import com.example.japaneseapp.common.GlobalExceptionHandler;
import com.example.japaneseapp.roleplay.dto.CharacterDto;
import com.example.japaneseapp.roleplay.dto.TranslationRequest;
import com.example.japaneseapp.roleplay.dto.TranslationResponse;
import com.example.japaneseapp.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RoleplayControllerTest {
    private static final UUID OWNER = UUID.randomUUID();

    private RoleplayService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(RoleplayService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new RoleplayController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        var principal = new UserPrincipal(OWNER, "owner@example.test", "hash");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatedCatalogEndpointReturnsOriginalEntries() throws Exception {
        when(service.listCharacters()).thenReturn(List.of(
                new CharacterDto("aiko-cafe-guide", "Aiko", "Café guide", List.of("beginner"))));

        mockMvc.perform(get("/api/v1/characters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("aiko-cafe-guide"));
    }

    @Test
    void translationEndpointReturnsSelectedLanguage() throws Exception {
        when(service.translate(any(TranslationRequest.class)))
                .thenReturn(new TranslationResponse("Xin chào", "vi"));

        mockMvc.perform(post("/api/v1/translations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"こんにちは","sourceLanguage":"ja","targetLanguage":"vi"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.translatedText").value("Xin chào"))
                .andExpect(jsonPath("$.targetLanguage").value("vi"));
    }

    @Test
    void ownerDeletionEndpointRespondsNoContent() throws Exception {
        UUID conversationId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/conversations/{conversationId}", conversationId))
                .andExpect(status().isNoContent());

        verify(service).deleteConversation(OWNER, conversationId);
    }

    @Test
    void unauthenticatedCatalogRequestIsRejected() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/characters"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }
}
