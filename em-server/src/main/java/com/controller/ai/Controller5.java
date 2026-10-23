package com.controller.ai;

import com.alibaba.cloud.ai.dashscope.api.DashScopeAudioSpeechApi;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioSpeechModel;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioSpeechOptions;
import org.springframework.ai.audio.tts.Speech;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

@RestController @RequestMapping("/ai/sound")
public class Controller5{

private final DashScopeAudioSpeechModel speechModel;

public Controller5(DashScopeAudioSpeechModel model) {
    this.speechModel = model;
}


/**
 * AI 文生音：输入文本，返回音频字节流，前端可直接播放
 *
 * @param text   要合成语音的文本
 * @param model  语音模型，默认 sambert-zhichu-v1（CosyVoice 系列当前账号不可用）
 * @param voice  音色，默认 zhichu（与 sambert-zhichu-v1 绑定）
 * @param speed  语速，默认 1.0
 * @param format 音频格式：mp3 / wav / pcm，默认 mp3
 * @return 音频字节流
 */
@GetMapping("/text2sound")
public ResponseEntity<byte[]> text2Sound(
        @RequestParam("text") String text,
        @RequestParam(value = "model", defaultValue = "sambert-zhichu-v1") String model,
        @RequestParam(value = "voice", defaultValue = "zhichu") String voice,
        @RequestParam(value = "speed", defaultValue = "1.0") Double speed,
        @RequestParam(value = "format", defaultValue = "mp3") String format) {

    DashScopeAudioSpeechApi.ResponseFormat responseFormat = parseFormat(format);
    DashScopeAudioSpeechOptions options = DashScopeAudioSpeechOptions.builder()
            .model(model)
            .voice(voice)
            .speed(speed)
            .responseFormat(responseFormat)
            .build();

    TextToSpeechResponse response = speechModel.call(new TextToSpeechPrompt(text, options));

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    for (Speech speech : response.getResults()) {
        outputStream.writeBytes(speech.getOutput());
    }
    byte[] audio = outputStream.toByteArray();
    if (audio.length == 0) {
        throw new IllegalStateException("语音合成结果为空");
    }

    return ResponseEntity.ok()
            .contentType(toMediaType(responseFormat))
            .body(audio);
}

private DashScopeAudioSpeechApi.ResponseFormat parseFormat(String format) {
    return switch (format.toLowerCase(Locale.ROOT)) {
        case "wav" -> DashScopeAudioSpeechApi.ResponseFormat.WAV;
        case "pcm" -> DashScopeAudioSpeechApi.ResponseFormat.PCM;
        default -> DashScopeAudioSpeechApi.ResponseFormat.MP3;
    };
}

private MediaType toMediaType(DashScopeAudioSpeechApi.ResponseFormat format) {
    return switch (format) {
        case WAV -> MediaType.parseMediaType("audio/wav");
        case PCM -> MediaType.APPLICATION_OCTET_STREAM;
        default -> MediaType.parseMediaType("audio/mpeg");
    };
}
			}
