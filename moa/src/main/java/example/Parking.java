package example;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Objects;

public class Parking {
    @SerializedName("poiID")
    private Integer id;
    @SerializedName("nombre")
    private String name;
    @SerializedName("latitude")
    private Double latitude;
    @SerializedName("longitude")
    private Double longitude;
    @SerializedName("altitud")
    private Double altitude;
    @SerializedName("fechahora_ultima_actualizacion")
    private Date date;
    @SerializedName("libres")
    private Integer free;

    public Parking(Integer id, String name, Double latitude, Double longitude, Double altitude, Date date, Integer free) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.date = date;
        this.free = free;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getFree() {
        return free;
    }

    public void setFree(Integer free) {
        this.free = free;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parking parking = (Parking) o;
        return Objects.equals(id, parking.id) &&
                Objects.equals(name, parking.name) &&
                Objects.equals(latitude, parking.latitude) &&
                Objects.equals(longitude, parking.longitude) &&
                Objects.equals(altitude, parking.altitude) &&
                Objects.equals(date, parking.date) &&
                Objects.equals(free, parking.free);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, latitude, longitude, altitude, date, free);
    }

    @Override
    public String toString() {
        return "example.Parking{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", date=" + date +
                ", free=" + free +
                '}';
    }
}
