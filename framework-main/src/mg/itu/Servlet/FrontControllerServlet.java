package mg.itu.Servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import mg.itu.utils.JsonFormatter;
import mg.itu.utils.MapFile;
import mg.itu.utils.MethodInfo;
import mg.itu.utils.ModelAndView;
import mg.itu.utils.UrlMethod;

/** Single entry point for controller routes. */
public class FrontControllerServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { process(req, resp); }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { process(req, resp); }
    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { process(req, resp); }
    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { process(req, resp); }

    private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RuntimeException initError = (RuntimeException) getServletContext().getAttribute(AppListener.INIT_ERROR_ATTRIBUTE);
        if (initError == null) initError = AppListener.initError;
        if (initError != null) throw new ServletException("MVC route initialization failed", initError);

        String path = request.getRequestURI().substring(request.getContextPath().length());
        String verb = request.getMethod().toUpperCase();
        Map<UrlMethod, MethodInfo> routes = AppListener.urlMethodMappings;
        @SuppressWarnings("unchecked")
        Map<UrlMethod, MethodInfo> configured = (Map<UrlMethod, MethodInfo>) getServletContext().getAttribute(AppListener.MAPPINGS_ATTRIBUTE);
        if (configured != null) routes = configured;
        MethodInfo info = routes.get(new UrlMethod(path, verb));
        if (info == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<!doctype html><html><head><meta charset=\"utf-8\"><title>404</title></head><body><h1>404 — route introuvable</h1><p>" + escape(verb + " " + path) + "</p><h2>Routes disponibles</h2><ul>");
            for (Map.Entry<UrlMethod, MethodInfo> route : routes.entrySet()) {
                response.getWriter().write("<li>" + escape(route.getKey().getMethod() + " " + route.getKey().getUrl()) + "</li>");
            }
            response.getWriter().write("</ul></body></html>");
            return;
        }

        try {
            Class<?> controllerClass = Class.forName(info.className);
            Object controller = controllerClass.getDeclaredConstructor().newInstance();
            Method handler = findHandler(controllerClass, info.methodName);
            handler.setAccessible(true);
            Object result = handler.invoke(controller, resolveArguments(handler, request, response));
            render(result, info, request, response);
        } catch (Exception exception) {
            Throwable cause = exception instanceof java.lang.reflect.InvocationTargetException && exception.getCause() != null
                    ? exception.getCause() : exception;
            throw new ServletException("Error handling " + verb + " " + path, cause);
        }
    }

    private Method findHandler(Class<?> controllerClass, String name) throws NoSuchMethodException {
        for (Method method : controllerClass.getDeclaredMethods()) if (method.getName().equals(name)) return method;
        throw new NoSuchMethodException(controllerClass.getName() + "." + name);
    }

    private Object[] resolveArguments(Method method, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] values = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> type = parameters[i].getType();
            String name = parameters[i].getName();
            if (type == HttpServletRequest.class) values[i] = request;
            else if (type == HttpServletResponse.class) values[i] = response;
            else if (type == Part.class) values[i] = request.getPart(name);
            else if (type == MapFile.class) {
                Part part = request.getPart(name);
                values[i] = part == null ? null : MapFile.from(part);
            } else {
                String raw = request.getParameter(name);
                if (raw == null && type.isPrimitive()) throw new IllegalArgumentException("Missing required parameter: " + name);
                values[i] = convert(raw, type);
            }
        }
        return values;
    }

    private Object convert(String value, Class<?> type) {
        if (value == null) return null;
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.valueOf(value);
        if (type == long.class || type == Long.class) return Long.valueOf(value);
        if (type == double.class || type == Double.class) return Double.valueOf(value);
        if (type == float.class || type == Float.class) return Float.valueOf(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.valueOf(value);
        if (type == short.class || type == Short.class) return Short.valueOf(value);
        if (type == byte.class || type == Byte.class) return Byte.valueOf(value);
        if (type == char.class || type == Character.class) {
            if (value.length() != 1) throw new IllegalArgumentException("Expected one character");
            return value.charAt(0);
        }
        throw new IllegalArgumentException("Unsupported controller parameter type: " + type.getName());
    }

    private void render(Object result, MethodInfo info, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (result instanceof ModelAndView) {
            ModelAndView view = (ModelAndView) result;
            if (view.getData() != null) view.getData().forEach(request::setAttribute);
            String prefix = (String) getServletContext().getAttribute(AppListener.VIEW_PREFIX_ATTRIBUTE);
            String suffix = (String) getServletContext().getAttribute(AppListener.VIEW_SUFFIX_ATTRIBUTE);
            if (prefix == null) prefix = AppListener.viewPrefix;
            if (suffix == null) suffix = AppListener.viewSuffix;
            RequestDispatcher dispatcher = request.getRequestDispatcher(prefix + view.getViewName() + suffix);
            if (dispatcher == null) throw new ServletException("View not found: " + view.getViewName());
            dispatcher.forward(request, response);
        } else if (result instanceof String && !info.isJson()) {
            String string = (String) result;
            String prefix = AppListener.viewPrefix;
            String suffix = AppListener.viewSuffix;
            RequestDispatcher dispatcher = request.getRequestDispatcher(prefix + string + suffix);
            dispatcher.forward(request, response);
        } else if (info.isJson()) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(result instanceof String ? (String) result : JsonFormatter.toJson(result));
        } else if (result != null) {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write(result.toString());
        } else if (!response.isCommitted()) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private String escape(String input) {
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
