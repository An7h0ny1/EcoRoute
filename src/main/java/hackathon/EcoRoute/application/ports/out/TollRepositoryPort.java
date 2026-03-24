package hackathon.EcoRoute.application.ports.out;

public interface TollRepositoryPort {
    double calculateTollsForRoute(double startLat, double startLon, double endLat, double endLon);
}
