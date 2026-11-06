(function () {
    'use strict';

    // Alternar tema claro/escuro (a escolha fica salva no navegador)
    var botaoTema = document.querySelector('[data-alternar-tema]');
    if (botaoTema) {
        botaoTema.addEventListener('click', function () {
            var atual = document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'dark' : 'light';
            var novo = atual === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-bs-theme', novo);
            try { localStorage.setItem('bib-tema', novo); } catch (e) { /* ignora */ }
        });
    }

    // Pede confirmação antes de enviar formulários marcados com data-confirmar
    document.querySelectorAll('form[data-confirmar]').forEach(function (form) {
        form.addEventListener('submit', function (evento) {
            if (!window.confirm(form.getAttribute('data-confirmar'))) {
                evento.preventDefault();
            }
        });
    });

    // Campos de telefone aceitam só dígitos
    document.querySelectorAll('input[data-somente-numeros]').forEach(function (campo) {
        campo.addEventListener('input', function () {
            campo.value = campo.value.replace(/\D/g, '');
        });
    });
})();
