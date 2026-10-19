package com.framework.corps;

import java.util.LinkedHashMap;
import java.util.Map;

import mg.itu.annotation.Controller.Controller;
import mg.itu.annotation.Json.Json;
import mg.itu.annotation.Url.UrlMapping;
import mg.itu.utils.ModelAndView;

@Controller
public class TestController {

    @UrlMapping(value = "/test/json", method = "GET")
    @Json
    public Map<String, Object> jsonExample() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "Bonjour depuis JSON");
        data.put("count", 3);
        data.put("active", true);
        return data;
    }

    @UrlMapping(value = "/test/view", method = "GET")
    public ModelAndView viewExample() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("title", "Vue de test");
        return new ModelAndView("home", data);
    }
}