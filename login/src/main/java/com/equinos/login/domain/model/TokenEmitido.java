package com.equinos.login.domain.model;

import java.time.Instant;


public record TokenEmitido(String token, Instant expiraEn) {
}
