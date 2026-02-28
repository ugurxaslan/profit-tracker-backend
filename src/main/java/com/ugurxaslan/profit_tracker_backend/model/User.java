package com.ugurxaslan.profit_tracker_backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ugurxaslan.profit_tracker_backend.enums.UserRole;
import com.ugurxaslan.profit_tracker_backend.enums.UserStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter 
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(name = "email", nullable = false, unique = true)
    @Email(message = "Email should be valid")
    private String email;

    @Builder.Default//burda gerek yok ama kalsın
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = UserRole.class)
    @CollectionTable(
        name = "user_roles", 
        joinColumns = @JoinColumn(name = "user_id", nullable = false))
    @Column(name = "role_name", nullable = false )
    private Set<UserRole> roles = new HashSet<>(Set.of(UserRole.USER));

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.INACTIVE;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    //Relationships
    
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Wallet> walletList = new ArrayList<>();

    @PrePersist
    public void prePersist() {

        //Default wallet oluşturulması
        if (this.walletList.isEmpty()) {
            
            Wallet defaultWallet = Wallet.builder()
                .user(this)
                .build();
            this.walletList.add(defaultWallet);
    
        }
    }
}
