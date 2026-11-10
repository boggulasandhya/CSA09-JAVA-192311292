package parking.system.main;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import parking.system.facility.ParkingFacility;
import parking.system.facility.SlotLocation;
import parking.system.models.*;
import parking.system.users.ParkingUser;
import parking.system.users.PassType;
import parking.system.users.UserCategory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Lightweight, zero-dependency HTTP Web Server using Java's built-in HttpServer.
 * Enables hosting the Campus Smart-Parking System on any cloud provider (Render, Railway, Fly.io, AWS, Heroku)
 * with a responsive web dashboard and REST API.
 */
public class ParkingWebServer {

    private static final ParkingFacility facility = new ParkingFacility(new int[]{4, 4, 3, 2});
    private static final ConcurrentHashMap<String, ParkingTicket> activeTickets = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        String envPort = System.getenv("PORT");
        int port = (envPort != null && !envPort.isEmpty()) ? Integer.parseInt(envPort) : 8080;

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        server.createContext("/", new DashboardHandler());
        server.createContext("/api/status", new StatusApiHandler());
        server.createContext("/api/demo-park", new DemoParkHandler());

        System.out.println("==================================================================");
        System.out.println("  CAMPUS SMART-PARKING SYSTEM - WEB SERVICE RUNNING");
        System.out.println("  Server listening on: http://localhost:" + port);
        System.out.println("  Endpoints: / (Web Dashboard), /api/status, /api/demo-park");
        System.out.println("==================================================================");

        server.start();
    }

    static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
                return;
            }

            String template = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Campus Smart-Parking System</title>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
                    <style>
                        :root {
                            --bg: #0f172a; --card: #1e293b; --accent: #38bdf8;
                            --text: #f8fafc; --muted: #94a3b8; --border: #334155;
                            --success: #22c55e;
                        }
                        * { box-sizing: border-box; margin: 0; padding: 0; }
                        body { font-family: 'Inter', sans-serif; background: var(--bg); color: var(--text); padding: 2rem; }
                        .container { max-width: 1000px; margin: 0 auto; }
                        header { margin-bottom: 2rem; border-bottom: 1px solid var(--border); padding-bottom: 1rem; }
                        h1 { font-size: 2rem; color: var(--accent); }
                        p.subtitle { color: var(--muted); margin-top: 0.25rem; }
                        .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.5rem; margin-bottom: 2rem; }
                        .card { background: var(--card); border: 1px solid var(--border); border-radius: 12px; padding: 1.5rem; }
                        .stat-val { font-size: 2.25rem; font-weight: 700; color: var(--accent); margin-top: 0.5rem; }
                        pre { background: #0b1120; border: 1px solid var(--border); padding: 1rem; border-radius: 8px; overflow-x: auto; font-size: 0.9rem; color: #a5f3fc; }
                        button { background: var(--accent); color: #0f172a; font-weight: 600; border: none; padding: 0.75rem 1.5rem; border-radius: 8px; cursor: pointer; transition: 0.2s; }
                        button:hover { background: #7dd3fc; }
                        .badge { display: inline-block; padding: 0.25rem 0.6rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 600; background: rgba(34, 197, 94, 0.2); color: var(--success); }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <header>
                            <span class="badge">ONLINE SERVICE</span>
                            <h1>Campus Smart-Parking System</h1>
                            <p class="subtitle">Multi-floor Jagged Array Allocation & Polymorphic Billing Engine (Java 17+)</p>
                        </header>
                        <div class="grid">
                            <div class="card">
                                <h3>Total Capacity</h3>
                                <div class="stat-val">%d <span style="font-size: 1rem; color: var(--muted)">Slots</span></div>
                            </div>
                            <div class="card">
                                <h3>Active Occupancy</h3>
                                <div class="stat-val">%d <span style="font-size: 1rem; color: var(--muted)">Parked</span></div>
                            </div>
                            <div class="card">
                                <h3>Available Slots</h3>
                                <div class="stat-val">%d <span style="font-size: 1rem; color: var(--success)">Free</span></div>
                            </div>
                        </div>
                        <div class="card" style="margin-bottom: 2rem;">
                            <h3 style="margin-bottom: 1rem;">Facility Floor Map (Jagged Array Grid)</h3>
                            <pre>%s</pre>
                        </div>
                        <div class="card">
                            <h3 style="margin-bottom: 0.5rem;">Interactive Allocation</h3>
                            <p style="color: var(--muted); margin-bottom: 1rem;">Park a sample vehicle (ElectricCar, DeliveryVan, TwoWheeler) into the live facility.</p>
                            <form action="/api/demo-park" method="POST">
                                <button type="submit">Auto-Park Sample Vehicle & Issue Ticket</button>
                            </form>
                        </div>
                    </div>
                </body>
                </html>
                """;

            String html = String.format(
                    template,
                    facility.getTotalCapacity(),
                    ParkingFacility.getTotalOccupancy(),
                    facility.getAvailableSlotCount(),
                    escapeHtml(facility.renderOccupancyMap())
            );

            sendResponse(exchange, 200, "text/html; charset=UTF-8", html);
        }
    }

    static class StatusApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String json = String.format(
                    "{\"status\":\"UP\",\"totalCapacity\":%d,\"totalOccupancy\":%d,\"availableSlots\":%d,\"floors\":%d}",
                    facility.getTotalCapacity(),
                    ParkingFacility.getTotalOccupancy(),
                    facility.getAvailableSlotCount(),
                    facility.getFloorCount()
            );
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    static class DemoParkHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int ticketSeq = ParkingTicket.getIssuedTicketCount() + 1;
            Vehicle v;
            if (ticketSeq % 3 == 0) {
                v = new ElectricCar("TN" + (10 + ticketSeq) + "EV" + (1000 + ticketSeq));
            } else if (ticketSeq % 2 == 0) {
                v = new DeliveryVan("TN" + (10 + ticketSeq) + "DV" + (2000 + ticketSeq));
            } else {
                v = new Car("TN" + (10 + ticketSeq) + "CR" + (3000 + ticketSeq));
            }

            ParkingUser user = new ParkingUser("USR-" + ticketSeq, "Patron #" + ticketSeq, UserCategory.VISITOR, PassType.NONE);
            SlotLocation slot = facility.allocate(v);

            if (slot != null) {
                ParkingTicket ticket = new ParkingTicket(v, user, slot, LocalTime.now());
                activeTickets.put(ticket.getTicketId(), ticket);
            }

            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
