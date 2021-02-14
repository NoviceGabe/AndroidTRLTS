package com.example.androidtrlts.Utils;

public interface Task<T> {
   void onSuccess();
   void onError(T arg);
}
