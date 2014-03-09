import org.hexbot.api.methods.core.MethodContext;
import org.hexbot.api.methods.interactive.GroundItems;
import org.hexbot.api.util.Condition;
import org.hexbot.api.util.Time;
import org.hexbot.api.wrapper.Area;
import org.hexbot.api.wrapper.GroundItem;
import org.hexbot.api.wrapper.Tile;
import org.hexbot.script.Category;
import org.hexbot.script.Employer;
import org.hexbot.script.ScriptManifest;
import org.hexbot.script.Worker;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Arturo on 3/8/14.
 */
@ScriptManifest(author = "countvidal", description = "Lights logs in Alkharid Bank, start it with tinderbox equipped and logs in first bank slot.", name = "VFire", category = Category.FIREMAKING)
public class VFire extends Employer {
    Area bank = new Area(new Tile(3272,3173),new Tile(3269,3161));
    Tile spot = new Tile(3291,3156);
    String process = "";
    String logs = "";
    Boolean StartScript = false;
    GUI GUI;



    public VFire(MethodContext context) {
        super(context);
    }

    @Override
    public void onStart() {
        GUI = new GUI();
        GUI.setVisible(true);
      
        while (!StartScript) {
            Time.sleep(100);
        }
        submit(new Bank(context),new WalkToSpot(context),new Burn(context), new WalkToBank(context));

    }

    private class Bank extends Worker {

        public Bank(MethodContext context) {
            super(context);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void work() {
            process = "Banking";
            if(context.bank.isOpen()){
                context.bank.withdraw(logs,28);
            } else {
                context.bank.open();
            }
        }

        @Override
        public boolean validate() {
            return bank.contains(context.players.getLocal()) && context.inventory.getCount(logs) == 0;
        }

    }

    private class Burn extends Worker {

        public Burn(MethodContext context) {
            super(context);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void work() {
            process = "Burning logs";

            if(context.inventory.isItemSelected() && !context.gameObjects.getAt(context.players.getLocal().getLocation()).getName().equals("Fire")){
                if(context.inventory.getSelectedItem().equals(context.inventory.getItem("Tinderbox")) && context.players.getLocal().getAnimationIndex() == -1){
                    context.inventory.getItem(logs).click();
                    Time.waitFor(new Condition() {
                        public boolean accept() {
                            return context.players.getLocal().getAnimationIndex() != -1;
                        }
                    }, 2000);
                } else if(context.players.getLocal().getAnimationIndex() == -1){
                    context.inventory.getSelectedItem().click();
                }
            } else if(!context.gameObjects.getAt(context.players.getLocal().getLocation()).getName().equals("Fire")){
                context.inventory.getItem("Tinderbox").click();
            } else if(context.gameObjects.getAt(context.players.getLocal().getLocation()).getName().equals("Fire")){
                int range = (0 - -5) + 1;
                context.walking.walk(new Tile(spot.getX(), spot.getY() + (int)(Math.random() * range) + -5));
            }

        }

        @Override
        public boolean validate() {
            return context.inventory.contains(logs) && !bank.contains(context.players.getLocal());
        }

    }

    private class WalkToBank extends Worker {

        public WalkToBank(MethodContext context) {
            super(context);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void work() {
            process = "Walking to Bank";
           context.walking.walk(bank.getCentralTile());
        }

        @Override
        public boolean validate() {
            return !context.inventory.contains(logs) && context.groundItems.getNearest(logs) == GroundItem.EMPTY;
        }

    }

    private class WalkToSpot extends Worker {

        public WalkToSpot(MethodContext context) {
            super(context);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void work() {
            process = "Walking to Stop";
            context.walking.walk(spot);
        }

        @Override
        public boolean validate() {
            return context.inventory.contains(logs) && context.calculations.distanceBetween(context.players.getLocal().getLocation(),spot) > 20;
        }

    }
    public class GUI extends JFrame {
        public GUI(){
            initComponents();
        }

        private void initComponents() {
            JPanel Panel = new JPanel(new BorderLayout());
            String[] a = {"Logs","Oak logs","Maple logs","Willow logs","Yew logs","Magic logs"};
            final JComboBox options = new JComboBox(a);
            JLabel instructions = new JLabel("Select Logs");
            Panel.add(instructions,"North");
            Panel.add(options,"Center");
            JButton start = new JButton("Start");
            Panel.add(start,"South");
            start.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    logs = options.getSelectedItem().toString();
                    StartScript = true;
                    dispose();
                }
            });
            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            setTitle("VFire - Simple Fire Script");
            Panel.setMinimumSize(new Dimension(300,100));
            Panel.setSize(300,100);
            this.add(Panel);
            pack();
        }
    }
}
