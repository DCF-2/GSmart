document.addEventListener('DOMContentLoaded', function() {
    // Animação suave para links de navegação internos
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            document.querySelector(this.getAttribute('href')).scrollIntoView({
                behavior: 'smooth'
            });
        });
    });

    // Adiciona uma sombra ao header quando o usuário rola a página
    const header = document.querySelector('header');
    window.addEventListener('scroll', () => {
        if (window.scrollY > 20) {
            header.style.backgroundColor = 'rgba(26, 26, 26, 0.95)';
        } else {
            header.style.backgroundColor = 'rgba(26, 26, 26, 0.85)';
        }
    });
});

window.onload = function() {
    const preloader = document.getElementById('preloader');
    const siteContent = document.getElementById('site-content');

    // 1. Adiciona a classe para iniciar o 'fade-out'
    preloader.classList.add('fade-out');

    // 2. Após a transição do fade-out terminar, esconde o preloader
    //    e mostra o conteúdo do site para evitar que ele bloqueie cliques.
    setTimeout(() => {
        preloader.classList.add('hidden');
        siteContent.classList.remove('hidden');
    }, 5750); // O tempo (750ms) deve ser igual à duração da transição no CSS
};