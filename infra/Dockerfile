# Oracle Ampere A1(ARM64) 위에서 그대로 빌드해 쓰는 이미지.
# VM 이 arm64 라 크로스 컴파일이 필요 없다 — VM 에서 docker compose build 하면 네이티브다.
#
# 이전 Dockerfile.render 는 gradle:8.14.3-jdk24 / temurin:24-jre 로 빌드했는데
# build.gradle 의 toolchain 은 Java 21 이었다. 여기서는 21 로 통일한다.

FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# 래퍼와 빌드 스크립트를 먼저 복사해 의존성 레이어를 캐시한다.
COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --version --no-daemon

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

# ffmpeg/ffprobe 는 트랜스코딩과 썸네일 추출에 필요하다(FfmpegLocalAdapter, FfprobeAdapter).
# curl 은 컨테이너 헬스체크용.
RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/libs/*.jar /app/app.jar

# 트랜스코딩 작업 공간. 최종 산출물은 R2 로 올라가고 여기는 임시로만 쓴다.
RUN mkdir -p /app/data/tmp /app/data/uploads /app/data/hls /app/data/thumbnails \
    && useradd -r -u 10001 -d /app appuser \
    && chown -R appuser:appuser /app
USER appuser

# 힙은 2GB 로 제한한다. ffmpeg 는 별도 프로세스라 JVM 힙과 무관하게 메모리를 쓰므로
# 24GB 중 대부분을 ffmpeg 와 Postgres, 페이지 캐시에 남겨 둔다.
ENV JAVA_OPTS="-Xms256m -Xmx2g -XX:MaxMetaspaceSize=256m"
ENV PORT=8080
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD curl -fsS "http://localhost:${PORT}/health" || exit 1

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -Dserver.port=${PORT} -jar /app/app.jar"]
