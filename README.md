# 🌱 EcoRoute.co | Optimización de Rutas Logísticas en Colombia

![EcoRoute Banner](https://github.com/user-attachments/assets/adb7c3c5-ee4e-4c6c-8642-80290774e8d6)

**EcoRoute** es una solución técnica para la gestión eficiente del transporte de carga en el territorio colombiano. El sistema procesa listas de entregas desordenadas para calcular la ruta óptima, estimar costos de peajes y reducir el impacto ambiental mediante el ahorro de combustible.

## 🌎 El Contexto: El reto logístico en Colombia

Mover carga en el país es un desafío crítico para la competitividad. EcoRoute aborda tres problemas que inflan los costos operativos:

* **Altos Costos Logísticos:** Según [Analdex](https://analdex.org/2025/11/20/estamos-lejos-de-unos-costos-logisticos-competitivos-para-el-comercio-exterior-analdex/), Colombia sigue lejos de niveles competitivos, con costos que impactan severamente el comercio exterior.
* **Inflación en el Transporte:** El costo del transporte de carga [subió un 4.39% en 2025](https://apnnoticias.com/el-costo-del-transporte-de-carga-subio-439-en-2025-y-prende-alertas-para-2026/), lo que ha encendido las alertas para la operación logística en 2026.
* **Actualización de Peajes:** Con la [actualización de tarifas de la ANI e INVIAS en enero de 2026](https://www.presidencia.gov.co/prensa/Paginas/Desde-el-16-de-enero-se-actualizaran-tarifas-de-peajes-a-cargo-de-la-ANI-260110.aspx), una mala planeación de ruta puede devorar el margen de ganancia de un viaje.

---

## 🚀 Funcionalidades Clave

1.  **Optimización de Secuencia:** Reordenamiento automático de paradas (vía CSV) para minimizar el kilometraje total mediante algoritmos de ruta corta.
2.  **Estimación de Peajes:** Cruce de la ruta con coordenadas de estaciones para calcular el costo proyectado según la categoría del vehículo.
3.  **Métricas de Impacto:** Reporte inmediato de ahorro en distancia, costos operativos y reducción de emisiones de $CO_2$.
4.  **Despacho Directo:** Integración para enviar la hoja de ruta optimizada al conductor a través de WhatsApp.

---

## 🧪 Guía de Prueba (Entorno Producción)

Para validar el algoritmo con datos de la ruta Bogotá - Tunja:

1.  Acceda a `http://108.165.47.224:8080/`.
2.  Descargue la **Plantilla de Ejemplo**: Este archivo simula un despacho real pero críticamente ineficiente, con paradas en este orden:
    Bogotá ➔ Tunja ➔ Chía ➔ Villapinzón ➔ Cajicá ➔ Tocancipá.
3.  Cargue el archivo y ejecute el algoritmo.

**Resultados visuales:**

![Mapa Ruta Original Ineficiente](https://github.com/user-attachments/assets/a26e515e-afa1-4591-8d0d-96dfcb051088)
![Mapa Ruta Optimizada](https://github.com/user-attachments/assets/14bc500d-dd5a-4a0d-b25d-2d7c521704b8)

*Comparativa del trazado geográfico: El primer mapa representa el trayecto original (ineficiente) y el segundo muestra el cálculo optimizado por OSRM.*

**Resultados esperados:**
* **Optimización:** Una ruta caótica de **412.4 km** se reduce a un trayecto lógico de **140 km**.
* **Detección de Peajes:** Identificación de casetas (Andes, El Roble, Albarracín) con un costo estimado de referencia.
* **Sostenibilidad:** Ahorro proyectado de **57.2 kg de $CO_2$**.

![Desglose Financiero](https://github.com/user-attachments/assets/f0eda9b9-4690-474e-9ead-701f01f0210c)
*Desglose de costos operativos y peajes detectados en la vía.*

---

## 🏗️ Stack Tecnológico

* **Backend:** Java 21 / Spring Boot 4.
* **Arquitectura:** Hexagonal (Ports & Adapters) para desacoplamiento de lógica de negocio.
* **Frontend:** Tailwind CSS + **HTMX** (interactividad de alta velocidad).
* **Motor Geoespacial:** OSRM (Open Source Routing Machine) y Leaflet JS.

---

### ☁️ Infraestructura y Despliegue (CubePath)

Para garantizar la disponibilidad y portabilidad de **EcoRoute**, la aplicación ha sido desplegada en una instancia de **Ubuntu Server** dentro de **CubePath** siguiendo una arquitectura basada en contenedores:

* **📦 Dockerización:** Se creó una imagen de Docker personalizada para asegurar que la aplicación Spring Boot se ejecute de forma idéntica en cualquier entorno".
* **🚀 Despliegue:** Se configuró **Docker Engine** en el VPS de CubePath para gestionar el ciclo de vida de la aplicación.
* **🛡️ Resiliencia:** Se implementaron políticas de **reinicio automático** (`--restart always`).
* **🔗 Conexión Remota:** El servidor en CubePath actúa como el núcleo de cómputo, conectándose de manera segura a una base de datos **PostgreSQL en la nube (Neon)**.

---

## ⚡ Especificaciones Técnicas y Datos

* **Rendimiento:** Implementación de un **Bounding Box** geográfico para filtrar peajes relevantes, optimizando las consultas a la base de datos nacional.
* **Precisión de Cruce:** Uso de la fórmula de **Haversine** con un radio de 150 metros para confirmar el paso por estación y evitar falsos positivos en vías paralelas.
* **Nota sobre la Precisión:** Los montos de peajes son **estimaciones de referencia**. Debido a la descentralización de tarifas (ANI, Invías y concesiones privadas), los valores se basan en la última base de datos integrada y pueden variar frente al cobro real en carretera debido a actualizaciones no centralizadas.

---

**© 2026 Anthony Danilo Parra.**
