import { API } from './api.js';

export const getCurrentUser = () => sessionStorage.getItem('currentUser');
export const setCurrentUser = (u) => sessionStorage.setItem('currentUser', u);
export const logout = () => { sessionStorage.removeItem('currentUser'); window.location.href = '/index.html'; };

export function requireAuth() {
  const u = getCurrentUser();
  if (!u) { window.location.href = '/index.html'; return null; }
  return u;
}

function showAlert(containerId, msg, type = 'danger') {
  document.getElementById(containerId).innerHTML = `
    <div class="alert alert-${type} alert-dismissible fade show" role="alert">
      ${msg}
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>`;
}

export function initLoginPage() {
  document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('loginUser').value.trim();
    const password = document.getElementById('loginPass').value;
    if (!username || !password) { showAlert('loginAlert', 'Completa todos los campos.'); return; }

    const res = await API.login(username, password);
    if (res.ok) {
      setCurrentUser(username);
      window.location.href = '/subastas.html';
    } else {
      const msg = (typeof res.data === 'object' && res.data?.message)
        || (res.status === 401 ? 'Credenciales incorrectas.' : 'Error al iniciar sesión.');
      showAlert('loginAlert', msg);
    }
  });

  document.getElementById('regForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('regUser').value.trim();
    const password = document.getElementById('regPass').value;
    if (!username || !password) { showAlert('regAlert', 'Completa todos los campos.'); return; }

    const res = await API.registro(username, password);
    if (res.ok || res.status === 201) {
      showAlert('regAlert', '¡Registro exitoso! Ahora puedes iniciar sesión.', 'success');
      document.getElementById('regForm').reset();
    } else {
      const msg = (typeof res.data === 'object' && res.data?.message)
        || (res.status === 409 ? 'El usuario ya existe.' : 'Error al registrarse.');
      showAlert('regAlert', msg);
    }
  });
}
