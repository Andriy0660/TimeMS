package com.example.timecraft.domain.user.persistence;

import java.util.ArrayList;
import java.util.List;

import com.example.timecraft.domain.multitenant.persistence.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @ToString.Include
  private Long id;

  @Column(name = "email")
  @ToString.Include
  private String email;

  @Column(name = "password")
  @ToString.Include
  private String password;

  @Column(name = "first_name")
  @ToString.Include
  private String firstName;

  @Column(name = "last_name")
  @ToString.Include
  private String lastName;

  @Column(name = "access_token")
  @ToString.Include
  private String accessToken;

  @ManyToMany
  @JoinTable(name = "user_tenant", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "tenant_id"))
  @Builder.Default
  private List<TenantEntity> tenants = new ArrayList<>();
}
