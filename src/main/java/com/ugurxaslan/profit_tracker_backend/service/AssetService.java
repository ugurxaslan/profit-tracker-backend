package com.ugurxaslan.profit_tracker_backend.service;

import com.ugurxaslan.profit_tracker_backend.dto.response.AssetResponseDTO;
import com.ugurxaslan.profit_tracker_backend.mapper.AssetMapper;
import com.ugurxaslan.profit_tracker_backend.model.Asset;
import com.ugurxaslan.profit_tracker_backend.repository.AssetRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;

    @Transactional(readOnly = true)
    public AssetResponseDTO getAssetBySymbol(@NonNull String symbol) {
        Asset asset = assetRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));

        return assetMapper.toResponse(asset);
    }

    @Transactional(readOnly = true)
    public List<AssetResponseDTO> getAllAssets() {
        return assetRepository.findAll()
                .stream()
                .map(assetMapper::toResponse)
                .toList();
    }

    public void upsertAssets(@NonNull List<MarketAssetItem> marketItems) {
        for (MarketAssetItem item : marketItems) {
            if (item == null || item.symbol() == null || item.symbol().isBlank() || item.currentPrice() == null) {
                continue;
            }

            Asset asset = assetRepository.findBySymbol(item.symbol())
                    .orElseGet(() -> {
                        Asset createdAsset = new Asset();
                        createdAsset
                                .setName(item.name() == null || item.name().isBlank() ? item.symbol() : item.name());
                        createdAsset.setSymbol(item.symbol());
                        return createdAsset;
                    });

            asset.setCurrentPrice(item.currentPrice());
            assetRepository.save(asset);
        }
    }

    public record MarketAssetItem(String name, String symbol, BigDecimal currentPrice) {
    }
}
