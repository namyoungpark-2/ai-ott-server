package com.aiott.ottpoc.application.port.out.admin;

import com.aiott.ottpoc.application.dto.admin.AdminUserSummary;

import java.util.List;

public interface AdminUserQueryPort {

    List<AdminUserSummary> findAll();

    List<AdminUserSummary> findByTier(String tier);
}
