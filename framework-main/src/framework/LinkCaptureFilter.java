package framework;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class LinkCaptureFilter implements Filter {

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        String uri = request.getRequestURI();

        LinkCollector.add(uri);
        chain.doFilter(req, res);
    }

    public void init(FilterConfig config) {
    }

    public void destroy() {
    }
}
