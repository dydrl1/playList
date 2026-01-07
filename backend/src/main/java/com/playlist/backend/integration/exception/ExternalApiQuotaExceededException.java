package com.playlist.backend.integration.exception;


public class ExternalApiQuotaExceededException extends RuntimeException{
    public ExternalApiQuotaExceededException(String message){
        super(message);
    }
}
