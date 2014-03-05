
import org.hexbot.api.event.MessageEvent;
import org.hexbot.api.event.RenderEvent;
import org.hexbot.api.methods.core.MethodContext;
import org.hexbot.api.methods.interfaces.Equipment;
import org.hexbot.api.methods.interfaces.Skills;
import org.hexbot.api.methods.interfaces.Tabs;
import org.hexbot.api.methods.movement.PathFinder;
import org.hexbot.api.util.Condition;
import org.hexbot.api.util.Filter;
import org.hexbot.api.util.Random;
import org.hexbot.api.util.Time;
import org.hexbot.api.wrapper.*;
import org.hexbot.script.AbstractScript;
import org.hexbot.script.Category;
import org.hexbot.script.ScriptManifest;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;


@ScriptManifest(author = "countvidal", category = Category.FISHING, name = "VFish", description = "All in One Fisher, ")
public class VFish extends AbstractScript implements RenderEvent, MouseMotionListener, MouseListener, MessageEvent {
    // States
    Date pressedTime;
    long timeClicked;

    public VFish(MethodContext context){
        super(context);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        pressedTime = new Date();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        timeClicked = new Date().getTime() - pressedTime.getTime();

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void onMessageReceived(int i, String s, String s2) {
        if(i == 0 && s2.contains("You catch")){
            fishCount++;
        }
    }

    public interface BankingOperation {
        public boolean walkFromBank();

        public boolean walkToBank();
    }

    public class FishingSpot {

        public String option;
        public int id, bankID;
        public final Area bank, spot;
        public BankingOperation bankOp;

        public FishingSpot(final int id, final String option, final int bankID,
                           final BankingOperation bankOp) {
            this.bankOp = bankOp;
            this.id = id;
            this.option = option;
            this.bankID = bankID;
            spot = bank = null;
        }

        public FishingSpot(final int id, final String option,
                           final Area bank, final int bankID, final Area spot) {
            this.id = id;
            this.option = option;
            this.bank = bank;
            this.spot = spot;
            this.bankID = bankID;
            bankOp = new BankingOperation() {
                public boolean walkFromBank() {
                    if(spot == null){
                        return false;
                    }
                    PathFinder p = new PathFinder(context);
                    Tile closest = getClosestOnZone(spot.getCentralTile());
                    Tile[] pathFromSpot = p.findPath(context.players.getLocal().getLocation(),closest);
                    System.out.println(pathFromSpot[0].toString()+" "+pathFromSpot[1].toString());
                    if ((spot.getNearest(context.players.getLocal())) != null) {
                        context.walking.walk(pathFromSpot);
                    } else {
                        System.out.println("Couldn't find path to bank spot. Exiting.");
                    }
                    return spot.contains(context.players.getLocal());
                }

                public boolean walkToBank() {
                    if(bank == null){
                        return false;
                    }
                    PathFinder p = new PathFinder(context);
                    Tile closest = getClosestOnZone(bank.getCentralTile());
                    Tile[] pathFromSpot = p.findPath(context.players.getLocal().getLocation(),closest);
                    if ((bank.getNearest(context.players.getLocal())) != null) {
                        context.walking.walk(pathFromSpot);
                    } else {
                        System.out.println("Couldn't find path to bank spot. Exiting.");
                    }
                    return bank.contains(context.players.getLocal());
                }

                private Tile getClosestOnZone(Tile tile) {
                    Tile closest = context.players.getLocal().getLocation();
                    for(int i = context.client.getBaseX(); i < (context.client.getBaseX()+101); i++ ){
                        for(int n = context.client.getBaseY(); n < (context.client.getBaseY()+101); n++ ){
                            Tile tmp = new Tile(i,n);
                            if(context.walking.canReach(tmp) && context.calculations.distanceBetween(tmp,tile) < context.calculations.distanceBetween(closest,tile)){
                                closest = tmp;
                            }
                        }
                    }

                    return closest;
                }
            };
        }

    }

    public class VFishGUI extends javax.swing.JFrame {

        /**
         *
         */
        private static final long serialVersionUID = 1212036273772147185L;

        // Variables declaration - do not modify
        private javax.swing.JButton applyButton;

        private javax.swing.JCheckBox barbarianBox;

        private javax.swing.JComboBox fishBox;

        private javax.swing.JTextField hoursBox;

        private javax.swing.JComboBox locationBox;

        private javax.swing.JLabel jLabel1;

        private javax.swing.JLabel jLabel2;

        private javax.swing.JLabel jLabel3;

        private javax.swing.JCheckBox logOutBox;

        private javax.swing.JTextField minutesBox;

        private javax.swing.JButton pauseButton;
        private javax.swing.JCheckBox powerFishBox;
        private javax.swing.JButton startStopButton;
        private javax.swing.JButton updateButton;
        private javax.swing.JCheckBox paintBox;
        public javax.swing.JLabel timeLabel;
        private javax.swing.JCheckBox dropTuna;

        // End of variables declaration

        public VFishGUI() {
            initComponents();
        }

        private void applyButtonActionPerformed(
                final java.awt.event.ActionEvent evt) {
            barbarianMode = barbarianBox.isSelected();
            painting = paintBox.isSelected();
            timerUsed = logOutBox.isSelected();
            powerFish = powerFishBox.isSelected();
            droppingTunas = dropTuna.isSelected();
            if (!barbarianMode) {
                final String fishType = (String) fishBox.getSelectedItem();
                gear = getFishingGear(fishType);
            }
            int size = 0;
            for (int i = 0; i < locationBox.getSelectedIndex(); i++) {
                size += fishingSpotNames[i].length;
            }
            size += fishBox.getSelectedIndex();
            fs = spots[size];
            fishingSpot = fs.id;
            fishingAction = fs.option;
            bankArea = fs.bank;
            fishingArea = fs.spot;
           // bankID = fs.bankID;
           // isBankPiscBanker = bankID == 3824;
           // isBankBanker = bankID == 499 || bankID == 494;
           // isBankRasolo = bankID == 1972;
            check = getFishingCheck((String) fishBox.getSelectedItem());
            if (lastLocation != locationBox.getSelectedIndex()) {
                lastLocation = locationBox.getSelectedIndex();
            }
            if (timerUsed) {
                int h = 0, m = 0;
                timerUsed = false;
                try {
                    h = Integer.parseInt(hoursBox.getText());
                    m = Integer.parseInt(minutesBox.getText());
                } finally {
                    timeToStop = nextTime(m * 60000 + h * 60000 * 60);
                    timerUsed = true;
                }
            } else {
                timerUsed = false;
            }
            BufferedWriter out;
            try {
                String currentDir = System.getProperty("user.dir");
                out = new BufferedWriter(new FileWriter(new File(
                        currentDir + File.separator + "VFishConfig.txt")));
                final String[] settings = {"" + fishBox.getSelectedIndex(),
                        "" + locationBox.getSelectedIndex(),
                        powerFishBox.isSelected() ? "1" : "0",
                        barbarianBox.isSelected() ? "1" : "0",
                        logOutBox.isSelected() ? "1" : "0", hoursBox.getText(),
                        minutesBox.getText(),
                        paintBox.isSelected() ? "1" : "0",
                        dropTuna.isSelected() ? "1" : "0"};
                for (final String line : settings) {
                    out.write(line);
                    out.newLine();
                }
                out.close();
                System.out.println("Configuration saved to VFishConfig.txt!");
            } catch (final IOException e) {
                e.printStackTrace();
                System.out.println("Saving configuration failed!");
            }
        }

        private void formWindowClosed(final java.awt.event.WindowEvent evt) {
        }

        private void initComponents() {

            fishBox = new javax.swing.JComboBox();
            jLabel1 = new javax.swing.JLabel();
            powerFishBox = new javax.swing.JCheckBox();
            barbarianBox = new javax.swing.JCheckBox();
            startStopButton = new javax.swing.JButton();
            pauseButton = new javax.swing.JButton();
            updateButton = new javax.swing.JButton();
            jLabel2 = new javax.swing.JLabel();
            locationBox = new javax.swing.JComboBox();
            logOutBox = new javax.swing.JCheckBox();
            hoursBox = new javax.swing.JTextField();
            minutesBox = new javax.swing.JTextField();
            applyButton = new javax.swing.JButton();
            jLabel3 = new javax.swing.JLabel();
            paintBox = new javax.swing.JCheckBox();
            dropTuna = new javax.swing.JCheckBox();

            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            setTitle("VFisher - The State of the Art Fisher");
            setResizable(false);
            addWindowListener(new java.awt.event.WindowAdapter() {

                public void windowClosed(final java.awt.event.WindowEvent evt) {
                    formWindowClosed(evt);
                }
            });

            fishBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
                    "Shrimp and Anchovies", "Crayfish", "Sardine and Herring",
                    "Pike", "Trout and Salmon", "Lobsters", "Monkfish",
                    "Swordfish and Tuna", "Sharks"}));
            fishBox.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    makeChangesFish();
                }
            });
            jLabel1.setText("Location:");

            powerFishBox.setText("Drop Fish");
            powerFishBox
                    .setToolTipText("Tick this to enable power fishing, which will drop all your fish when you get a full context.inventory. Otherwise, the script will bank (if possible).");

            barbarianBox.setText("Barbarian Fishing");
            barbarianBox
                    .setToolTipText("Check this to enable barbarian fishing mode. This will fish without any equipment and gives you strength experience.\n*Must have completed this part of barbarian training to use!");

            startStopButton.setText("Start Script");
            startStopButton.setToolTipText("Click to start/stop the script.");
            startStopButton
                    .addActionListener(new java.awt.event.ActionListener() {

                        public void actionPerformed(
                                final java.awt.event.ActionEvent evt) {
                            startStopButtonActionPerformed(evt);
                        }
                    });

            pauseButton.setText("Pause Script");
            pauseButton.setToolTipText("Click to Pause/Resume the script.");
            pauseButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    pauseButtonActionPerformed(evt);
                }
            });

            updateButton.setText("Check for Updates");
            updateButton
                    .setToolTipText("Click to check for a newer version of the script.");
            updateButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    updateButtonActionPerformed(evt);
                }
            });

            jLabel2.setText("Fish:");

            locationBox.setModel(new javax.swing.DefaultComboBoxModel(
                    fishingLocationNames));
            locationBox.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    locationBoxActionPerformed(evt);
                }
            });
            logOutBox.setText("Log Out After");
            logOutBox
                    .setToolTipText("Check this to log out after the time in the boxes to the right.");

            hoursBox.setColumns(3);
            hoursBox.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
            hoursBox.setText("H");
            hoursBox.setToolTipText("Amount of hours until the script stops. Leave blank if \"Log Out After\" is unchecked.");

            minutesBox.setColumns(3);
            minutesBox.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
            minutesBox.setText("M");
            minutesBox
                    .setToolTipText("Amount of minutes until the script stops. Leave blank if \"Log Out After\" is unchecked.");

            applyButton.setText("Apply Settings");
            applyButton.setToolTipText("Click to save the settings.");
            applyButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    applyButtonActionPerformed(evt);
                }
            });

            jLabel3.setText("0x098b4a40");

            timeLabel = new javax.swing.JLabel();
            timeLabel.setText("Time until logout: -:--:-- (Timer not set)");

            paintBox.setText("Paint Progress");
            paintBox.setToolTipText("Check this to enable a paint that will display your progress.");

            dropTuna.setText("Drop Tuna");
            dropTuna.setToolTipText("Toggle this to drop Tuna when banking Swordfish. (Unneeded if powerfishing)");

            BufferedReader in = null;
            try {
                String currentDir = System.getProperty("user.dir");
                in = new BufferedReader(new FileReader(new File(
                        currentDir + File.separator + "VFishConfig.txt")));
                final String[] settings = new String[9];
                String line = "";
                for (int i = 0; i < settings.length
                        && (line = in.readLine()) != null; i++) {
                    settings[i] = line;
                }
                locationBox.setSelectedIndex(Integer.parseInt(settings[1]));
                makeChanges();
                fishBox.setSelectedIndex(Integer.parseInt(settings[0]));
                powerFishBox.setSelected(settings[2].equals("1"));
                barbarianBox.setSelected(settings[3].equals("1"));
                logOutBox.setSelected(settings[4].equals("1"));
                hoursBox.setText(settings[5]);
                minutesBox.setText(settings[6]);
                paintBox.setSelected(settings[7].equals("1"));
                dropTuna.setSelected(settings[8].equals("1"));
                in.close();
                System.out.println("Configuration found and loaded!");
            } catch (final IOException e) {
                System.out.println("No configuration found.");
                makeChanges();
                powerFishBox.setSelected(true);
            }
            final javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
                    getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(layout
                    .createParallelGroup(
                            javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(
                            layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(
                                            layout.createParallelGroup(
                                                    javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(
                                                            layout.createSequentialGroup()
                                                                    .addGroup(
                                                                            layout.createParallelGroup(
                                                                                    javax.swing.GroupLayout.Alignment.LEADING)
                                                                                    .addComponent(
                                                                                            locationBox,
                                                                                            javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                            javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                            javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                    .addComponent(
                                                                                            jLabel1))
                                                                    .addPreferredGap(
                                                                            javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                    .addGroup(
                                                                            layout.createParallelGroup(
                                                                                    javax.swing.GroupLayout.Alignment.LEADING)
                                                                                    .addComponent(
                                                                                            jLabel2)
                                                                                    .addComponent(
                                                                                            fishBox,
                                                                                            javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                            javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                            javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                    .addGroup(
                                                            layout.createSequentialGroup()
                                                                    .addGroup(
                                                                            layout.createParallelGroup(
                                                                                    javax.swing.GroupLayout.Alignment.LEADING)
                                                                                    .addComponent(
                                                                                            powerFishBox)
                                                                                    .addComponent(
                                                                                            barbarianBox))
                                                                    .addPreferredGap(
                                                                            javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                    .addGroup(
                                                                            layout.createParallelGroup(
                                                                                    javax.swing.GroupLayout.Alignment.LEADING)
                                                                                    .addComponent(
                                                                                            paintBox)
                                                                                    .addGroup(
                                                                                            layout.createSequentialGroup()
                                                                                                    .addComponent(
                                                                                                            logOutBox)
                                                                                                    .addPreferredGap(
                                                                                                            javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                    .addComponent(
                                                                                                            hoursBox,
                                                                                                            javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                            javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                            javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                    .addPreferredGap(
                                                                                                            javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                    .addComponent(
                                                                                                            minutesBox,
                                                                                                            javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                            javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                            javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                    .addPreferredGap(
                                                                                                            javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                    .addComponent(
                                                                                                            dropTuna))))
                                                    .addGroup(
                                                            layout.createParallelGroup(
                                                                    javax.swing.GroupLayout.Alignment.TRAILING,
                                                                    false)
                                                                    .addGroup(
                                                                            layout.createSequentialGroup()
                                                                                    .addComponent(
                                                                                            timeLabel)
                                                                                    .addPreferredGap(
                                                                                            javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                            javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                            Short.MAX_VALUE)
                                                                                    .addComponent(
                                                                                            jLabel3))
                                                                    .addGroup(
                                                                            layout.createSequentialGroup()
                                                                                    .addComponent(
                                                                                            startStopButton)
                                                                                    .addPreferredGap(
                                                                                            javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                    .addComponent(
                                                                                            pauseButton)
                                                                                    .addPreferredGap(
                                                                                            javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                    .addComponent(
                                                                                            applyButton)
                                                                                    .addPreferredGap(
                                                                                            javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                    .addComponent(
                                                                                            updateButton))))
                                    .addContainerGap(
                                            javax.swing.GroupLayout.DEFAULT_SIZE,
                                            Short.MAX_VALUE)));
            layout.setVerticalGroup(layout
                    .createParallelGroup(
                            javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(
                            layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(
                                            layout.createParallelGroup(
                                                    javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(jLabel1)
                                                    .addComponent(jLabel2))
                                    .addPreferredGap(
                                            javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(
                                            layout.createParallelGroup(
                                                    javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(
                                                            fishBox,
                                                            javax.swing.GroupLayout.PREFERRED_SIZE,
                                                            javax.swing.GroupLayout.DEFAULT_SIZE,
                                                            javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(
                                                            locationBox,
                                                            javax.swing.GroupLayout.PREFERRED_SIZE,
                                                            javax.swing.GroupLayout.DEFAULT_SIZE,
                                                            javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(
                                            javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(
                                            layout.createParallelGroup(
                                                    javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(powerFishBox)
                                                    .addGroup(
                                                            layout.createSequentialGroup()
                                                                    .addGap(3,
                                                                            3,
                                                                            3)
                                                                    .addGroup(
                                                                            layout.createParallelGroup(
                                                                                    javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                    .addComponent(
                                                                                            logOutBox)
                                                                                    .addComponent(
                                                                                            hoursBox,
                                                                                            javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                            javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                            javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                    .addComponent(
                                                                                            minutesBox,
                                                                                            javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                            javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                            javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                    .addComponent(
                                                                                            dropTuna))))
                                    .addPreferredGap(
                                            javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(
                                            layout.createParallelGroup(
                                                    javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(barbarianBox)
                                                    .addComponent(paintBox))
                                    .addPreferredGap(
                                            javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(
                                            layout.createParallelGroup(
                                                    javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(
                                                            startStopButton)
                                                    .addComponent(pauseButton)
                                                    .addComponent(updateButton)
                                                    .addComponent(applyButton))
                                    .addPreferredGap(
                                            javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                            javax.swing.GroupLayout.DEFAULT_SIZE,
                                            Short.MAX_VALUE)
                                    .addGroup(
                                            layout.createParallelGroup(
                                                    javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(jLabel3)
                                                    .addComponent(timeLabel))));

            pack();
        }

        private void locationBoxActionPerformed(
                final java.awt.event.ActionEvent evt) {
            makeChanges();
            makeChangesFish();
        }

        public void makeChanges() {
            if (fishingLocationHasBank[locationBox.getSelectedIndex()]) {
                powerFishBox.setEnabled(true);
            } else {
                powerFishBox.setEnabled(false);
                powerFishBox.setSelected(true);
            }
            fishBox.setSelectedIndex(0);
            fishBox.setModel(new javax.swing.DefaultComboBoxModel(
                    fishingSpotNames[locationBox.getSelectedIndex()]));
        }

        public void makeChangesFish() {
            final String fish = (String) fishBox.getSelectedItem();
            if (fish.equals("Tuna and Swordfish") || fish.equals("Sharks")) {
                barbarianBox.setEnabled(true);
            } else {
                barbarianBox.setEnabled(false);
                barbarianBox.setSelected(false);
            }
            if (fish.equals("Tuna and Swordfish")
                    || fish.equals("Tuna and Swordfish (Piscatoris)")) {
                dropTuna.setEnabled(true);
            } else {
                dropTuna.setEnabled(false);
                dropTuna.setSelected(false);
            }
        }

        private void pauseButtonActionPerformed(
                final java.awt.event.ActionEvent evt) {
            paused = !paused;
            if (!paused) {
                pauseButton.setText("Pause Script");
                System.out.println("Script resumed.");
            } else {
                pauseButton.setText("Resume Script");
                System.out.println("Script paused.");
            }
        }

        private void startStopButtonActionPerformed(
                final java.awt.event.ActionEvent evt) {
            if (!started) {
                startStopButton.setText("Stop Script");
                applyButtonActionPerformed(evt);
                started = true;
            } else {
                stopScript = true;
                paused = false;
            }
        }

        private void updateButtonActionPerformed(
                final java.awt.event.ActionEvent evt) {

        }
    }


    private final int LOOKING = 0, WALKING_TO_FISHING_SPOT = 1,
            STARTING_FISHING = 2, FISHING = 3, WALKING_TO_BANK = 4,
            BANKING = 5, WALKING_FROM_BANK = 6, DROPPING = 7;
    // Fishing supplies IDs
    private final int NET = 303, NORMAL_POLE = 307, FLYING_POLE = 309,
            HARPOON = 311, POT = 301, CRAYCAGE = 13431, BAIT = 313,
            FEATHERS = 314;
    private final int[] fishingAnimations = {623, 622, 621, 619, 618, 10009,
            5108, 6704, 6710, 6709, 6711, 6707, 6703, 6705, 6706, 10616};
    private final FishingSpot[] spots = {
            new FishingSpot(329, "Lure", null, 0, null),
            new FishingSpot(329, "Bait", null, 0, null),
            new FishingSpot(1172, "Net", null, 0, null),
            new FishingSpot(1172, "Bait", null, 0, null),
            new FishingSpot(6267, "Cage", null, 0, null),
            new FishingSpot(328, "Lure", new Area(new Tile(3092, 3493), new Tile(3096, 3489)),
                    26972, new Area(new Tile(3103, 3436), new Tile(3109, 3430))),
            new FishingSpot(328, "Bait", new Area(new Tile(3092, 3493), new Tile(3096, 3489)),
                    26972, new Area(new Tile(3103, 3436), new Tile(3109, 3430))),
            new FishingSpot(327, "Net", new Area(new Tile(3092, 3245), new Tile(3095, 3241)),
                    2213, new Area(new Tile(3086, 3234), new Tile(3086, 3230))),
            new FishingSpot(327, "Bait", new Area(new Tile(3092, 3245), new Tile(3095, 3241)),
                    2213, new Area(new Tile(3086, 3234), new Tile(3086, 3230))),
            new FishingSpot(323, "Net", null, 0, null),
            new FishingSpot(323, "Bait", null, 0, null),
            new FishingSpot(324, "Cage", null, 0, null),
            new FishingSpot(324, "Harpoon", null, 0, null),
            new FishingSpot(330, "Net", new Area(new Tile(3269, 3169), new Tile(3270, 3165)),
                    35647, new Area(new Tile(3270, 3147), new Tile(3274, 3145))),
            new FishingSpot(330, "Bait", new Area(new Tile(3269, 3169), new Tile(3270, 3165)),
                    35647, new Area(new Tile(3270, 3147), new Tile(3274, 3145))),
            new FishingSpot(325, "Net", null, 0, null),
            new FishingSpot(325, "Bait", null, 0, null),
            new FishingSpot(320, "Net", new Area(new Tile(2807, 3441), new Tile(2810, 3440)),
                    2213, new Area(new Tile(2839, 3433), new Tile(2848, 3432))),
            new FishingSpot(320, "Bait", new Area(new Tile(2807, 3441), new Tile(2810, 3440)),
                    2213, new Area(new Tile(2839, 3433), new Tile(2848, 3432))),
            new FishingSpot(321, "Cage", new Area(new Tile(2807, 3441), new Tile(2810, 3440)),
                    2213, new Area(new Tile(2839, 3433), new Tile(2848, 3432))),
            new FishingSpot(321, "Harpoon",
                    new Area(new Tile(2807, 3441), new Tile(2810, 3440)), 2213, new Area(new Tile(
                    2839, 3433), new Tile(2848, 3432))),
            new FishingSpot(322, "Harpoon",
                    new Area(new Tile(2807, 3441), new Tile(2810, 3440)), 2213, new Area(new Tile(
                    2839, 3433), new Tile(2848, 3432))),
            new FishingSpot(312, "Cage", new Area(new Tile(2586, 3422), new Tile(2587, 3418)),
                    494, new Area(new Tile(2598, 3421), new Tile(2600, 3420))),
            new FishingSpot(312, "Harpoon",
                    new Area(new Tile(2586, 3422), new Tile(2587, 3418)), 494, new Area(new Tile(
                    2598, 3421), new Tile(2600, 3420))),
            new FishingSpot(313, "Harpoon",
                    new Area(new Tile(2586, 3422), new Tile(2587, 3418)), 494, new Area(new Tile(
                    2598, 3421), new Tile(2600, 3420))),
            new FishingSpot(3848, "Harpoon", new Area(new Tile(2328, 3691), new Tile(2331,
                    3687)), 3824, new Area(new Tile(2338, 3701), new Tile(2346, 3699))),
            new FishingSpot(3848, "Net", new Area(new Tile(2328, 3691), new Tile(2331, 3687)),
                    3824, new Area(new Tile(2338, 3701), new Tile(2346, 3699))),
            new FishingSpot(310, "Lure", new Area(new Tile(2531, 3435), new Tile(2540, 3427)),
                    1972, new Area(new Tile(2526, 3416), new Tile(2531, 3414))),
            new FishingSpot(310, "Bait", new Area(new Tile(2531, 3435), new Tile(2540, 3427)),
                    1972, new Area(new Tile(2526, 3416), new Tile(2531, 3414))),
            new FishingSpot(315, "Lure", new Area(new Tile(2724, 3493), new Tile(2727, 3491)),
                    25808, new Area(new Tile(2726, 3528), new Tile(2732, 3525))),
            new FishingSpot(315, "Bait", new Area(new Tile(2724, 3493), new Tile(2727, 3491)),
                    25808, new Area(new Tile(2726, 3528), new Tile(2732, 3525))),
            new FishingSpot(317, "Lure", new Area(new Tile(2850, 2956), new Tile(2854, 2952)),
                    499, new Area(new Tile(2839, 2970), new Tile(2850, 2968))),
            new FishingSpot(317, "Bait", new Area(new Tile(2850, 2956), new Tile(2853, 2954)),
                    499, new Area(new Tile(2839, 2970), new Tile(2850, 2968))),
            new FishingSpot(1331, "Net", null, 0, null),
            new FishingSpot(1331, "Bait", null, 0, null),
            new FishingSpot(1332, "Cage", null, 0, null),
            new FishingSpot(1332, "Harpoon", null, 0, null),
            new FishingSpot(1333, "Harpoon", null, 0, null),
            new FishingSpot(2722, "Use-rod", null, 0, null)};
    private final String[][] fishingSpotNames = {
            new String[]{"Trout and Salmon", "Pike"},
            new String[]{"Shrimp and Anchovies", "Sardines and Herring"},
            new String[]{"Crayfish"},
            new String[]{"Trout and Salmon", "Pike"},
            new String[]{"Shrimp and Anchovies", "Sardines and Herring"},
            new String[]{"Shrimp and Anchovies", "Sardines and Herring",
                    "Lobsters", "Tuna and Swordfish"},
            new String[]{"Shrimp and Anchovies", "Sardines and Herring"},
            new String[]{"Shrimp and Anchovies", "Sardines and Herring"},
            new String[]{"Shrimp and Anchovies", "Sardines and Herring",
                    "Lobsters", "Tuna and Swordfish", "Sharks"},
            new String[]{"Lobsters", "Tuna and Swordfish", "Sharks"},
            new String[]{"Tuna and Swordfish (Piscatoris)", "Monkfish"},
            new String[]{"Trout and Salmon", "Pike"},
            new String[]{"Trout and Salmon", "Pike"},
            new String[]{"Trout and Salmon", "Pike"},
            new String[]{"Shrimp and Anchovies", "Sardines and Herring",
                    "Lobsters", "Tuna and Swordfish", "Sharks"},
            new String[]{"Leaping Fish"}};
    private final String[] fishingLocationNames = {"River Lum (Goblin House)",
            "River Lum (Lumbridge Swamp)", "River Lum (Lumbridge Church)",
            "River Lum (Barbarian Village)", "Draynor Village", "Karamja",
            "Al Kharid", "Southern Asgarnia (Thurgo)", "Catherby",
            "Fishing Guild", "Piscatoris", "Rasolo", "Seer's Village Lake",
            "Shilo Village", "Rellekka", "Otto's Grotto"};
    private final boolean[] fishingLocationHasBank = {false, // gob
            false, // swamp
            false, // church
            true, // barb vill
            true, // draynor
            false, // karamja
            true, // al kharid
            false, // southern asgarnia
            true, // catherby
            true, // fishing guild
            true, // piscatoris
            true, // rasolo
            true, // seer's
            true, // shilo
            false, // rellekka
            false // Otto's Grotto
    };
    private final int[] leapingFish = {11328, 11332, 11330};
    private final String[] leapingNames = {"trout", "sturgeon", "salmon"};
    private FishingSpot fs;
    private int fishingSpot, lastLocation;
    private int fishingSpotID = -1;
    private int[] gear;
    private final int[] BAIT_FISHING = {BAIT, NORMAL_POLE},
            NET_FISHING = {NET}, BARB_FISHING = {11323, 11334, BAIT,
            FEATHERS, 946}, HARP_FISHING = {HARPOON},
            FLY_FISHING = {FLYING_POLE, FEATHERS},
            CRAY_FISHING = {CRAYCAGE}, CAGE_FISHING = {POT};
    private boolean powerFish, barbarianMode, started, paused, painting, found,
            timerUsed, stopScript, droppingTunas, isBankBanker,
            isBankPiscBanker, isBankRasolo;
    private String fishingAction, stringBefore, stringAfter;
    private int state, check;
    private int failedFishingSpotClickAttempts;
    private long waitingTime;
    private Tile fishingSpotLocation;
    private Npc fishingSpotNpc;
    private Area bankArea;
    private Area fishingArea;

    // Paint variables
    private long startTime, timeToStop;

    private int startingExperience, fishingSkillIndex, startingLevel;

    private boolean notBefore;

    private Thread fishCountThread;

    // End Paint variables
    private VFishGUI GUI;

    private int oldFish, fishCount;

    private String[] oldArray;

    private final String[] FISH_IDS = {"Raw anchovies", // anchovies
            "Raw bass", // raw bass
            "Raw crayfish", // crayfish
            "Raw herring", // herring
            "Raw lobster", // lobs
            "Raw monkfish", // monks
            "Raw pike", // pikes
            "Raw salmon", // salmon
            "Raw sardine", // sardine
            "Raw shark", // shark
            "Raw shrimps", // shrimps
            "Raw swordfish", // swordy
            "Raw trout", // trout
            "Raw tuna", // tuna
            "Leaping trout", // leaping trout
            "Leaping sturgeon", // leaping sturgeon
           "Leaping salmon" // leaping salmon
    };

    private long timeToNext;

    public void actHuman() {
        final int roll = (int) (Math.random() * 1000);
        if (timeToNext < System.currentTimeMillis()) {
            if (roll > 995) {
                /*
                 * RSPlayer observed = getNearestPlayerByLevel(3, 138); if
				 * (observed != null && tileOnScreen(observed.getLocation())) {
				 * int rand = Random.nextInt(0, 20); if(Random.nextInt(0, 2) == 0) { time =
				 * System.currentTimeMillis() + Random.nextInt(2000, 4000); while
				 * (System.currentTimeMillis() < time && !hasSpotMoved() &&
				 * !context.inventory.isFull()) { Point p =
				 * Calculations.tileToScreen(observed.getLocation()); p.y -=
				 * rand; if (!pointOnScreen(p)) { break; } context.mouse.move(p); } }
				 * else { Point p =
				 * Calculations.tileToScreen(observed.getLocation()); p.y -=
				 * rand; context.mouse.click(p, false); Time.sleep(2000, 4000));
				 * while(Menu.isOpen()) moveMouseRandomly(250); } } REMOVED UNTIL
				 * RSBOT IS UPDATED
				 */
            } else if (roll > 990 && context.inventory.getCount() < 23
                    && state != DROPPING) {
                context.tabs.open(Tabs.Tab.STATS);
                context.mouse.move(Random.nextInt(554, 709), Random.nextInt(227, 444));
                timeToNext = System.currentTimeMillis() + Random.nextInt(2000, 25000);
            } else if (roll > 985
                    && context.inventory.getCount() < 23
                    && state != DROPPING
                    && !context.widgets.getChild(751, 15).getText()
                    .contains("Off")) {
                context.tabs.open(Tabs.Tab.FRIEND);
                context.mouse.move(Random.nextInt(554, 709), Random.nextInt(227, 444));
                timeToNext = System.currentTimeMillis() + Random.nextInt(2000, 25000);
            } else if (roll > 980 && context.inventory.getCount() < 23
                    && state != DROPPING) {
                context.tabs.open(Tabs.Tab.STATS);
                context.mouse.move(Random.nextInt(662, 709), Random.nextInt(290, 321));
                timeToNext = System.currentTimeMillis() + Random.nextInt(2000, 25000);
            } else if (roll > 960) {
                if (Random.nextInt(0, 2) == 0) {
                    context.camera.setCameraRotation((int) (context.camera.getAngle() + (Math.random() * 50 > 25 ? 1
                            : -1)
                            * (30 + Math.random() * 90)));
                } else {
                    final int key = Random.nextInt(0, 2) == 0 ? KeyEvent.VK_UP
                            : KeyEvent.VK_DOWN;
                    context.keyboard.pressKey((char) key);
                    Time.sleep(1000, 1500);
                    context.keyboard.releaseKey((char) key);
                }
            } else if (roll > 940) {
                timeToNext = System.currentTimeMillis() + Random.nextInt(2000, 25000);
                context.tabs.open(Tabs.Tab.INVENTORY);
            } else if (roll > 780) {
                // context.mouse.move(Random.nextInt(0, 750), Random.nextInt(0, 500));
                moveMouseRandomly(500);
                timeToNext = nextTime(500, 7500);
            }
        }
    }

    public boolean allTrue(final boolean[] someBooleans) {
        for (final boolean b : someBooleans) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    public boolean animationIs(final int... ids) {
        final int curAnim = context.players.getLocal().getAnimationIndex();
        for (final int id : ids) {
            if (curAnim == id) {
                return true;
            }
        }
        return false;
    }





    public void drawTextRight(final String text, final int y, final Graphics g) {
        final FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
        final Rectangle2D rectangle = fontMetrics.getStringBounds(text, g);
        final int textWidth = (int) rectangle.getWidth() + 8;
        g.setColor(new Color(0, 0, 0, 125));
        g.fillRect(515 - textWidth, y, textWidth, 16);
        g.setColor(new Color(0, 0, 0));
        g.drawRect(515 - textWidth, y, textWidth, 16);
        g.setColor(new Color(0, 0, 0, 50));
        g.drawRect(516 - textWidth, y + 1, textWidth - 2, 14);
        g.setColor(Color.WHITE);
        g.drawString(text, 519 - textWidth, y + 13);
        g.setColor(new Color(255, 255, 255, 65));
        g.fillRect(515 - textWidth, y, textWidth, 8);
    }

    public Tile getAdjacentLocationToFishingSpot(final Tile location) {
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                final Tile t = new Tile(location.getX() + x, location.getY()
                        + y);
                if (!(x == 0 && y == 0) && context.walking.canReach(t)) {
                    return t;
                }
            }
        }
        return null;
    }

    public Npc getBestFishingSpot(final String ids) {
        double Dist = -1;
        Npc closest = null;
        // final int [] validNpcs = Bot.getClient().getNpcIndexArray();
        final Npc[] npcs = context.npcs.getLoaded();

        for (final Npc element : npcs) {
            if (element == null) {
                continue;
            }
            final Npc Monster = element;
            try {
                for (final String id : Monster.getDefinition().getActions()) {
                    if (!ids.equals(id)) {
                        continue;
                    }
                    if(fishingSpotID != -1 && Monster.getId() != -1){
                        if(fishingSpotID  != Monster.getId()){
                        continue;
                        }
                    }

                    final double distance = context.calculations.distanceTo(getAdjacentLocationToFishingSpot(Monster
                            .getLocation()));
                    if (distance < Dist || Dist == -1) {
                        Dist = distance;
                        closest = Monster;
                    }
                    if(fishingSpotID == -1 && Monster.getId() != -1){
                        fishingSpotID = Monster.getId();
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return closest;
    }


    public int getFishingCheck(final String option) {
        if (option.equals("Shrimp and Anchovies")) {
            stringAfter = "Bait";
            return 2;
        }
        if (option.equals("Sharks")) {
            stringBefore = "Net";
            return 1;
        }
        if (option.equals("Tuna and Swordfish")) {
            stringBefore = "Cage";
            return 1;
        }
        return 0;
    }

    public int[] getFishingGear(final String option) {
        if (barbarianMode) {
            return null;
        }
        if (option.equals("Shrimp and Anchovies") || option.equals("Monkfish")) {
            return NET_FISHING;
        }
        if (option.equals("Crayfish")) {
            return CRAY_FISHING;
        }
        if (option.equals("Sardines and Herring") || option.equals("Pike")) {
            return BAIT_FISHING;
        }
        if (option.equals("Trout and Salmon")) {
            return FLY_FISHING;
        }
        if (option.equals("Sharks") || option.equals("Tuna and Swordfish")
                || option.equals("Tuna and Swordfish (Piscatoris)")) {
            return HARP_FISHING;
        }
        if (option.equals("Lobsters")) {
            return CAGE_FISHING;
        }
        if (option.equals("Leaping Fish")) {
            return BARB_FISHING;
        }
        return null;
    }


    public int getRandomMouseX(final int maxDistance) {
        final Point p = context.mouse.getLocation();
        if (Random.nextInt(0, 2) == 0) {
            return p.x
                    - Random.nextInt(0, p.x < maxDistance ? p.x : maxDistance);
        } else {
            return p.x
                    + Random.nextInt(1, 762 - p.x < maxDistance ? 762 - p.x
                    : maxDistance);
        }
    }

    public int getRandomMouseY(final int maxDistance) {
        final Point p = context.mouse.getLocation();
        if (Random.nextInt(0, 2) == 0) {
            return p.y
                    - Random.nextInt(0, p.y < maxDistance ? p.y : maxDistance);
        } else {
            return p.y
                    + Random.nextInt(1, 500 - p.y < maxDistance ? 500 - p.y
                    : maxDistance);
        }
    }

    public boolean hasBait() {
        if (gear.length == 1) {
            return true;
        }
        for (int i = 1; i < gear.length; i++) {
            if (946 != i && context.inventory.contains(gear[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSpotMoved() {
        if (fishingSpotNpc == null || fishingSpotLocation == null) {
            return true;
        }
        return !fishingSpotNpc.getLocation().equals(fishingSpotLocation);
    }

    public boolean inventoryEmptyExcept(final int... ids) {
        if (ids == null) {
            return context.inventory.getCount() == 0;
        }
        boolean no = false;
        outer:
        for (final InventoryItem items : context.inventory.getItems()) {
            if (items.getId() == -1) {
                continue;
            }
            for (final int id : ids) {
                if (items.getId() == id) {
                    continue outer;
                }
            }
            no = true;
            break;
        }
        return !no;
    }

    private static double round(double val, int places) {
        long factor = (long) Math.pow(10, places);
        val = val * factor;
        long tmp = Math.round(val);
        return (double) tmp / factor;
    }


    public boolean isFishing() {
        return animationIs(fishingAnimations);
    }


    @Override
    public int loop() {
        if (notBefore/*
										 * && !(context.widgets.getChild(378) .isValid()
										 * && !context.widgets.getChild(378)
										 * .getChild(89
										 * ).getText().contains("10.1120.190"))
										 */) {
            notBefore = false;
            return Random.nextInt(6000, 10000);
        }
        if (!started || paused/*
													 * || (context.widgets.getChild(378
													 * ) .isValid() &&
													 * !RSInterface.getInterface
													 * (378)
													 * .getChild(89).getText().
													 * contains("10.1120.190"))
													 */
                ) {
            fishingSpotNpc = null;
            fishingSpotLocation = null;
            return 500;
        }
        if (timerUsed && timePassed(timeToStop)) {
            return stopScript("Time passed.", true);
        }
        if (!barbarianMode && gear != null) {
            if (context.inventory.getCount(gear[0]) == 0
                    && (!(context.equipment.getItem(Equipment.Slot.WEAPON).getId() == 14109 || context.equipment.getItem(Equipment.Slot.WEAPON).getId() == 10129) || gear != HARP_FISHING)
                    || gear.length > 1 && !hasBait()) {
                return stopScript("Out of fishing supplies.", true);
            }
        }
        if (stopScript) {
            return stopScript("Script ended.", false);
        }
        if (context.bank.isOpen() && state != WALKING_FROM_BANK) {
            state = BANKING;
        } else if (context.inventory.isFull() && state != BANKING
                && state != WALKING_TO_BANK) {
            if (droppingTunas) {
                while (context.inventory.getItem(new Filter<InventoryItem>() {

                    public boolean accept(final InventoryItem a) {
                        if(a == null){
                            return false;
                        }
                        if (a.getId() == gear[0] || a.getId() == 371) {
                            return false;
                        }
                        return true;
                    }
                }) != null) {
                    if(context.inventory.isItemSelected()){
                        context.mouse.click(true);
                    }
                    InventoryItem a = context.inventory.getItem(new Filter<InventoryItem>() {
                        public boolean accept(final InventoryItem a) {
                            if(a == null){
                                return false;
                            }
                            if (a.getId() == gear[0] || a.getId() == 371) {
                                return false;
                            }
                            return true;
                        }
                    });
                    if(!a.getName().equals("")){
                        a.drop();
                    } else {
                        break;
                    }
                }
            }
            if (context.inventory.isFull()) {
                state = powerFish ? DROPPING : WALKING_TO_BANK;
            }
        } else if (state == STARTING_FISHING && isFishing()
                && context.calculations.distanceTo(fishingSpotLocation) == 1 && fishingSpotNpc.isVisible() && fishingSpotNpc.getId() == fishingSpotID) {
            state = FISHING;
        }
        switch (state) {
            case LOOKING:
                if ((fishingSpotNpc = getBestFishingSpot(fishingAction)) != null) {
                    fishingSpotLocation = fishingSpotNpc.getLocation();
                    if (!fishingSpotLocation.getMatrix(context).isVisible()) {
                        context.camera.turnTo(fishingSpotLocation);
                        if (!fishingSpotLocation.getMatrix(context).isVisible()) {
                            state = WALKING_TO_FISHING_SPOT;
                        } else {
                            state = STARTING_FISHING;
                        }
                    } else {
                        state = STARTING_FISHING;
                    }
                }
                else if (!fishingArea.contains(context.players.getLocal())) {
                    context.walking.walk(fishingArea.getCentralTile());
                }
                break;
            case WALKING_TO_FISHING_SPOT:
                final Tile adjacentTile = getAdjacentLocationToFishingSpot(fishingSpotLocation);
                if (adjacentTile.getMatrix(context).isOnMap()) {
                    context.walking.walk(adjacentTile);
                } else {
                    context.walking.walk(adjacentTile);
                }
                state = STARTING_FISHING;
                break;
            case STARTING_FISHING:
                if (hasSpotMoved() || fishingSpotNpc == null) {
                    state = LOOKING;
                    break;
                }
                if (context.players.getLocal().isMoving()) {
                    actHuman();
                    return 15;
                }
                if (!fishingSpotLocation.getMatrix(context).isVisible()) {
                    context.camera.turnTo(fishingSpotLocation);
                    if (!fishingSpotLocation.getMatrix(context).isVisible()) {
                        state = LOOKING;
                        break;
                    }
                }
                if (fishingSpotNpc.interact(fishingAction)
                       // + " Fishing spot")
                        && context.calculations.distanceTo(fishingSpotLocation) == 1) {
                    System.out.println(fishingAction);
                    waitToStop();
                    state = FISHING;
                    failedFishingSpotClickAttempts = 0;
                } else if (++failedFishingSpotClickAttempts > 3) {
                    failedFishingSpotClickAttempts = 0;
                    state = LOOKING;
                    System.out.println("Failed clicking the spot 3 times. Looking for a new spot.  " + fishingAction);
                }
                break;
            case FISHING:
                if (context.inventory.isFull()) {
                    state = powerFish ? DROPPING : WALKING_TO_BANK;
                    break;
                }
                waitingTime = nextTime(2000, 3000);
                found = false;
                boolean interrupted = false;
                while (!timePassed(waitingTime) && !found && !interrupted
                        && !context.inventory.isFull()) {
                    Time.sleep(15);
                    actHuman();
                    found = isFishing();
                    interrupted = hasSpotMoved();
                }
                if (found) {
                    waitingTime = nextTime(800, 1250);
                    while (!hasSpotMoved() && !timePassed(waitingTime)
                            && !context.inventory.isFull()) {
                        actHuman();
                        Time.sleep(15);
                    }
                }
                if ((hasSpotMoved() || !found) && !context.inventory.isFull()) {
                    state = LOOKING;
                    Time.sleep(2000, 5000);
                }
                break;
            case WALKING_TO_BANK:
                if (fs.bankOp.walkToBank()) {
                    state = BANKING;
                }
                break;
            case BANKING:
                if (gear != null && inventoryEmptyExcept(gear)) {
                    state = WALKING_FROM_BANK;
                    break;
                }
                final GameObject bankBooth = context.gameObjects.getNearest("Bank booth");
                final Npc bankNpc = context.npcs.getNearest("Banker","Rasolo");
                if (isBankRasolo && bankNpc != null
                        && bankNpc.isVisible()) {
                    if (!bankNpc.isVisible()) {
                        context.walking.walk(bankNpc.getLocation());
                    }
                    if (!bankNpc.isVisible()) {
                        break;
                    }
                    //bankNpc.interact("trade rasolo");
                    bankNpc.interact("trade");
                    if (Time.waitFor(new Condition() {
                        public boolean accept() {
                            return !context.players.getLocal().isMoving();
                        }
                    }, 1000)) {
                        waitToStop();
                    }
                    Time.sleep(100, 200);
                    if (context.widgets.getChild(620, 22).getText()
                            .contains("Rasolo the Wandering")) {
                        while (context.inventory.contains("raw salmon")) {
                            context.inventory.getItem("raw salmon").interact("sell 50 raw salmon");
                            Time.sleep(800, 1000);
                        }
                        while (context.inventory.contains("raw trout")) {
                            context.inventory.getItem("raw trout").interact("sell 50 raw trout");
                            Time.sleep(800, 1000);
                        }
                        while (context.inventory.contains("raw pike")) {
                            context.inventory.getItem("raw pike").interact("sell 50 raw pike");
                            Time.sleep(800, 1000);
                        }
                        while (context.widgets.getChild(620, 22).getText()
                                .contains("Rasolo the Wandering")) {
                            context.mouse.click(Random.nextInt(478, 494),
                                    Random.nextInt(34, 50), true);
                            Time.sleep(100, 200);
                        }
                        state = WALKING_FROM_BANK;
                    }
                } else if (bankBooth != null && !isBankBanker && !isBankPiscBanker
                        || bankNpc != null && (isBankBanker || isBankPiscBanker)) {
                    if (!bankArea.contains(context.players.getLocal())
                           ) {
                        state = WALKING_TO_BANK;
                        break;
                    }
                    Tile bt;
                    if (isBankBanker || isBankPiscBanker) {
                        bt = bankNpc.getLocation();
                    } else {
                        bt = bankBooth.getLocation();
                    }
                    if (!bt.getMatrix(context).isVisible()) {
                        context.camera.turnTo(bt);
                    }
                    if (!context.bank.isOpen()) {
                        if (isBankPiscBanker) {
                            if(bankNpc.interact("bank")){
                                Time.waitFor(new Condition() {
                                    @Override
                                    public boolean accept() {
                                        return context.bank.isOpen();
                                    }
                                }, 1000);
                            }
                        } else if (isBankBanker) {
                            //bankNpc.interact("bank banker");
                            if(bankNpc.interact("bank")){
                                Time.waitFor(new Condition() {
                                    @Override
                                    public boolean accept() {
                                        return context.bank.isOpen();
                                    }
                                }, 1000);
                            }
                        } else {
                            if(bankBooth.interact("bank")){
                                Time.waitFor(new Condition() {
                                    @Override
                                    public boolean accept() {
                                        return context.bank.isOpen();
                                    }
                                }, 1000);
                            }
                        }
                        if (Time.waitFor(new Condition() {
                            @Override
                            public boolean accept() {
                                return !context.players.getLocal().isMoving();
                            }
                        }, 1000)) {
                            waitToStop();
                        }
                        Time.sleep(100, 200);
                    }
                    if (context.bank.isOpen()) {
                        if (barbarianMode || gear == HARP_FISHING
                                && context.equipment.getItem(Equipment.Slot.WEAPON).getId() == 10129) {
                            context.mouse.click(Random.nextInt(379, 413),
                                    Random.nextInt(296, 320), true);
                            Time.sleep(150, 200);
                        } else {
                            context.bank.depositAllExcept(gear);
                        }
                        state = WALKING_FROM_BANK;
                    }
                }
                break;
            case WALKING_FROM_BANK:
                if (fs.bankOp.walkFromBank()) {
                    state = LOOKING;
                }
                break;
            case DROPPING:
                context.tabs.open(Tabs.Tab.INVENTORY);
                final boolean[] fishDropped = {false, false, false};
                if (gear == BARB_FISHING && context.inventory.contains(946)) {
                    for (int i = 0; !allTrue(fishDropped); i = Random.nextInt(0, 3)) {
                        if (fishDropped[i]) {
                            continue;
                        }
                        if (context.inventory.contains(leapingFish[i])) {
                            while (!context.inventory.getItem(946).interact("use")) {
                                Time.sleep(150, 250);
                                context.mouse.click(Random.nextInt(516, 546),
                                        Random.nextInt(205, 467), true);
                            }
                            Time.sleep(250, 300);
                            if (!context.inventory.getItem(leapingFish[i]).interact(
                                    "use knife -> leaping " + leapingNames[i])) {
                                context.mouse.click(Random.nextInt(516, 546),
                                        Random.nextInt(205, 467), true);
                            }
                            Time.sleep(400, 800);
                            if (context.inventory.getCount(leapingFish[i]) > 1) {
                                context.widgets.getChild(513, 3).interact("make all");
                            }
                            if (Time.waitFor(new Condition() {
                                @Override
                                public boolean accept() {
                                    return context.players.getLocal().getAnimationIndex() != 6702;
                                }
                            }, Random.nextInt(1100, 1500))) {
                                continue;
                            }
                            while (context.players.getLocal().getAnimationIndex() == 6702
                                    && context.inventory.contains(leapingFish[i])) {
                                actHuman();
                                Time.sleep(15);
                            }
                        }
                        fishDropped[i] = !context.inventory.contains(leapingFish[i]);
                    }
                }
                Time.sleep(150, 200);
                if (gear != null) {
                    if (gear != BARB_FISHING || !context.inventory.contains(946)
                            || context.inventory.contains(FEATHERS)
                            || context.inventory.contains(BAIT)) {
                        while (context.inventory.getItem(new Filter<InventoryItem>() {

                            public boolean accept(final InventoryItem a) {
                                for (int i = 0; i < gear.length; i++) {
                                    if(a == null){
                                        return false;
                                    }
                                    if (a.getId() == gear[i]) {
                                        return false;
                                    }
                                }
                                return true;
                            }
                        }) != null) {
                            if(context.inventory.isItemSelected()){
                                context.mouse.click(true);
                            }
                            InventoryItem a = context.inventory.getItem(new Filter<InventoryItem>() {
                                public boolean accept(final InventoryItem a) {
                                    if(a == null){
                                        return false;
                                    }
                                    for (int i = 0; i < gear.length; i++) {
                                        if (a.getId() == gear[i]) {
                                            return false;
                                        }
                                    }
                                    return true;
                                }
                            });
                            if(!a.getName().equals("")){
                                a.drop();
                            } else {
                                break;
                            }
                        }
                    }
                }
                state = STARTING_FISHING;
                break;
        }

        return 0;
    }

    public boolean moveMouseRandomly(int maxDistance) {
        if (maxDistance == 0) {
            return false;
        }
        maxDistance = Random.nextInt(1, maxDistance);
        final Point p = new Point(getRandomMouseX(maxDistance),
                getRandomMouseY(maxDistance));
        if (p.x < 1 || p.y < 1) {
            p.x = p.y = 1;
        }
        context.mouse.move(p);
        if (Random.nextInt(0, 2) == 0) {
            return false;
        }
        return moveMouseRandomly(maxDistance / 2);
    }

    public boolean moving() {
        return context.players.getLocal().isMoving();
    }

    public long nextTime(final int waitTime) {
        return System.currentTimeMillis() + waitTime;
    }

    public long nextTime(final int min, final int max) {
        return nextTime(Random.nextInt(min, max));
    }


    @Override
    public void onStop() {
        GUI.dispose();
        if (!stopScript) {

        }
    }

    @Override
    public void onRender(final Graphics g1) {
        if (painting && started) {
            final Graphics2D g = (Graphics2D) g1;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            final int gainedExp = context.skills.getCurrentExp(Skills.FISHING)
                    - startingExperience, XPToGo = context.skills.getRemainingExp(Skills.FISHING);
            int catchesPerHour;
            final long time = (System.currentTimeMillis() - startTime) / 1000;
            final long timeLeft = (timeToStop - System.currentTimeMillis()) / 1000;
            long estimatedHourly, estimatedTimeForLevel;
            if (time > 0) {
                estimatedHourly = gainedExp * 60 * 60 / time;
                estimatedTimeForLevel = (long) (60 * 60 * XPToGo / (double) estimatedHourly);
                catchesPerHour = (int) (fishCount * 60 * 60 / time);
            } else {
                estimatedHourly = 0;
                estimatedTimeForLevel = 0;
                catchesPerHour = 0;
            }
            if (!timerUsed) {
                GUI.timeLabel
                        .setText("Time until logout: -:--:-- (Timer not set)");
            } else {
                GUI.timeLabel.setText("Time until logout: " + timeLeft / 3600
                        + ":" + (timeLeft / 60 % 60 < 10 ? "0" : "") + timeLeft
                        / 60 % 60 + ":" + (timeLeft % 60 < 10 ? "0" : "")
                        + timeLeft % 60);
            }
            g.setColor(new Color(0, 0, 0, 125));
            g.fillRect(4, 124, 16, 100);
            g.setColor(new Color(0, 0, 0));
            g.drawRect(4, 124, 16, 100);
            g.setColor(new Color(0, 0, 0, 50));
            g.drawRect(5, 125, 14, 98);
            g.setColor(new Color(255, 18, 0, 125));
            g.fillRoundRect(
                    6,
                    126 + 96 - (int) (96 * context.skills.getPercentToNextLevel(Skills.FISHING) / 100.0),
                    12,
                    (int) (96 * context.skills.getPercentToNextLevel(Skills.FISHING) / 100.0),
                    4, 4);
            g.setColor(new Color(0, 0, 0));
            g.drawRoundRect(
                    6,
                    126 + 96 - (int) (96 * context.skills.getPercentToNextLevel(Skills.FISHING) / 100.0),
                    12,
                    (int) (96 * context.skills.getPercentToNextLevel(Skills.FISHING) / 100.0),
                    4, 4);
            g.setColor(new Color(112, 8, 0, 125));
            g.drawRoundRect(
                    7,
                    127 + 94 - (int) (94 * context.skills.getPercentToNextLevel(Skills.FISHING) / 100.0),
                    10,
                    (int) (94 * context.skills.getPercentToNextLevel(Skills.FISHING) / 100.0),
                    4, 4);
            g.setColor(new Color(255, 255, 255, 65));
            g.fillRect(4, 124, 16, 50);
            g.setColor(new Color(0, 0, 0, 125));
            g.fillRect(4, 224, 30, 16);
            g.setColor(new Color(0, 0, 0));
            g.drawRect(4, 224, 30, 16);
            g.setColor(new Color(0, 0, 0, 50));
            g.drawRect(5, 225, 28, 14);
            g.setColor(Color.WHITE);
            g.drawString(context.skills.getPercentToNextLevel(Skills.FISHING) + "%", 8, 237);
            g.setColor(new Color(255, 255, 255, 65));
            g.fillRect(4, 224, 30, 8);
            // Estimated fish/hour
            drawTextRight("Fish/hour: " + catchesPerHour, 240, g);
            // Fish caught
            drawTextRight("Fish caught: " + fishCount, 224, g);
            // Current fishing level
            drawTextRight("Fishing Level: " + context.skills.getCurrentLevel(Skills.FISHING),
                    208, g);
            // Current state
            String stateText = "";
            if (state == LOOKING) {
                stateText = "Looking for a spot";
            } else if (state == WALKING_TO_FISHING_SPOT) {
                stateText = "Walking to spot";
            } else if (state == STARTING_FISHING) {
                stateText = "Starting fishing";
            } else if (state == FISHING) {
                stateText = "Fishing";
            } else if (state == DROPPING) {
                stateText = "Dropping fish";
            } else if (state == WALKING_TO_BANK) {
                stateText = "Walking to bank";
            } else if (state == BANKING) {
                stateText = "Depositing";
            } else if (state == WALKING_FROM_BANK) {
                stateText = "Walking back";
            }
            drawTextRight(stateText, 160, g);
            // Average exp/hour
            drawTextRight("XP/hour: " + estimatedHourly, 176, g);
            // Estimated time for level
            drawTextRight("Level in: " + estimatedTimeForLevel / 3600 + ":"
                    + (estimatedTimeForLevel / 60 % 60 < 10 ? "0" : "")
                    + estimatedTimeForLevel / 60 % 60 + ":"
                    + (estimatedTimeForLevel % 60 < 10 ? "0" : "")
                    + estimatedTimeForLevel % 60, 192, g);
            // VFish Fisher text
            g.setColor(new Color(0, 0, 0, 125));
            g.fillRect(330, 305, 116, 16);
            g.setColor(new Color(0, 0, 0));
            g.drawRect(330, 305, 116, 16);
            g.setColor(new Color(0, 0, 0, 50));
            g.drawRect(331, 306, 114, 14);
            g.setColor(new Color(255, 255, 255));
            g.drawString("VFish", 350, 318);
            g.setColor(new Color(255, 255, 255, 65));
            g.fillRect(330, 305, 116, 8);
            g.setColor(new Color(0, 0, 0, 125));
            g.fillRect(4, 321, 511, 16);
            g.setColor(new Color(0, 0, 0));
            g.drawRect(4, 321, 511, 16);
            g.setColor(new Color(0, 0, 0, 50));
            g.drawRect(5, 322, 509, 14);
            g.setColor(new Color(255, 255, 255));
            g.drawString(
                    "Time running: "
                            + time
                            / 3600
                            + ":"
                            + (time / 60 % 60 < 10 ? "0" : "")
                            + time
                            / 60
                            % 60
                            + ":"
                            + (time % 60 < 10 ? "0" : "")
                            + time
                            % 60
                            + " - XP Earned: "
                            + gainedExp
                            + " - XP to Level: "
                            + XPToGo
                            + " - Levels gained: "
                            + (context.skills.getCurrentLevel(Skills.FISHING) - startingLevel),
                    14, 334);
            g.setColor(new Color(255, 255, 255, 65));
            g.fillRect(4, 321, 511, 8);
            // mouse
            final Point p = context.mouse.getLocation();
            final long timeSince = timeClicked;
            if (timeSince > System.currentTimeMillis() - 500) {
                g.setColor(new Color(255, 255, 255, 125));
            } else {
                g.setColor(new Color(0, 0, 0, 125));
            }
            g.drawLine(0, p.y, 762, p.y);
            g.drawLine(p.x, 0, p.x, 500);
			/*
			 * Point tp; ArrayList<Point> validPoints = new ArrayList<Point>();
			 * if(state == WALKING_TO_BANK && toBankPath != null){
			 * g.setColor(new Color(156, 0, 255)); for(Tile t : toBankPath){ tp
			 * = tileToMinimap(t); if(tp.x >= 0 && tp.y >= 0){
			 * g.fillOval(tp.x-2, tp.y-2, 4, 4); validPoints.add(tp); } } }
			 * for(int i = 0; i < validPoints.size() - 1; i++)
			 * g.drawLine(validPoints.get(i).x, validPoints.get(i).y,
			 * validPoints.get(i+1).x, validPoints.get(i+1).y);
			 * validPoints.clear(); if(state == WALKING_FROM_BANK && toSpotPath
			 * != null){ g.setColor(new Color(255, 156, 0)); for(Tile t :
			 * toSpotPath){ tp = tileToMinimap(t); if(tp.x >= 0 && tp.y >= 0){
			 * validPoints.add(tp); g.fillOval(tp.x-2, tp.y-2, 4, 4); } } }
			 * for(int i = 0; i < validPoints.size() - 1; i++)
			 * g.drawLine(validPoints.get(i).x, validPoints.get(i).y,
			 * validPoints.get(i+1).x, validPoints.get(i+1).y);
			 */
        }
    }

    @Override
    public void onStart() {
        state = LOOKING;
        timeToNext = 0;
        started = false;
        paused = false;
        notBefore = false;
        GUI = new VFishGUI();
        GUI.setVisible(true);
        oldArray = null;
        lastLocation = -1;
        failedFishingSpotClickAttempts = 0;
        while (!started) {
            Time.sleep(100);
        }
        oldFish = context.inventory.getCount();
        fishCount = 0;
        System.out.println("Starting script...");
        startTime = System.currentTimeMillis();
        startingExperience = context.skills.getCurrentExp(Skills.FISHING);
        startingLevel = context.skills.getCurrentLevel(Skills.FISHING);
        Random.nextInt(1, 5);
        System.out.println("Started Successfully!");
    }

    public Tile randomizeTile(final Tile t) {
        final int x = t.getX(), y = t.getY();
        Tile ret = null;
        while (ret == null || !context.walking.canReach(ret)) {
            ret = new Tile(x + Random.nextInt(-1, 2), y + Random.nextInt(-1, 2));
        }
        return ret;
    }

    private int stopScript(final String reason, final boolean logout) {
        if (logout) {
            System.out.println(reason + " Stopping script and logging out.");
            while (context.bank.isOpen()) {
                context.bank.close();
            }
            while (context.client.getLoginState() == -1) {
                System.out.println("out");
            }
        } else {
            System.out.println(reason + " Stopping script.");
        }
        stop();
        return -1;
    }


    public boolean timePassed(final long time) {
        return System.currentTimeMillis() > time;
    }

    public void waitToStop() {
        while (context.players.getLocal().isMoving()) {
            Time.sleep(15);
        }
    }

    @Override
    public void mouseDragged(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseMoved(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

}