// Task 4.4 experiment: @Override is kept, the method is misspelled as mouseclicked.
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DrawPadMisspelled extends Frame {
    public DrawPadMisspelled() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseclicked(MouseEvent e) {   // wrong name: lower-case 'c'
                System.out.println("clicked");
            }
        });
    }
}
