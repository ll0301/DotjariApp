package com.example.wante.dotjariapp.Notifications;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService{
        @Headers(
                {
                        "Content-Type:application/json",
                        "Authorization: key= AAAAloL2OAU:APA91bFoK07S2U2NzZaHtijvBDmrcjFRw-SgxIlvMZcDvGVekjChqES-eJBvoGyjC4nescnWe-T5zkktU852i9v4UbFDyh3c5kvAcxrSgvmZPn0QhoYELTseuw17ZdWiv42CdAZncQo9"

                }

        )

        @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
    }




