package dev.java10x.BibliotecaInteligente.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Regras da biblioteca, ajustáveis em application.properties (prefixo "biblioteca"). */
@Component
@ConfigurationProperties(prefix = "biblioteca")
public class BibliotecaProperties {

    private int prazoPadraoDias = 14;
    private int limiteEmprestimos = 3;
    private int maxRenovacoes = 2;
    private BigDecimal multaPorDia = new BigDecimal("0.50");

    public int getPrazoPadraoDias() {
        return prazoPadraoDias;
    }

    public void setPrazoPadraoDias(int prazoPadraoDias) {
        this.prazoPadraoDias = prazoPadraoDias;
    }

    public int getLimiteEmprestimos() {
        return limiteEmprestimos;
    }

    public void setLimiteEmprestimos(int limiteEmprestimos) {
        this.limiteEmprestimos = limiteEmprestimos;
    }

    public int getMaxRenovacoes() {
        return maxRenovacoes;
    }

    public void setMaxRenovacoes(int maxRenovacoes) {
        this.maxRenovacoes = maxRenovacoes;
    }

    public BigDecimal getMultaPorDia() {
        return multaPorDia;
    }

    public void setMultaPorDia(BigDecimal multaPorDia) {
        this.multaPorDia = multaPorDia;
    }
}
