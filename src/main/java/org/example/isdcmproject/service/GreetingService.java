package org.example.isdcmproject.service;

import org.example.isdcmproject.model.Greeting;

public class GreetingService {
    public Greeting getGreeting() {
        return new Greeting("Hello MVC!");
    }
}
