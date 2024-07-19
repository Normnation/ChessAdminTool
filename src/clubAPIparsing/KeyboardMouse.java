package clubAPIparsing;

import javax.swing.event.MouseInputListener;
import java.awt.event.MouseEvent;

public class KeyboardMouse implements MouseInputListener {
    ChessAdminGUI gui;

    public KeyboardMouse(ChessAdminGUI gui) {
        this.gui = gui;
        gui.getContentPane().addMouseListener(this);
    }

    // Using for placement.
    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub

    }

}
