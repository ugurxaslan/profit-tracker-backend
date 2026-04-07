package com.ugurxaslan.profit_tracker_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ugurxaslan.profit_tracker_backend.service.entityService.AssetService;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {

    private final AssetService assetService;

    @Scheduled(fixedDelayString = "${asset.sync.fixed-delay-ms:300000}", initialDelayString = "${asset.sync.initial-delay-ms:15000}")
    @Transactional
    public void syncAssets() {
        List<AssetService.MarketAssetItem> marketItems = fetchMarketAssets();

        if (marketItems.isEmpty()) {
            log.warn("Asset sync skipped: no market data received");
            return;
        }

        assetService.upsertAssets(marketItems);
        log.info("Asset sync completed. Processed {} asset(s)", marketItems.size());
    }

    public List<AssetService.MarketAssetItem> fetchMarketAssets() {
        String goldPriceString = null;
        String silverPriceString = null;

        try {
            Document doc = Jsoup.connect("https://altin.doviz.com/")
                    .userAgent("Mozilla/5.0")
                    .get();

            Element gramAltinRow = doc.selectFirst("tr:has(div:contains(Gram Altın))");
            goldPriceString = gramAltinRow.selectFirst("td[data-socket-attr=ask]").text();

            Element gumusRow = doc.selectFirst("tr:has(div:contains(Gram Gümüş))");
            silverPriceString = gumusRow.selectFirst("td[data-socket-attr=ask]").text();

        } catch (Exception e) {
            log.error("Market data fetch failed ", e);
        }

        BigDecimal goldPrice = parsePrice(goldPriceString);
        BigDecimal silverPrice = parsePrice(silverPriceString);

        List<AssetService.MarketAssetItem> items = new ArrayList<>();

        if (goldPrice != null) {
            items.add(new AssetService.MarketAssetItem("Gram Altın", "XAUTRYG", goldPrice));
        }

        if (silverPrice != null) {
            items.add(new AssetService.MarketAssetItem("Gram Gümüş", "XAGTRYG", silverPrice));
        }

        items.add(new AssetService.MarketAssetItem("Türk Lirası", "TRY", new BigDecimal("1.00")));

        log.info("Fetched market data: {} item(s)", items);

        return items;
    }

    private BigDecimal parsePrice(String rawPrice) {
        String normalized = rawPrice
                .replace(".", "")
                .replace(",", ".");

        return new BigDecimal(normalized);
    }
}
