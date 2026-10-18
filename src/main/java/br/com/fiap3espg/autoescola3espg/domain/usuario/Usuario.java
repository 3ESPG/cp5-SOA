package br.com.fiap3espg.autoescola3espg.domain.usuario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity(name = "Usuario")
@Table(name = "usuarios")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String login;
    private String senha;

    @Enumerated(EnumType.STRING)
    private Role perfil;

    /** Cria usuário já com a senha codificada (nunca em texto puro). */
    public Usuario(String login, String senhaCodificada, Role perfil) {
        this.login = login;
        this.senha = senhaCodificada;
        this.perfil = perfil;
    }

    public void atualizarInformacoes(String novoLogin, String novaSenhaCodificada, Role novoPerfil) {
        if (novoLogin != null && !novoLogin.isBlank()) {
            this.login = novoLogin;
        }
        if (novaSenhaCodificada != null && !novaSenhaCodificada.isBlank()) {
            this.senha = novaSenhaCodificada;
        }
        if (novoPerfil != null) {
            this.perfil = novoPerfil;
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
