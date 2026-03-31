# 🌱 EcoRoute.co | Optimización de Rutas Logísticas en Colombia

**EcoRoute** es una solución técnica para la gestión eficiente del transporte de carga en el territorio colombiano. El sistema procesa listas de entregas desordenadas para calcular la ruta óptima, estimar costos de peajes y reducir el impacto ambiental mediante el ahorro de combustible.

## 🌎 El Contexto: El reto logístico en Colombia

Mover carga en el país es un desafío para la competitividad. EcoRoute aborda tres problemas que inflan los costos operativos:

* **Altos Costos Logísticos:** Según [Analdex](https://analdex.org/2025/11/20/estamos-lejos-de-unos-costos-logisticos-competitivos-para-el-comercio-exterior-analdex/), Colombia sigue lejos de niveles competitivos, con costos que impactan severamente el comercio exterior.
* **Inflación en el Transporte:** El costo del transporte de carga [subió un 4.39% en 2025](https://apnnoticias.com/el-costo-del-transporte-de-carga-subio-439-en-2025-y-prende-alertas-para-2026/), lo que ha encendido las alertas para la operación logística en 2026.
* **Actualización de Peajes:** Con la [actualización de tarifas de la ANI e INVIAS en enero de 2026](https://www.presidencia.gov.co/prensa/Paginas/Desde-el-16-de-enero-se-actualizaran-tarifas-de-peajes-a-cargo-de-la-ANI-260110.aspx), una mala planeación de ruta puede devorar el margen de ganancia de un viaje.

---

## 🚀 Funcionalidades Clave

1.  **Optimización VROOM Engine:** Reordenamiento automático de paradas mediante el algoritmo **VROOM** para minimizar el kilometraje total considerando eficiencia de giro y tiempos.
2.  **Rutas Carga-Sensibles (HGV vs. LGV):** El sistema detecta si la carga supera las 3.5 toneladas y recalcula la ruta usando restricciones de **Vehículos de Carga Pesada (HGV)**.
3.  **Auditoría de Costos Comparativa:** Cálculo del "Daño Económico" de la ruta convencional vs. el beneficio de EcoRoute, comparando combustible y peajes.
4.  **Despacho Directo:** Integración para enviar la hoja de ruta optimizada al conductor a través de WhatsApp.

---

## 🧪 Guía de Prueba

Para validar el algoritmo con datos reales de la ruta Bogotá - Tunja:

1.  Acceda a `http://108.165.47.224:8080/`.
2.  Descargue la **Plantilla de Ejemplo**: Este archivo simula un despacho real pero críticamente ineficiente, con paradas en este orden:
    Bogotá ➔ Tunja ➔ Chía ➔ Villapinzón ➔ Cajicá ➔ Tocancipá.
3.  Cargue el archivo y ejecute el algoritmo.

**Resultados esperados:**
* **Inteligencia de Flota:** El sistema detectará los pesos y asignará el perfil de vehículo correcto.
* **Optimización:** Una ruta caótica de **412.4 km** se reduce a un trayecto lógico de **140 km**.
* **Detección de Peajes:** Identificación de casetas (Andes, El Roble, Albarracín) con un costo estimado de referencia.
* **Sostenibilidad:** Ahorro proyectado de **57.2 kg de $CO_2$**.

---

## 🏗️ Stack Tecnológico

* **Backend:** Java 21 / Spring Boot 3.3 (LTS).
* **Arquitectura:** Hexagonal (Ports & Adapters) para desacoplamiento de lógica de negocio.
* **Frontend:** Tailwind CSS + **HTMX** (interactividad de alta velocidad).
* **Motor Geoespacial:** OpenRouteService API + **VROOM Optimization Engine** y Leaflet JS.

---

## ⚡ Especificaciones Técnicas y Datos

* **Rendimiento:** Implementación de un **Bounding Box** geográfico para filtrar peajes relevantes, optimizando las consultas a la base de datos nacional.
* **Precisión de Cruce:** Uso de la fórmula de **Haversine** con un radio de 150 metros para confirmar el paso por estación y evitar falsos positivos en vías paralelas.
* **Nota sobre la Precisión:** Los montos de peajes son **estimaciones de referencia**. Debido a la descentralización de tarifas (ANI, Invías y concesiones privadas), los valores se basan en la última base de datos integrada y pueden variar frente al cobro real en carretera debido a actualizaciones no centralizadas.