package dev.java10x.BibliotecaInteligente.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.java10x.BibliotecaInteligente.exception.NaoEncontradoException;
import dev.java10x.BibliotecaInteligente.exception.RegraNegocioException;
import dev.java10x.BibliotecaInteligente.model.Usuario;
import dev.java10x.BibliotecaInteligente.repository.EmprestimoRepository;
import dev.java10x.BibliotecaInteligente.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, EmprestimoRepository emprestimoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll(Sort.by("nome"));
    }

    /** Busca por nome ou e-mail. Sem termo, lista todos. */
    @Transactional(readOnly = true)
    public List<Usuario> buscar(String termo) {
        if (termo == null || termo.isBlank()) {
            return listarTodos();
        }
        return usuarioRepository.buscar(termo.trim());
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado."));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado."));
    }

    @Transactional
    public Usuario salvar(Usuario usuario) {
        usuario.setId(null); // cadastro nunca sobrescreve um usuário existente
        usuario.setEmail(usuario.getEmail().trim().toLowerCase());

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RegraNegocioException("E-mail já cadastrado.");
        }
        if (usuarioRepository.existsByTelefone(usuario.getTelefone())) {
            throw new RegraNegocioException("Telefone já cadastrado.");
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizar(Long id, Usuario dados) {
        Usuario usuario = buscarPorId(id);
        String email = dados.getEmail().trim().toLowerCase();

        if (!usuario.getEmail().equals(email) && usuarioRepository.existsByEmail(email)) {
            throw new RegraNegocioException("E-mail já cadastrado.");
        }
        if (!usuario.getTelefone().equals(dados.getTelefone())
                && usuarioRepository.existsByTelefone(dados.getTelefone())) {
            throw new RegraNegocioException("Telefone já cadastrado.");
        }

        usuario.setNome(dados.getNome());
        usuario.setEmail(email);
        usuario.setTelefone(dados.getTelefone());
        usuario.setDataMatricula(dados.getDataMatricula());

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = buscarPorId(id);

        if (emprestimoRepository.existsByUsuarioId(id)) {
            throw new RegraNegocioException("Este usuário tem histórico de empréstimos e não pode ser excluído.");
        }

        usuarioRepository.delete(usuario);
    }
}
