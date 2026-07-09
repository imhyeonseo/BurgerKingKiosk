package com.example.kiosk_backend.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractJacksonHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 멀티파트 JSON 파트 처리 보강.
 * 업로드된 메뉴 이미지 서빙은 정적 리소스 매핑 대신 {@link com.example.kiosk_backend.controller.ImageController}가
 * 전담한다(Swagger에서 "Try it out"으로 바로 미리보기가 가능하도록 OpenAPI 문서에 노출되는 진짜 엔드포인트로 만들기 위함).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * multipart/form-data로 JSON 파트(@RequestPart)를 보낼 때, 클라이언트(브라우저 FormData, curl -F,
     * Swagger UI의 "Try it out" 등)가 해당 파트에 Content-Type을 명시적으로 application/json으로
     * 지정하지 않고 application/octet-stream이나 text/plain으로 보내는 경우가 흔하다.
     * Jackson 컨버터가 이런 Content-Type도 JSON으로 처리할 수 있도록 지원 범위를 넓혀
     * 클라이언트 쪽에서 Content-Type을 신경 쓰지 않아도 동작하게 한다.
     *
     * 주의: 이 프로젝트는 Jackson 3(tools.jackson) 기반이라 Spring Boot가 실제로 등록하는 JSON 컨버터는
     * {@code JacksonJsonHttpMessageConverter}이며, 구버전 {@code MappingJackson2HttpMessageConverter}는
     * 이제 등록되지 않는다(둘 다 대비해 방어적으로 체크한다).
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            List<MediaType> supportedMediaTypes = null;

            if (converter instanceof AbstractJacksonHttpMessageConverter<?> jackson3Converter) {
                supportedMediaTypes = new ArrayList<>(jackson3Converter.getSupportedMediaTypes());
                supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
                supportedMediaTypes.add(MediaType.TEXT_PLAIN);
                jackson3Converter.setSupportedMediaTypes(supportedMediaTypes);
            } else if (converter instanceof MappingJackson2HttpMessageConverter jackson2Converter) {
                supportedMediaTypes = new ArrayList<>(jackson2Converter.getSupportedMediaTypes());
                supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
                supportedMediaTypes.add(MediaType.TEXT_PLAIN);
                jackson2Converter.setSupportedMediaTypes(supportedMediaTypes);
            }
        }
    }
}
