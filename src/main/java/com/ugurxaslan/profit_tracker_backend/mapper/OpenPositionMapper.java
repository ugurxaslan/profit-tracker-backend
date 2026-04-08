package com.ugurxaslan.profit_tracker_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.ugurxaslan.profit_tracker_backend.dto.response.OpenPositionResponseDTO;
import com.ugurxaslan.profit_tracker_backend.model.OpenPosition;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OpenPositionMapper {

    @Mapping(target = "walletId", source = "walletAsset.wallet.id")
    @Mapping(target = "walletAssetId", source = "walletAsset.id")
    @Mapping(target = "transactionId", source = "transaction.id")
    @Mapping(target = "assetSymbol", source = "asset.symbol")
    @Mapping(target = "transactionDate", source = "transaction.transactionDate")
    OpenPositionResponseDTO toResponse(OpenPosition openPosition);
}
