package com.example.demo.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MomEnterForm {
    @NotBlank(message = "合言葉を入力してね")
    private String secret;
}
