package com.aiassess.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatService {

    @Value("${wechat.miniapp.appid}")
    private String appid;

    @Value("${wechat.miniapp.secret}")
    private String secret;

    @Value("${wechat.miniapp.code2session-url}")
    private String code2sessionUrl;

    private final WebClient.Builder webClientBuilder;

    public String code2Session(String code) {
        // 开发 mock 模式：appid 未配置时直接用 code 作为 openid，跳过微信 API
        if (!StringUtils.hasText(appid) || appid.startsWith("your_")) {
            log.warn("[DEV MOCK] appid 未配置，使用 code 作为 openid: {}", code);
            return "mock_openid_" + code;
        }

        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
            code2sessionUrl, appid, secret, code);

        Map<String, Object> resp = webClientBuilder.build()
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        if (resp == null || resp.containsKey("errcode") && !resp.get("errcode").equals(0)) {
            log.error("微信登录失败: {}", resp);
            throw new RuntimeException("微信登录失败");
        }

        return (String) resp.get("openid");
    }
}
