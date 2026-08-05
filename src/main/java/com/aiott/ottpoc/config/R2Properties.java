package com.aiott.ottpoc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.r2")
public class R2Properties {
    private String accountId;
    private String accessKeyId;
    private String secretAccessKey;
    private String bucket;
    /** Public base URL, e.g. {@code https://pub-xxxxxxxx.r2.dev} or a custom domain. */
    private String publicUrl;

    /**
     * master.m3u8 을 presigned S3 URL 로 내려줄지 여부. 기본값 false.
     *
     * <p>true 로 켜면 재생이 깨진다. presigned URL 은 S3 엔드포인트
     * ({@code <account>.r2.cloudflarestorage.com}) 를 가리키는데, master.m3u8 안의
     * 세그먼트 경로는 상대 경로({@code seg_000.ts})다. 따라서 플레이어가 세그먼트를
     * 같은 S3 엔드포인트에서 서명 없이 요청하게 되고, R2 의 S3 API 는 항상 SigV4 를
     * 요구하므로 모든 세그먼트가 403 을 받는다.
     *
     * <p>제대로 보호하려면 세그먼트까지 함께 서명해야 하며(플레이리스트 재작성 또는
     * Cloudflare Signed Token), 그때 이 플래그를 다시 검토한다.
     */
    private boolean presignPlayback = false;
}
