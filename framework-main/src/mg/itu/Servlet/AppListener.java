package mg.itu.Servlet;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import mg.itu.annotation.Controller.Controller;
import mg.itu.annotation.Json.Json;
import mg.itu.annotation.Url.UrlMapping;
import mg.itu.utils.MethodInfo;
import mg.itu.utils.PackageScanner;
import mg.itu.utils.UrlMethod;

/** Scans application controllers once and publishes their routes to the servlet context. */
public class AppListener implements ServletContextListener {
    public static final String MAPPINGS_ATTRIBUTE = AppListener.class.getName() + ".mappings";
    public static final String VIEW_PREFIX_ATTRIBUTE = AppListener.class.getName() + ".viewPrefix";
    public static final String VIEW_SUFFIX_ATTRIBUTE = AppListener.class.getName() + ".viewSuffix";
    public static final String INIT_ERROR_ATTRIBUTE = AppListener.class.getName() + ".initError";
    public static volatile Map<UrlMethod, MethodInfo> urlMethodMappings = Collections.emptyMap();
    public static volatile String viewPrefix = "/WEB-INF/views/";
    public static volatile String viewSuffix = ".jsp";
    public static volatile RuntimeException initError;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        viewPrefix = valueOrDefault(context.getInitParameter("viewPrefix"), "/WEB-INF/views/");
        viewSuffix = valueOrDefault(context.getInitParameter("viewSuffix"), ".jsp");
        String packageName = valueOrDefault(context.getInitParameter("controllerPackage"), "com.framework.corps");
        Map<UrlMethod, MethodInfo> routes = new LinkedHashMap<>();
        initError = null;

        try {
            List<String> classNames = PackageScanner.getClasses(packageName);
            for (String className : classNames) {
                Class<?> type = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
                if (!type.isAnnotationPresent(Controller.class)) continue;
                for (Method method : type.getDeclaredMethods()) {
                    UrlMapping mapping = method.getAnnotation(UrlMapping.class);
                    if (mapping == null) continue;
                    String url = normalizeUrl(mapping.value());
                    String httpMethod = valueOrDefault(mapping.method(), "GET").toUpperCase();
                    if (!List.of("GET", "POST", "PUT", "DELETE").contains(httpMethod)) {
                        throw new IllegalStateException("Unsupported HTTP method " + httpMethod + " on " + className + "." + method.getName());
                    }
                    UrlMethod key = new UrlMethod(url, httpMethod);
                    MethodInfo previous = routes.putIfAbsent(key,
                            new MethodInfo(className, method.getName(), method.isAnnotationPresent(Json.class)));
                    if (previous != null) {
                        throw new IllegalStateException("Duplicate route " + httpMethod + " " + url + " mapped to " + previous.className + "." + previous.methodName + " and " + className + "." + method.getName());
                    }
                }
            }
            urlMethodMappings = Collections.unmodifiableMap(routes);
        } catch (Exception exception) {
            initError = exception instanceof RuntimeException ? (RuntimeException) exception : new IllegalStateException("Could not initialize MVC routes", exception);
            context.log("MVC route initialization failed", initError);
        }

        context.setAttribute(MAPPINGS_ATTRIBUTE, urlMethodMappings);
        context.setAttribute(VIEW_PREFIX_ATTRIBUTE, viewPrefix);
        context.setAttribute(VIEW_SUFFIX_ATTRIBUTE, viewSuffix);
        context.setAttribute(INIT_ERROR_ATTRIBUTE, initError);
        context.log("MVC initialized with " + urlMethodMappings.size() + " route(s) from " + packageName);
    }

    private static String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String normalizeUrl(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("@UrlMapping requires a URL");
        return value.startsWith("/") ? value : "/" + value;
    }
}
