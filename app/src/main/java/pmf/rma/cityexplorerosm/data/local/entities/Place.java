package pmf.rma.cityexplorerosm.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "places")
public class Place {

    @PrimaryKey
    public int id;

    @NonNull public String name;
    public String description;
    public double latitude;
    public double longitude;
    public String category;
    public String imageUrl;
    public String workingHours;

    //  verifikacija
    public String verificationType;     // "NONE" | "QR" | "GPS"
    public String verificationSecret;   // za QR (payload/kod)
    public Integer verificationRadiusM; // za GPS (npr. 75)
    public Integer verificationDwellSec;// dwell (MVP: 0)

    /** Prazan ctor za Room */
    public Place() {}

    /** Kratki ctor (kompatibilan sa starim kodom) */
    @Ignore
    public Place(int id, @NonNull String name, String description,
                 double latitude, double longitude,
                 String category, String imageUrl, String workingHours) {
        this(id, name, description, latitude, longitude, category, imageUrl, workingHours,
                "NONE", null, null, null);
    }

    /** Puni ctor */
    public Place(int id, @NonNull String name, String description,
                 double latitude, double longitude,
                 String category, String imageUrl, String workingHours,
                 String verificationType, String verificationSecret,
                 Integer verificationRadiusM, Integer verificationDwellSec) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.imageUrl = imageUrl;
        this.workingHours = workingHours;
        this.verificationType = verificationType;
        this.verificationSecret = verificationSecret;
        this.verificationRadiusM = verificationRadiusM;
        this.verificationDwellSec = verificationDwellSec;
    }
}
