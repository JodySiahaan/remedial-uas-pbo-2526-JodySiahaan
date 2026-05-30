package pbo.f01;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

/**
 * Driver class utama
 * Nama: Jody Alfonso Siahaan
 * Nim:  12S24039
 * 
 * Sistem Manajemen Parkir Park-IT
 * Mengelola penempatan kendaraan(mobil/motor) ke area parkir dengan validasi kapasitas dan tipe
 */
public class App {
    private static EntityManagerFactory emf;
    private static Map<String, ParkingArea> parkingAreas = new HashMap<>();
    private static Map<String, Vehicle> vehicles = new HashMap<>();
    
    public static void main(String[] args) {
        try {
            emf = Persistence.createEntityManagerFactory("parkit-pu");
        } catch (Exception e) {
            System.err.println("Error initializing persistence: " + e.getMessage());
            return;
        }
        
        Scanner scanner = new Scanner(System.in);
        
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            
            if (input == null || input.trim().isEmpty()) {
                break;
            }
            
            processCommand(input.trim());
        }
        
        scanner.close();
        
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
    
    /**
     * Parse dan execute command berdasarkan format
     */
    private static void processCommand(String input) {
        String[] parts = input.split("#");
        
        if (parts.length == 0) {
            return;
        }
        
        String command = parts[0];
        
        try {
            switch (command) {
                case "area-add":
                    handleAreaAdd(parts);
                    break;
                case "vehicle-add":
                    handleVehicleAdd(parts);
                    break;
                case "park":
                    handlePark(parts);
                    break;
                case "display-all":
                    handleDisplayAll();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error processing command: " + e.getMessage());
        }
    }
    
    /**
     * Handle area-add#<name>#<capacity>#<allowed_type>
     */
    private static void handleAreaAdd(String[] parts) {
        if (parts.length != 4) {
            return;
        }
        
        String name = parts[1];
        int capacity = Integer.parseInt(parts[2]);
        String allowedType = parts[3];
        
        if (parkingAreas.containsKey(name)) {
            return; 
        }
        
        ParkingArea area = new ParkingArea(name, capacity, allowedType);
        parkingAreas.put(name, area);
        
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(area);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
        } finally {
            em.close();
        }
    }
    
    /**
     * Handle vehicle-add#<plate_number>#<owner>#<type>
     */
    private static void handleVehicleAdd(String[] parts) {
        if (parts.length != 4) {
            return;
        }
        
        String plateNumber = parts[1];
        String owner = parts[2];
        String type = parts[3];
        
        if (vehicles.containsKey(plateNumber)) {
            return; 
        }
        
        Vehicle vehicle = new Vehicle(plateNumber, owner, type);
        vehicles.put(plateNumber, vehicle);
        
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(vehicle);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
        } finally {
            em.close();
        }
    }
    
    /**
     * Handle park#<plate_number>#<area_name>
     * Validasi: kendaraan harus ada, area harus ada, tipe harus sesuai, kapasitas harus cukup
     */
    private static void handlePark(String[] parts) {
        if (parts.length != 3) {
            return;
        }
        
        String plateNumber = parts[1];
        String areaName = parts[2];
        
        if (!vehicles.containsKey(plateNumber)) {
            return; 
        }
        
        if (!parkingAreas.containsKey(areaName)) {
            return; 
        }
        
        Vehicle vehicle = vehicles.get(plateNumber);
        ParkingArea area = parkingAreas.get(areaName);
    
        if (!vehicle.getType().equals(area.getAllowedType())) {
            return; 
        }
        
        if (area.getCurrentCount() >= area.getCapacity()) {
            return; 
        }
        
        if (vehicle.getParkingArea() != null) {
            return; 
        }
        
        vehicle.setParkingArea(area);
        area.getVehicles().add(vehicle);
        
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(vehicle);
            em.merge(area);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
        } finally {
            em.close();
        }
    }
    
    /**
     * Handle display-all
     * Tampilkan semua area (sorted by name) dan kendaraan di dalamnya (sorted by plate)
     * Format area: <name> <type> <capacity>|<current_count>
     * Format kendaraan: <plate> <owner> <type>
     */
    private static void handleDisplayAll() {
        List<ParkingArea> sortedAreas = new ArrayList<>(parkingAreas.values());
        sortedAreas.sort(Comparator.comparing(ParkingArea::getName));
        
        for (ParkingArea area : sortedAreas) {
            System.out.println(area.getName() + " " + area.getAllowedType() + " " + 
                             area.getCapacity() + "|" + area.getCurrentCount());
            
            List<Vehicle> sortedVehicles = new ArrayList<>(area.getVehicles());
            sortedVehicles.sort(Comparator.comparing(Vehicle::getPlateNumber));
            
            for (Vehicle vehicle : sortedVehicles) {
                System.out.println(vehicle.getPlateNumber() + " " + vehicle.getOwner() + " " + 
                                 vehicle.getType());
            }
        }
    }
}
