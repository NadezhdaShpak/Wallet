package com.shpak.wallet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletRequestTo {
        @NotNull
        private UUID walletId;

        @NotNull
        private String operationType;

        @NotNull
        @Positive
        private Long amount;

}
