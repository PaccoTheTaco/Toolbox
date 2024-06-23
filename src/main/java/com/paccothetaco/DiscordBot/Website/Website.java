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
                        String currentWelcomeChannelName = "--deactivated--";
                        String currentLeaveChannelName = "--deactivated--";
                        String currentTicketChannelName = "--deactivated--";
                        String currentMessageLogChannelName = "--deactivated--";
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

                        Map<String, Boolean> ticketOptions = dataManager.getTicketOptions(guildId);

                        String htmlTemplate = readFileAsString(System.getProperty("user.dir") + "/src/main/java/com/paccothetaco/DiscordBot/Website/HTMLDocuments/settings.html");
                        htmlTemplate = htmlTemplate.replace("<!-- SERVER_NAME -->", serverName)
                                .replace("<!-- WELCOME_CHANNEL_NAME -->", currentWelcomeChannelName)
                                .replace("<!-- LEAVE_CHANNEL_NAME -->", currentLeaveChannelName)
                                .replace("<!-- TICKET_CHANNEL_NAME -->", currentTicketChannelName)
                                .replace("<!-- MESSAGE_LOG_CHANNEL_NAME -->", currentMessageLogChannelName)
                                .replace("<!-- TICKET_CATEGORY_NAME -->", currentTicketCategoryName)
                                .replace("<!-- CLOSED_TICKET_CATEGORY_NAME -->", currentClosedTicketCategoryName)
                                .replace("<!-- TICKET_OPTION_SUPPORT -->", ticketOptions.get("support") ? "checked" : "")
                                .replace("<!-- TICKET_OPTION_REPORT -->", ticketOptions.get("report") ? "checked" : "")
                                .replace("<!-- TICKET_OPTION_APPLICATION -->", ticketOptions.get("application") ? "checked" : "")
                                .replace("<!-- MOD_ROLE_NAME -->", dataManager.getModRole(guildId) != null ? guild.getRoleById(dataManager.getModRole(guildId)).getName() : "--not selected--")
                                .replace("<!-- GUILD_ID -->", guildId)
                                .replace("<!-- SESSION_KEY -->", sessionKey);

                        resp.setContentType("text/html");
                        resp.getWriter().write(htmlTemplate);
                    } else {
                        resp.setContentType("text/html");
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().println("<h1>Error</h1>");
                        resp.getWriter().println("<p>Could not retrieve server details.</p>");
                    }
                } else {
                    String htmlTemplate = readFileAsString(System.getProperty("user.dir") + "/src/main/java/com/paccothetaco/DiscordBot/Website/HTMLDocuments/verify.html");
                    htmlTemplate = htmlTemplate.replace("<!-- SESSION_KEY -->", sessionKey);

                    resp.setContentType("text/html");
                    resp.getWriter().write(htmlTemplate);
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

            // Message Log Channel Handling
            if ("none".equals(messageLogChannelId)) {
                dataManager.deactivateMessageLog(guildId);
                generalChanges = true;
            } else {
                if (!messageLogChannelId.equals(dataManager.getMessageLogChannel(guildId))) {
                    dataManager.setMessageLogChannel(guildId, messageLogChannelId);
                    generalChanges = true;
                }
            }

            // Ticket Category Handling
            if (ticketCategoryId != null && !"none".equals(ticketCategoryId)) {
                dataManager.setTicketCategory(guildId, ticketCategoryId);
                generalChanges = true;
            }

            // Closed Ticket Category Handling
            if (closedTicketCategoryId != null && !"none".equals(closedTicketCategoryId)) {
                dataManager.setClosedTicketCategory(guildId, closedTicketCategoryId);
                generalChanges = true;
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
            String sessionKey = req.getParameter("sk");
            String htmlTemplate = readFileAsString(System.getProperty("user.dir") + "/src/main/java/com/paccothetaco/DiscordBot/Website/HTMLDocuments/verify.html");
            htmlTemplate = htmlTemplate.replace("${sessionKey}", sessionKey);

            resp.setContentType("text/html");
            resp.getWriter().write(htmlTemplate);
        }
    }
}
