package hackathon.EcoRoute.infrastructure.adapters.web;

import hackathon.EcoRoute.application.ports.in.OptimizeRouteUseCase;
import hackathon.EcoRoute.domain.model.Delivery;
import hackathon.EcoRoute.domain.model.Route;
import hackathon.EcoRoute.infrastructure.adapters.util.CsvParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RouteController {

    private final OptimizeRouteUseCase optimizeRouteUseCase;
    private final CsvParser csvParser;

    @GetMapping("/")
    public String index() {
        return "index"; // Esto buscará index.html en la carpeta templates
    }

    @PostMapping("/optimize")
    public String optimize(@RequestParam("file") MultipartFile file, Model model) {
        try {
            // 1. Parseo real del CSV subido por el usuario
            List<Delivery> deliveries = csvParser.parse(file);

            // 2. Ejecución de la lógica de optimización (Haversine + ArcGIS + OSRM)
            // Aquí es donde se calcula el ahorro real y los peajes de la ANI
            Route optimizedRoute = optimizeRouteUseCase.execute(deliveries);

            // 3. Pasamos el objeto enriquecido a la vista
            model.addAttribute("route", optimizedRoute);

            // 4. HTMX: Devolvemos solo el fragmento de resultados
            // Esto evita que la página parpadee y da una sensación de fluidez total
            return "fragments/results :: route-results";

        } catch (Exception e) {
            model.addAttribute("error", "Error procesando el archivo: " + e.getMessage());
            return "fragments/error :: error-message";
        }
    }
    @GetMapping(value = "/plantilla-ecoroute", produces = "text/csv")
    @org.springframework.web.bind.annotation.ResponseBody
    public String downloadTemplate(jakarta.servlet.http.HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment; filename=plantilla_ecoroute.csv");

        // Plantilla intencionalmente DESORDENADA para probar el algoritmo TSP (Ruta Bogotá - Tunja)
        // El orden real óptimo de sur a norte debería ser: Bogotá -> Chía -> Cajicá -> Tocancipá -> Villapinzón -> Tunja
        return "Nombre,Dirección,Latitud,Longitud,PesoKg\n" +
                "Bodega Norte,Calle 170 #67-51 Bogotá,4.7531,-74.0456,1500\n" +
                "Cliente Tunja,Carrera 10 #20-50 Centro Tunja,5.5353,-73.3678,800\n" +
                "Cliente Chía,Avenida Pradilla #4-31 Chía,4.8624,-74.0305,1200\n" +
                "Cliente Villapinzón,Calle 4 #5-12 Villapinzón,5.2167,-73.6000,950\n" +
                "Cliente Cajicá,Carrera 6 #3-20 Cajicá,4.9167,-74.0333,600\n" +
                "Cliente Tocancipá,Zona Industrial Tocancipá,4.9653,-73.9130,1100";
    }
}
