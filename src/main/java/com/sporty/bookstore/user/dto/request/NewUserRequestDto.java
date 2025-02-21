package com.sporty.bookstore.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewUserRequestDto {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotEmpty
    private List<@NotBlank String> roles;
}
