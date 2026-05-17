import { API } from './api.js';
import { requireAuth, logout } from './auth.js';

const fmt = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' });

function formatTime(sec) {
  if (sec <= 0) return '00:00';
  const m = Math.floor(sec / 60).toString().padStart(2, '0');
  const s = (sec % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
}

let activeSearch = null;
let refreshInterval;

function renderCard(s) {
  const img = s.articulo.urlImagen || 'https://placehold.co/400x200?text=Sin+imagen';
  const urgente = s.tiempoRestanteSegundos < 30;
  return `
    <div class="col fade-in">
      <div class="card h-100 subasta-card shadow-sm">
        <img src="${img}" class="card-img-top" alt="${s.articulo.nombre}"
             style="height:180px;object-fit:cover;" onerror="this.src='https://placehold.co/400x200?text=Sin+imagen'">
        <div class="card-body d-flex flex-column">
          <h5 class="card-title text-truncate" title="${s.articulo.nombre}">${s.articulo.nombre}</h5>
          <p class="card-text text-muted small flex-grow-1">${s.articulo.descripcion || ''}</p>
          <div class="mt-2">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <span class="fw-bold text-primary fs-5">${fmt.format(s.precioActual)}</span>
              <span class="badge bg-secondary"><i class="bi bi-hand-index"></i> ${s.pujas?.length || 0}</span>
            </div>
            <div class="small text-muted mb-2">
              <i class="bi bi-person-fill text-primary"></i>
              ${s.liderActual || '<span class="fst-italic">Sin pujas</span>'}
            </div>
            <div class="d-flex justify-content-between align-items-center">
              <span class="badge ${urgente ? 'bg-danger' : 'bg-success'}">
                <i class="bi bi-clock"></i> ${formatTime(s.tiempoRestanteSegundos)}
              </span>
              <a href="/subasta.html?id=${s.id}" class="btn btn-primary btn-sm">
                <i class="bi bi-eye"></i> Ver detalle
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>`;
}

async function loadSubastas(searchType) {
  const grid = document.getElementById('subastasGrid');
  const empty = document.getElementById('emptyState');
  let res;

  if (searchType === 'text') {
    const filtro = document.getElementById('searchText').value.trim();
    res = await API.listarSubastas(filtro || null);
  } else if (searchType === 'precio') {
    const min = document.getElementById('precioMin').value;
    const max = document.getElementById('precioMax').value;
    res = (min || max)
      ? await API.buscarPorPrecio(min || 0, max || 999999999)
      : await API.listarSubastas();
  } else if (searchType === 'tiempo') {
    const seg = document.getElementById('tiempoSeg').value;
    res = seg ? await API.buscarPorTiempo(seg) : await API.listarSubastas();
  } else {
    res = await API.listarSubastas();
  }

  if (!res.ok) {
    grid.innerHTML = '<div class="col-12"><div class="alert alert-warning"><i class="bi bi-exclamation-triangle"></i> Error al cargar subastas.</div></div>';
    return;
  }

  const data = Array.isArray(res.data) ? res.data : [];

  if (data.length === 0) {
    grid.innerHTML = '';
    empty.classList.remove('d-none');
  } else {
    empty.classList.add('d-none');
    grid.innerHTML = data.map(renderCard).join('');
  }
}

async function crearSubasta() {
  const alertDiv = document.getElementById('modalAlert');
  const nombre = document.getElementById('artNombre').value.trim();
  const precio = parseFloat(document.getElementById('artPrecio').value);
  const duracion = parseInt(document.getElementById('duracion').value);

  if (!nombre || isNaN(precio) || precio < 0 || !duracion || duracion <= 0) {
    alertDiv.innerHTML = '<div class="alert alert-danger">Completa los campos obligatorios correctamente.</div>';
    return;
  }

  const articulo = {
    id: document.getElementById('artId').value.trim() || crypto.randomUUID(),
    nombre,
    descripcion: document.getElementById('artDesc').value.trim(),
    urlImagen: document.getElementById('artImg').value.trim(),
    precioSalida: precio,
  };

  const res = await API.crearSubasta(articulo, duracion);
  if (res.ok || res.status === 201) {
    bootstrap.Modal.getInstance(document.getElementById('nuevaSubastaModal'))?.hide();
    document.getElementById('nuevaSubastaForm').reset();
    alertDiv.innerHTML = '';
    loadSubastas(activeSearch);
  } else {
    const msg = (typeof res.data === 'object' && res.data?.message) || 'Error al crear la subasta.';
    alertDiv.innerHTML = `<div class="alert alert-danger">${msg}</div>`;
  }
}

function init() {
  const currentUser = requireAuth();
  if (!currentUser) return;

  document.getElementById('navUser').textContent = currentUser;
  document.getElementById('btnLogout').addEventListener('click', logout);

  document.getElementById('btnSearchText').addEventListener('click', () => {
    activeSearch = 'text';
    loadSubastas('text');
  });
  document.getElementById('searchText').addEventListener('keydown', (e) => {
    if (e.key === 'Enter') { activeSearch = 'text'; loadSubastas('text'); }
  });

  document.getElementById('btnSearchPrecio').addEventListener('click', () => {
    activeSearch = 'precio';
    loadSubastas('precio');
  });

  document.getElementById('btnSearchTiempo').addEventListener('click', () => {
    activeSearch = 'tiempo';
    loadSubastas('tiempo');
  });

  document.getElementById('btnResetSearch').addEventListener('click', () => {
    activeSearch = null;
    document.getElementById('searchText').value = '';
    document.getElementById('precioMin').value = '';
    document.getElementById('precioMax').value = '';
    document.getElementById('tiempoSeg').value = '';
    loadSubastas();
  });

  document.getElementById('btnCrearSubasta').addEventListener('click', crearSubasta);

  document.getElementById('nuevaSubastaModal').addEventListener('hidden.bs.modal', () => {
    document.getElementById('modalAlert').innerHTML = '';
  });

  loadSubastas();
  refreshInterval = setInterval(() => loadSubastas(activeSearch), 5000);
}

init();
