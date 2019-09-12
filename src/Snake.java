import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Snake extends Canvas implements Runnable{

	public static void main(String[] args) {
		new Snake();
	}
	
	JFrame frame;
	
	Thread thread;
	
	ArrayList<Point> snake = new ArrayList<Point>();
	byte x = 1;
	byte y = 0;
	byte speed = 1;
	
	Point food = new Point(5,5);
	boolean skip = false;
	
	Color backgroundColor = new Color(0,50,0);
	Color snakeColor = new Color(0,0,0);
	Color snakeContrastColor = new Color(200,200,200);
	
	boolean debug = false;
	boolean graphics = false;
	Ai ai;	
	
	HashMap<String, Image> sprites = new HashMap<String, Image>();
	
	Snake instance;
	
	public Snake() {
		BufferedImage spriteSheet = loadSpriteSheet("snake.png");
		loadSprite("snakeBody", 0, 0, spriteSheet);
		loadSprite("snakeRight", 1, 0, spriteSheet);
		loadSprite("snakeDown", 1, 1, spriteSheet);
		loadSprite("snakeUp", 1, 2, spriteSheet);
		loadSprite("snakeLeft", 1, 3, spriteSheet);
		loadSprite("food", 0, 1, spriteSheet);
		spriteSheet = null;
		
		instance = this;
		ai = new Ai();
		
		frame = new JFrame();
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.add(this);
		frame.setVisible(true);
		
		this.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_UP) {
					if(y != 1) {
						x = 0;
						y = -1;
					}
				}
				if(e.getKeyCode() == KeyEvent.VK_DOWN) {
					if(y != -1) {
						x = 0;
						y = 1;
					}
				}
				if(e.getKeyCode() == KeyEvent.VK_LEFT) {
					if(x != 1) {
						x = -1;
						y = 0;
					}
				}
				if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
					if(x != -1) {
						x = 1;
						y = 0;
					}
				}
				if(e.getKeyCode() == KeyEvent.VK_NUMPAD9) {
					if(!skip) {
						grow();
					}
				}
				if(e.getKeyChar() == KeyEvent.VK_ESCAPE) {
					if(ai == null) {
						ai = new Ai();
					}else {
						ai = null;
					}
				}
			}
		});
		
		thread = new Thread(this);
		thread.start();
	}

	private BufferedImage loadSpriteSheet(String name) {
		try {
			this.graphics = true;
			return ImageIO.read(new File("snake.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void loadSprite(String name, int x, int y, BufferedImage spriteSheet) {
		Image sprite = spriteSheet.getSubimage(x*16, y*16, 16, 16);
		sprites.put(name, sprite);
	}
	
	public void run() {
		long last = 0;
		init();
		while(true) {
			if(System.currentTimeMillis()-last > (1000/(speed+10))) {
				tick();
				last = System.currentTimeMillis();
			}
			render();
		}
	}
		
	private void init() {
		this.snake.clear();
		this.snake.add(new Point(9,5));
		this.snake.add(new Point(8,5));
		this.snake.add(new Point(7,6));
		
		this.skip = false;
		this.speed = 1;
		this.x = 1;
		this.y = 0;
		
		generateFood();
	}
	
	private void generateFood() {
		int x = (int) (Math.random()*(this.getWidth()/16));
		int y = (int) (Math.random()*(this.getHeight()/16));
		food = new Point(x,y);
	}

	
	private void tick() {
		if(ai != null) {
			ai.tick(this);
		}
		move();
		for(int i = 1; i < this.snake.size(); i++) {
			if(this.snake.get(0).distance(this.snake.get(i)) == 0) {
				init();
			}
		}
		if(food.distance(snake.get(0)) == 0) {
			grow();
		}
	}
	
	private void grow() {
		generateFood();
		Point last = this.snake.get(0);
		this.snake.add(new Point(last.x, last.y));
		speed++;
	}
	
	private void move() {
		for(int i = snake.size()-1; i >= 0; i--) {
			if(i == snake.size()-1 && skip) {skip = false; continue;}
			Point p = snake.get(i);
			if(i-1 >= 0) {
				p.x = snake.get(i-1).x;
				p.y = snake.get(i-1).y;
			}else {
				p.x += x;
				p.y += y;
				if(p.x > this.getWidth()/16) {
					p.x = 0;
					x = 1;
				}else if(p.x < 0) {
					p.x = this.getWidth()/16;
					x = -1;
				}else if(p.y > this.getHeight()/16) {
					p.y = 0;
					y = 1;
				}else if(p.y < 0) {
					p.y = this.getHeight()/16;
					y = -1;
				}
			}
		}
	}
	
	private void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(3);
			return;
		}
		
		Graphics g = bs.getDrawGraphics();
		
		g.setColor(backgroundColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		if(!graphics || debug) {
			g.setColor(Color.WHITE);
			g.fillRect(food.x*16+4, food.y*16+4, 8, 8);
			g.setColor(Color.BLACK);
			g.drawRect(food.x*16+4, food.y*16+4, 8, 8);
			
			for(int i = 0; i < this.snake.size(); i++) {
				Point p = this.snake.get(i);
				if(p != null) {
					g.setColor(snakeColor);
					g.fillRect(p.x*16, p.y*16, 16, 16);
					g.setColor(snakeContrastColor);
					g.drawRect(p.x*16, p.y*16, 16, 16);
					g.setColor(Color.WHITE);
					g.drawString(""+i, p.x*16, p.y*16+16);
				}
			}
		}else {
			g.drawImage(sprites.get("food"), food.x*16, food.y*16, null);
			
			for(int i = 0; i < this.snake.size(); i++) {
				Point p = this.snake.get(i);
				if(p != null) {
					if(i == 0) {
						if(x == 1) {
							g.drawImage(sprites.get("snakeRight"), p.x*16, p.y*16, null);		
						}else if(x == -1) {
							g.drawImage(sprites.get("snakeLeft"), p.x*16, p.y*16, null);		
						}else if(y == 1) {
							g.drawImage(sprites.get("snakeDown"), p.x*16, p.y*16, null);	
						}else if(y == -1) {
							g.drawImage(sprites.get("snakeUp"), p.x*16, p.y*16, null);	
						}
					}else {
						g.drawImage(sprites.get("snakeBody"), p.x*16, p.y*16, null);		
					}
				}
			}
		}
		
		g.dispose();
		bs.show();
	}
	
	
	
}

class Ai {
	
	public void tick(Snake instance) {
		Point head = instance.snake.get(0);
		if(instance.food.x < head.x) {
			moveLeft(instance);
		}else if(instance.food.x > head.x) {
			moveRight(instance);
		}else if(instance.food.y < head.y) {
			moveUp(instance);
		}else if(instance.food.y > head.y){
			moveDown(instance);
		}
		
		checkDanger(instance);
		
	}
	
	private Point getClosest(Snake instance) {
		Point head = instance.snake.get(0);
		Point closestP = null;
		int closest = Integer.MAX_VALUE;
		for(int i = 2; i < instance.snake.size(); i++) {
			int deltaX = head.x - instance.snake.get(i).x;
			int deltaY = head.y - instance.snake.get(i).y;
			int delta = Math.abs(deltaX) + Math.abs(deltaY);
			
			if(delta > 1) {
				if(deltaX > 0 && instance.x == 1) {continue;}
				if(deltaX < 0 && instance.x == -1) {continue;}
				if(deltaY > 0 && instance.y == 1) {continue;}
				if(deltaY < 0 && instance.y == -1) {continue;}
			}
			
			if(delta < closest) {
				closest = delta;
				closestP = instance.snake.get(i);
			}		
		}
		return closestP;
	}
	
	public Point getFoodDelta(Snake instance) {
		Point head = instance.snake.get(0);
		return new Point(head.x-instance.x, head.y-instance.y);
	}
	
	private boolean checkDanger(Snake instance) {
		Point head = instance.snake.get(0);
		Point closestP = null;
		int closest = Integer.MAX_VALUE;
		for(int i = 2; i < instance.snake.size(); i++) {
			int deltaX = head.x - instance.snake.get(i).x;
			int deltaY = head.y - instance.snake.get(i).y;
			int delta = Math.abs(deltaX) + Math.abs(deltaY);
			
			if(delta > 1) {
				if(deltaX > 0 && instance.x == 1) {continue;}
				if(deltaX < 0 && instance.x == -1) {continue;}
				if(deltaY > 0 && instance.y == 1) {continue;}
				if(deltaY < 0 && instance.y == -1) {continue;}
			}
			
			if(delta < closest) {
				closest = delta;
				closestP = instance.snake.get(i);
			}		
		}
		if(closestP == null) {
			return false;
		}
		int deltaX = head.x - closestP.x;
		int deltaY = head.y - closestP.y;
		
		if(instance.x == 1) {
			if(deltaX == -1) {
				instance.x = 0;
				instance.y = 1;
			}
		}else if(instance.x == -1) {
			if(deltaX == 1) {
				instance.x = 0;
				instance.y = -1;
			}
		}else if(instance.y == 1) {
			if(deltaY == -1) {
				instance.x = 1;
				instance.y = 0;
			}			
		}else if(instance.y == -1) {
			if(deltaY == 1) {
				instance.x = 1;
				instance.y = 0;
			}
		}
		
		return false;
	}
	
	private void moveLeft(Snake instance) {
		if(instance.x != 1) {
			instance.x = -1;
			instance.y = 0;
		}else {
			Point fDelta = getFoodDelta(instance);
			if(fDelta.y > 0) {
				instance.x = 0;
				instance.y = -1;
			}else if(fDelta.y < 0) {
				instance.x = 0;
				instance.y = 1;
			}
		}
	}
	
	private void moveRight(Snake instance) {
		if(instance.x != -1) {
			instance.x = 1;
			instance.y = 0;
		}else {
			Point fDelta = getFoodDelta(instance);
			if(fDelta.y > 0) {
				instance.x = 0;
				instance.y = -1;
			}else if(fDelta.y < 0) {
				instance.x = 0;
				instance.y = 1;
			}
		}	
	}
	
	private void moveUp(Snake instance) {
		if(instance.y != 1) {
			instance.x = 0;
			instance.y = -1;
		}else {
			Point fDelta = getFoodDelta(instance);
			if(fDelta.x > 0) {
				instance.x = -1;
				instance.y = 0;
			}else if(fDelta.x < 0) {
				instance.x = 1;
				instance.y = 0;
			}
		}
	}
	
	private void moveDown(Snake instance) {
		if(instance.y != -1) {
			instance.x = 0;
			instance.y = 1;
		}else {
			Point fDelta = getFoodDelta(instance);
			if(fDelta.x > 0) {
				instance.x = -1;
				instance.y = 0;
			}else if(fDelta.x < 0) {
				instance.x = 1;
				instance.y = 0;
			}
		}
	}
	
}
