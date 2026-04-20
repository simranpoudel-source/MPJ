package com.enterprise.wms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Locale;

@Service
public class VoiceService {
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile Model voskModel;

    @Value("${wms.voice.stt-provider:simulated}")
    private String sttProvider;
    @Value("${wms.voice.stt-url:}")
    private String sttUrl;
    @Value("${wms.voice.vosk-model-path:}")
    private String voskModelPath;
    @Value("${wms.voice.vosk-sample-rate:16000}")
    private Float voskSampleRate;
    @Value("${wms.voice.google-stt-api-key:}")
    private String googleSttApiKey;
    @Value("${wms.voice.google-stt-language:en-US}")
    private String googleSttLanguage;
    @Value("${wms.voice.google-stt-sample-rate:16000}")
    private Integer googleSttSampleRate;
    @Value("${wms.voice.google-stt-encoding:LINEAR16}")
    private String googleSttEncoding;
    @Value("${wms.voice.tts-url:}")
    private String ttsUrl;
    @Value("${wms.voice.api-key:}")
    private String voiceApiKey;
    @Value("${wms.openai.api-key:}")
    private String openAiApiKey;
    @Value("${wms.voice.openai-tts-model:gpt-4o-mini-tts}")
    private String openAiTtsModel;
    @Value("${wms.voice.openai-tts-voice:alloy}")
    private String openAiTtsVoice;
    @Value("${wms.voice.openai-tts-format:mp3}")
    private String openAiTtsFormat;

    public String speechToText(byte[] audioPayload) {
        String provider = sttProvider == null ? "simulated" : sttProvider.trim().toLowerCase(Locale.ROOT);
        if ("google".equals(provider)) {
            return googleSpeechToText(audioPayload);
        }
        if ("custom".equals(provider)) {
            return customSpeechToText(audioPayload);
        }
        if ("vosk".equals(provider)) {
            return voskSpeechToText(audioPayload);
        }
        return "simulated voice command: show products expiring in 7 days";
    }

    private String customSpeechToText(byte[] audioPayload) {
        if (sttUrl == null || sttUrl.isBlank()) {
            return "simulated voice command: show products expiring in 7 days";
        }
        try {
            String json = "{\"audioBase64\":\"" + Base64.getEncoder().encodeToString(audioPayload) + "\"}";
            Request request = new Request.Builder()
                    .url(sttUrl)
                    .addHeader("Authorization", "Bearer " + voiceApiKey)
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return "simulated voice command: show products expiring in 7 days";
                }
                return response.body().string();
            }
        } catch (IOException e) {
            return "simulated voice command: show products expiring in 7 days";
        }
    }

    private String googleSpeechToText(byte[] audioPayload) {
        if (googleSttApiKey == null || googleSttApiKey.isBlank()) {
            return "simulated voice command: show products expiring in 7 days";
        }
        try {
            String encodedAudio = Base64.getEncoder().encodeToString(audioPayload);
            var payload = objectMapper.createObjectNode();
            payload.set("config", objectMapper.createObjectNode()
                    .put("encoding", googleSttEncoding)
                    .put("sampleRateHertz", googleSttSampleRate)
                    .put("languageCode", googleSttLanguage));
            payload.set("audio", objectMapper.createObjectNode().put("content", encodedAudio));
            String body = objectMapper.writeValueAsString(payload);
            Request request = new Request.Builder()
                    .url("https://speech.googleapis.com/v1/speech:recognize?key=" + googleSttApiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body, MediaType.parse("application/json")))
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return "simulated voice command: show products expiring in 7 days";
                }
                JsonNode root = objectMapper.readTree(response.body().string());
                JsonNode transcript = root.path("results").path(0).path("alternatives").path(0).path("transcript");
                if (transcript.isTextual() && !transcript.asText().isBlank()) {
                    return transcript.asText();
                }
                return "simulated voice command: show products expiring in 7 days";
            }
        } catch (IOException e) {
            return "simulated voice command: show products expiring in 7 days";
        }
    }

    private String voskSpeechToText(byte[] audioPayload) {
        try {
            Model model = getOrLoadVoskModel();
            if (model == null) {
                return "simulated voice command: show products expiring in 7 days";
            }
            try (Recognizer recognizer = new Recognizer(model, voskSampleRate)) {
                recognizer.acceptWaveForm(audioPayload, audioPayload.length);
                String finalJson = recognizer.getFinalResult();
                JsonNode root = objectMapper.readTree(finalJson);
                String text = root.path("text").asText("");
                if (!text.isBlank()) {
                    return text;
                }
            }
            return "simulated voice command: show products expiring in 7 days";
        } catch (Exception | UnsatisfiedLinkError e) {
            return "simulated voice command: show products expiring in 7 days";
        }
    }

    private Model getOrLoadVoskModel() throws IOException {
        if (voskModel != null) {
            return voskModel;
        }
        synchronized (this) {
            if (voskModel != null) {
                return voskModel;
            }
            if (voskModelPath == null || voskModelPath.isBlank()) {
                return null;
            }
            voskModel = new Model(voskModelPath);
            return voskModel;
        }
    }

    public byte[] textToSpeech(String responseText) {
        return textToSpeech(responseText, null);
    }

    public byte[] textToSpeech(String responseText, String formatOverride) {
        String effectiveFormat = resolveFormat(formatOverride);
        if (ttsUrl == null || ttsUrl.isBlank()) {
            return openAiTextToSpeech(responseText, effectiveFormat);
        }
        try {
            String json = "{\"text\":\"" + responseText.replace("\"", "'") + "\"}";
            Request request = new Request.Builder()
                    .url(ttsUrl)
                    .addHeader("Authorization", "Bearer " + voiceApiKey)
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return ("SPOKEN:" + responseText).getBytes();
                }
                return response.body().bytes();
            }
        } catch (IOException e) {
            return ("SPOKEN:" + responseText).getBytes();
        }
    }

    public String ttsContentType() {
        return ttsContentType(null);
    }

    public String ttsContentType(String formatOverride) {
        String format = resolveFormat(formatOverride);
        return switch (format) {
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "opus" -> "audio/opus";
            case "flac" -> "audio/flac";
            case "aac" -> "audio/aac";
            default -> "application/octet-stream";
        };
    }

    private byte[] openAiTextToSpeech(String responseText, String responseFormat) {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            return ("SPOKEN:" + responseText).getBytes();
        }
        try {
            String safeText = responseText.replace("\"", "\\\"");
            String json = """
                    {
                      "model": "%s",
                      "voice": "%s",
                      "input": "%s",
                      "response_format": "%s"
                    }
                    """.formatted(openAiTtsModel, openAiTtsVoice, safeText, responseFormat);
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/audio/speech")
                    .addHeader("Authorization", "Bearer " + openAiApiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return ("SPOKEN:" + responseText).getBytes();
                }
                return response.body().bytes();
            }
        } catch (IOException e) {
            return ("SPOKEN:" + responseText).getBytes();
        }
    }

    private String resolveFormat(String formatOverride) {
        String candidate = (formatOverride == null || formatOverride.isBlank())
                ? openAiTtsFormat
                : formatOverride;
        String format = candidate == null ? "" : candidate.trim().toLowerCase();
        return switch (format) {
            case "mp3", "wav", "opus", "flac", "aac" -> format;
            default -> "mp3";
        };
    }
}
