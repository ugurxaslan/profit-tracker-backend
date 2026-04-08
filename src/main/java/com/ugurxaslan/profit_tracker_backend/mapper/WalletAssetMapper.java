package com.ugurxaslan.profit_tracker_backend.mapper;

import com.ugurxaslan.profit_tracker_backend.dto.response.WalletAssetResponseDTO;
import com.ugurxaslan.profit_tracker_backend.model.WalletAsset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WalletAssetMapper {

    @Mapping(target = "assetSymbol", source = "asset.symbol")
    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "assetId", source = "asset.id")
    WalletAssetResponseDTO toResponse(WalletAsset walletAsset);
}
