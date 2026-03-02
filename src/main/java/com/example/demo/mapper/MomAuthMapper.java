package com.example.demo.mapper;

import org.apache.ibatis.annotations.Param;

import com.example.demo.model.MomAuthSetting;

public interface MomAuthMapper {
    MomAuthSetting findSetting();

    int updateSecret(@Param("secret") String secret);

    int resetApprover(@Param("approverCode") String approverCode);
}
