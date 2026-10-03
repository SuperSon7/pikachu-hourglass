import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Java 17+ only. No JavaScript, framework, or external dependencies. */
public class Main {
    private static final ConcurrentHashMap<String, Timer> TIMERS = new ConcurrentHashMap<>();
    private static String template;
    private static byte[] pikachu;

    static class Timer {
        long total = 300_000, remaining = total, deadline;
        boolean running;
        String status = "준비되면 시작해 볼까요?";

        void tick() {
            if (!running) return;
            remaining = Math.max(0, (deadline - System.nanoTime() + 999_999) / 1_000_000);
            if (remaining == 0) {
                running = false;
                status = "피카피카! 시간이 다 됐어요 ⚡";
            }
        }

        void reset() {
            running = false;
            remaining = total;
            status = "준비되면 시작해 볼까요?";
        }

        boolean act(String action) {
            tick();
            switch (action) {
                case "toggle" -> {
                    if (running) {
                        running = false;
                        status = "잠깐 쉬어가도 괜찮아요.";
                    } else {
                        if (remaining == 0) remaining = total;
                        deadline = System.nanoTime() + remaining * 1_000_000;
                        running = true;
                        status = "피카츄도 함께 집중 중!";
                    }
                }
                case "reset" -> reset();
                case "minutes-1", "minutes-3", "minutes-5", "minutes-10" -> {
                    total = Integer.parseInt(action.substring(8)) * 60_000L;
                    reset();
                }
                default -> { return false; }
            }
            return true;
        }

        String render() {
            tick();
            long seconds = (remaining + 999) / 1000;
            double top = 52.0 * remaining / total, bottom = 52 - top;
            String html = template
                .replace("{{refresh}}", running ? "<meta http-equiv=\"refresh\" content=\"1;url=/\">" : "")
                .replace("{{running}}", running ? "running" : "")
                .replace("{{time}}", String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60))
                .replace("{{status}}", status)
                .replace("{{button}}", running ? "일시정지" : remaining == 0 ? "다시 시작" : remaining == total ? "시작하기" : "계속하기")
                .replace("{{topY}}", Double.toString(81 - top))
                .replace("{{topHeight}}", Double.toString(top))
                .replace("{{bottomY}}", Double.toString(151 - bottom))
                .replace("{{bottomHeight}}", Double.toString(bottom));
            for (int minutes : new int[]{1, 3, 5, 10})
                html = html.replace("{{selected" + minutes + "}}", Boolean.toString(total == minutes * 60_000L));
            return html;
        }
    }

    private static Timer session(HttpExchange exchange) {
        String cookie = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookie != null) {
            for (String part : cookie.split(";")) {
                String value = part.trim();
                if (value.startsWith("timerId=")) {
                    Timer timer = TIMERS.get(value.substring(8));
                    if (timer != null) return timer;
                }
            }
        }
        String id = UUID.randomUUID().toString();
        Timer timer = new Timer();
        TIMERS.put(id, timer);
        exchange.getResponseHeaders().add("Set-Cookie", "timerId=" + id + "; Path=/; HttpOnly; SameSite=Strict");
        return timer;
    }

    private static void send(HttpExchange exchange, int status, String type, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, body.length);
        try (var out = exchange.getResponseBody()) { out.write(body); }
    }

    private static void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        if (path.equals("/pikachu.png") && method.equals("GET")) {
            send(exchange, 200, "image/png", pikachu);
            return;
        }
        if (!(path.equals("/") || path.equals("/timer"))) {
            send(exchange, 404, "text/plain; charset=utf-8", "Not found".getBytes(StandardCharsets.UTF_8));
            return;
        }
        if (!(path.equals("/") && method.equals("GET") || path.equals("/timer") && method.equals("POST"))) {
            exchange.getResponseHeaders().set("Allow", path.equals("/") ? "GET" : "POST");
            send(exchange, 405, "text/plain", "Method not allowed".getBytes(StandardCharsets.UTF_8));
            return;
        }
        Timer timer = session(exchange);
        synchronized (timer) {
            if (method.equals("POST")) {
                String body = new String(exchange.getRequestBody().readNBytes(1025), StandardCharsets.UTF_8);
                if (body.length() > 1024 || !body.startsWith("action=") || !timer.act(body.substring(7))) {
                    send(exchange, 400, "text/plain", "Invalid action".getBytes(StandardCharsets.UTF_8));
                    return;
                }
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
            } else {
                send(exchange, 200, "text/html; charset=utf-8", timer.render().getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        template = Files.readString(Path.of("public/index.html"));
        pikachu = Files.readAllBytes(Path.of("public/pikachu.png"));
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", Main::handle);
        server.start();
        System.out.println("피카츄 모래시계: http://localhost:" + port);
    }
}
