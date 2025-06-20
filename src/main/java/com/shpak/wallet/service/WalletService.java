package com.shpak.wallet.service;

import com.shpak.wallet.dto.WalletRequestTo;
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
public class WalletService {

    private final WalletRepository repository;

    @Transactional
    public void changeBalance(WalletRequestTo request) {
        Wallet wallet = repository.findById(request.getWalletId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wallet not found"));

        if ("DEPOSIT".equalsIgnoreCase(request.getOperationType())) {
            wallet.setBalance(wallet.getBalance() + request.getAmount());
        } else if ("WITHDRAW".equalsIgnoreCase(request.getOperationType())) {
            if (request.getAmount() > wallet.getBalance()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough funds");
            }
            wallet.setBalance(wallet.getBalance() - request.getAmount());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown operation type");
        }

        repository.save(wallet);
    }

    @Transactional(readOnly = true)
    public Long getBalance(UUID walletId) {
        return repository.findById(walletId)
                .map(Wallet::getBalance)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
    }
}
