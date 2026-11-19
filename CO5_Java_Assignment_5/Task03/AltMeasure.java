import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/**
 * Part B3 Alternative analysis: time for 100,000 rows with
 * (1) PreparedStatement batch (the PassLoader method),
 * (2) multi-row INSERT of 1,000 rows per statement,
 * (3) LOAD DATA LOCAL INFILE from a CSV file.
 * Each run starts from an empty table and uses one transaction.
 */
public class AltMeasure {
    static final String URL = "jdbc:mysql://localhost:3306/transport_test?allowLocalInfile=true";

    public static void main(String[] args) throws Exception {
        int n = 100_000;
        List<String[]> rows = Passes.generate(n);
        Path csv = Paths.get("data/passes_100k.csv");
        Files.createDirectories(csv.getParent());
        try (BufferedWriter w = Files.newBufferedWriter(csv, StandardCharsets.UTF_8)) {
            for (String[] p : rows) {
                w.write(String.join(",", p));
                w.newLine();
            }
        }

        try (Connection con = DriverManager.getConnection(URL, "root", "password")) {
            Db.truncate(con);
            long t0 = System.nanoTime();
            PassLoader.loadAll(con, rows);
            System.out.println("PreparedStatement batch (1000/batch) : " + ms(t0) + " ms, rows=" + Db.count(con));

            Db.truncate(con);
            con.setAutoCommit(false);
            t0 = System.nanoTime();
            for (int start = 0; start < n; start += 1000) {
                StringBuilder sql = new StringBuilder("INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES ");
                for (int i = 0; i < 1000; i++) sql.append(i == 0 ? "(?,?,?,?,?)" : ",(?,?,?,?,?)");
                try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                    for (int i = 0; i < 1000; i++) {
                        String[] p = rows.get(start + i);
                        int b = i * 5;
                        ps.setString(b + 1, p[0]); ps.setString(b + 2, p[1]); ps.setString(b + 3, p[2]);
                        ps.setInt(b + 4, Integer.parseInt(p[3])); ps.setDouble(b + 5, Double.parseDouble(p[4]));
                    }
                    ps.executeUpdate();
                }
            }
            con.commit();
            System.out.println("Multi-row INSERT (1000 rows/stmt)    : " + ms(t0) + " ms, rows=" + Db.count(con));

            Db.truncate(con);
            con.setAutoCommit(true);
            t0 = System.nanoTime();
            try (Statement st = con.createStatement()) {
                st.execute("LOAD DATA LOCAL INFILE '" + csv.toAbsolutePath()
                        + "' INTO TABLE bus_pass FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n'");
            }
            System.out.println("LOAD DATA LOCAL INFILE              : " + ms(t0) + " ms, rows=" + Db.count(con));
        }
    }

    static long ms(long t0) { return (System.nanoTime() - t0) / 1_000_000; }
}
