package hackathon.EcoRoute.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum VehicleCategory {
    // Agrupamos las variantes de tu base de datos para que ninguna se escape
    CAT_1(List.of("I", "IE", "IE1", "1"), 0, 3500, 900.0, "Automóviles y camionetas"),
    CAT_2(List.of("II", "IIA", "IIE", "2"), 3501, 8500, 1400.0, "Buses y camiones de 2 ejes"),
    CAT_3(List.of("III", "IIIE", "E1-III", "3"), 8501, 12500, 1900.0, "Camiones de 3 y 4 ejes"),
    CAT_4(List.of("IV", "IVE", "E1-IV", "4"), 12501, 20500, 2400.0, "Camiones de 5 ejes"),
    CAT_5(List.of("V", "VE", "E1-V", "5"), 20501, 26500, 2400.0, "Camiones de 6 ejes"),
    CAT_6(List.of("VI", "VIE", "E1-VI", "6"), 26501, 32500, 2400.0, "Especiales 1"),
    CAT_7(List.of("VII", "VIIE", "E1-VII", "7"), 32501, Integer.MAX_VALUE, 2400.0, "Especiales 2");

    private final List<String> compatibleCodes;
    private final double minWeight;
    private final double maxWeight;
    private final double fuelPricePerKm;
    private final String description;

    public static VehicleCategory fromWeight(double weightKg) {
        return Arrays.stream(values())
                .filter(cat -> weightKg >= cat.minWeight && weightKg <= cat.maxWeight)
                .findFirst()
                .orElse(CAT_1);
    }
}
