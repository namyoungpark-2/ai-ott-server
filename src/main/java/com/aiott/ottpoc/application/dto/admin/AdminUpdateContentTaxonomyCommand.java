package com.aiott.ottpoc.application.dto.admin;

import java.util.List;

public record AdminUpdateContentTaxonomyCommand(
        List<String> categorySlugs,
        List<String> tags
) {}
