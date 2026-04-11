package com.ugurxaslan.profit_tracker_backend.service.entityService;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import com.ugurxaslan.profit_tracker_backend.dto.request.TransactionFilterRequestDTO;
import com.ugurxaslan.profit_tracker_backend.dto.response.TransactionResponseDTO;
import com.ugurxaslan.profit_tracker_backend.enums.TransactionType;
import com.ugurxaslan.profit_tracker_backend.mapper.TransactionMapper;
import com.ugurxaslan.profit_tracker_backend.model.Transaction;
import com.ugurxaslan.profit_tracker_backend.repository.TransactionRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByWalletId(@NonNull Long walletId) {
        return transactionRepository.findByWallet_IdOrderByTransactionDateDesc(walletId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByWalletIdAndAssetId(@NonNull Long walletId, @NonNull Long assetId) {
        return transactionRepository.findByWallet_IdAndAsset_IdOrderByTransactionDateDesc(walletId, assetId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByWalletIdAndAssetIdAndType(
            @NonNull Long walletId,
            @NonNull Long assetId,
            @NonNull TransactionType transactionType) {
        return transactionRepository.findByWallet_IdAndAsset_IdAndTransactionTypeOrderByTransactionDateDesc(
                walletId,
                assetId,
                transactionType.name());
    }

    @Transactional(readOnly = true)
    public Transaction getTransactionEntityById(@NonNull Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponseDTO> getTransactionsByFilter(@NonNull Long walletId,
            @NonNull TransactionFilterRequestDTO filter,
            @NonNull Pageable pageable) {
        Specification<Transaction> specification = buildTransactionSpecification(walletId, filter);
        return transactionRepository.findAll(specification, pageable)
                .map(transactionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransaction(@NonNull Long transactionId) {
        Transaction transaction = getTransactionEntityById(transactionId);
        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public Transaction createTransaction(@NonNull Transaction transaction) {

        return transactionRepository.save(Objects.requireNonNull(transaction));
    }

    //
    private Specification<Transaction> buildTransactionSpecification(@NonNull Long walletId,
            @NonNull TransactionFilterRequestDTO filter) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.equal(root.get("wallet").get("id"), walletId);

            if (filter.getAssetSymbol() != null && !filter.getAssetSymbol().isBlank()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.upper(root.get("asset").get("symbol")),
                                filter.getAssetSymbol().trim().toUpperCase()));
            }

            if (filter.getTransactionType() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("transactionType"), filter.getTransactionType()));
            }

            if (filter.getFromDate() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), filter.getFromDate()));
            }

            if (filter.getToDate() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), filter.getToDate()));
            }

            return predicate;
        };
    }
}
