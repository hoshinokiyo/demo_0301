package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.mapper.MomAuthMapper;
import com.example.demo.model.FamilyAssignee;
import com.example.demo.model.MomAuthSetting;

@Service
public class MomAuthService {

    private final MomAuthMapper momAuthMapper;

    public MomAuthService(MomAuthMapper momAuthMapper) {
        this.momAuthMapper = momAuthMapper;
    }

    public FamilyAssignee getApprover() {
        MomAuthSetting setting = momAuthMapper.findSetting();
        if (setting == null) {
            return FamilyAssignee.MOTHER;
        }
        return FamilyAssignee.fromInput(setting.getApproverCode()).orElse(FamilyAssignee.MOTHER);
    }

    public boolean isApprover(String userCode) {
        return getApprover().code().equals(userCode);
    }

    public boolean isSecretInitialized() {
        MomAuthSetting setting = momAuthMapper.findSetting();
        return setting != null && Boolean.TRUE.equals(setting.getSecretInitialized());
    }

    public boolean verify(String secret) {
        if (secret == null || secret.isBlank()) {
            return false;
        }
        MomAuthSetting setting = momAuthMapper.findSetting();
        if (setting == null || !Boolean.TRUE.equals(setting.getSecretInitialized())) {
            return false;
        }
        return setting.getSecret() != null && setting.getSecret().equals(secret.trim());
    }

    public boolean initialize(String newSecret) {
        if (newSecret == null || newSecret.isBlank()) {
            return false;
        }
        return momAuthMapper.updateSecret(newSecret.trim()) > 0;
    }

    public boolean change(String currentSecret, String newSecret) {
        if (!verify(currentSecret)) {
            return false;
        }
        if (newSecret == null || newSecret.isBlank()) {
            return false;
        }
        return momAuthMapper.updateSecret(newSecret.trim()) > 0;
    }

    public boolean resetApprover(String approverCode) {
        FamilyAssignee selected = FamilyAssignee.fromInput(approverCode).orElse(null);
        if (selected == null) {
            return false;
        }
        return momAuthMapper.resetApprover(selected.code()) > 0;
    }
}
