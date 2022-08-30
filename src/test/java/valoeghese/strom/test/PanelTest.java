package valoeghese.strom.test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

// I should really put all my utilities into a library at some point so as not to copy paste every time
public abstract class PanelTest extends JPanel {
	protected abstract int getColour(int x, int y);

	public PanelTest size(int size) {
		this.size = size;
		return this;
	}

	public PanelTest scale(int scale) {
		this.scale = scale;
		return this;
	}

	public PanelTest icon(String icon) {
		this.icon = icon;
		return this;
	}

	private int size = 500;
	private int scale = 4;
	private String icon = null;

	// drag
	private Point mousePt;
	protected int xo, lxo = 0;
	protected int yo, lyo = 0;

	public void start(String title) {
		image = new BufferedImage(this.size, this.size, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < this.size; ++x) {
			for (int z = 0; z < this.size; ++z) {
				image.setRGB(x, z, this.getColour(x, z));
			}
		}

		// https://stackoverflow.com/questions/15574065/java-mousedragged-and-moving-around-in-a-graphical-interface
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				mousePt = e.getPoint();
			}
		});

		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int dx = e.getX() - mousePt.x;
				int dy = e.getY() - mousePt.y;
				xo -= dx;
				yo -= dy;
				mousePt = e.getPoint();
				redraw(true);
			}
		});

		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == 'z') {
					if (scale < 16) {
						scale <<= 1;
						xo = (xo >> 1) - (size >> 2);
						yo = (yo >> 1) - (size >> 2);
						redraw(false);
					}
				} else if (e.getKeyChar() == 'x') {
					int to = scale >> 1;

					// checking bits won't drop off
					if (scale == (to << 1)) {
						scale = to;
						xo = (xo << 1) + (size >> 1);
						yo = (yo << 1) + (size >> 1);
						redraw(false);
					}
				}
			}
		});

		JFrame frame = new JFrame(title);

		if (this.icon != null) {
			try {
				frame.setIconImage(ImageIO.read(new File(this.icon)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.setPreferredSize(new Dimension(this.size, this.size));
		this.setFocusable(true);
		this.grabFocus();
		this.redraw(false);
		frame.add(this);
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	static BufferedImage image;

	public void redraw(boolean reuse) {
		BufferedImage next = new BufferedImage(this.size, this.size, BufferedImage.TYPE_INT_RGB);

		if (reuse) { // if same scale only
			BufferedImage current = image;

			for (int x = 0; x < this.size; ++x) {
				int actualX = (x + xo) * scale;
				int oldX = (actualX / scale) - lxo;

				for (int y = 0; y < this.size; ++y) {
					int actualY = (y + yo) * scale;
					int oldY = (actualY / scale) - lyo;

					if (oldX >= 0 && oldX < this.size && oldY >= 0 && oldY < this.size) {
						next.setRGB(x, y, current.getRGB(oldX, oldY));
					} else {
						next.setRGB(x, y, this.getColour(actualX, actualY));
					}
				}
			}
		} else {
			int half = this.size / 2;

			CompletableFuture<Void> q0 = CompletableFuture.runAsync(() -> { // quarter 0
				for (int x = 0; x < half; ++x) {
					int actualX = (x + xo) * scale;

					for (int y = 0; y < half; ++y) {
						int actualY = (y + yo) * scale;
						next.setRGB(x, y, this.getColour(actualX, actualY));
					}
				}
			});

			CompletableFuture<Void> q1 = CompletableFuture.runAsync(() -> {
				for (int x = half; x < this.size; ++x) {
					int actualX = (x + xo) * scale;

					for (int y = 0; y < half; ++y) {
						int actualY = (y + yo) * scale;
						next.setRGB(x, y, this.getColour(actualX, actualY));
					}
				}
			});

			CompletableFuture<Void> q2 = CompletableFuture.runAsync(() -> {
				for (int x = 0; x < half; ++x) {
					int actualX = (x + xo) * scale;

					for (int y = half; y < this.size; ++y) {
						int actualY = (y + yo) * scale;
						next.setRGB(x, y, this.getColour(actualX, actualY));
					}
				}
			});

			CompletableFuture<Void> q3 = CompletableFuture.runAsync(() -> {
				for (int x = half; x < this.size; ++x) {
					int actualX = (x + xo) * scale;

					for (int y = half; y < this.size; ++y) {
						int actualY = (y + yo) * scale;
						next.setRGB(x, y, this.getColour(actualX, actualY));
					}
				}
			});

			try {
				// make sure all done
				q0.get();
				q1.get();
				q2.get();
				q3.get();
			} catch (Exception e) {
				throw new RuntimeException("Exception while threading worldgen", e);
			}
		}

		synchronized (this) {
			this.image = next;
			lxo = xo;
			lyo = yo;
		}
		super.repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(image, 0, 0, null);
	}
}
