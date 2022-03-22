public class Clock extends Thread{
	private int time;
	private GameManager gm;
	
	public Clock(GameManager gm, int t) {
		time = t;
		this.gm = gm;
	}
	public void run() {
		while (true) {
			//System.out.println("DEBUG TIME " + time);
			if (time <= 0) {
				System.out.println("Changing phase");
				gm.switchPhase();
				time = gm.ROUNDTIME;
				/*System.out.println(clock.get());
				if (clock.get() <= 0) {
					//phase shit
					if (phase.equals("MESSAGING")) phase = "VOTING";
					else phase = "MESSAGING";
					
					sendPhaseAll();
					//reset clock
					clock.set(ROUNDTIME);
					System.out.println("SET CLOCK");
				}*/
			}
			else {
				gm.sendTimeAll();
				try{
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
					System.out.println("Clock interrupted.");
				}
				time --;
				gm.sendTimeAll();
				//send time
			}
		}
	}
	public void set(int t) {
		time = t;
	}
	public int get() {
		return time;
	}
}
