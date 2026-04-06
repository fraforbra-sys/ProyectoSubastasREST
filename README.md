# Sistema de Subastas Distribuido - Java RMI

Este proyecto es una implementación de un sistema de subastas online distribuido, desarrollado en Java utilizando **RMI (Remote Method Invocation)**. Ha sido creado como aplicación práctica de los conceptos de la asignatura "Sistemas Distribuidos y Servicios Web".

El sistema permite a los usuarios buscar subastas, crear nuevas subastas, realizar pujas y recibir notificaciones en tiempo real cuando otros usuarios pujan por un artículo de su interés.

---

## Arquitectura y Conceptos Aplicados

El proyecto hace uso intensivo de los siguientes patrones y características de los sistemas distribuidos:

* **Gestión del Estado (Stateful vs Stateless):**
    * Se implementa un `BuscadorSubastas` **sin estado**, encargado únicamente de filtrar y devolver subastas activas.
    * Las subastas individuales (`SubastaImpl`) son objetos **con estado**, manteniendo en memoria el precio actual, el líder de la puja y el tiempo restante.
* **Fábricas de Referencias Remotas (Factory Pattern):** * El `GestorSubastasPrincipal` actúa como una fábrica. En lugar de registrar todas las subastas en el *rmiregistry*, el gestor instancia dinámicamente nuevos objetos remotos al crear una subasta y devuelve su referencia al cliente.
* **Callbacks RMI (Patrón Observador):** * El servidor es capaz de invocar métodos en los clientes. Los clientes se registran como observadores y el servidor les notifica de forma asíncrona mediante el método `notificarNuevaPuja()` cada vez que el estado de la subasta cambia. Se implementa control de excepciones para evitar que clientes caídos bloqueen el servidor.
* **Paso de Objetos Complejos (Serialización):** * A diferencia de los objetos remotos que se pasan por referencia, los datos del sistema (como `Articulo`, `Puja` e `HistorialSubasta`) implementan la interfaz `Serializable` para ser transmitidos por valor a través de la red.

---

## 📁 Estructura del Proyecto

El código está organizado de la siguiente manera:

```text
ProyectoSubastasRMI/
├── interfaces/               # Interfaces remotas compartidas
│   ├── IGestorSubastas.java
│   ├── ISubasta.java
│   ├── IBuscadorSubastas.java
│   └── IObservadorCliente.java
├── modelos/                  # Clases de datos (Paso por valor)
│   ├── Articulo.java
│   ├── Puja.java
│   └── HistorialSubasta.java
├── servidor/                 # Implementación de la lógica de negocio
│   ├── GestorSubastasImpl.java
│   ├── SubastaImpl.java
│   ├── BuscadorSubastasImpl.java
│   └── ServidorSubastas.java # Clase Main que arranca el registry y hace el rebind
└── cliente/                  # Lógica del cliente
    ├── ObservadorClienteImpl.java
    └── ClientePrueba.java    # Clase Main para probar la funcionalidad
