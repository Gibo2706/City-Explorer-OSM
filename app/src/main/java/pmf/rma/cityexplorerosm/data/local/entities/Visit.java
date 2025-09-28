package pmf.rma.cityexplorerosm.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(
        tableName = "visits",
        primaryKeys = {"userId", "placeId"}
)
public class Visit {
    @NonNull
    public String userId;     // 🔗 vlasnik posete
    public int placeId;       // deo PK

    public long timestamp;
    @NonNull
    public String status;     // "PENDING" ili "VERIFIED"
    public String proofType;  // GPS/QR/NONE
    public String proofValue; // QR sadržaj, ili "lat,lon" za GPS

    public Visit(@NonNull String userId, int placeId, long timestamp,
                 @NonNull String status, String proofType, String proofValue) {
        this.userId = userId;
        this.placeId = placeId;
        this.timestamp = timestamp;
        this.status = status;
        this.proofType = proofType;
        this.proofValue = proofValue;
    }
}
