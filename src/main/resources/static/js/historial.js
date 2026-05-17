import { API } from './api.js';
import { requireAuth, logout } from './auth.js';

const fmt = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' });
const dateFmt = new Intl.DateTimeFormat('es-ES', { dateStyle: 'long', timeStyle: 'short' });

let currentUser;
let allHistorial = [];

function setActiveFilter(mine) {
  document.getElementById('btnVerTodas').classList.toggle('btn-primary', !mine);
  document.getElementById('btnVerTodas').classList.toggle('btn-outline-primary', mine);
  document.getElementById('btnVerMias').classList.toggle('btn-primary', mine);
  document.getElementById('btnVerMias').classList.toggle('btn-outline-primary', !mine);
}

function showDetailModal(h) {
  document.getElementById('modalImg').src = h.foto || 'https://placehold.co/500x350?text=Sin+imagen';
  document.getElementById('modalImg').alt = h.nombreArticulo;
  document.getElementById('modalNombre').textContent = h.nombreArticulo;
  document.getElementById('modalPrecio').textContent = fmt.format(h.precioFinal);
  document.getElementById('modalComprador').textContent = h.comprador;
  document.getElementById('modalFecha').textContent = h.fechaFinalizacion
    ? dateFmt.format(new Date(h.fechaFinalizacion)) : '-';
  document.getElementById('modalId').textContent = h.idSubasta;
  new bootstrap.Modal(document.getElementById('detalleModal')).show();
}

function renderTable(data) {
  const tbody = document.getElementById('historialBody');
  const empty = document.getElementById('emptyState');

  if (data.length === 0) {
    tbody.innerHTML = '';
    empty.classList.remove('d-none');
    return;
  }

  empty.classList.add('d-none');
  tbody.innerHTML = data.map(h => `
    <tr class="clickable-row" data-id="${h.idSubasta}" style="cursor:pointer;">
      <td>
        <img src="${h.foto || 'https://placehold.co/60x45?text=N/A'}"
             width="60" height="45" style="object-fit:cover;border-radius:4px;"
             alt="${h.nombreArticulo}"
             onerror="this.src='https://placehold.co/60x45?text=N/A'">
      </td>
      <td class="fw-semibold">${h.nombreArticulo}</td>
      <td class="fw-bold text-primary">${fmt.format(h.precioFinal)}</td>
      <td><i class="bi bi-person-check text-success"></i> ${h.comprador}</td>
      <td class="text-muted small">${h.fechaFinalizacion ? dateFmt.format(new Date(h.fechaFinalizacion)) : '-'}</td>
    </tr>`).join('');

  tbody.querySelectorAll('.clickable-row').forEach(row => {
    row.addEventListener('click', () => {
      const item = allHistorial.find(h => h.idSubasta === row.dataset.id);
      if (item) showDetailModal(item);
    });
  });
}

async function loadHistorial(mine = false) {
  const tbody = document.getElementById('historialBody');
  tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4">
    <div class="spinner-border text-primary" role="status"></div>
  </td></tr>`;

  const res = mine
    ? await API.historialPorComprador(currentUser)
    : await API.listarHistorial();

  if (!res.ok) {
    tbody.innerHTML = '<tr><td colspan="5" class="text-center text-danger">Error al cargar historial.</td></tr>';
    return;
  }

  allHistorial = Array.isArray(res.data) ? res.data : [];
  renderTable(allHistorial);
}

async function updateCount() {
  const res = await API.contarHistorial();
  if (res.ok) {
    const count = typeof res.data === 'number'
      ? res.data
      : (typeof res.data === 'string' ? parseInt(res.data) : allHistorial.length);
    document.getElementById('totalCount').textContent = isNaN(count) ? allHistorial.length : count;
  }
}

function init() {
  currentUser = requireAuth();
  if (!currentUser) return;

  document.getElementById('navUser').textContent = currentUser;
  document.getElementById('btnLogout').addEventListener('click', logout);

  document.getElementById('btnVerTodas').addEventListener('click', () => {
    setActiveFilter(false);
    loadHistorial(false).then(updateCount);
  });

  document.getElementById('btnVerMias').addEventListener('click', () => {
    setActiveFilter(true);
    loadHistorial(true);
  });

  loadHistorial(false).then(updateCount);
}

init();
