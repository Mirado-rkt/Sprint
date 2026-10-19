package com.framework.corps;

import java.util.LinkedHashMap;
import java.util.Map;

import mg.itu.annotation.Controller.Controller;
import mg.itu.annotation.Json.Json;
import mg.itu.annotation.Url.UrlMapping;
import mg.itu.utils.ModelAndView;
import mg.itu.utils.MapFile;
import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;

@Controller
public class TestController {

    @UrlMapping(value = "/test", method = "GET")
    public ModelAndView home() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("title", "Mini framework MVC");
        return new ModelAndView("home", data);
    }

    @UrlMapping(value = "/test/form", method = "POST")
    public ModelAndView submit(String name) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("title", "Formulaire envoyé");
        data.put("name", name);
        return new ModelAndView("home", data);
    }

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

    @UrlMapping(value = "/test/upload", method = "POST")
    public ModelAndView upload(MapFile file, HttpServletRequest request) throws Exception {
        Map<String, Object> data = new LinkedHashMap<>();
        if (file != null && file.getFileName() != null && !file.getFileName().isBlank()) {
            String uploadRoot = request.getServletContext().getRealPath("/uploads");
            if (uploadRoot == null) throw new IllegalStateException("The servlet container does not expose a writable deployment directory");
            Path saved = file.saveTo(Path.of(uploadRoot));
            data.put("name", saved.getFileName().toString());
        } else {
            data.put("name", "Aucun fichier choisi");
        }
        data.put("title", "Résultat du téléchargement");
        return new ModelAndView("home", data);
    }
}
