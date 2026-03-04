package com.ugurxaslan.profit_tracker_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ugurxaslan.profit_tracker_backend.model.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findByUser_Id(Long userId);

    Optional<Wallet> findByUser_IdAndWalletName(Long userId, String walletName);

    boolean existsByUser_IdAndWalletName(Long userId, String walletName);
}
