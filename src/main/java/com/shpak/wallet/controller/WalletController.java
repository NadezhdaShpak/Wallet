package com.shpak.wallet.controller;

import com.shpak.wallet.dto.WalletRequestTo;
import com.shpak.wallet.dto.WalletResponseTo;
import com.shpak.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/wallet")
    public ResponseEntity<Void> updateWallet(@RequestBody @Valid WalletRequestTo request) {
        walletService.changeBalance(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/wallets/{id}")
    public ResponseEntity<WalletResponseTo> getBalance(@PathVariable("id") UUID walletId) {
        Long balance = walletService.getBalance(walletId);
        return ResponseEntity.ok(new WalletResponseTo(walletId, balance));
    }
}