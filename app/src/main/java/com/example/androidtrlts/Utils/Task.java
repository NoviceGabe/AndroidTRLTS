package com.example.androidtrlts.Utils;

public interface Task<T, U> {
   void onSuccess(T arg);
   void onError(U arg);
}
