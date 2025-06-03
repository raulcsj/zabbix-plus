package io.zabbixplus.framework.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class VueAppController {

    // Forward all routes that are not API calls (e.g. /api/**)
    // and not requests for actual static files (e.g. /main.js, /css/style.css)
    // to index.html. This allows Vue Router to handle frontend routing.
    // The regex '^(?!api|static|.*\..*).*$' tries to achieve this:
    // - ^(?!api|static) -> not starting with /api or /static
    // - (?!.*\..*) -> not containing a dot (like .js, .css) in the last path segment
    // A simpler approach might be needed if this regex is problematic in Spring's path matching.
    // For instance, just map "/" and "/ui/**" or specific known Vue app paths.
    // Using a slightly more robust regex that should work with Spring MVC path patterns
    @RequestMapping(value = {"/", "/{path:^(?!api|static|assets|.*\\.[a-zA-Z0-9]+$).*}/**"})
    public String serveVueApp() {
        return "forward:/index.html";
    }
}
