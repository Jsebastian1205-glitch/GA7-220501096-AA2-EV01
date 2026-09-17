// Muestra u oculta el modal de inicio de sesión.
function toggleLogin(show) {
    const modal = document.getElementById('login-modal');

    if (show) {
        modal.classList.add('active'); // Activa la vista del modal.
    } else {
        modal.classList.remove('active'); // Lo cierra al quitar la clase.
    }
}

// Cierra el modal si el usuario hace clic fuera de su contenido.
document.getElementById('login-modal').addEventListener('click', function(e) {
    if (e.target === this) {
        toggleLogin(false);
    }
});