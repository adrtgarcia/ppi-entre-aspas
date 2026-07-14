// Entre Aspas — utilitários de interface (modais renderizados no servidor,
// sem chamadas assíncronas — toda a interatividade de dados é feita por
// recarregamento de página via formulários/JSP).

function abrirModal(id) {
    var overlay = document.getElementById(id);
    if (overlay) overlay.classList.add('aberto');
}

function fecharModal(id) {
    var overlay = document.getElementById(id);
    if (overlay) overlay.classList.remove('aberto');
}

// fecha ao clicar fora do card do modal
document.addEventListener('click', function (e) {
    if (e.target.classList && e.target.classList.contains('overlay')) {
        e.target.classList.remove('aberto');
    }
});

// fecha com a tecla ESC
document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
        document.querySelectorAll('.overlay.aberto').forEach(function (o) {
            o.classList.remove('aberto');
        });
    }
});

function confirmarRemocao(mensagem) {
    return confirm(mensagem || 'Tem certeza que deseja remover este item?');
}
