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
}
