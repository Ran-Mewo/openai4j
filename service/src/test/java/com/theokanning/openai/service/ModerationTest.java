package com.theokanning.openai.service;

import com.theokanning.openai.moderation.Moderation;
import com.theokanning.openai.moderation.ModerationRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class ModerationTest {

    com.theokanning.openai.service.OpenAiService service = new OpenAiService();

    @Test
    void createModeration() {
        ModerationRequest moderationRequest = ModerationRequest.builder()
                .input("I want to kill him")
                .model("text-moderation-latest")
                .build();

        Moderation moderationScore = service.createModeration(moderationRequest).getResults().get(0);

        assertTrue(moderationScore.isFlagged());
    }
}
