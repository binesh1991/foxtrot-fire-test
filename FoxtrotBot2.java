import robocode.*;
import java.awt.geom.Point2D;

public class FoxtrotBot2 extends CharlieBot {
	private EnemyBot enemy = new EnemyBot();
	private byte radarDirection = 1;
	private byte moveDirection = 1;

	public void run() {
		while (true) {
			doRadar();
			doMove();
			doGun();
		}
	}

	public void onHitByBullet(HitByBulletEvent e) {
		// keep track of the enemy robot that sent the bullet
		ScannedRobotEvent sre = new ScannedRobotEvent(e.getName(),
		enemy.getEnergy(),
		e.getBearing(),
		enemy.getDistance(),
		e.getHeading(),
		e.getVelocity(),
		false);
		enemy.update(sre, this);
	}

	public void onHitWall(HitWallEvent e) {
		// just being dumb here
		turnRight(90);
		ahead(150);
	}

	public void onRobotDetected(ScannedRobotEvent e) {
		// keep track of the robot that was scanned by the radar
		enemy.update(e, this);
	}

	void doRadar() {
		double turn = getHeading() - getGunHeading() + 180 + enemy.getBearing();
		turn += 30 * radarDirection;
		turnGunRight(normalizeBearing(turn));
		radarDirection *= -1;
	}
	
	public void onRobotDeath(RobotDeathEvent e) {
		if (e.getName().equals(enemy.getName())) {
			enemy.reset();
		}
	}

	public void doMove() {
		// always face the robot and move 200 units in either direction to avoid bullets
		if (getTime() % 20 == 0) {
			turnRight(normalizeBearing(enemy.getBearing() + 90));
			moveDirection *= -1;
			ahead(200 * moveDirection);
		}
	}

	void doGun() {
		// calculate power of bullet and figure out the bearin of the enemy bot
		double firePower = Math.min(400 / enemy.getDistance(), 3);
		double b = absoluteBearing(this.getX(), this.getY(), enemy.getX(), enemy.getY());
		turnGunRight(normalizeBearing(b - getGunHeading()));

	    if (getGunHeat() == 0) {
			fire(firePower);
		}
	}
	
	double absoluteBearing(double x1, double y1, double x2, double y2) {
		// some mathematical wizardry because arctan((x2-x1)/(y2-y1)) didn't work
		double x0 = x2 - x1;
		double y0 = y2 - y1;
		double diag = Point2D.distance(x1, y1, x2, y2);
		double a = Math.toDegrees(Math.asin(x0 / diag));
		double b = 0;

		if (x0 > 0 && y0 > 0) {
			b = a;
		} else if (x0 < 0 && y0 > 0) {
			b = 360 + a;
		} else if (x0 > 0 && y0 < 0) {
			b = 180 - a;
		} else if (x0 < 0 && y0 < 0) {
			b = 180 - a;
		}

		return b;
	}
	
	double normalizeBearing(double angle) {
		// this is done to keep the bearing value between -180 and 180 degrees
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}

	// a nested class to keep the details of the enemy bots
	private class EnemyBot {
		private double bearing;
		private double distance;
		private double energy;
		private String name;
		private double x;
		private double y;

		public EnemyBot() {
			reset();
		}

		public void reset() {
			bearing = 0.0;
			distance = 0.0;
			energy = 0.0;
			name = "";
			x = 0.0;
			y = 0.0;
		}

		public double getBearing() {
			return bearing;
		}

		public double getDistance() {
			return distance;
		}

		public double getEnergy() {
			return energy;
		}

		public String getName() {
			return name;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}
		

		public void update(ScannedRobotEvent e, Robot robot) {
			bearing = e.getBearing();
			distance = e.getDistance();
			energy = e.getEnergy();
			name = e.getName();

			double absBearingDeg = (robot.getHeading() + e.getBearing());
			if (absBearingDeg < 0) absBearingDeg += 360;

			x = robot.getX() + Math.sin(Math.toRadians(absBearingDeg)) * e.getDistance();
			y = robot.getY() + Math.cos(Math.toRadians(absBearingDeg)) * e.getDistance();
		}
	}
}
