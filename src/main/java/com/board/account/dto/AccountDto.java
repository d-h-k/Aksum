package com.board.account.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import com.board.account.entity.AccountRole;

@Getter
@Setter
@NoArgsConstructor
public class AccountDto {
    private Long id;
    private String nickname;
    private String email;
    private Set<AccountRole> roles = new HashSet<>();
}
