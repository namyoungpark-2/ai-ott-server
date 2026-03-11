package com.aiott.ottpoc.application.security;

public final class Permissions {
    private Permissions() {}

    // Content
    public static final String CONTENT_READ = "content:read";
    public static final String CONTENT_CREATE = "content:create";
    public static final String CONTENT_UPDATE = "content:update";
    public static final String CONTENT_PUBLISH = "content:publish";
    public static final String CONTENT_ARCHIVE = "content:archive";

    // Asset
    public static final String ASSET_READ = "asset:read";
    public static final String ASSET_CREATE = "asset:create";
    public static final String ASSET_DELETE = "asset:delete";

    // Job (transcoding/retry)
    public static final String JOB_READ = "job:read";
    public static final String JOB_RETRY = "job:retry";
    public static final String JOB_CANCEL = "job:cancel";

    // Ops
    public static final String METRICS_READ = "metrics:read";
    public static final String LOGS_READ = "logs:read";
    public static final String ALERTS_READ = "alerts:read";
    public static final String ALERTS_UPDATE = "alerts:update";

    // IAM
    public static final String IAM_ALL = "iam:*";
}
