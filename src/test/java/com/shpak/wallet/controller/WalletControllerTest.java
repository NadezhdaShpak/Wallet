package com.shpak.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shpak.wallet.dto.WalletRequestTo;
import com.shpak.wallet.model.OperationType;
import com.shpak.wallet.service.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WalletController.class)
class WalletControllerTest {
    private static final String BASE_URL = "/api/v1/wallets/";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final UUID VALID_ID = UUID.fromString("2f3a52ff-3969-476f-a25f-d99bc3718f73");
    private static final UUID NOT_EXISTING_ID = UUID.fromString("3f3a52ff-3969-476f-a25f-d99bc3718f73");

    @Test
    @DisplayName("When wallet exists return balance")
    void whenWalletExistsReturnBalance() throws Exception {
        Long balance = 100L;
        when(walletService.getBalance(VALID_ID)).thenReturn(balance);

        mockMvc.perform(get(BASE_URL + VALID_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.walletId").value(VALID_ID.toString()))
                .andExpect(jsonPath("$.balance").value(balance));
    }

    @Test
    @DisplayName("When UUID format is invalid return BadRequest")
    void whenUuidFormatInvalidReturnBadRequest() throws Exception {
        String invalidUuid = "invalid-uuid-format";
        mockMvc.perform(get(BASE_URL + invalidUuid))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("When wallet not found return 404")
    void whenWalletNotFoundReturnNotFound() throws Exception {
        when(walletService.getBalance(NOT_EXISTING_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get(BASE_URL + NOT_EXISTING_ID))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("When deposit is successful return OK")
    void whenDepositIsSuccessfulReturnOk() throws Exception {
        WalletRequestTo request = new WalletRequestTo(VALID_ID, OperationType.DEPOSIT, 100L);
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(walletService, times(1)).changeBalance(request);
    }

    @Test
    @DisplayName("When withdraw is successful return OK")
    void whenWithdrawIsSuccessfulReturnOk() throws Exception {
        WalletRequestTo request = new WalletRequestTo(VALID_ID, OperationType.WITHDRAW, 50L);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(walletService, times(1)).changeBalance(request);
    }

    @Test
    @DisplayName("When wallet not found then return NotFound")
    void whenWalletNotFoundThenReturnNotFound() throws Exception {
        WalletRequestTo request = new WalletRequestTo(NOT_EXISTING_ID, OperationType.DEPOSIT, 100L);

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(walletService).changeBalance(request);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("When not enough funds then return NotFound")
    void whenNotEnoughFundsThenReturnBadRequest() throws Exception {
        WalletRequestTo request = new WalletRequestTo(VALID_ID, OperationType.WITHDRAW, 1000L);

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough funds"))
                .when(walletService).changeBalance(request);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("When operation type is unknown then return NotFound")
    void whenOperationTypeUnknownThenReturnBadRequest() throws Exception {
        WalletRequestTo request = new WalletRequestTo(VALID_ID, null, 100L);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("When amount is negative then return BadRequest")
    void whenAmountIsNegativeThenReturnBadRequest() throws Exception {
        WalletRequestTo request = new WalletRequestTo(VALID_ID, OperationType.DEPOSIT, -100L);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("When optimistic locking conflict then return CONFLICT")
    void whenOptimisticLockingConflictThenReturnConflict() throws Exception {
        WalletRequestTo request = new WalletRequestTo(VALID_ID, OperationType.DEPOSIT, 100L);

        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Operation conflict after 5 attempts"))
                .when(walletService).changeBalance(request);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}