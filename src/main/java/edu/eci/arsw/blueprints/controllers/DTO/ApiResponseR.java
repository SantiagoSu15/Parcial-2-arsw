package edu.eci.arsw.blueprints.controllers.DTO;



public record ApiResponseR<T>(int code, String message, T data){}













