package com.jari.jari.account.dto;

import com.jari.jari.account.entity.Account;
import com.jari.jari.account.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountRequestDto {
    private String nickname;
    private AccountType accountType;

    public Account createAccount() {
        return Account.builder()
                .nickname(nickname)
                .accountType(accountType)
                .authentication("NONE")
                .quit(Boolean.FALSE)
                .build();
    }
}
