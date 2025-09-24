package pmf.rma.cityexplorerosm.data.remote;

import java.util.List;

import pmf.rma.cityexplorerosm.data.remote.model.PlaceDto;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    // Endpoint koji vraća listu mesta
    @GET("places.json")
    Call<List<PlaceDto>> getPlaces();
}
