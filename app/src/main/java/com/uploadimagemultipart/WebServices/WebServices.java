package com.uploadimagemultipart.WebServices;



import com.uploadimagemultipart.SimpleMessageStatusResponse.SimpleMessageStatusResponse;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.PartMap;
import retrofit.mime.TypedFile;


public interface WebServices
{



    @Multipart
    @POST("/registerProfile")
    public void uploadPhoto(@Part("profile_pic") TypedFile file, @PartMap Map<String, String> map, Callback<SimpleMessageStatusResponse> callback);
}







