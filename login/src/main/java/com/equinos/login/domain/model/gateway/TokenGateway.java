package com.equinos.login.domain.model.gateway;

import com.equinos.login.domain.model.TokenEmitido;
import com.equinos.login.domain.model.Usuario;

import java.time.Instant;


public interface TokenGateway {

    TokenEmitido emitir(Usuario usuario, Instant ahora);
}
