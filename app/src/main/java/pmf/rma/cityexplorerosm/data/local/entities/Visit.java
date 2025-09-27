package pmf.rma.cityexplorerosm.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "visits")
public class Visit {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public int placeId;
    public long visitedAt;
    public String status;      // "NOT_VISITED" | "PENDING" | "VERIFIED"
    public String proofType;   // "QR" | "GPS" | "NONE"
    public String proofValue;  // npr. QR payload ili "lat,lon,accuracy"
    public Visit(long id, int placeId, long visitedAt, String status, String proofType, String proofValue) {
        this.id = id;
        this.placeId = placeId;
        this.visitedAt = visitedAt;
        this.status = status;
        this.proofType = proofType;
        this.proofValue = proofValue;
    }
}
