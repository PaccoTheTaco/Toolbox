package com.paccothetaco.DiscordBot.Website;

import com.paccothetaco.DiscordBot.DataManager;
import com.paccothetaco.DiscordBot.Ticketsystem.command.TicketEmbedCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
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
    private static DataManager dataManager = new DataManager();

    public static void startServer(JDA jdaInstance) throws Exception {
        jda = jdaInstance;
        Server server = new Server(8080);

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");

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
                    Guild guild = jda.getGuildById(guildId);

                    if (guild != null) {
                        String serverName = guild.getName();
                        String currentWelcomeChannelId = dataManager.getWelcomeChannelId(guildId);
                        String currentLeaveChannelId = dataManager.getLeaveChannelId(guildId);
                        String currentTicketChannelId = dataManager.getTicketChannel(guildId);
                        String currentTicketCategoryId = dataManager.getTicketCategory(guildId);
                        String currentClosedTicketCategoryId = dataManager.getClosedTicketCategory(guildId);
                        String currentWelcomeChannelName = "--deactivated--";
                        String currentLeaveChannelName = "--deactivated--";
                        String currentTicketChannelName = "--deactivated--";
                        String currentTicketCategoryName = "--not set--";
                        String currentClosedTicketCategoryName = "--not set--";

                        if (currentWelcomeChannelId != null) {
                            TextChannel welcomeChannel = guild.getTextChannelById(currentWelcomeChannelId);
                            if (welcomeChannel != null) {
                                currentWelcomeChannelName = welcomeChannel.getName();
                            }
                        }

                        if (currentLeaveChannelId != null) {
                            TextChannel leaveChannel = guild.getTextChannelById(currentLeaveChannelId);
                            if (leaveChannel != null) {
                                currentLeaveChannelName = leaveChannel.getName();
                            }
                        }

                        if (currentTicketChannelId != null) {
                            TextChannel ticketChannel = guild.getTextChannelById(currentTicketChannelId);
                            if (ticketChannel != null) {
                                currentTicketChannelName = ticketChannel.getName();
                            }
                        }

                        if (currentTicketCategoryId != null) {
                            Category ticketCategory = guild.getCategoryById(currentTicketCategoryId);
                            if (ticketCategory != null) {
                                currentTicketCategoryName = ticketCategory.getName();
                            }
                        }

                        if (currentClosedTicketCategoryId != null) {
                            Category closedTicketCategory = guild.getCategoryById(currentClosedTicketCategoryId);
                            if (closedTicketCategory != null) {
                                currentClosedTicketCategoryName = closedTicketCategory.getName();
                            }
                        }

                        Map<String, Boolean> ticketOptions = dataManager.getTicketOptions(guildId);

                        resp.setContentType("text/html");
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().println("<!DOCTYPE html>");
                        resp.getWriter().println("<html lang=\"en\">");
                        resp.getWriter().println("<head>");
                        resp.getWriter().println("    <meta charset=\"UTF-8\">");
                        resp.getWriter().println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
                        resp.getWriter().println("    <title>Settings - Configure Your Server</title>");
                        resp.getWriter().println("    <link rel=\"stylesheet\" href=\"style.css\">");
                        resp.getWriter().println("    <style>");
                        resp.getWriter().println("        .dropdown-menu { display: none; margin-top: 10px; }");
                        resp.getWriter().println("        .dropdown-container { margin-bottom: 20px; }");
                        resp.getWriter().println("        .channel-selection { display: flex; align-items: center; }");
                        resp.getWriter().println("        .channel-selection span { margin-right: 10px; }");
                        resp.getWriter().println("        .dropdown-button { margin-left: 10px; }");
                        resp.getWriter().println("        .ticket-options { margin-top: 20px; }");
                        resp.getWriter().println("        .ticket-option { display: flex; align-items: center; margin-bottom: 10px; }");
                        resp.getWriter().println("    </style>");
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

                        // Welcome Channel Selection
                        resp.getWriter().println("            <div class=\"dropdown-container\">");
                        resp.getWriter().println("                <label>Select Welcome Channel:</label>");
                        resp.getWriter().println("                <div class=\"channel-selection\" id=\"welcomeChannelText\">");
                        resp.getWriter().println("                    <span>" + currentWelcomeChannelName + "</span>");
                        resp.getWriter().println("                    <button type=\"button\" class=\"dropdown-button\" onclick=\"toggleDropdown('welcomeChannel')\">Change</button>");
                        resp.getWriter().println("                </div>");
                        resp.getWriter().println("                <div class=\"dropdown-menu\" id=\"welcomeChannelMenu\">");
                        resp.getWriter().println("                    <select name=\"welcomeChannel\" id=\"welcomeChannel\">");
                        resp.getWriter().println("                        <option value=\"none\">--deactivate--</option>");
                        for (TextChannel channel : guild.getTextChannels()) {
                            resp.getWriter().println("                        <option value=\"" + channel.getId() + "\">" + channel.getName() + "</option>");
                        }
                        resp.getWriter().println("                    </select>");
                        resp.getWriter().println("                </div>");
                        resp.getWriter().println("            </div>");

                        // Leave Channel Selection
                        resp.getWriter().println("            <div class=\"dropdown-container\">");
                        resp.getWriter().println("                <label>Select Leave Channel:</label>");
                        resp.getWriter().println("                <div class=\"channel-selection\" id=\"leaveChannelText\">");
                        resp.getWriter().println("                    <span>" + currentLeaveChannelName + "</span>");
                        resp.getWriter().println("                    <button type=\"button\" class=\"dropdown-button\" onclick=\"toggleDropdown('leaveChannel')\">Change</button>");
                        resp.getWriter().println("                </div>");
                        resp.getWriter().println("                <div class=\"dropdown-menu\" id=\"leaveChannelMenu\">");
                        resp.getWriter().println("                    <select name=\"leaveChannel\" id=\"leaveChannel\">");
                        resp.getWriter().println("                        <option value=\"none\">--deactivate--</option>");
                        for (TextChannel channel : guild.getTextChannels()) {
                            resp.getWriter().println("                        <option value=\"" + channel.getId() + "\">" + channel.getName() + "</option>");
                        }
                        resp.getWriter().println("                    </select>");
                        resp.getWriter().println("                </div>");
                        resp.getWriter().println("            </div>");

                        // Ticket Channel Selection
                        resp.getWriter().println("            <div class=\"dropdown-container\">");
                        resp.getWriter().println("                <label>Select Ticket Channel:</label>");
                        resp.getWriter().println("                <div class=\"channel-selection\" id=\"ticketChannelText\">");
                        resp.getWriter().println("                    <span>" + currentTicketChannelName + "</span>");
                        resp.getWriter().println("                    <button type=\"button\" class=\"dropdown-button\" onclick=\"toggleDropdown('ticketChannel')\">Change</button>");
                        resp.getWriter().println("                </div>");
                        resp.getWriter().println("                <div class=\"dropdown-menu\" id=\"ticketChannelMenu\">");
                        resp.getWriter().println("                    <select name=\"ticketChannel\" id=\"ticketChannel\">");
                        resp.getWriter().println("                        <option value=\"none\">--deactivate--</option>");
                        for (TextChannel channel : guild.getTextChannels()) {
                            resp.getWriter().println("                        <option value=\"" + channel.getId() + "\">" + channel.getName() + "</option>");
                        }
                        resp.getWriter().println("                    </select>");
                        resp.getWriter().println("                </div>");
                        resp.getWriter().println("            </div>");

                        // Ticket Category Selection
                        resp.getWriter().println("            <div class=\"dropdown-container\">");
                        resp.getWriter().println("                <label>Select Ticket Category:</label>");
                        resp.getWriter().println("                <div class=\"channel-selection\" id=\"ticketCategoryText\">");
                        resp.getWriter().println("                    <span>" + currentTicketCategoryName + "</span>");
                        resp.getWriter().println("                    <button type=\"button\" class=\"dropdown-button\" onclick=\"toggleDropdown('ticketCategory')\">Change</button>");
                        resp.getWriter().println("                </div>");
                        resp.getWriter().println("                <div class=\"dropdown-menu\" id=\"ticketCategoryMenu\">");
                        resp.getWriter().println("                    <select name=\"ticketCategory\" id=\"ticketCategory\">");
                        resp.getWriter().println("                        <option value=\"none\">--not set--</option>");
                        for (Category category : guild.getCategories()) {
                            resp.getWriter().println("                        <option value=\"" + category.getId() + "\">" + category.getName() + "</option>");
                        }
                        resp.getWriter().println("                    </select>");
                        resp.getWriter().println("                </div>");
                        resp.getWriter().println("            </div>");

                        // Closed Ticket Category Selection
                        resp.getWriter().println("            <div class=\"dropdown-container\">");
                        resp.getWriter().println("                <label>Select Closed Ticket Category:</label>");
                        resp.getWriter().println("                <div class=\"channel-selection\" id=\"closedTicketCategoryText\">");
                        resp.getWriter().println("                    <span>" + currentClosedTicketCategoryName + "</span>");
                        resp.getWriter().println("                    <button type=\"button\" class=\"dropdown-button\" onclick=\"toggleDropdown('closedTicketCategory')\">Change</button>");
                        resp.getWriter().println("                </div>");
                        resp.getWriter().println("                <div class=\"dropdown-menu\" id=\"closedTicketCategoryMenu\">");
                        resp.getWriter().println("                    <select name=\"closedTicketCategory\" id=\"closedTicketCategory\">");
                        resp.getWriter().println("                        <option value=\"none\">--not set--</option>");
                        for (Category category : guild.getCategories()) {
                            resp.getWriter().println("                        <option value=\"" + category.getId() + "\">" + category.getName() + "</option>");
                        }
                        resp.getWriter().println("                    </select>");
                        resp.getWriter().println("                </div>");
                        resp.getWriter().println("            </div>");

                        // Ticket Options
                        resp.getWriter().println("            <div class=\"ticket-options\">");
                        resp.getWriter().println("                <label>Ticket Options:</label>");
                        resp.getWriter().println("                <div class=\"ticket-option\">");
                        resp.getWriter().println("                    <input type=\"checkbox\" name=\"ticketOptionSupport\" " + (ticketOptions.getOrDefault("support", true) ? "checked" : "") + "> Support");
                        resp.getWriter().println("                </div>");
                        resp.getWriter().println("                <div class=\"ticket-option\">");
                        resp.getWriter().println("                    <input type=\"checkbox\" name=\"ticketOptionReport\" " + (ticketOptions.getOrDefault("report", true) ? "checked" : "") + "> Report");
                        resp.getWriter().println("                </div>");
                        resp.getWriter().println("                <div class=\"ticket-option\">");
                        resp.getWriter().println("                    <input type=\"checkbox\" name=\"ticketOptionApplication\" " + (ticketOptions.getOrDefault("application", true) ? "checked" : "") + "> Application");
                        resp.getWriter().println("                </div>");
                        resp.getWriter().println("            </div>");

                        // Modrole Selection
                        resp.getWriter().println("<div class=\"dropdown-container\">");
                        resp.getWriter().println("    <label>Select Moderator Role:</label>");
                        resp.getWriter().println("    <div class=\"channel-selection\" id=\"modRoleText\">");
                        String currentModRoleId = dataManager.getModRole(guildId);
                        String currentModRoleName = currentModRoleId != null ? guild.getRoleById(currentModRoleId).getName() : "--not selected--";
                        resp.getWriter().println("        <span>" + currentModRoleName + "</span>");
                        resp.getWriter().println("        <button type=\"button\" class=\"dropdown-button\" onclick=\"toggleDropdown('modRole')\">Change</button>");
                        resp.getWriter().println("    </div>");
                        resp.getWriter().println("    <div class=\"dropdown-menu\" id=\"modRoleMenu\">");
                        resp.getWriter().println("        <select name=\"modRole\" id=\"modRole\">");
                        resp.getWriter().println("            <option value=\"none\">--not selected--</option>");
                        for (Role role : guild.getRoles()) {
                            resp.getWriter().println("            <option value=\"" + role.getId() + "\">" + role.getName() + "</option>");
                        }
                        resp.getWriter().println("        </select>");
                        resp.getWriter().println("    </div>");
                        resp.getWriter().println("</div>");


                        resp.getWriter().println("            <input type=\"hidden\" name=\"guildId\" value=\"" + guildId + "\"/>");
                        resp.getWriter().println("            <input type=\"hidden\" name=\"sessionKey\" value=\"" + sessionKey + "\"/>");
                        resp.getWriter().println("            <button type=\"submit\">Save</button>");
                        resp.getWriter().println("        </form>");
                        resp.getWriter().println("    </div>");
                        resp.getWriter().println("    <div class=\"bottom-bar\">Guild ID: " + guildId + "</div>");
                        resp.getWriter().println("    <script>");
                        resp.getWriter().println("        function toggleSidebar() {");
                        resp.getWriter().println("            document.getElementById('sidebar').classList.toggle('open');");
                        resp.getWriter().println("        }");

                        resp.getWriter().println("        function toggleDropdown(channelType) {");
                        resp.getWriter().println("            const menu = document.getElementById(channelType + 'Menu');");
                        resp.getWriter().println("            if (menu.style.display === 'none' || menu.style.display === '') {");
                        resp.getWriter().println("                menu.style.display = 'block';");
                        resp.getWriter().println("            } else {");
                        resp.getWriter().println("                menu.style.display = 'none';");
                        resp.getWriter().println("            }");
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
            String leaveChannelId = req.getParameter("leaveChannel");
            String ticketChannelId = req.getParameter("ticketChannel");
            String ticketCategoryId = req.getParameter("ticketCategory");
            String closedTicketCategoryId = req.getParameter("closedTicketCategory");
            String modRoleId = req.getParameter("modRole");
            String sessionKey = req.getParameter("sessionKey");

            boolean generalChanges = false;
            boolean ticketChanges = false;

            // Welcome Channel Handling
            if ("none".equals(welcomeChannelId)) {
                if (dataManager.isWelcomeActive(guildId)) {
                    dataManager.setWelcomeActive(guildId, false);
                    dataManager.setWelcomeChannel(guildId, null);
                    generalChanges = true;
                }
            } else {
                if (!welcomeChannelId.equals(dataManager.getWelcomeChannelId(guildId))) {
                    dataManager.setWelcomeChannel(guildId, welcomeChannelId);
                    dataManager.setWelcomeActive(guildId, true);
                    generalChanges = true;
                }
            }

            // Leave Channel Handling
            if ("none".equals(leaveChannelId)) {
                if (dataManager.isLeaveActive(guildId)) {
                    dataManager.setLeaveActive(guildId, false);
                    dataManager.setLeaveChannel(guildId, null);
                    generalChanges = true;
                }
            } else {
                if (!leaveChannelId.equals(dataManager.getLeaveChannelId(guildId))) {
                    dataManager.setLeaveChannel(guildId, leaveChannelId);
                    dataManager.setLeaveActive(guildId, true);
                    generalChanges = true;
                }
            }

            // Ticket Channel Handling
            if (ticketChannelId != null && "none".equals(ticketChannelId)) {
                String currentTicketChannelId = dataManager.getTicketChannel(guildId);
                if (currentTicketChannelId != null) {
                    TextChannel channel = jda.getGuildById(guildId).getTextChannelById(currentTicketChannelId);
                    if (channel != null) {
                        dataManager.deleteOldTicketEmbed(guildId, channel);
                    }
                }
                dataManager.setTicketChannel(guildId, null);
                dataManager.setTicketsActive(guildId, false);
            } else if (ticketChannelId != null) {
                dataManager.setTicketChannel(guildId, ticketChannelId);
                dataManager.setTicketsActive(guildId, true);
                ticketChanges = true;
            }

            // Ticket Category Handling
            if (ticketCategoryId != null && !"none".equals(ticketCategoryId)) {
                dataManager.setTicketCategory(guildId, ticketCategoryId);
                generalChanges = true;
            } else if (ticketCategoryId == null || "none".equals(ticketCategoryId)) {
                // Create Ticket Category if not set
                Guild guild = jda.getGuildById(guildId);
                if (guild != null) {
                    Category newCategory = guild.createCategory("Tickets").complete();
                    dataManager.setTicketCategory(guildId, newCategory.getId());
                }
            }

            // Closed Ticket Category Handling
            if (closedTicketCategoryId != null && !"none".equals(closedTicketCategoryId)) {
                dataManager.setClosedTicketCategory(guildId, closedTicketCategoryId);
                generalChanges = true;
            } else if (closedTicketCategoryId == null || "none".equals(closedTicketCategoryId)) {
                // Create Closed Ticket Category if not set
                Guild guild = jda.getGuildById(guildId);
                if (guild != null) {
                    Category newCategory = guild.createCategory("Closed Tickets").complete();
                    dataManager.setClosedTicketCategory(guildId, newCategory.getId());
                }
            }

            // Mod Role Handling
            if ("none".equals(modRoleId)) {
                if (dataManager.getModRole(guildId) != null) {
                    dataManager.setModRole(guildId, null);
                    generalChanges = true;
                }
            } else {
                if (!modRoleId.equals(dataManager.getModRole(guildId))) {
                    dataManager.setModRole(guildId, modRoleId);
                    generalChanges = true;
                }
            }

            // Ticket Options Handling
            boolean supportOption = req.getParameter("ticketOptionSupport") != null;
            boolean reportOption = req.getParameter("ticketOptionReport") != null;
            boolean applicationOption = req.getParameter("ticketOptionApplication") != null;

            if (supportOption != dataManager.getTicketOptions(guildId).get("support") ||
                    reportOption != dataManager.getTicketOptions(guildId).get("report") ||
                    applicationOption != dataManager.getTicketOptions(guildId).get("application")) {
                dataManager.setTicketOption(guildId, "support", supportOption);
                dataManager.setTicketOption(guildId, "report", reportOption);
                dataManager.setTicketOption(guildId, "application", applicationOption);
                ticketChanges = true;
            }

            if (ticketChanges) {
                Guild guild = jda.getGuildById(guildId);
                if (guild != null) {
                    String currentTicketChannelId = dataManager.getTicketChannel(guildId);
                    if (currentTicketChannelId != null) {
                        TextChannel channel = guild.getTextChannelById(currentTicketChannelId);
                        if (channel != null) {
                            TicketEmbedCommand ticketEmbedCommand = new TicketEmbedCommand(dataManager);
                            ticketEmbedCommand.sendNewTicketEmbed(channel, guildId, true);
                        }
                    }
                }
            }

            if (generalChanges || ticketChanges) {
                dataManager.notifyListeners(guildId);
            }

            resp.sendRedirect("/settings?sk=" + sessionKey);
        }
    }

    public static class VerifyServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        }
    }
}
