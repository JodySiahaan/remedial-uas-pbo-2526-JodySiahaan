package pbo.f01;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class untuk Area Parkir
 * Atribut: name, capacity, allowed_type (car/motorcycle)
 */
@Entity
@Table(name = "parking_areas")
public class ParkingArea {
    
    @Id
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "capacity", nullable = false)
    private int capacity;
    
    @Column(name = "allowed_type", nullable = false)
    private String allowedType; 
    
    @OneToMany(mappedBy = "parkingArea", cascade = CascadeType.ALL)
    private List<Vehicle> vehicles = new ArrayList<>();
    
    public ParkingArea() {}
    
    public ParkingArea(String name, int capacity, String allowedType) {
        this.name = name;
        this.capacity = capacity;
        this.allowedType = allowedType;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getCapacity() {
        return capacity;
    }
    
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    
    public String getAllowedType() {
        return allowedType;
    }
    
    public void setAllowedType(String allowedType) {
        this.allowedType = allowedType;
    }
    
    public List<Vehicle> getVehicles() {
        return vehicles;
    }
    
    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }
    
    public int getCurrentCount() {
        return vehicles.size();
    }
}
