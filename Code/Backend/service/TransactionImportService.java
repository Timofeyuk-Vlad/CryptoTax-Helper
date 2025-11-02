package com.cryptotax.helper.service;

import com.cryptotax.helper.entity.*;
import com.cryptotax.helper.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionImportService {

    private final TransactionRepository transactionRepository;

    public List<Transaction> importDemoTransactions(User user) {
        List<Transaction> transactions = new ArrayList<>();

        // Демо данные - покупка BTC
        Transaction buyBtc = createTransaction(
                user, TransactionType.BUY, "BTC", "USDT",
                new BigDecimal("0.1"), new BigDecimal("50000"),
                new BigDecimal("5000"), new BigDecimal("10"), "USDT",
                LocalDateTime.of(2024, 1, 15, 10, 30, 0)
        );
        transactions.add(buyBtc);

        // Продажа BTC
        Transaction sellBtc = createTransaction(
                user, TransactionType.SELL, "BTC", "USDT",
                new BigDecimal("0.05"), new BigDecimal("60000"),
                new BigDecimal("3000"), new BigDecimal("8"), "USDT",
                LocalDateTime.of(2024, 6, 20, 14, 45, 0)
        );
        transactions.add(sellBtc);

        // Покупка ETH
        Transaction buyEth = createTransaction(
                user, TransactionType.BUY, "ETH", "USDT",
                new BigDecimal("2.0"), new BigDecimal("2500"),
                new BigDecimal("5000"), new BigDecimal("12"), "USDT",
                LocalDateTime.of(2024, 3, 10, 9, 15, 0)
        );
        transactions.add(buyEth);

        // Стейкинг награда
        Transaction stakingReward = createTransaction(
                user, TransactionType.STAKING, "ADA", null,
                new BigDecimal("100"), null, null,
                new BigDecimal("0.5"), "ADA",
                LocalDateTime.of(2024, 8, 5, 12, 0, 0)
        );
        transactions.add(stakingReward);

        return transactionRepository.saveAll(transactions);
    }

    private Transaction createTransaction(User user, TransactionType type, String baseAsset,
                                          String quoteAsset, BigDecimal amount, BigDecimal price,
                                          BigDecimal total, BigDecimal fee, String feeAsset,
                                          LocalDateTime timestamp) {
        Transaction transaction = new Transaction(user, type, baseAsset, timestamp);
        transaction.setQuoteAsset(quoteAsset);
        transaction.setAmount(amount);
        transaction.setPrice(price);
        transaction.setTotal(total);
        transaction.setFee(fee);
        transaction.setFeeAsset(feeAsset);
        transaction.setExchangeTxId("DEMO_" + System.currentTimeMillis() + "_" + baseAsset);
        transaction.setIsProcessed(true);

        return transaction;
    }

    public long getUserTransactionCount(Long userId) {
        User user = new User();
        user.setId(userId);
        return transactionRepository.countByUser(user);
    }
}