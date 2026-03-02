package com.example.demo.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApproverResetForm {
    @NotBlank(message = "次の承認者を選んでね")
    private String nextApprover;
}
