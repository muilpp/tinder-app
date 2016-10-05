package com.tinderapp.model;

import com.tinderapp.model.api_data.FacebookTokenDTO;
import com.tinderapp.model.api_data.LocationDTO;
import com.tinderapp.view.ChatActivity;
import com.tinderapp.view.MatchesFragment;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TinderAPI {

    @POST("/auth")
    Call<ResponseBody> login(@Body FacebookTokenDTO facebookToken);

    @POST("/user/recs")
    Call<ResponseBody> getRecommendations();

    @GET("/user/"+"{userID}")
    Call<ResponseBody> getUserProfile(@Path("userID") String userID);

    @POST("/like/"+"{userID}")
    Call<ResponseBody> sendLike(@Path("userID") String userID);

    @POST("/pass/"+"{userID}")
    Call<ResponseBody> sendPass(@Path("userID") String userID);

    @POST("/like/"+"{userID}"+"/super")
    Call<ResponseBody> sendSuperLike(@Path("userID") String userID);

    @POST("/updates")
    Call<ResponseBody> getMatches(@Body MatchesFragment.MatchesDTO matches);

    @POST("/user/matches/"+"{matchID}")
    Call<ResponseBody> sendMessage(@Path("matchID") String userID, @Body ChatActivity.MessageDTO message);

    @POST("/user/ping")
    Call<ResponseBody> changeLocation(@Body LocationDTO location);

    @DELETE("/user/matches/"+"{matchID}")
    Call<ResponseBody> deleteMatch(@Path("matchID") String matchID);
}