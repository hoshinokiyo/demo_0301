package com.example.demo.model;

import lombok.Data;

@Data
public class MomAuthSetting {
    private Integer id;
    private String approverCode;
    private String secret;
    private Boolean secretInitialized;
}
