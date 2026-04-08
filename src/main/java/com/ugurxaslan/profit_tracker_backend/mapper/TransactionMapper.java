package com.ugurxaslan.profit_tracker_backend.mapper;

import com.ugurxaslan.profit_tracker_backend.dto.response.TransactionResponseDTO;
import com.ugurxaslan.profit_tracker_backend.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {

        @Mapping(target = "walletId", source = "wallet.id")
        @Mapping(target = "assetSymbol", source = "asset.symbol")
        TransactionResponseDTO toResponse(Transaction transaction);
}
