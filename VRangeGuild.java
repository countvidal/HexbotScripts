import java.applet.Applet;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.regex.Pattern;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.hexbot.api.event.MessageEvent;
import org.hexbot.api.event.RenderEvent;
import org.hexbot.api.methods.core.MethodContext;
import org.hexbot.api.methods.input.Camera;
import org.hexbot.api.methods.input.Keyboard;
import org.hexbot.api.methods.input.Mouse;
import org.hexbot.api.methods.interactive.GameObjects;
import org.hexbot.api.methods.interactive.Npcs;
import org.hexbot.api.methods.interactive.Players;
import org.hexbot.api.methods.interactive.Widgets;
import org.hexbot.api.methods.interfaces.Inventory;
import org.hexbot.api.methods.interfaces.Skills;
import org.hexbot.api.methods.movement.Walking;
import org.hexbot.api.methods.util.Settings;
import org.hexbot.api.util.Random;
import org.hexbot.api.util.Time;
import org.hexbot.api.wrapper.Area;
import org.hexbot.api.wrapper.GameObject;
import org.hexbot.api.wrapper.InventoryItem;
import org.hexbot.api.wrapper.Npc;
import org.hexbot.api.wrapper.Tile;
import org.hexbot.api.wrapper.WidgetComponent;
import org.hexbot.script.Category;
import org.hexbot.script.Employer;
import org.hexbot.script.ScriptManifest;
import org.hexbot.script.Worker;

import util.ExchangeItem;
import util.ExchangeParser;

@ScriptManifest(author = "countvidal", description = "Bots Ranging Competition at Range Guild", name = "Vidals Guild Ranger", category = Category.RANGED)
public class VRangeGuild extends Employer implements RenderEvent, MessageEvent,
		MouseMotionListener {

	public VRangeGuild(MethodContext context) {
		super(context);
	}

	// Paint
	private static final String PAINT_IMAGE_URL = "http://i.imgur.com/aEDg8dX.png";
	private BufferedImage paintImage = null;
	// paint related variables
	private long startTime;
	private long timeRunning;
	private int startExp;
	private int gainedExp;
	private boolean closePaint = false;
	private boolean startScript = false;
	int once = 0;
	private int price;
	private int starttickets;
	String process = "Null";

	@Override
	public void onStart() {
		submit(new StartGame(context), new PlayGame(context,new EquipArrows(context),
				new ShootTarget(context), new CloseTarget(context),
				new GetPrize(context)), new ReturnToSpot(context));
		loadImage();
		if (context.inventory.contains(1464)) {
			starttickets = context.inventory.getItem(1464).getStackSize();
		} else {
			starttickets = 0;
		}

		if (!context.inventory.contains(995)) {
			System.out.println("No coins in inventory stopping.");
			stop();
		}

		ExchangeItem item = ExchangeParser.lookup("Rune arrows", true);

		price = (int) item.getAverage();
		System.out.println(Integer.toString(price));
        context.mouse.setSpeed(Mouse.Speed.FAST);
		startTime = System.currentTimeMillis();
		startExp = context.skills.getCurrentExp(context.skills.RANGE);

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	private void updateStats() {

		timeRunning = System.currentTimeMillis() - startTime;
		gainedExp = context.skills.getCurrentExp(context.skills.RANGE)
				- startExp;

	}

	private void loadImage() {
		try {
			URL painImageUrl = new URL(PAINT_IMAGE_URL);
			paintImage = ImageIO.read(painImageUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not get paintImage");
		}

	}

	@Override
	public void onRender(Graphics g) {

		Graphics2D g2d = (Graphics2D) g.create();
		// drawing mouse
		drawMouse(g);

		if (context.mouse.getX() >= 483 && context.mouse.getX() <= 507
				&& context.mouse.getY() >= 344 && context.mouse.getY() <= 368) {
			if (closePaint && once == 0) {
				closePaint = false;
				once = 1;
			} else if (once == 0) {
				closePaint = true;
				once = 1;
			}
		} else {
			once = 0;
		}

		// drawing paint

		if (paintImage != null && !closePaint) {
			drawPaint(g2d);
		}

		g2d.dispose();

	}

	public void drawMouse(Graphics g) {

		Graphics2D g2d = (Graphics2D) g.create();

		g2d.setColor(Color.CYAN);
		int x = context.mouse.getX();
		int y = context.mouse.getY();
		g2d.drawLine(x - 5, y - 5, x + 5, y + 5);
		g2d.drawLine(x - 5, y + 5, x + 5, y - 5);
		g2d.drawString("(" + context.mouse.getX() + "," + context.mouse.getY()
				+ ")", x + 10, y + 10);

		g2d.dispose();

	}

	private void drawPaint(Graphics2D g2d) {

		updateStats();

		// paint the image
		g2d.drawImage(paintImage, 3, 330, null);

		// paint expGained
		g2d.drawString(
				String.format("%,d", gainedExp)
						+ " | "
						+ String.format(
								"%,d",
								(Skills.getExperienceAt(context.skills
										.getCurrentLevel(context.skills.RANGE) + 1) - context.skills
										.getCurrentExp(context.skills.RANGE)))
						+ " XP to LvL", 342, 398);

		// paint time running
		int seconds = (int) (timeRunning / 1000) % 60;
		int minutes = (int) (timeRunning / 60000) % 60;
		int hours = (int) timeRunning / 1000 / 60 / 60;
		String timeRunningString = String.format("%02dh:%02dm:%02ds", hours,
				minutes, seconds);
		g2d.drawString(timeRunningString, 325, 438);

		// paint exp per hour
		int expPerSecond = 0;
		if (timeRunning > 0) {
			if (seconds < 5 && minutes <= 0 && hours <= 0) {
				expPerSecond = 5;
			} else {
				expPerSecond = gainedExp / (int) (timeRunning / 1000);
			}
		}

		double expPerHour = expPerSecond * 60 * 60;
		// System.out.println(Integer.toString(expPerHour));
		double expLeft = (double) Skills.getExperienceAt(context.skills
				.getCurrentLevel(context.skills.RANGE) + 1)
				- context.skills.getCurrentExp(context.skills.RANGE);
		DecimalFormat df = new DecimalFormat("#.##");
		double timeToLvl = 0.00;
		if (expPerHour > 0) {
			timeToLvl = expLeft / expPerHour;
			g2d.drawString(
					String.format("%,.2f", expPerHour) + " | "
							+ df.format(timeToLvl) + " Hrs to lvl.", 335, 420);
		}

		g2d.drawString(process, 99, 435);

		g2d.drawString(Integer.toString(getCurrentScore()) + " | "
				+ getTargetColor(), 110, 397);
		int tickets = context.inventory.getItem(1464).getStackSize()
				- starttickets;
		int arrows = tickets / 40;
		g2d.drawString(millValue(price * arrows), 83, 418);

	}

	/**
	 * Substitutes 000000 for M
	 * 
	 * @return String Number with M
	 */
	private static String millValue(final int value) {
		if (value >= 1000000) {
			return Integer.toString(value / 1000000) + "M";
		} else {
			return Integer.toString(value / 1000) + "K";
		}
	}

	public abstract class WorkerGroup extends Worker {

		private Worker[] workers;

		public WorkerGroup(MethodContext context, Worker... workers) {
			super(context);
			this.workers = workers;
		}

		public abstract boolean activate();

		public void work() {
			for (Worker worker : workers) {
				if (worker.validate()) {
					worker.work();
				}
			}
		}

		public boolean validate() {
			return activate();
		}
	}

	public boolean isInGame() {
		return getArrowNum() > 0;
	}

	public int getArrowNum() {
		return context.settings.getAt(156);
	}

	public int getCurrentScore() {
		return context.settings.getAt(157);
	}

	public String getTargetColor() {
		int c = context.settings.getAt(158);
		if (c == 9 || c == 10) {
			return "Black";
		} else if (c == 5 || c == 7 || c == 8) {
			return "Blue";
		} else if (c == 1) {
			return "Yellow";
		} else if (c == 2 || c == 3 || c == 4) {
			return "Red";
		} else if (c == 0) {
			return "Bulls Eye";
		} else if (c == 1) {
			return "Missed";
		}
		return "";
	}

	private class ReturnToSpot extends Worker {

		public ReturnToSpot(MethodContext context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		Area arena = new Area(new Tile(2667, 3417), new Tile(2672, 3421),
				new Tile(2675, 3418), new Tile(2672, 3413));

		@Override
		public void work() {
			if (context.walking.walk(arena.getCentralTile())) {
				Time.sleep(1000, 1200);
			}
		}

		@Override
		public boolean validate() {
			// TODO Auto-generated method stub
			return isInGame() && !arena.contains(context.players.getLocal());
		}

	}

	private class StartGame extends Worker {
		public StartGame(MethodContext context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void work() {
			process = "Starting Game";
			final int PARENT_JUDGE = 242;
			WidgetComponent judge = context.widgets.getChild(PARENT_JUDGE, 1);
			if (judge != null && judge.isVisible()) {
				WidgetComponent toClick = context.widgets.getChild(
						PARENT_JUDGE, 4);
				if (toClick != null && toClick.isVisible()) {
					toClick.click();
					Time.sleep(800, 900);
				}
			}
			final int SURE = 230;
			WidgetComponent sure = context.widgets.getChild(SURE, 1);
			if (sure != null && sure.isVisible()) {
				sure.click();
				Time.sleep(800, 900);
			}

			final int CONTINUE2 = 64;
			WidgetComponent continues2 = context.widgets.getChild(CONTINUE2, 3);
			if (continues2 != null && continues2.isVisible()) {
				continues2.click();
				Time.sleep(800, 900);
			}

            final int CONTINUE = 241;
            WidgetComponent continues = context.widgets.getChild(CONTINUE, 3);
            if (continues != null && continues.isVisible()) {
                continues.click();
                Time.sleep(800, 900);
            }

			if (!continues2.isVisible() && !continues.isVisible() && !sure.isVisible()
					&& !judge.isVisible()) {
				Npc judges = context.npcs.getNearest(5809);
				judges.click();
			}
			return;
		}

		@Override
		public boolean validate() {
			return !isInGame();
		}

	}

	private class PlayGame extends WorkerGroup {
		Area arena = new Area(new Tile(2667, 3417), new Tile(2672, 3421),
				new Tile(2675, 3418), new Tile(2672, 3413));

		public PlayGame(MethodContext ctx, Worker... workers) {
			super(ctx, workers);
		}

		@Override
		public boolean activate() {
			return isInGame() && arena.contains(context.players.getLocal());
		}

	}

	private class EquipArrows extends Worker {

		public EquipArrows(MethodContext context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void work() {
			process = "Equipping Arrows";
			context.inventory.getItem(882).click();
			Time.sleep(1000, 1200);
		}

		@Override
		public boolean validate() {
			return context.inventory.contains(882);
		}

	}

	private class ShootTarget extends Worker {

		public ShootTarget(MethodContext context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void work() {
			process = "Shooting Target";
			GameObject target = context.gameObjects.getNearest(23940);
			if (target == null) {
				return;
			}
			if (target.isVisible()) {
				target.click();
				Time.sleep(200, 500);
			} else {
				context.camera.turnTo(target);
				context.camera.setPitch(false);
			}
		}

		@Override
		public boolean validate() {
			return getArrowNum() > 0 && getArrowNum() < 11
					&& !context.widgets.getChild(325, 89).isVisible();
		}

	}

	private class CloseTarget extends Worker {

		public CloseTarget(MethodContext context) {
			super(context);
		}

		@Override
		public void work() {
			process = "Closing Target";
			context.widgets.getChild(325, 88).click();
			Time.sleep(500, 800);
		}

		@Override
		public boolean validate() {
			return getArrowNum() > 0
					&& context.widgets.getChild(325, 88).isVisible();
		}

	}

	private class GetPrize extends Worker {

		public GetPrize(MethodContext context) {
			super(context);
		}

		@Override
		public void work() {
			process = "Getting Prize";
			Npc judge = context.npcs.getNearest(5809);
			if (judge == null) {
				return;
			}
			if (judge.isVisible()) {
				judge.click();
			} else {
				context.camera.turnTo(judge);
			}
			Time.sleep(500, 800);
		}

		@Override
		public boolean validate() {
			return getArrowNum() == 11
					&& !context.widgets.getChild(325, 89).isVisible();
		}

	}

	@Override
	public void onMessageReceived(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}

}