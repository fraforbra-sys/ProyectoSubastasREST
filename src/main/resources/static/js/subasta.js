import { API } from './api.js';
import { requireAuth } from './auth.js';

const fmt = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' });
const dateFmt = new Intl.DateTimeFormat('es-ES', { dateStyle: 'medium', timeStyle: 'short' });

function formatTime(sec) {
  if (sec <= 0) return '00:00';
  const m = Math.floor(sec / 60).toString().padStart(2, '0');
  const s = (sec % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
}

let currentUser;
let subastaId;
let refreshInterval;
let countdownInterval;
let tiempoRestante = 0;
let subastaActiva = true;

function showPujaAlert(msg, type = 'danger') {
  document.getElementById('pujaAlert').innerHTML = `
    <div class="alert alert-${type} alert-dismissible fade show">
      ${msg}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>`;
}

function renderSubasta(s) {
  const img = s.articulo.urlImagen || 'https://placehold.co/600x400?text=Sin+imagen';
  document.getElementById('artImg').src = img;
  document.getElementById('artImg').alt = s.articulo.nombre;
  document.getElementById('artNombre').textContent = s.articulo.nombre;
  document.getElementById('artDesc').textContent = s.articulo.descripcion || '';
  document.getElementById('precioActual').textContent = fmt.format(s.precioActual);
  document.getElementById('liderActual').textContent = s.liderActual || 'Nadie';
  document.getElementById('numPujas').textContent = s.pujas?.length || 0;

  tiempoRestante = s.tiempoRestanteSegundos ?? 0;
  subastaActiva = s.activa;

  const tiempoEl = document.getElementById('tiempoRestante');
  const tiempoBadge = document.getElementById('tiempoBadge');
  tiempoEl.textContent = formatTime(tiempoRestante);
  tiempoBadge.className = `badge fs-6 ${tiempoRestante < 30 ? 'bg-danger' : 'bg-success'}`;

  const pujas = [...(s.pujas || [])].reverse();
  const tbody = document.getElementById('pujasBody');
  if (pujas.length === 0) {
    tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted fst-italic">Sin pujas aún</td></tr>';
  } else {
    tbody.innerHTML = pujas.map(p => `
      <tr>
        <td><i class="bi bi-person text-primary"></i> ${p.usuario}</td>
        <td class="fw-bold text-primary">${fmt.format(p.cantidad)}</td>
        <td class="text-muted small">${p.timestamp ? dateFmt.format(new Date(p.timestamp)) : '-'}</td>
      </tr>`).join('');
  }

  const badgeActiva = document.getElementById('badgeActiva');
  const formPuja = document.getElementById('formPuja');
  const btnFinalizar = document.getElementById('btnFinalizar');
  const alertFinalizada = document.getElementById('alerta-finalizada');

  if (s.activa) {
    badgeActiva.textContent = 'Activa';
    badgeActiva.className = 'badge bg-success fs-6';
    formPuja.classList.remove('d-none');
    btnFinalizar.classList.remove('d-none');
    alertFinalizada.classList.add('d-none');
  } else {
    badgeActiva.textContent = 'Finalizada';
    badgeActiva.className = 'badge bg-secondary fs-6';
    formPuja.classList.add('d-none');
    btnFinalizar.classList.add('d-none');
    alertFinalizada.classList.remove('d-none');
    clearInterval(countdownInterval);
    clearInterval(refreshInterval);
  }
}

async function loadSubasta() {
  const res = await API.obtenerSubasta(subastaId);
  if (!res.ok) {
    document.getElementById('mainContent').innerHTML =
      '<div class="alert alert-danger mt-4"><i class="bi bi-exclamation-triangle"></i> Subasta no encontrada.</div>';
    return;
  }
  renderSubasta(res.data);
}

function startCountdown() {
  clearInterval(countdownInterval);
  countdownInterval = setInterval(() => {
    if (!subastaActiva) { clearInterval(countdownInterval); return; }
    tiempoRestante = Math.max(0, tiempoRestante - 1);
    document.getElementById('tiempoRestante').textContent = formatTime(tiempoRestante);
    document.getElementById('tiempoBadge').className =
      `badge fs-6 ${tiempoRestante < 30 ? 'bg-danger' : 'bg-success'}`;
  }, 1000);
}

async function hacerPuja() {
  const input = document.getElementById('cantidadPuja');
  const cantidad = parseFloat(input.value);
  if (isNaN(cantidad) || cantidad <= 0) {
    showPujaAlert('Introduce una cantidad válida y mayor que 0.');
    return;
  }

  const res = await API.pujar(subastaId, currentUser, cantidad);
  if (res.ok) {
    input.value = '';
    showPujaAlert('¡Puja realizada con éxito!', 'success');
    loadSubasta();
  } else {
    const msg = (typeof res.data === 'object' && res.data?.message)
      || 'La cantidad debe ser mayor al precio actual.';
    showPujaAlert(msg);
  }
}

async function finalizarSubasta() {
  if (!confirm('¿Finalizar esta subasta? Esta acción no se puede deshacer.')) return;
  const res = await API.finalizarSubasta(subastaId);
  if (res.ok) {
    showPujaAlert('Subasta finalizada correctamente.', 'success');
    loadSubasta();
  } else {
    showPujaAlert((typeof res.data === 'object' && res.data?.message) || 'Error al finalizar la subasta.');
  }
}

function init() {
  currentUser = requireAuth();
  if (!currentUser) return;

  document.getElementById('navUser').textContent = currentUser;

  const params = new URLSearchParams(window.location.search);
  subastaId = params.get('id');
  if (!subastaId) { window.location.href = '/subastas.html'; return; }

  document.getElementById('btnPujar').addEventListener('click', hacerPuja);
  document.getElementById('cantidadPuja').addEventListener('keydown', (e) => {
    if (e.key === 'Enter') hacerPuja();
  });
  document.getElementById('btnFinalizar').addEventListener('click', finalizarSubasta);

  loadSubasta().then(() => {
    if (subastaActiva) startCountdown();
  });
  refreshInterval = setInterval(loadSubasta, 3000);
}

init();
