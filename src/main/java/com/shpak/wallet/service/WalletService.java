package com.shpak.wallet.service;

import com.shpak.wallet.dto.WalletRequestTo;
import com.shpak.wallet.model.OperationType;
import com.shpak.wallet.model.Wallet;
import com.shpak.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private static final int MAX_RETRIES = 5;
    private final WalletRepository repository;

    @Transactional
    public void changeBalance(WalletRequestTo request) {
        OperationType operationType = request.getOperationType();

        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
            Wallet wallet = repository.findById(request.getWalletId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

            if (OperationType.DEPOSIT.equals(operationType)) {
                wallet.setBalance(wallet.getBalance() + request.getAmount());
            }

            else if (OperationType.WITHDRAW.equals(operationType)) {
                if (request.getAmount() > wallet.getBalance()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough funds");
                }
                wallet.setBalance(wallet.getBalance() - request.getAmount());

            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown operation type");
            }
                repository.save(wallet);
                return;
            } catch (ObjectOptimisticLockingFailureException ex) {
                if (i == MAX_RETRIES - 1) {throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Operation conflict after " + MAX_RETRIES + " attempts");
                }
                try {
                    Thread.sleep(10 + (int)(Math.random() * 20));
                } catch (InterruptedException ignored) {}
            }
        }
    }

    @Transactional(readOnly = true)
    public Long getBalance(UUID walletId) {
        return repository.findById(walletId)
                .map(Wallet::getBalance)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
    }
}
