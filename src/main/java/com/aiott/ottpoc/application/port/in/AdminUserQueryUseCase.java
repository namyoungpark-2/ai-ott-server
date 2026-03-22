package com.aiott.ottpoc.application.port.in;

import com.aiott.ottpoc.application.dto.admin.AdminUserSummary;

import java.util.List;

public interface AdminUserQueryUseCase {

    /** List all users, optionally filtered by subscription tier. */
    List<AdminUserSummary> listUsers(String tierFilter);
}
