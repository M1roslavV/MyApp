package com.mypropertyapp.user;

import com.mypropertyapp.company.Company;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user")
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    private boolean isEnabled;
    @Column(name = "is_account_non_expired")
    private boolean accountNonExpired;
    @Column(name = "is_account_non_locked")
    private boolean accountNonLocked;
    @Column(name = "is_credentials_non_expired")
    private boolean credentialsNonExpired;
}
