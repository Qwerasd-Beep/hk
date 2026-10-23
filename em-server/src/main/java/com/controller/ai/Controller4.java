package com.controller.ai;

import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import jakarta.annotation.Resource;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/ai/image")
public class Controller4 {
    @Resource
    private DashScopeImageModel imgModel;

    /**
     * 基础文生图：仅传入提示词，使用默认配置生成一张图片
     * @param msg 文本提示词，描述想要生成的图片内容
     * @return 生成的图片 URL
     */
    @GetMapping("/text2image")
    public String text2Image(@RequestParam("msg") String msg) {
        ImageResponse resp=imgModel.call(new ImagePrompt(msg));

        ImageGeneration generation = resp.getResult();

        Image image = generation.getOutput();
        return  image.getUrl();
    }

    /**
     * 带参数的文生图：可自定义尺寸、风格、数量、随机种子等
     *
     * @param prompt        文本提示词
     * @param size          图片尺寸，支持 1024*1024、720*1280、1280*720（也支持用 x 分隔）
     * @param style         图片风格：auto / photography / portrait / 3d cartoon / anime /
     *                      oil painting / watercolor / sketch / chinese painting / flat illustration
     * @param n             生成图片数量，1~4
     * @param seed          随机种子，相同种子可生成相似图片（可选）
     * @param negativePrompt 负向提示词，描述不希望出现的内容（可选）
     * @return 生成的图片 URL 列表
     */

    @GetMapping("/text2image-options")
    public Map<String, Object> text2ImageOptions(
            @RequestParam("prompt") String prompt,
            @RequestParam(value = "size", defaultValue = "1024*1024") String size,
            @RequestParam(value = "style", defaultValue = "auto") String style,
            @RequestParam(value = "n", defaultValue = "1") Integer n,
            @RequestParam(value = "seed", required = false) Integer seed,
            @RequestParam(value = "negativePrompt", required = false) String negativePrompt) {

        ImagePrompt imagePrompt = buildImagePrompt(prompt, size, style, n, seed, negativePrompt);
        ImageResponse response = imgModel.call(imagePrompt);

        List<ImageGeneration> results = response.getResults();
        List<String> urls = (results == null) ? List.of()
                : results.stream().map(gen -> gen.getOutput().getUrl()).toList();

        // 返回图片URL列表 + 元数据（任务状态、错误信息等），便于排查问题
        return Map.of(
                "urls", urls,
                "metadata", response.getMetadata());
    }

    private ImagePrompt buildImagePrompt(String prompt, String size, String style, Integer n, Integer seed,
                                         String negativePrompt) {
        // 解析尺寸：支持 "1024*1024" 和 "1024x1024" 两种格式
        String[] parts = size.split("[*x]");
        int width = Integer.parseInt(parts[0].trim());
        int height = Integer.parseInt(parts[1].trim());

        // DashScope 风格参数需要尖括号包裹，如 <auto>、<anime>
        // 若用户未加尖括号则自动补上
        String dashScopeStyle = style;
        if (dashScopeStyle != null && !dashScopeStyle.startsWith("<")) {
            dashScopeStyle = "<" + dashScopeStyle + ">";
        }

        DashScopeImageOptions.Builder optionsBuilder = DashScopeImageOptions.builder()
                .width(width)
                .height(height)
                .style(dashScopeStyle)
                .n(n);

        if (seed != null) {
            optionsBuilder.seed(seed);
        }
        if (negativePrompt != null && !negativePrompt.isBlank()) {
            optionsBuilder.negativePrompt(negativePrompt);
        }

        return new ImagePrompt(prompt, optionsBuilder.build());
    }


    /**
     * 文生图并下载到项目 uploads/ai-image 目录，返回项目内可长期访问的 URL
     *
     * @param prompt         文本提示词
     * @param size           图片尺寸，支持 1024*1024、720*1280、1280*720（也支持用 x 分隔）
     * @param style          图片风格：auto / photography / portrait / 3d cartoon / anime /
     *                       oil painting / watercolor / sketch / chinese painting / flat illustration
     * @param n              生成图片数量，1~4
     * @param seed           随机种子，相同种子可生成相似图片（可选）
     * @param negativePrompt 负向提示词，描述不希望出现的内容（可选）
     * @return 本地图片 URL 列表 + 元数据
     */

    @GetMapping("/text2image-local")
    public Map<String, Object> text2ImageLocal(
            @RequestParam("prompt") String prompt,
            @RequestParam(value = "size", defaultValue = "1024*1024") String size,
            @RequestParam(value = "style", defaultValue = "auto") String style,
            @RequestParam(value = "n", defaultValue = "1") Integer n,
            @RequestParam(value = "seed", required = false) Integer seed,
            @RequestParam(value = "negativePrompt", required = false) String negativePrompt) throws Exception {

        ImagePrompt imagePrompt = buildImagePrompt(prompt, size, style, n, seed, negativePrompt);
        ImageResponse response = imgModel.call(imagePrompt);

        //下载
        List<String> urls = down(response.getResults());
        return Map.of(
                "urls", urls,
                "metadata", response.getMetadata());
    }

    private List<String> down(List<ImageGeneration> results) throws Exception {
        List<String> localUrls = new ArrayList<>();
        if (results == null) {
            return localUrls;
        }

        // 保存到项目根目录 uploads/ai-image，配合 application.yml 的 static-locations 通过 /ai-image/** 访问
        Path saveDir = Paths.get("uploads", "ai-image");
        Files.createDirectories(saveDir);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        for (ImageGeneration generation : results) {
            String remoteUrl = generation.getOutput().getUrl();
            if (remoteUrl == null || remoteUrl.isBlank()) {
                continue;
            }

            HttpRequest request = HttpRequest.newBuilder(URI.create(remoteUrl))
                    .timeout(Duration.ofMinutes(2))
                    .GET()
                    .build();
            HttpResponse<byte[]> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (httpResponse.statusCode() != 200) {
                throw new IllegalStateException("下载生成图片失败，HTTP状态: " + httpResponse.statusCode());
            }

            String fileName = UUID.randomUUID().toString().replace("-", "") + ".png";
            Files.write(saveDir.resolve(fileName), httpResponse.body());
            localUrls.add("/ai-image/" + fileName);
        }

        return localUrls;
    }

    /**
     * 返回完整的 ImageResponse 元信息，便于排查问题（查看请求ID、任务状态、生成数量等）
     *
     * @param prompt 文本提示词
     * @return ImageResponse 的 toString 结果
     */
    @GetMapping("/text2image-debug")
    public String text2ImageDebug(@RequestParam("prompt") String prompt) {
        ImageResponse response = imgModel.call(new ImagePrompt(prompt));
        return response.toString();
    }

}
