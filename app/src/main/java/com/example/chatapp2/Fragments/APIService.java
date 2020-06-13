package com.example.chatapp2.Fragments;

import com.example.chatapp2.Notification.MyResponse;
import com.example.chatapp2.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAzjL2meM:APA91bG0eOIyl-FflwplB5QVzMOkFeu42CWx-vpVX1j5b70j7G2h6ucaV1mGoUlbl1e9EwVaDtNkrzN1GoaKO36YfDfoc0zOBEGLVq6xUlgzM8nvdccHr2Xfg1lZHhKs2VWHucamHcow"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}

