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
import java.nio.file.Files;
import java.nio.file.Paths;
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

        // Setze das Basisverzeichnis für statische Dateien
        String resourceBase = System.getProperty("user.dir") + "/src/main/java/com/paccothetaco/DiscordBot/Website/HTMLDocuments";
        handler.setResourceBase(resourceBase);
        handler.addServlet(DefaultServlet.class, "/");

        server.setHandler(handler);

        // Füge weitere Servlets hinzu, wenn notwendig
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

    private static String readFileAsString(String file) throws IOException {
        return new String(Files.readAllBytes(Paths.get(file)));
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
                        String currentMessageLogChannelId = dataManager.getMessageLogChannel(guildId);
                        String currentTicketCategoryId = dataManager.getTicketCategory(guildId);
                        String currentClosedTicketCategoryId = dataManager.getClosedTicketCategory(guildId);
                        String currentModRoleId = dataManager.getModRole(guildId);
                        String currentWelcomeChannelName = "--deactivated--";
                        String currentLeaveChannelName = "--deactivated--";
                        String currentTicketChannelName = "--deactivated--";
                        String currentMessageLogChannelName = "--deactivated--";
                        String currentTicketCategoryName = "--not set--";
                        String currentClosedTicketCategoryName = "--not set--";
                        String currentModRoleName = "--not selected--";
                        boolean changeNameLogActive = dataManager.isChangeNameLogActive(guildId);
                        boolean changeNicknameLogActive = dataManager.isChangeNicknameLogActive(guildId);
                        boolean voiceChannelLogActive = dataManager.isVoiceChannelLogActive(guildId);
                        boolean channelLogActive = dataManager.isChannelLogActive(guildId);
                        boolean modLogActive = dataManager.isModLogActive(guildId); // New line

                        // Get channel, category, and role options
                        String channelOptions = "";
                        String categoryOptions = "";
                        String roleOptions = "";
                        for (TextChannel channel : guild.getTextChannels()) {
                            channelOptions += "<option value=\"" + channel.getId() + "\">" + channel.getName() + "</option>";
                        }
                        for (Category category : guild.getCategories()) {
                            categoryOptions += "<option value=\"" + category.getId() + "\">" + category.getName() + "</option>";
                        }
                        for (Role role : guild.getRoles()) {
                            roleOptions += "<option value=\"" + role.getId() + "\">" + role.getName() + "</option>";
                        }

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

                        if (currentMessageLogChannelId != null) {
                            TextChannel messageLogChannel = guild.getTextChannelById(currentMessageLogChannelId);
                            if (messageLogChannel != null) {
                                currentMessageLogChannelName = messageLogChannel.getName();
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

                        if (currentModRoleId != null) {
                            Role modRole = guild.getRoleById(currentModRoleId);
                            if (modRole != null) {
                                currentModRoleName = modRole.getName();
                            }
                        }

                        Map<String, Boolean> ticketOptions = dataManager.getTicketOptions(guildId);

                        String htmlTemplate = readFileAsString(System.getProperty("user.dir") + "/src/main/java/com/paccothetaco/DiscordBot/Website/HTMLDocuments/settings.html");
                        htmlTemplate = htmlTemplate.replace("<!-- SERVER_NAME -->", serverName != null ? serverName : "")
                                .replace("<!-- WELCOME_CHANNEL_NAME -->", currentWelcomeChannelName != null ? currentWelcomeChannelName : "--deactivated--")
                                .replace("<!-- LEAVE_CHANNEL_NAME -->", currentLeaveChannelName != null ? currentLeaveChannelName : "--deactivated--")
                                .replace("<!-- TICKET_CHANNEL_NAME -->", currentTicketChannelName != null ? currentTicketChannelName : "--deactivated--")
                                .replace("<!-- MESSAGE_LOG_CHANNEL_NAME -->", currentMessageLogChannelName != null ? currentMessageLogChannelName : "--deactivated--")
                                .replace("<!-- TICKET_CATEGORY_NAME -->", currentTicketCategoryName != null ? currentTicketCategoryName : "--not set--")
                                .replace("<!-- CLOSED_TICKET_CATEGORY_NAME -->", currentClosedTicketCategoryName != null ? currentClosedTicketCategoryName : "--not set--")
                                .replace("<!-- CHANNEL_OPTIONS_WELCOME -->", channelOptions != null ? channelOptions : "")
                                .replace("<!-- CHANNEL_OPTIONS_LEAVE -->", channelOptions != null ? channelOptions : "")
                                .replace("<!-- CHANNEL_OPTIONS_TICKET -->", channelOptions != null ? channelOptions : "")
                                .replace("<!-- CHANNEL_OPTIONS_MESSAGE_LOG -->", channelOptions != null ? channelOptions : "")
                                .replace("<!-- CATEGORY_OPTIONS_TICKET -->", categoryOptions != null ? categoryOptions : "")
                                .replace("<!-- CATEGORY_OPTIONS_CLOSED_TICKET -->", categoryOptions != null ? categoryOptions : "")
                                .replace("<!-- ROLE_OPTIONS -->", roleOptions != null ? roleOptions : "")
                                .replace("<!-- TICKET_OPTION_SUPPORT -->", ticketOptions.get("support") ? "checked" : "")
                                .replace("<!-- TICKET_OPTION_REPORT -->", ticketOptions.get("report") ? "checked" : "")
                                .replace("<!-- TICKET_OPTION_APPLICATION -->", ticketOptions.get("application") ? "checked" : "")
                                .replace("<!-- MOD_ROLE_NAME -->", currentModRoleName != null ? currentModRoleName : "--not selected--")
                                .replace("<!-- MOD_ROLE_ID -->", currentModRoleId != null ? currentModRoleId : "")
                                .replace("<!-- GUILD_ID -->", guildId != null ? guildId : "")
                                .replace("<!-- SESSION_KEY -->", sessionKey != null ? sessionKey : "")
                                .replace("<!-- CHANGE_NAME_LOG_ACTIVE -->", changeNameLogActive ? "checked" : "")
                                .replace("<!-- CHANGE_NICKNAME_LOG_ACTIVE -->", changeNicknameLogActive ? "checked" : "")
                                .replace("<!-- VOICE_CHANNEL_LOG_ACTIVE -->", voiceChannelLogActive ? "checked" : "")
                                .replace("<!-- CHANNEL_LOG_ACTIVE -->", channelLogActive ? "checked" : "")
                                .replace("<!-- MOD_LOG_ACTIVE -->", modLogActive ? "checked" : "") // New line
                                .replace("<!-- CURRENT_WELCOME_CHANNEL_ID -->", currentWelcomeChannelId != null ? currentWelcomeChannelId : "")
                                .replace("<!-- CURRENT_LEAVE_CHANNEL_ID -->", currentLeaveChannelId != null ? currentLeaveChannelId : "")
                                .replace("<!-- CURRENT_TICKET_CHANNEL_ID -->", currentTicketChannelId != null ? currentTicketChannelId : "")
                                .replace("<!-- CURRENT_MESSAGE_LOG_CHANNEL_ID -->", currentMessageLogChannelId != null ? currentMessageLogChannelId : "")
                                .replace("<!-- CURRENT_TICKET_CATEGORY_ID -->", currentTicketCategoryId != null ? currentTicketCategoryId : "")
                                .replace("<!-- CURRENT_CLOSED_TICKET_CATEGORY_ID -->", currentClosedTicketCategoryId != null ? currentClosedTicketCategoryId : "")
                                .replace("<!-- CURRENT_MOD_ROLE_ID -->", currentModRoleId != null ? currentModRoleId : "");

                        resp.setContentType("text/html");
                        resp.getWriter().write(htmlTemplate);
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
                    resp.getWriter().println("        html, body {");
                    resp.getWriter().println("            margin: 0;");
                    resp.getWriter().println("            padding: 0;");
                    resp.getWriter().println("            height: 100%;");
                    resp.getWriter().println("            width: 100%;");
                    resp.getWriter().println("            display: flex;");
                    resp.getWriter().println("            justify-content: center;");
                    resp.getWriter().println("            align-items: center;");
                    resp.getWriter().println("            background: radial-gradient(circle, rgba(126, 87, 241, 0.7) 0%, rgba(69, 73, 204, 0.7) 100%);");
                    resp.getWriter().println("            color: white;");
                    resp.getWriter().println("            text-align: center;");
                    resp.getWriter().println("            font-family: Arial, sans-serif;");
                    resp.getWriter().println("        }");
                    resp.getWriter().println("        .verify-content {");
                    resp.getWriter().println("            display: flex;");
                    resp.getWriter().println("            flex-direction: column;");
                    resp.getWriter().println("            align-items: center;");
                    resp.getWriter().println("            justify-content: center;");
                    resp.getWriter().println("            padding: 20px;");
                    resp.getWriter().println("            border-radius: 10px;");
                    resp.getWriter().println("            background: rgba(0, 0, 0, 0.5);");
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
            String messageLogChannelId = req.getParameter("messageLogChannel");
            String ticketCategoryId = req.getParameter("ticketCategory");
            String closedTicketCategoryId = req.getParameter("closedTicketCategory");
            String modRoleId = req.getParameter("modRole");
            String sessionKey = req.getParameter("sessionKey");

            boolean generalChanges = false;
            boolean ticketChanges = false;

            // Welcome Channel Handling
            String currentWelcomeChannelId = dataManager.getWelcomeChannelId(guildId);
            if (welcomeChannelId != null && !welcomeChannelId.equals(currentWelcomeChannelId)) {
                if ("none".equals(welcomeChannelId)) {
                    if (currentWelcomeChannelId != null) {
                        dataManager.setWelcomeChannel(guildId, null);
                        dataManager.setWelcomeActive(guildId, false);
                        generalChanges = true;
                    }
                } else {
                    dataManager.setWelcomeChannel(guildId, welcomeChannelId);
                    dataManager.setWelcomeActive(guildId, true);
                    generalChanges = true;
                }
            }

            // Leave Channel Handling
            String currentLeaveChannelId = dataManager.getLeaveChannelId(guildId);
            if (leaveChannelId != null && !leaveChannelId.equals(currentLeaveChannelId)) {
                if ("none".equals(leaveChannelId)) {
                    if (currentLeaveChannelId != null) {
                        dataManager.setLeaveChannel(guildId, null);
                        dataManager.setLeaveActive(guildId, false);
                        generalChanges = true;
                    }
                } else {
                    dataManager.setLeaveChannel(guildId, leaveChannelId);
                    dataManager.setLeaveActive(guildId, true);
                    generalChanges = true;
                }
            }

            // Ticket Channel Handling
            String currentTicketChannelId = dataManager.getTicketChannel(guildId);
            if (ticketChannelId != null && !ticketChannelId.equals(currentTicketChannelId)) {
                if ("none".equals(ticketChannelId)) {
                    if (currentTicketChannelId != null) {
                        TextChannel channel = jda.getGuildById(guildId).getTextChannelById(currentTicketChannelId);
                        if (channel != null) {
                            dataManager.deleteOldTicketEmbed(guildId, channel);
                        }
                        dataManager.setTicketChannel(guildId, null);
                        dataManager.setTicketsActive(guildId, false);
                        ticketChanges = true;
                    }
                } else {
                    dataManager.setTicketChannel(guildId, ticketChannelId);
                    dataManager.setTicketsActive(guildId, true);
                    ticketChanges = true;
                }
            }

            // Message Log Channel Handling
            String currentMessageLogChannelId = dataManager.getMessageLogChannel(guildId);
            if (messageLogChannelId != null && !messageLogChannelId.equals(currentMessageLogChannelId)) {
                if ("none".equals(messageLogChannelId)) {
                    if (currentMessageLogChannelId != null) {
                        dataManager.deactivateMessageLog(guildId);
                        generalChanges = true;
                    }
                } else {
                    dataManager.setMessageLogChannel(guildId, messageLogChannelId);
                    generalChanges = true;
                }
            }

            // Ticket Category Handling
            if (ticketCategoryId != null && !ticketCategoryId.equals(dataManager.getTicketCategory(guildId)) && !"none".equals(ticketCategoryId)) {
                dataManager.setTicketCategory(guildId, ticketCategoryId);
                generalChanges = true;
            }

            // Closed Ticket Category Handling
            if (closedTicketCategoryId != null && !closedTicketCategoryId.equals(dataManager.getClosedTicketCategory(guildId)) && !"none".equals(closedTicketCategoryId)) {
                dataManager.setClosedTicketCategory(guildId, closedTicketCategoryId);
                generalChanges = true;
            }

            // Mod Role Handling
            String currentModRoleId = dataManager.getModRole(guildId);
            if (modRoleId != null && !modRoleId.equals(currentModRoleId)) {
                if ("none".equals(modRoleId)) {
                    if (currentModRoleId != null) {
                        dataManager.setModRole(guildId, null);
                        generalChanges = true;
                    }
                } else {
                    dataManager.setModRole(guildId, modRoleId);
                    generalChanges = true;
                }
            }

            // Ticket Options Handling
            boolean supportOption = req.getParameter("ticketOptionSupport") != null;
            boolean reportOption = req.getParameter("ticketOptionReport") != null;
            boolean applicationOption = req.getParameter("ticketOptionApplication") != null;

            Map<String, Boolean> currentTicketOptions = dataManager.getTicketOptions(guildId);
            if (supportOption != currentTicketOptions.get("support") ||
                    reportOption != currentTicketOptions.get("report") ||
                    applicationOption != currentTicketOptions.get("application")) {
                dataManager.setTicketOption(guildId, "support", supportOption);
                dataManager.setTicketOption(guildId, "report", reportOption);
                dataManager.setTicketOption(guildId, "application", applicationOption);
                ticketChanges = true;
            }

            // Log Options Handling
            boolean changeNameLogOption = req.getParameter("changeNameLogActive") != null;
            boolean changeNicknameLogOption = req.getParameter("changeNicknameLogActive") != null;
            boolean voiceChannelLogOption = req.getParameter("voiceChannelLogActive") != null;
            boolean channelLogOption = req.getParameter("channelLogActive") != null;
            boolean modLogOption = req.getParameter("modLogActive") != null; // New line

            if (changeNameLogOption != dataManager.isChangeNameLogActive(guildId)) {
                dataManager.setChangeNameLogActive(guildId, changeNameLogOption);
                generalChanges = true;
            }
            if (changeNicknameLogOption != dataManager.isChangeNicknameLogActive(guildId)) {
                dataManager.setChangeNicknameLogActive(guildId, changeNicknameLogOption);
                generalChanges = true;
            }
            if (voiceChannelLogOption != dataManager.isVoiceChannelLogActive(guildId)) {
                dataManager.setVoiceChannelLogActive(guildId, voiceChannelLogOption);
                generalChanges = true;
            }
            if (channelLogOption != dataManager.isChannelLogActive(guildId)) {
                dataManager.setChannelLogActive(guildId, channelLogOption);
                generalChanges = true;
            }
            if (modLogOption != dataManager.isModLogActive(guildId)) { // New line
                dataManager.setModLogActive(guildId, modLogOption); // New line
                generalChanges = true; // New line
            }

            if (ticketChanges) {
                Guild guild = jda.getGuildById(guildId);
                if (guild != null) {
                    String updatedTicketChannelId = dataManager.getTicketChannel(guildId);
                    if (updatedTicketChannelId != null) {
                        TextChannel channel = guild.getTextChannelById(updatedTicketChannelId);
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
            String sessionKey = req.getParameter("sk");

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
            resp.getWriter().println("        html, body {");
            resp.getWriter().println("            margin: 0;");
            resp.getWriter().println("            padding: 0;");
            resp.getWriter().println("            height: 100%;");
            resp.getWriter().println("            width: 100%;");
            resp.getWriter().println("            display: flex;");
            resp.getWriter().println("            justify-content: center;");
            resp.getWriter().println("            align-items: center;");
            resp.getWriter().println("            background: radial-gradient(circle, rgba(126, 87, 241, 0.7) 0%, rgba(69, 73, 204, 0.7) 100%);");
            resp.getWriter().println("            color: white;");
            resp.getWriter().println("            text-align: center;");
            resp.getWriter().println("            font-family: Arial, sans-serif;");
            resp.getWriter().println("        }");
            resp.getWriter().println("        .verify-content {");
            resp.getWriter().println("            display: flex;");
            resp.getWriter().println("            flex-direction: column;");
            resp.getWriter().println("            align-items: center;");
            resp.getWriter().println("            justify-content: center;");
            resp.getWriter().println("            padding: 20px;");
            resp.getWriter().println("            border-radius: 10px;");
            resp.getWriter().println("            background: rgba(0, 0, 0, 0.5);");
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
    }
}
