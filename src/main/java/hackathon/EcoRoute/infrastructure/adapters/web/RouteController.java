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
            // Convertimos el CSV a lista de Deliveries
            List<Delivery> deliveries = csvParser.parse(file);

            // Ejecutamos el caso de uso (Lógica de Negocio)
            Route optimizedRoute = optimizeRouteUseCase.execute(deliveries);

            // Pasamos el resultado al modelo de la vista
            model.addAttribute("route", optimizedRoute);

            // HTMX: Devolvemos solo el fragmento de los resultados para actualizar la página parcialmente
            return "fragments/results :: route-results";

        } catch (Exception e) {
            model.addAttribute("error", "Error processing CSV: " + e.getMessage());
            return "fragments/error :: error-message";
        }
    }
}
