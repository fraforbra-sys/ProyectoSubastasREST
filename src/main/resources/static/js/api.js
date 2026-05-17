async function req(method, url, body) {
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (body !== undefined) opts.body = JSON.stringify(body);
  try {
    const res = await fetch(url, opts);
    let data = null;
    try {
      data = await res.json();
    } catch {
      try { const t = await res.text(); data = t || null; } catch {}
    }
    return { ok: res.ok, status: res.status, data };
  } catch (err) {
    return { ok: false, status: 0, data: { message: err.message } };
  }
}

export const API = {
  login:               (u, p)     => req('POST',   '/usuario/login',              { username: u, password: p }),
  registro:            (u, p)     => req('POST',   '/usuario/registro',           { username: u, password: p }),
  obtenerUsuario:      (u)        => req('GET',    `/usuario/${u}`),

  listarSubastas:      (f)        => req('GET',    f ? `/subasta?filtro=${encodeURIComponent(f)}` : '/subasta'),
  obtenerSubasta:      (id)       => req('GET',    `/subasta/${id}`),
  crearSubasta:        (art, dur) => req('POST',   '/subasta',                    { articulo: art, duracionSegundos: dur }),
  pujar:               (id, u, c) => req('POST',   `/subasta/${id}/pujar`,        { usuario: u, cantidad: c }),
  finalizarSubasta:    (id)       => req('DELETE', `/subasta/${id}`),
  buscarPorPrecio:     (min, max) => req('GET',    `/subasta/buscar?precioMin=${min}&precioMax=${max}`),
  buscarPorTiempo:     (seg)      => req('GET',    `/subasta/porTiempo?segundos=${seg}`),

  listarHistorial:     ()         => req('GET',    '/historial'),
  obtenerHistorial:    (id)       => req('GET',    `/historial/${id}`),
  historialPorComprador: (u)      => req('GET',    `/historial/comprador/${u}`),
  contarHistorial:     ()         => req('GET',    '/historial/count'),
};
