package com.logos.dto;

import com.logos.entity.User;
import lombok.Data;

@Data
public class AdminUserUpdateDTO {
    private User.Role role;
    private Boolean active;
}
