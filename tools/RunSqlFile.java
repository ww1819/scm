import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 逐条执行 MySQL 脚本（按分号 + 换行切分，勿在字符串字面量中含 ; + 换行）。
 * 用法: java RunSqlFile <jdbcUrl> <user> <sqlFilePath>
 * 密码: 环境变量 SCM_JDBC_PASSWORD
 */
public class RunSqlFile {
    private static final Pattern STMT_SPLIT = Pattern.compile(";\\s*\r?\n");

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage: java RunSqlFile <jdbcUrl> <user> <sqlFilePath>");
            System.exit(1);
        }
        String url = args[0];
        String user = args[1];
        java.nio.file.Path sqlPath = Paths.get(args[2]);
        String pass = System.getenv("SCM_JDBC_PASSWORD");
        if (pass == null || pass.isEmpty()) {
            System.err.println("Set environment variable SCM_JDBC_PASSWORD");
            System.exit(1);
        }
        Properties p = new Properties();
        p.setProperty("user", user);
        p.setProperty("password", pass);
        String raw = new String(Files.readAllBytes(sqlPath), StandardCharsets.UTF_8);
        if (!raw.isEmpty() && raw.charAt(0) == '\uFEFF') {
            raw = raw.substring(1);
        }
        List<String> stmts = new ArrayList<>();
        for (String chunk : STMT_SPLIT.split(raw)) {
            String s = chunk.trim();
            if (s.isEmpty()) {
                continue;
            }
            // 去掉整段纯注释
            String noLineComments = stripLineComments(s);
            if (noLineComments.replaceAll("\\s+", "").isEmpty()) {
                continue;
            }
            stmts.add(s);
        }
        System.out.println("Statements to run: " + stmts.size());
        try (Connection c = DriverManager.getConnection(url, p)) {
            c.setAutoCommit(true);
            try (Statement st = c.createStatement()) {
                st.setQueryTimeout(0);
                int n = 0;
                for (String sql : stmts) {
                    n++;
                    try {
                        boolean isRs = st.execute(sql);
                        if (isRs) {
                            try (ResultSet rs = st.getResultSet()) {
                                ResultSetMetaData md = rs.getMetaData();
                                int cols = md.getColumnCount();
                                while (rs.next()) {
                                    StringBuilder line = new StringBuilder();
                                    for (int i = 1; i <= cols; i++) {
                                        if (i > 1) {
                                            line.append('\t');
                                        }
                                        Object v = rs.getObject(i);
                                        line.append(v == null ? "NULL" : v.toString());
                                    }
                                    System.out.println(line);
                                }
                            }
                        } else {
                            int uc = st.getUpdateCount();
                            if (n <= 5 || n % 200 == 0) {
                                System.out.println("[" + n + "] updateCount=" + uc);
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Failed at statement #" + n + ": " + e.getMessage());
                        System.err.println(sql.substring(0, Math.min(200, sql.length())) + "...");
                        throw e;
                    }
                }
            }
        }
        System.out.println("Done.");
    }

    private static String stripLineComments(String sql) {
        StringBuilder out = new StringBuilder();
        for (String line : sql.split("\r?\n")) {
            String t = line.trim();
            if (t.startsWith("--")) {
                continue;
            }
            out.append(line).append('\n');
        }
        return out.toString();
    }
}
