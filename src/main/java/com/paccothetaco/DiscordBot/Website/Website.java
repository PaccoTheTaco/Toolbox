package com.paccothetaco.DiscordBot.Website;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.DefaultServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Website {
    private static Map<String, String> sessionKeys = new HashMap<>();
    private static Map<String, String> verifiedSessionKeys = new HashMap<>();

    public static void startServer() throws Exception {
        Server server = new Server(8080);

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");

        // Set the resource base to the current directory
        String resourceBase = System.getProperty("user.dir");
        handler.setResourceBase(resourceBase);
        handler.addServlet(DefaultServlet.class, "/");

        server.setHandler(handler);

        handler.addServlet(new ServletHolder(new SettingsServlet()), "/settings");
        handler.addServlet(new ServletHolder(new VerifyServlet()), "/verify");

        server.start();
        server.join();
    }

    public static void addSessionKey(String sessionKey, String guildId) {
        sessionKeys.put(sessionKey, guildId);
    }

    public static String getGuildId(String sessionKey) {
        return sessionKeys.get(sessionKey);
    }

    public static void addVerifiedSessionKey(String sessionKey, String guildId) {
        verifiedSessionKeys.put(sessionKey, guildId);
    }

    public static boolean isSessionKeyVerified(String sessionKey) {
        return verifiedSessionKeys.containsKey(sessionKey);
    }

    public static class SettingsServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String sessionKey = req.getParameter("sk");
            String guildId = Website.getGuildId(sessionKey);

            if (guildId != null) {
                if (Website.isSessionKeyVerified(sessionKey)) {
                    resp.setContentType("text/html");
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().println("<!DOCTYPE html>");
                    resp.getWriter().println("<html lang=\"en\">");
                    resp.getWriter().println("<head>");
                    resp.getWriter().println("    <meta charset=\"UTF-8\">");
                    resp.getWriter().println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
                    resp.getWriter().println("    <title>Settings - Configure Your Server</title>");
                    resp.getWriter().println("    <link rel=\"stylesheet\" href=\"style.css\">");
                    resp.getWriter().println("</head>");
                    resp.getWriter().println("<body>");
                    resp.getWriter().println("    <div class=\"burger-menu\" onclick=\"toggleSidebar()\">");
                    resp.getWriter().println("        <div></div>");
                    resp.getWriter().println("        <div></div>");
                    resp.getWriter().println("        <div></div>");
                    resp.getWriter().println("    </div>");
                    resp.getWriter().println("    <div class=\"sidebar\" id=\"sidebar\">");
                    resp.getWriter().println("        <a href=\"Home.html\">Home</a>");
                    resp.getWriter().println("        <a href=\"Socials.html\">Socials</a>");
                    resp.getWriter().println("        <a href=\"#\">Plugins</a>");
                    resp.getWriter().println("        <a href=\"#\">Projects</a>");
                    resp.getWriter().println("        <a href=\"Settings.html\">Settings</a>");
                    resp.getWriter().println("    </div>");
                    resp.getWriter().println("    <div class=\"content settings-content\">");
                    resp.getWriter().println("        <h1>Settings Page for Guild: " + guildId + "</h1>");
                    resp.getWriter().println("        <p>Here you can configure your Discord server settings.</p>");
                    // Hier können die eigentlichen Einstellungen angezeigt werden
                    resp.getWriter().println("    </div>");
                    resp.getWriter().println("    <div class=\"bottom-bar\"></div>");
                    resp.getWriter().println("    <script>");
                    resp.getWriter().println("        function toggleSidebar() {");
                    resp.getWriter().println("            document.getElementById('sidebar').classList.toggle('open');");
                    resp.getWriter().println("        }");
                    resp.getWriter().println("    </script>");
                    resp.getWriter().println("</body>");
                    resp.getWriter().println("</html>");
                } else {
                    resp.setContentType("text/html");
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().println("<h1>Verify that you are an Admin</h1>");
                    resp.getWriter().println("<p>Use <code>/verify sessionkey:" + sessionKey + "</code> on the Discord Server</p>");
                    resp.getWriter().println("<p>Then refresh this page.</p>");
                }
            } else {
                resp.setContentType("text/html");
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().println("<h1>Unauthorized</h1>");
                resp.getWriter().println("<p>Invalid session key</p>");
            }
        }
    }

    public static class VerifyServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            // No implementation needed, verification happens via Discord
        }
    }
}
