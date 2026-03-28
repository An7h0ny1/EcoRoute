# 🌱 EcoRoute.co | Motor de Optimización Logística

EcoRoute es una aplicación web desarrollada para optimizar rutas de entrega de carga en Colombia. Su objetivo es reorganizar manifiestos de despacho ineficientes, trazar la ruta real por carretera, detectar los peajes en el trayecto y calcular los costos operativos.

## ⚙️ Funcionalidades Principales

1. **Optimización de Ruta (TSP):** Recibe un archivo CSV con entregas desordenadas y utiliza el motor de OSRM para resolver el Problema del Agente Viajero, devolviendo el orden óptimo por carretera.
2. **Detección de Peajes (ANI, INVIAS):** Cruza la polilínea de la ruta calculada con la base de datos de peajes. Calcula el costo exacto dependiendo de la categoría del vehículo (basado en el peso ingresado).
3. **Métricas de Impacto:** Estima el ahorro en kilómetros, el costo de combustible y la reducción de la huella de carbono (CO₂).
4. **Despacho:** Permite exportar la ruta optimizada directamente a WhatsApp para el conductor.

## 🧪 Guía de Prueba para el Jurado

Para comprobar la efectividad del algoritmo, configuré un caso de prueba extremo en la plataforma.

**Pasos para probar:**
1. Inicia la aplicación Spring Boot y abre `http://localhost:8080`.
2. Haz clic en **"Descargar Plantilla de Ejemplo"**.
   *Nota: Este CSV contiene una ruta intencionalmente desordenada (Bogotá -> Tunja -> Chía -> Villapinzón -> Cajicá -> Tocancipá) que sumaría casi 500 km si se hace en ese orden.*
3. Sube el archivo descargado en la zona de Drag & Drop y haz clic en "Ejecutar Algoritmo".

**Resultados esperados:**
* **Distancia:** El algoritmo reordena los puntos lógicamente de sur a norte, reduciendo el recorrido de ~496 km a ~140 km (un ahorro de más del 70%).
* **Peajes:** El sistema detectará únicamente los peajes correspondientes a esa vía (Andes, El Roble, Albarracín), evadiendo correctamente peajes de vías paralelas (como Fusca).
* **Mapa:** Se dibujará la ruta exacta por las carreteras usando Leaflet.

## 🏗️ Arquitectura y Stack Tecnológico

El proyecto está construido aplicando principios SOLID y **Arquitectura Hexagonal (Puertos y Adaptadores)** para desacoplar el dominio de la infraestructura.

**Stack Backend:**
* Java 21 + Spring Boot 4
* Spring Data JPA
* Integración HTTP Reactiva (WebClient)

**Stack Frontend:**
* HTML5 + Thymeleaf
* HTMX (Para actualización dinámica del DOM sin recargar la página)
* Tailwind CSS
* Leaflet JS + CartoDB (Renderizado de mapas)

**Motores Geoespaciales:**
* OSRM (Open Source Routing Machine) vía API.

## ⚡ Optimizaciones Técnicas

El mayor reto de rendimiento del proyecto era cruzar la geometría de la ruta (miles de coordenadas devueltas por OSRM) contra la base de datos de peajes sin saturar el servidor.

Para resolverlo, implementé la siguiente lógica en capas:
1. **Bounding Box Dinámico (Capa DB):** Antes de procesar puntos, el backend calcula el cuadrante de la ruta completa y extrae de la base de datos solo los peajes que se encuentran en esa zona mediante una consulta nativa.
2. **Filtro Previo Matemático (Capa Dominio):** Antes de usar la costosa función trigonométrica de Haversine para calcular distancias, se hace una resta de coordenadas (grados absolutos). Si el punto está lejos, se descarta en nanosegundos.
3. **Radar de Alta Precisión:** El radio final de validación (Haversine) se ajustó a 150 metros (0.15 km). Esto garantiza que el sistema detecte la plaza de peaje sobre la que pasa la ruta, pero ignore peajes ubicados en carreteras paralelas o vías de servicio.