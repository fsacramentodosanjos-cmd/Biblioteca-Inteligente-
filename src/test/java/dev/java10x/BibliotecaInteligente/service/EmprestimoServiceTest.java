package dev.java10x.BibliotecaInteligente.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import dev.java10x.BibliotecaInteligente.config.BibliotecaProperties;
import dev.java10x.BibliotecaInteligente.dto.EmprestimoRequest;
import dev.java10x.BibliotecaInteligente.dto.EmprestimoResponse;
import dev.java10x.BibliotecaInteligente.enums.SituacaoEmprestimo;
import dev.java10x.BibliotecaInteligente.enums.StatusLivros;
import dev.java10x.BibliotecaInteligente.exception.RegraNegocioException;
import dev.java10x.BibliotecaInteligente.model.Emprestimo;
import dev.java10x.BibliotecaInteligente.model.Livro;
import dev.java10x.BibliotecaInteligente.model.Usuario;
import dev.java10x.BibliotecaInteligente.repository.EmprestimoRepository;
import dev.java10x.BibliotecaInteligente.repository.LivroRepository;
import dev.java10x.BibliotecaInteligente.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmprestimoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 13);

    @Mock
    private EmprestimoRepository emprestimoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private LivroRepository livroRepository;

    private EmprestimoService service;
    private Usuario usuario;
    private Livro livro;

    @BeforeEach
    void preparar() {
        // Regras padrão: prazo de 14 dias, limite de 3 livros, 2 renovações, multa de R$ 0,50 por dia
        BibliotecaProperties regras = new BibliotecaProperties();
        ZoneId zona = ZoneId.of("UTC");
        Clock relogioFixo = Clock.fixed(HOJE.atStartOfDay(zona).toInstant(), zona);
        service = new EmprestimoService(emprestimoRepository, usuarioRepository, livroRepository, regras, relogioFixo);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Ana Souza");

        livro = new Livro();
        livro.setId(10L);
        livro.setTitulo("Dom Casmurro");
        livro.setStatus(StatusLivros.DISPONIVEL);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(livroRepository.findWithLockById(10L)).thenReturn(Optional.of(livro));
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(i -> i.getArgument(0));
        when(livroRepository.save(any(Livro.class))).thenAnswer(i -> i.getArgument(0));
    }

    // ---------- realizar empréstimo ----------

    @Test
    void registraEmprestimoComPrazoPadrao() {
        EmprestimoResponse resposta = service.realizarEmprestimo(new EmprestimoRequest(1L, 10L, null));

        assertThat(resposta.dataEmprestimo()).isEqualTo(HOJE);
        assertThat(resposta.dataDevolucaoPrevista()).isEqualTo(HOJE.plusDays(14));
        assertThat(resposta.situacao()).isEqualTo(SituacaoEmprestimo.EM_ABERTO);
        assertThat(livro.getStatus()).isEqualTo(StatusLivros.EMPRESTADO);
    }

    @Test
    void usaADataDeDevolucaoInformada() {
        LocalDate data = HOJE.plusDays(7);

        EmprestimoResponse resposta = service.realizarEmprestimo(new EmprestimoRequest(1L, 10L, data));

        assertThat(resposta.dataDevolucaoPrevista()).isEqualTo(data);
    }

    @Test
    void recusaLivroJaEmprestado() {
        livro.setStatus(StatusLivros.EMPRESTADO);

        assertThatThrownBy(() -> service.realizarEmprestimo(new EmprestimoRequest(1L, 10L, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já está emprestado");
    }

    @Test
    void recusaLivroPerdido() {
        livro.setStatus(StatusLivros.PERDIDO);

        assertThatThrownBy(() -> service.realizarEmprestimo(new EmprestimoRequest(1L, 10L, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("perdido");
    }

    @Test
    void bloqueiaUsuarioComEmprestimoEmAtraso() {
        when(emprestimoRepository.existsByUsuarioIdAndDataDevolucaoRealIsNullAndDataDevolucaoPrevistaBefore(1L, HOJE))
                .thenReturn(true);

        assertThatThrownBy(() -> service.realizarEmprestimo(new EmprestimoRequest(1L, 10L, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("em atraso");
    }

    @Test
    void respeitaOLimiteDeLivrosPorUsuario() {
        when(emprestimoRepository.countByUsuarioIdAndDataDevolucaoRealIsNull(1L)).thenReturn(3L);

        assertThatThrownBy(() -> service.realizarEmprestimo(new EmprestimoRequest(1L, 10L, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Limite de 3");
    }

    @Test
    void recusaDevolucaoPrevistaAnteriorAHoje() {
        assertThatThrownBy(() -> service.realizarEmprestimo(new EmprestimoRequest(1L, 10L, HOJE.minusDays(1))))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("anterior a hoje");
    }

    // ---------- devolução ----------

    @Test
    void devolucaoNoPrazoLiberaOLivroSemMulta() {
        emprestimoEmAberto(HOJE.plusDays(3), 0);

        EmprestimoResponse resposta = service.devolverLivro(5L);

        assertThat(resposta.situacao()).isEqualTo(SituacaoEmprestimo.DEVOLVIDO);
        assertThat(resposta.diasAtraso()).isZero();
        assertThat(resposta.multa()).isEqualByComparingTo("0.00");
        assertThat(livro.getStatus()).isEqualTo(StatusLivros.DISPONIVEL);
    }

    @Test
    void devolucaoAtrasadaCobraMultaPorDia() {
        emprestimoEmAberto(HOJE.minusDays(5), 0);

        EmprestimoResponse resposta = service.devolverLivro(5L);

        assertThat(resposta.diasAtraso()).isEqualTo(5);
        assertThat(resposta.multa()).isEqualByComparingTo("2.50");
    }

    @Test
    void naoDevolveOMesmoEmprestimoDuasVezes() {
        Emprestimo emprestimo = emprestimoEmAberto(HOJE.plusDays(3), 0);
        emprestimo.setDataDevolucaoReal(HOJE.minusDays(1));

        assertThatThrownBy(() -> service.devolverLivro(5L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já foi devolvido");
    }

    // ---------- renovação ----------

    @Test
    void renovacaoEstendeOPrazoEContaARenovacao() {
        emprestimoEmAberto(HOJE.plusDays(2), 0);

        EmprestimoResponse resposta = service.renovar(5L);

        assertThat(resposta.dataDevolucaoPrevista()).isEqualTo(HOJE.plusDays(2 + 14));
        assertThat(resposta.renovacoes()).isEqualTo(1);
    }

    @Test
    void naoRenovaEmprestimoEmAtraso() {
        emprestimoEmAberto(HOJE.minusDays(1), 0);

        assertThatThrownBy(() -> service.renovar(5L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("atraso");
    }

    @Test
    void naoRenovaAlemDoLimite() {
        emprestimoEmAberto(HOJE.plusDays(2), 2);

        assertThatThrownBy(() -> service.renovar(5L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Limite de 2 renovações");
    }

    private Emprestimo emprestimoEmAberto(LocalDate prevista, int renovacoes) {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(5L);
        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);
        emprestimo.setDataEmprestimo(HOJE.minusDays(10));
        emprestimo.setDataDevolucaoPrevista(prevista);
        emprestimo.setRenovacoes(renovacoes);
        livro.setStatus(StatusLivros.EMPRESTADO);
        when(emprestimoRepository.findById(5L)).thenReturn(Optional.of(emprestimo));
        return emprestimo;
    }
}
