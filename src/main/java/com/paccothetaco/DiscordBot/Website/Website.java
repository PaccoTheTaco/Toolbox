package com.paccothetaco.DiscordBot.Website;

import com.paccothetaco.DiscordBot.DataManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.JDA;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

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
    private static JDA jda;
    private static DataManager dataManager = new DataManager(); // Initialisiere DataManager

    public static void startServer(JDA jdaInstance) throws Exception {
        jda = jdaInstance; // Store the JDA instance
        Server server = new Server(8080);

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");

        // Set the resource base to the directory containing your HTML and CSS files
        String resourceBase = System.getProperty("user.dir") + "/src/main/java/com/paccothetaco/DiscordBot/Website";
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
                    // Fetch the Guild object using the stored JDA instance
                    Guild guild = jda.getGuildById(guildId);

                    if (guild != null) {
                        String serverName = guild.getName();
                        String currentWelcomeChannelId = dataManager.getWelcomeChannelId(guildId);

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
                        resp.getWriter().println("        <h1>Settings for " + serverName + "</h1>");
                        resp.getWriter().println("        <p>Here you can configure your Discord server settings.</p>");
                        resp.getWriter().println("        <form method=\"POST\" action=\"/settings\">");
                        resp.getWriter().println("            <label for=\"welcomeChannel\">Select Welcome Channel:</label>");
                        resp.getWriter().println("            <select name=\"welcomeChannel\" id=\"welcomeChannel\">");
                        resp.getWriter().println("                <option value=\"none\">--deactivate--</option>");

                        for (TextChannel channel : guild.getTextChannels()) {
                            String selected = channel.getId().equals(currentWelcomeChannelId) ? "selected" : "";
                            try {resp.getWriter().println("<option value=\"" + channel.getId() + "\" " + selected + ">" + channel.getName() + "</option>");}
                            catch (IOException e) {throw new RuntimeException(e);}
                        }

                        resp.getWriter().println("            </select>");
                        resp.getWriter().println("            <input type=\"hidden\" name=\"guildId\" value=\"" + guildId + "\"/>");
                        resp.getWriter().println("            <input type=\"hidden\" name=\"sessionKey\" value=\"" + sessionKey + "\"/>"); // Hier den sessionKey hinzufügen
                        resp.getWriter().println("            <button type=\"submit\">Save</button>");
                        resp.getWriter().println("        </form>");
                        resp.getWriter().println("    </div>");
                        resp.getWriter().println("    <div class=\"bottom-bar\">Guild ID: " + guildId + "</div>");
                        resp.getWriter().println("    <script>");
                        resp.getWriter().println("        function toggleSidebar() {");
                        resp.getWriter().println("            document.getElementById('sidebar').classList.toggle('open');");
                        resp.getWriter().println("        }");
                        resp.getWriter().println("    </script>");
                        resp.getWriter().println("</body>");
                        resp.getWriter().println("</html>");
                    } else {
                        resp.setContentType("text/html");
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().println("<h1>Error</h1>");
                        resp.getWriter().println("<p>Could not retrieve server details.</p>");
                    }
                } else {
                    resp.setContentType("text/html");
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().println("<!DOCTYPE html>");
                    resp.getWriter().println("<html lang=\"en\">");
                    resp.getWriter().println("<head>");
                    resp.getWriter().println("    <meta charset=\"UTF-8\">");
                    resp.getWriter().println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
                    resp.getWriter().println("    <title>Verify - Admin Verification</title>");
                    resp.getWriter().println("    <link rel=\"stylesheet\" href=\"style.css\">");
                    resp.getWriter().println("    <style>");
                    resp.getWriter().println("        .verify-content {");
                    resp.getWriter().println("            display: flex;");
                    resp.getWriter().println("            flex-direction: column;");
                    resp.getWriter().println("            align-items: center;");
                    resp.getWriter().println("            justify-content: center;");
                    resp.getWriter().println("            text-align: center;");
                    resp.getWriter().println("            flex-grow: 1;");
                    resp.getWriter().println("            padding: 20px;");
                    resp.getWriter().println("            width: 100%;");
                    resp.getWriter().println("            color: white;");
                    resp.getWriter().println("        }");
                    resp.getWriter().println("        .copy-button {");
                    resp.getWriter().println("            margin-top: 20px;");
                    resp.getWriter().println("            padding: 10px 20px;");
                    resp.getWriter().println("            font-size: 1rem;");
                    resp.getWriter().println("            color: white;");
                    resp.getWriter().println("            background-color: #444;");
                    resp.getWriter().println("            border: none;");
                    resp.getWriter().println("            cursor: pointer;");
                    resp.getWriter().println("            border-radius: 5px;");
                    resp.getWriter().println("        }");
                    resp.getWriter().println("        .copy-button:hover {");
                    resp.getWriter().println("            background-color: #555;");
                    resp.getWriter().println("        }");
                    resp.getWriter().println("    </style>");
                    resp.getWriter().println("</head>");
                    resp.getWriter().println("<body>");
                    resp.getWriter().println("    <div class=\"verify-content\">");
                    resp.getWriter().println("        <h1>Verify that you are an Admin</h1>");
                    resp.getWriter().println("        <p>Use <code id=\"verify-command\">/verify sessionkey:" + sessionKey + "</code> on the Discord Server</p>");
                    resp.getWriter().println("        <button class=\"copy-button\" onclick=\"copyToClipboard()\">Copy Command</button>");
                    resp.getWriter().println("        <p>Then refresh this page.</p>");
                    resp.getWriter().println("    </div>");
                    resp.getWriter().println("    <script>");
                    resp.getWriter().println("        function copyToClipboard() {");
                    resp.getWriter().println("            const command = document.getElementById('verify-command').textContent;");
                    resp.getWriter().println("            navigator.clipboard.writeText(command).then(function() {");
                    resp.getWriter().println("                alert('Command copied to clipboard');");
                    resp.getWriter().println("            }, function(err) {");
                    resp.getWriter().println("                console.error('Could not copy text: ', err);");
                    resp.getWriter().println("            });");
                    resp.getWriter().println("        }");
                    resp.getWriter().println("    </script>");
                    resp.getWriter().println("</body>");
                    resp.getWriter().println("</html>");
                }
            } else {
                resp.setContentType("text/html");
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().println("<h1>Unauthorized</h1>");
                resp.getWriter().println("<p>Invalid session key</p>");
            }
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String guildId = req.getParameter("guildId");
            String welcomeChannelId = req.getParameter("welcomeChannel");
            String sessionKey = req.getParameter("sessionKey");

            if ("none".equals(welcomeChannelId)) {
                // Deactivate welcome messages
                dataManager.setWelcomeActive(guildId, false);
            } else {
                // Set welcome channel
                dataManager.setWelcomeChannel(guildId, welcomeChannelId);
                dataManager.setWelcomeActive(guildId, true);
            }
            dataManager.notifyListeners(guildId);  // Benachrichtige Listener nach einer Änderung
            resp.sendRedirect("/settings?sk=" + sessionKey);
        }
    }

    public static class VerifyServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            // No implementation needed, verification happens via Discord
        }
    }
}
