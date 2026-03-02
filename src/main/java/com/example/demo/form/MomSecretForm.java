package com.example.demo.form;

import lombok.Data;

@Data
public class MomSecretForm {
    private String currentSecret;
    private String newSecret;
    private String confirmSecret;
}
