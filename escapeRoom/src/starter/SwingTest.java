package starter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;


public class SwingTest {

  private static HashMap<Class<?>, String> starters = new HashMap<>();

  public static void main(String[] args) {
    // Wir speichern hier direkt die KLASSE, das macht den ProcessBuilder-Aufruf einfacher
    starters.put(MushRoom.class, "Pilz-Level starten");
    starters.put(DemoRoom.class, "Demo-Raum starten");
    starters.put(SpriteTestRoom.class, "Grafik-Test");

    JFrame frame = new JFrame("Admin-Panel");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Dynamisches Layout: So viele Zeilen wie Starter + Abstände
    frame.setLayout(new GridLayout(starters.size(), 1, 10, 10));

    // ITERATION ÜBER DIE MAP
    starters.forEach((roomClass, displayName) -> {
      JButton btn = new JButton(displayName);

      btn.addActionListener(e -> {
        try {
          String javaBin = System.getProperty("java.home") + "/bin/java";
          if (System.getProperty("os.name").toLowerCase().contains("win")) javaBin += ".exe";

          String classpath = System.getProperty("java.class.path");

          // Wir nehmen den echten Namen der Klasse aus der Map
          ProcessBuilder pb = new ProcessBuilder(javaBin, "-cp", classpath, roomClass.getName());
          pb.inheritIO();

          frame.setVisible(false);
          Process p = pb.start();

          new Thread(() -> {
            try {
              p.waitFor();
              SwingUtilities.invokeLater(() -> frame.setVisible(true));
            } catch (InterruptedException ex) {
              ex.printStackTrace();
            }
          }).start();

        } catch (IOException ex) {
          ex.printStackTrace();
          JOptionPane.showMessageDialog(frame, "Startfehler: " + ex.getMessage());
        }
      });

      frame.add(btn);
    });

    frame.pack(); // Passt die Fenstergröße automatisch an die Anzahl der Buttons an
    frame.setLocationRelativeTo(null); // Zentriert das Fenster auf dem Bildschirm
    frame.setVisible(true);
  }
}
