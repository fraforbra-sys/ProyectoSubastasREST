# Memoria del Proyecto: Sistema de Subastas Distribuido con Java RMI

## 1. Introducción

Este proyecto implementa un sistema de subastas distribuido utilizando Java RMI (Remote Method Invocation). El sistema permite crear subastas activas, realizar pujas en tiempo real, y recibir notificaciones mediante callbacks cuando otros usuarios pujan.

### Autores
- Desarrollado para la asignatura de Sistemas Distribuidos y Servicios Web
- Universidad de Sevilla - Curso 2025/2026

---

## 2. Arquitectura del Sistema

### 2.1. Componentes Principales

```
┌─────────────────────────────────────────────────────────────────┐
│                        SERVIDOR RMI                             │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  GestorSubastasImpl (IGestorSubastas)                   │   │
│  │  - Servicio Factory (crea subastas)                     │   │
│  │  - Servicio de Autenticación                            │   │
│  │  - Registro de subastas activas                         │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│         ┌────────────────────┴────────────────────┐            │
│         │                                          │            │
│  ┌──────▼──────┐                          ┌───────▼────────┐  │
│  │ SubastaImpl │                          │ SubastaImpl    │  │
│  │ (ISubasta)  │                          │ (ISubasta)     │  │
│  │ - Stateful  │                          │ - Stateful     │  │
│  │ - Precio    │                          │ - Observadores │  │
│  │ - Líder     │                          │ - Callbacks    │  │
│  └─────────────┘                          └────────────────┘  │
│                                                                │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │  BuscadorSubastasImpl (Stateless)                       │ │
│  │  - Búsqueda por nombre                                  │ │
│  │  - Búsqueda por precio                                  │ │
│  │  - Búsqueda por tiempo restante                         │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ▲
                              │ RMI (IIOP)
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                          CLIENTE                                │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  ClienteSubastas                                        │   │
│  │  - Interfaz de usuario (consola)                        │   │
│  │  - Conexión al Gestor                                   │   │
│  └─────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  ObservadorClienteImpl (IObservadorCliente)             │   │
│  │  - Exportado como objeto RMI                            │   │
│  │  - Recibe callbacks del servidor                        │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2. Estructura de Directorios

```
ProyectoSubastasRMI/
├── comun/                      # Clases compartidas
│   ├── Articulo.java           # Datos del artículo (Serializable)
│   ├── Puja.java               # Datos de una puja (Serializable)
│   ├── HistorialSubasta.java   # Historial completo (Serializable)
│   ├── IGestorSubastas.java    # Interfaz del gestor (Remote)
│   ├── ISubasta.java           # Interfaz de subasta (Remote)
│   ├── BuscadorSubastas.java   # Interfaz del buscador (Remote)
│   └── IObservadorCliente.java # Interfaz para callbacks (Remote)
│
├── servidor/                   # Implementación del servidor
│   ├── GestorSubastasImpl.java # Implementación del gestor
│   ├── SubastaImpl.java        # Implementación de subasta
│   ├── BuscadorSubastasImpl.java # Implementación del buscador
│   └── ServidorSubastas.java   # Punto de entrada del servidor
│
├── cliente/                    # Implementación del cliente
│   ├── ClienteSubastas.java    # Cliente principal
│   └── ObservadorClienteImpl.java # Implementación del observador
│
├── compile.bat                 # Script de compilación
├── run-server.bat              # Script para ejecutar servidor
├── run-client.bat              # Script para ejecutar cliente
└── Memoria.md                  # Este documento
```

---

## 3. Conceptos de Sistemas Distribuidos Aplicados

### 3.1. Paso por Valor vs Paso por Referencia Remota

#### Paso por Valor (Serialización)
Las clases `Articulo`, `Puja` e `HistorialSubasta` implementan `Serializable`. Cuando se devuelven al cliente, se **serializan y envían por valor**:

```java
// En ISubasta.java
HistorialSubasta obtenerDetalles() throws RemoteException;
```

El objeto completo se copia y envía al cliente. Cualquier modificación posterior en el servidor NO se refleja en el cliente.

#### Paso por Referencia Remota
Las interfaces `IGestorSubastas` e `ISubasta` extienden `Remote`. Los objetos se **exportan** y se pasan por referencia:

```java
// En IGestorSubastas.java
ISubasta crearSubasta(Articulo articulo, long duracionSegundos) throws RemoteException;
```

El cliente recibe un **stub** (proxy) que redirige las llamadas al objeto real en el servidor.

### 3.2. Gestión del Estado: Stateless vs Stateful

#### Servicio Stateless: `BuscadorSubastasImpl`
- No mantiene estado entre llamadas
- Cada operación es independiente
- Solo lee datos, no modifica estado
- Más fácil de escalar horizontalmente

```java
public class BuscadorSubastasImpl extends UnicastRemoteObject implements BuscadorSubastas {
    private final Map<String, SubastaImpl> subastasActivas; // Solo lectura

    public List<ISubasta> buscarPorNombre(String texto) {
        // Búsqueda funcional sin modificar estado
    }
}
```

#### Objeto Stateful: `SubastaImpl`
- Mantiene estado interno: precio, líder, observadores, historial
- El estado cambia con cada puja
- Cada instancia representa una subasta concreta
- Sincronización necesaria para concurrencia

```java
public class SubastaImpl extends UnicastRemoteObject implements ISubasta {
    private double precioActual;      // Estado mutable
    private String liderActual;       // Estado mutable
    private List<Puja> historialPujas; // Estado mutable
    private boolean activa;           // Estado mutable

    public boolean pujar(String usuario, double cantidad) {
        synchronized (bloqueo) {
            // Modifica estado interno
        }
    }
}
```

### 3.3. Patrón Factory en RMI

El `GestorSubastasImpl` actúa como fábrica de objetos remotos:

```java
public ISubasta crearSubasta(Articulo articulo, long duracionSegundos)
        throws RemoteException {
    SubastaImpl nuevaSubasta = new SubastaImpl(articulo, duracionSegundos);
    subastasActivas.put(nuevaSubasta.getIdSubasta(), nuevaSubasta);
    return nuevaSubasta; // Devuelve stub al cliente
}
```

El cliente recibe una referencia remota a un objeto recién creado que puede invocar directamente.

### 3.4. Callbacks con el Patrón Observador

#### Registro de Observadores
El cliente exporta su propio objeto remoto y se registra en la subasta:

```java
// En el cliente
observador = new ObservadorClienteImpl(usuarioActual);
subasta.registrarObservador(observador);
```

#### Notificación desde el Servidor
Cuando alguien puja, el servidor itera sobre los observadores:

```java
private void notificarObservadores(String usuarioPuja, double nuevoPrecio) {
    List<IObservadorCliente> observadoresAEliminar = new ArrayList<>();

    for (IObservadorCliente observador : observadores) {
        try {
            observador.notificarNuevaPuja(idSubasta, nuevoPrecio, usuarioPuja);
        } catch (RemoteException e) {
            // Cliente desconectado - marcar para eliminar
            observadoresAEliminar.add(observador);
        }
    }

    // Limpieza de observadores desconectados
    observadores.removeAll(observadoresAEliminar);
}
```

### 3.5. Gestión de Excepciones y Desconexiones

#### RemoteException en todas las firmas
Todos los métodos remotos declaran `throws RemoteException`:

```java
public interface ISubasta extends Remote {
    boolean pujar(String usuario, double cantidad) throws RemoteException;
    void registrarObservador(IObservadorCliente observador) throws RemoteException;
}
```

#### Manejo de Clientes Desconectados
El servidor captura excepciones individualmente para evitar que un cliente desconectado bloquee las notificaciones a otros:

```java
try {
    observador.notificarNuevaPuja(idSubasta, nuevoPrecio, usuarioPuja);
} catch (RemoteException e) {
    System.out.println("Error al notificar observador: " + e.getMessage());
    observadoresAEliminar.add(observador);
}
```

#### Sincronización para Concurrencia
Se usa `synchronized` para proteger el estado compartido:

```java
private final Object bloqueo = new Object();

public boolean pujar(String usuario, double cantidad) throws RemoteException {
    synchronized (bloqueo) {
        // Operaciones atómicas sobre el estado
    }
}
```

---

## 4. Manual de Usuario

### 4.1. Requisitos Previos

- Java JDK 8 o superior instalado
- Variable de entorno `JAVA_HOME` configurada
- `javac` y `java` disponibles en el PATH

### 4.2. Compilación

1. Abre una terminal en el directorio del proyecto:
   ```
   cd C:\Users\forja\Desktop\US\4A\2C\SistmDistr\practicas\ProyectoSubastasRMI
   ```

2. Ejecuta el script de compilación:
   ```bat
   compile.bat
   ```

   O manualmente:
   ```bat
   mkdir bin
   javac -d bin comun/*.java
   javac -cp bin -d bin servidor/*.java
   javac -cp bin -d bin cliente/*.java
   ```

### 4.3. Ejecución del Servidor

1. Ejecuta el script del servidor:
   ```bat
   run-server.bat [puerto]
   ```

   Ejemplo:
   ```bat
   run-server.bat 1099
   ```

2. Verás la salida:
   ```
   ========================================
     SERVIDOR DE SUBASTAS RMI
     Puerto: 1099
   ========================================
   [Servidor] Iniciando RMI Registry en puerto 1099...
   [Servidor] RMI Registry iniciado correctamente
   [Servidor] Creando GestorSubastasImpl...
   [Servidor] Registrando servicio como: rmi://localhost:1099/GestorSubastas
   ========================================
     SERVIDOR LISTO
   ========================================
   ```

3. Mantén esta terminal abierta. El servidor se ejecuta en segundo plano.

### 4.4. Ejecución del Cliente

1. Abre **otra terminal** en el mismo directorio.

2. Ejecuta el script del cliente:
   ```bat
   run-client.bat [puerto] [host]
   ```

   Ejemplo:
   ```bat
   run-client.bat 1099 localhost
   ```

3. Sigue las instrucciones del menú interactivo.

### 4.5. Flujo de Prueba Recomendado

1. **Autenticación**: Usa las credenciales por defecto:
   - Usuario: `admin`
   - Contraseña: `admin123`

2. **Crear subasta**:
   - Opción 1 del menú
   - Introduce datos del artículo (ej: "Laptop Dell", 500€, 300 segundos)

3. **Abrir segundo cliente**:
   - Ejecuta `run-client.bat` en otra terminal
   - Autentícate con otro usuario (ej: `usuario1` / `pass1`)

4. **Unirse a subasta**:
   - Opción 5 en ambos clientes
   - Selecciona la subasta creada

5. **Realizar pujas**:
   - Opción 1 dentro de la subasta
   - Introduce cantidad mayor al precio actual

6. **Observar callbacks**:
   - Cuando un cliente puja, el otro recibe notificación automática:
   ```
   [15:30:45] *** NOTIFICACIÓN *** Subasta a1b2c3d4...: Nueva puja de 'admin' por 600.0€
   ```

7. **Ver historial**:
   - Opción 2 dentro de la subasta
   - Muestra el objeto `HistorialSubasta` serializado completo

---

## 5. Decisiones de Diseño

### 5.1. Uso de ConcurrentHashMap para Subastas Activas
Se eligió `ConcurrentHashMap` en lugar de `HashMap` sincronizado porque:
- Permite iteración thread-safe sin bloquear todo el mapa
- Mejor rendimiento con múltiples clientes concurrentes
- Operaciones atómicas como `putIfAbsent` disponibles

### 5.2. Finalización Automática con ScheduledExecutorService
Cada subasta programa su propia finalización:
```java
scheduler.schedule(this::finalizarAutomaticamente, duracionSegundos, TimeUnit.SECONDS);
```
Ventajas:
- No requiere polling del servidor
- Precisión en el tiempo de finalización
- Limpieza automática de recursos

### 5.3. Copias Defensivas en Objetos Serializables
```java
this.pujas = new ArrayList<>(pujas); // Copia defensiva
```
Previene que modificaciones externas afecten el estado interno después de la serialización.

### 5.4. Eliminación de Observadores Desconectados
El servidor detecta y elimina observadores que lanzan `RemoteException`:
- Evita acumulación de referencias inválidas
- Previene memory leaks
- Mejora rendimiento en notificaciones futuras

---

## 6. Posibles Mejoras Futuras

1. **Persistencia**: Guardar subastas en base de datos
2. **Seguridad**: SSL/TLS para comunicaciones RMI
3. **Web Services**: Exponer funcionalidad vía REST/SOAP
4. **Message Queue**: Usar JMS para notificaciones asíncronas
5. **Cluster**: Réplicas del gestor para alta disponibilidad

---

## 7. Conclusión

Este proyecto demuestra la aplicación práctica de los conceptos fundamentales de Sistemas Distribuidos:
- Comunicación remota transparente con RMI
- Gestión adecuada del estado (stateless vs stateful)
- Patrones de diseño distribuidos (Factory, Observer)
- Manejo robusto de excepciones y desconexiones
- Serialización de objetos complejos

La arquitectura resultante es escalable, mantenible y sigue las mejores prácticas de la asignatura.
