package com.ugurxaslan.profit_tracker_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.ugurxaslan.profit_tracker_backend.dto.response.ClosedPositionResponseDTO;
import com.ugurxaslan.profit_tracker_backend.model.ClosedPosition;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClosedPositionMapper {

    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "buyTransactionId", source = "buyTransaction.id")
    @Mapping(target = "sellTransactionId", source = "sellTransaction.id")
    @Mapping(target = "assetSymbol", source = "sellTransaction.asset.symbol")
    @Mapping(target = "sellTransactionDate", source = "sellTransaction.transactionDate")
    ClosedPositionResponseDTO toResponse(ClosedPosition closedPosition);
}
