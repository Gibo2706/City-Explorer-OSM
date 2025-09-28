package pmf.rma.cityexplorerosm.ui.leaderboard;

public class LeaderboardEntry {
    public final String uid;
    public final String displayName;
    public final String username;
    public final int points;
    public final int rank;
    public LeaderboardEntry(String uid, String displayName, String username, int points, int rank) {
        this.uid = uid;
        this.displayName = displayName == null ? "Korisnik" : displayName;
        this.username = username;
        this.points = points;
        this.rank = rank;
    }
}
