package com.shpak.wallet.service;

import com.shpak.wallet.dto.WalletRequestTo;
import com.shpak.wallet.model.OperationType;
import com.shpak.wallet.model.Wallet;
import com.shpak.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceV2 {

    private static final int MAX_RETRIES = 5;
    private final WalletRepository repository;

    @Transactional
    public void changeBalance(WalletRequestTo request) {
        var retry = MAX_RETRIES;
        while (retry != 0) {
            try {
                updateBalance(request);
                return;
            } catch (Exception e) {
                sleep();
                retry--;
            }
        }
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Operation conflict after " + MAX_RETRIES + " attempts");

    }

    private void updateBalance(WalletRequestTo request) {
        var wallet = repository.findById(request.getWalletId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
        OperationType operationType = request.getOperationType();

        switch (operationType) {
            case DEPOSIT -> wallet.setBalance(wallet.getBalance() + request.getAmount());
            case WITHDRAW -> {
                if (request.getAmount() > wallet.getBalance()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough funds");
                }
                wallet.setBalance(wallet.getBalance() - request.getAmount());
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown operation type");
        }
    }

    @Transactional(readOnly = true)
    public Long getBalance(UUID walletId) {
        return repository.findById(walletId)
                .map(Wallet::getBalance)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
    }

    private void sleep() {
        try {
            Thread.sleep(10 + (int) (Math.random() * 20));
        } catch (InterruptedException ignored) {
        }
    }
}
