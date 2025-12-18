package com.tickettriage.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tickettriage.dto.ClassificationResult;
import com.tickettriage.model.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiClassificationService {

    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();

    @Value("${spring.gemini.api-key}")
    private String apiKey;

    @Value("${spring.gemini.model:gemini-1.5-flash}")
    private String model;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/%s:generateContent?key=%s";

    private static final String CLASSIFICATION_PROMPT = """
            Analyze the following customer support ticket and provide classification:

            Subject: %s
            Description: %s

            Please classify this ticket with the following information:
            1. Category: Choose ONE from [BILLING, TECH_SUPPORT, BUG, FEATURE_REQUEST, GENERAL]
            2. Priority: Choose ONE from [LOW, MEDIUM, HIGH, URGENT]
            3. Sentiment: Rate from 1-10 (1=very negative, 10=very positive)

            Respond ONLY in this exact format:
            CATEGORY: <category>
            PRIORITY: <priority>
            SENTIMENT: <number>
            REASONING: <brief explanation>
            """;

    public ClassificationResult classify(Ticket ticket) {
        try {
            log.info("Classifying ticket ID: {} using Gemini REST API", ticket.getId());

            // Build the prompt
            String prompt = String.format(CLASSIFICATION_PROMPT,
                    ticket.getSubject(),
                    ticket.getDescription());

            // Create request body
            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            requestBody.add("contents", contents);

            // Build URL
            String url = String.format(GEMINI_API_URL, model, apiKey);

            // Make HTTP request
            RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response);
                }

                String responseBody = response.body().string();
                log.debug("Gemini API response: {}", responseBody);

                // Parse response
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                String responseText = jsonResponse
                        .getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();

                log.debug("Gemini classification text: {}", responseText);

                // Parse the classification
                return parseClassificationResponse(responseText);
            }

        } catch (Exception e) {
            log.error("Error classifying ticket ID: {}", ticket.getId(), e);
            throw new RuntimeException("Failed to classify ticket: " + e.getMessage(), e);
        }
    }

    private ClassificationResult parseClassificationResponse(String response) {
        ClassificationResult result = new ClassificationResult();

        // Extract category
        Pattern categoryPattern = Pattern.compile("CATEGORY:\\s*(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher categoryMatcher = categoryPattern.matcher(response);
        if (categoryMatcher.find()) {
            String category = categoryMatcher.group(1).toUpperCase();
            try {
                result.setCategory(Ticket.Category.valueOf(category));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid category: {}, defaulting to GENERAL", category);
                result.setCategory(Ticket.Category.GENERAL);
            }
        } else {
            result.setCategory(Ticket.Category.GENERAL);
        }

        // Extract priority
        Pattern priorityPattern = Pattern.compile("PRIORITY:\\s*(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher priorityMatcher = priorityPattern.matcher(response);
        if (priorityMatcher.find()) {
            String priority = priorityMatcher.group(1).toUpperCase();
            try {
                result.setPriority(Ticket.Priority.valueOf(priority));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid priority: {}, defaulting to MEDIUM", priority);
                result.setPriority(Ticket.Priority.MEDIUM);
            }
        } else {
            result.setPriority(Ticket.Priority.MEDIUM);
        }

        // Extract sentiment
        Pattern sentimentPattern = Pattern.compile("SENTIMENT:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher sentimentMatcher = sentimentPattern.matcher(response);
        if (sentimentMatcher.find()) {
            int sentiment = Integer.parseInt(sentimentMatcher.group(1));
            result.setSentiment(Math.min(10, Math.max(1, sentiment))); // Clamp to 1-10
        } else {
            result.setSentiment(5);
        }

        // Extract reasoning
        Pattern reasoningPattern = Pattern.compile("REASONING:\\s*(.+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher reasoningMatcher = reasoningPattern.matcher(response);
        if (reasoningMatcher.find()) {
            result.setReasoning(reasoningMatcher.group(1).trim());
        }

        return result;
    }
}
