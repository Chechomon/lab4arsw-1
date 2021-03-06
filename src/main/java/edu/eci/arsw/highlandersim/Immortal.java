package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private AtomicInteger health;
    
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());
    private boolean paused;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = new AtomicInteger(health);
        this.defaultDamageValue=defaultDamageValue;
        
    }

    public void run() {
    	paused=false;
        while (true) {
        	if (paused) {
        		try {
        			synchronized(this) {
        				this.wait();
        			}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
            Immortal im;

            int myIndex = immortalsPopulation.indexOf(this);

            int nextFighterIndex = r.nextInt(immortalsPopulation.size());
            

            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            im = immortalsPopulation.get(nextFighterIndex);

            this.fight(im);

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    public void fight(Immortal i2) {
    	synchronized(immortalsPopulation) {
    		
    			if (i2.getHealth() > 0) {
                    i2.substractHealth(defaultDamageValue);
                    this.health.addAndGet(defaultDamageValue);
                    updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
                } else {
                    updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");   
                }
    	}
        

    }

    public void substractHealth(int v) {
    		int x=health.get();
    		x-=v;
    		health.getAndSet(x);
    	
    }

    public int getHealth() {
        return health.get();
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }
    public void pause() {
    	paused=true;
    }
    public void Resume() {
    	paused=false;
    	synchronized(this) {
    		this.notify();
    	}
    	
    }
}
