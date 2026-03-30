package com.ugurxaslan.profit_tracker_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ugurxaslan.profit_tracker_backend.model.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findAllByUser_Username(String username);

    Optional<Wallet> findByIdAndUser_Username(Long walletId, String username);

    Optional<Wallet> findByUser_UsernameAndWalletName(String username, String walletName);

    boolean existsByUser_UsernameAndWalletName(String username, String walletName);

    boolean existsByIdAndUser_Username(Long walletId, String username);
}
