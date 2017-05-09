/*******************************************************************************
 * Copyright (C) 2017, Paul Scerri, Sean R Owens
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
/*
 * ParticleFilterCostMap.java
 *
 * Created on April 28, 2006, 9:51 AM
 *
 */

package AirSim.Machinetta.CostMaps;

import AirSim.Machinetta.Beliefs.RSSIReading;
import Machinetta.Coordination.MAC.InformationAgent;
import Machinetta.Coordination.MACoordination;
// import AirSim.Machinetta.RSSIReading;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.ProxyState;
import Machinetta.State.StateChangeListener;
import Gui.IntGrid;
import Gui.DoubleGrid;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.text.DecimalFormat;

/**
 *
 * @author pscerri
 */
public class ParticleFilterCostMap implements Runnable, CostMap, Dynamic, StateChangeListener {
    public final static DecimalFormat fmt = new DecimalFormat("0.000000");
    
    public final static boolean BUILD_REAL_ENTROPY_NOT_DENSITY=true;
    public final static boolean USE_RANDOM_MOTION = true;
    
    public final static int RESAMPLE_EVERY_N_READINGS=50;
    public int resampleCounter = 0;
    public int localReadingCounter = 0;
    public int remoteReadingCounter = 0;
    public int readingsSinceLastResample = 0;
    
    public final static double SHARING_PROB = 1.0;
    public final static int MAX_DISTANCE_BETWEEN_READINGS = 20;
    public final static double MAX_PARTICLE_UPDATE_DISTANCE = 1000;
    public final static double SENSOR_FOV_CONE_DEGREES = 30;
    public final static double DEFAULT_PARTICLE_WEIGHT = 0.9;
    public final static int ENTROPY_FOOTPRINT_SIZE = 80;
    
    
    public final static double EPSILON = 0.05;
    public final static double POSSIBLE_OVERLAPPED_P = 0.7;
    public final static double LIKELY_NOT_EMITTER_P2 = 0.01;
    public final static double LIKELY_EMITTER_P3 = 0.99;
    public final static double DEFAULT_INITIAL_PARTICLE_WEIGHT = 0.01;
    public final static int PARTICLE_MOVE_EW_PROB = 3;
    public final static int PARTICLE_MOVE_NS_PROB = 3;
    public final static double PARTICLE_MOVE_X_PERCENT_OF_WIDTH = .0015;
    public final static double PARTICLE_MOVE_Y_PERCENT_OF_HEIGHT = .0015;
    
    public final static double ENTROPY_DISTANCE_SCALE_FACTOR = 100.0;
    
    /*========
     *
     * From Robin
     */
    
    private double signalAlpha = 10000;
    private double mapWidth = 50000;
    private double mapHeight = 50000;
    
    //    public int distSize = 1000;
    
    // TODO: SRO Thu Jul 27 20:39:21 EDT 2006
    //
    // Not sure what purpose scaleFactor serves - it is used in
    // getCost but its not really clear to me why/how it is used.
    public int scaleFactor = 500;
    
    // public ProbDistDisplay pdd = null;
    public double timeStamp = 0.0;
    public double entropy = 0.0;
    public static int readingsQueueSize = 50;
    public static int sharedReadingsQueueSize = 100;
    public static int readingsToShare = 10;
    public static int NUM_PARTICLES = 10000;
    static Random rand = new Random();
    int readingIndex = 0;
    private int extraParts = 0;
    public boolean execute = false;
    
    BlockingQueue<RSSIReading> incomingReadingQueue = new LinkedBlockingQueue<RSSIReading>();
    Thread myThread = null;
    
    Object particlesLock = new Object();
    Particle [] particles = new Particle[NUM_PARTICLES];
    RSSIReading [] RSSIReadings = new RSSIReading[readingsQueueSize];
    PriorityQueue readings = new PriorityQueue();
    PriorityQueue sharedReadings = new PriorityQueue();
    
    /** Creates a new instance of ParticleFilter */
    public ParticleFilterCostMap(boolean execute, double signalAlpha, double mapWidth, double mapHeight) {
        this.execute = execute;
        this.signalAlpha = signalAlpha;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        
        Machinetta.Debugger.debug("Set mapWidth,mapHeight to "+mapWidth+", "+mapHeight,1,this);
        
        myThread = new Thread(this);
        
        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle();
        }
        
        state.addChangeListener(this);
    }
    
    public void start() {
        Machinetta.Debugger.debug("Starting particle filter thread",1,this);
        myThread.start();
    }
    
    /**
     * Simple implementation
     */
    
    public void initializeParticles(){
        particles = new Particle[NUM_PARTICLES];
        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle();
        }
    }
    public void addRSSIReading(RSSIReading sr){
        //RSSIReadings[readingIndex] = sr;
        //readingIndex++;
        
        if(readings.size()> 2*readingsQueueSize) {
            PriorityQueue tempQueue = new PriorityQueue();
            for(int i = 0; i<readingsQueueSize-1; i++){
                tempQueue.offer(readings.poll());
            }
            readings = tempQueue;
            //readingIndex = 0;
        }
        
        
        double oldEntropy = entropy;
        //calculateEntropy();
        double entropyChange = entropy - oldEntropy;
        sr.entropyChange = entropyChange;
        
        
        sr.importance = timeStamp++;
        readings.offer(sr);
    }
    
    
    private void addRandomMotionModel() {
        synchronized(particlesLock) {
            for(int loopi = 0; loopi < particles.length; loopi++) {
                particles[loopi].move();
            }
        }
    }
    
    public void updateParticles(){
        
        Iterator iter1 = readings.iterator();
        while(iter1.hasNext()){
            RSSIReading s1 = (RSSIReading)iter1.next();
            incorporateRSSIReadings(s1);
        }
    }
    
    /**
     * I don't know what this does, hence I don't know whether to scale it ?
     *
     */
    public boolean distanceBtwReadings(RSSIReading sr1, RSSIReading sr2){
        double dist = Math.sqrt((sr1.x-sr2.x)*(sr1.x-sr2.x) + (sr1.y-sr2.y)*(sr1.y-sr2.y));
        if(dist < MAX_DISTANCE_BETWEEN_READINGS) return true;
        else return false;
        
    }
    public double square(int a){
        return a*a;
    }
    public double distance(Point A, Point B){
        return Math.sqrt(square(A.x-B.x)+square(A.y-B.y));
    }
    //returns angle in rads formed at Point A by triangle that joins
    //points A, B, and C
    public double getAngle(Point A, Point B, Point C){
        double distAB = distance(A,B);
        double distAC = distance(A,C);
        double distBC = distance(B,C);
        return Math.acos(((-1*distBC*distBC)+(distAC*distAC)+(distAB*distAB))/(2*distAC*distAB));
    }
    public boolean testParticle(Particle pt, RSSIReading sr1, RSSIReading sr2){
        Point A = new Point((int)sr1.x,(int)sr1.y);
        Point B = new Point(pt.x,pt.y);
        Point C = new Point((int)sr2.x,(int)sr2.y);
        double theta = getAngle(A,B,C);
        theta = theta*180/Math.PI;
        if(theta > SENSOR_FOV_CONE_DEGREES) return false;
        double dist = distance(A,B);
        if(dist > MAX_PARTICLE_UPDATE_DISTANCE) return false;
        return true;
    }
    public RSSIReading[] shareReadings(){
        int index=0;
        RSSIReading[] senseReads = new RSSIReading[readings.size()];
        while(index < readingsToShare && index < readings.size()){
            senseReads[index] = (RSSIReading)readings.poll();
            index++;
        }
        return senseReads;
    }
    
    public boolean particleInSensorRange(double dist1){
        if(true) return true;
        if(dist1 > MAX_PARTICLE_UPDATE_DISTANCE) return false;
        return true;
    }
    
    public double expectedSignalStrength(double dist) {
        double signalStrength = signalAlpha;
        double expectedSignalStrength = signalStrength*(1.0/dist);
        return expectedSignalStrength;
    }
    
    public void incorporateRSSIReadings(RSSIReading sr1) {

	boolean comp = false;
	int numTimes = 0;
	Point sr1Loc = new Point((int)sr1.x,(int)sr1.y);
	Point particleLoc = null;
	Particle pt = null;
	double distParticleSr1 = 0.0;

	Machinetta.Debugger.debug("incorporateRSSIReadings: "+sr1.getID()+" at "+sr1Loc.toString()+" signal="+fmt.format(sr1.strength), 0,this);

	long likelyCount = 0;
	for (int j = 0; j < particles.length; j++) {
	    pt = particles[j];
	    particleLoc = new Point(pt.x,pt.y);
	    distParticleSr1 = distance(particleLoc,sr1Loc);

	    if (particleInSensorRange(distParticleSr1)) {

		double signal1 = sr1.strength;
		double signalExpected = expectedSignalStrength(distParticleSr1);
		double p = 0.0;

		double tolerance = Math.abs(signalExpected-signal1)/signalExpected;
		if(distParticleSr1 < 100 && tolerance <0.2) p = 0.9;
		else if(signal1 > signalExpected) p = 0.5;
		else p = 0.1;

		sr1.importance = p*timeStamp;

		particles[j].w += p;
		if(Double.isNaN(particles[j].w)) {
		    particles[j].w = 0;
		}
	    }
	    else {
		particles[j].w += DEFAULT_PARTICLE_WEIGHT;
		if(Double.isNaN(particles[j].w)){
		    particles[j].w = 0;
		}
	    }

	}
    }

    public boolean withinBounds(int x, int y){
        boolean validLocation = true;
        if(x>=mapWidth) validLocation = false;
        if(x<0) validLocation = false;
        if(y>=mapHeight) validLocation = false;
        if(y<0) validLocation = false;
        return validLocation;
    }
    
    public void resampleParticles(){
        int random = 0;
        //extraParts--;
        // Normalize
        double norm = 0.0;
        
        for (int i = 0; i < particles.length; i++) {
            norm += particles[i].w;
        }
        
        synchronized(particlesLock) {
            
            if (norm > 0.0) {
                //System.out.println("Weights: ");
                
                for (int i = 0; i < particles.length; i++) {
                    particles[i].w /= norm;
                    // System.out.println(i + " : " + particles[i].w);
                }
                
                Machinetta.Debugger.debug("normalized particle weights",1,this);
            } else {
                Machinetta.Debugger.debug("norm is <= 0.0",1,this);
                random = particles.length;
            }
            norm = 0.0;
            for (int i = 0; i < particles.length; i++) {
                norm += particles[i].w;
            }
            Machinetta.Debugger.debug("norm is now "+norm,1,this);
            
            //System.out.println("Norm: " + norm);
            // Need to do the resampling process
            Particle [] prev = particles;
            
            // Some are random
            for (int i = 0; i < random; i++) {
                particles[i] = new Particle();
            }
            // Weighted from previous probabilities
            for (int i = random; i < particles.length; i++) {
                double sample = rand.nextDouble();
                double c = 0.0;
                // System.out.println("Sample"+sample);
                int p = -1;
                do {
                    p++;
                    c += prev[p].w;
                    //System.out.println("probability"+lineParticles[p].w);
                } while (c <= sample && p < prev.length-1);
                particles[i] = prev[p].clone();
                Particle prt = particles[i];
            }
        }
    }
    
    public void dumpParticles() {
        Point[] particles = getParticlePoints();
        StringBuffer buf = new StringBuffer("PARTICLEDUMP: ");
        for(int loopi = 0; loopi < particles.length; loopi++) {
            
        }
    }
    
    public void step() {
        updateParticles();
        if(execute) resampleParticles();
        
    }
    
    class Particle {
        int x;
        int y;
        // Main.TargetTypes type = Main.TargetTypes.M1;
        
        //double w = 0.001; here
        double w = DEFAULT_INITIAL_PARTICLE_WEIGHT;
        public int VECTOR_LENGTH = 20;
        
        public Particle() {
            x = rand.nextInt((int)mapWidth);
            y = rand.nextInt((int)mapHeight);
        }
        public void move() {
            
            int ew = rand.nextInt(PARTICLE_MOVE_EW_PROB);
            int ns = rand.nextInt(PARTICLE_MOVE_NS_PROB);
            if (ew == 0) x = x - (int)(PARTICLE_MOVE_X_PERCENT_OF_WIDTH * mapWidth);
            else if (ew == 2) x = x + (int)(PARTICLE_MOVE_X_PERCENT_OF_WIDTH * mapWidth);
            if (ns == 0) y = y - (int)(PARTICLE_MOVE_Y_PERCENT_OF_HEIGHT * mapHeight);
            else if (ns == 2) y = y + (int)(PARTICLE_MOVE_Y_PERCENT_OF_HEIGHT * mapHeight);
            
            
            // System.out.println("Target " + id + " at " + x + ", " + y);
        }
        public Particle clone() {
            Particle np = new Particle();
            np.x = x;
            np.y = y;
            // np.type = type;
            np.w = w;
            return np;
        }
    }
    
    /*========
     *
     * End Robin
     */
    
    
    DoubleGrid entropyGrid = null;
    Boolean rebuildEntropyGrid = true;
    
    public double getEntropyValue(int localx, int localy) {
        // TODO:  50 should come from somewhere else.
        int gridx = localx/50;
        int gridy = localy/50;
        return entropyGrid.getValue(gridx,gridy);
    }
    public DoubleGrid getEntropyGrid() { return entropyGrid; }

    public double getProbabilityNormal(double mean, double x) {
        double sigma = 10/mean;
        double mult = (1/(sigma*Math.sqrt(2*Math.PI)));
        double arg = -(x-mean)*(x-mean)/(2*sigma*sigma);
        return mult*Math.exp(arg);
    }
    
    private void populateEntropyGrid(int footprintSize, int gridSize, Point[] points) {
        Machinetta.Debugger.debug("populateEntropyGrid: footprintSize="+footprintSize+", gridSize="+gridSize+", number of particles="+points.length, 1, this);
        DoubleGrid newEntropyGrid = new DoubleGrid(gridSize,gridSize);
        
        double[][] footprint = new double[footprintSize][footprintSize];
        double centerx = ((double)footprintSize)/2;
        double centery = ((double)footprintSize)/2;
        
        double footprintTotal = 0.0;
        for(int loopx = 0; loopx < footprintSize; loopx++) {
            for(int loopy = 0; loopy < footprintSize; loopy++) {

		double diffx = (centerx - loopx);
		double diffy = (centery - loopy);
		double r = Math.hypot(diffx, diffy);
		footprint[loopx][loopy] = getProbabilityNormal(1.0, r);
		footprintTotal += footprint[loopx][loopy];
            }
        }
        
        newEntropyGrid.clear(0);
        
        long timeStart = System.currentTimeMillis();
        int scale = (int)(mapWidth/gridSize);
        for(int loopi = 0; loopi < points.length; loopi++) {
            newEntropyGrid.addFootPrint(points[loopi].x/scale, points[loopi].y/scale,footprint);
        }
        
        
        if(null == entropyGrid)
            entropyGrid = new DoubleGrid(newEntropyGrid);
        else {
            entropyGrid.copy(newEntropyGrid,true);
        }
        
        if(BUILD_REAL_ENTROPY_NOT_DENSITY) {
            entropyGrid.entropy();
        } else {
            // int gridAvg = (int)((points.length * (double)footprintTotal)/(gridSize * gridSize));
            double gridAvg = newEntropyGrid.average();
            
            newEntropyGrid.computeHighestLowest();
            
            Machinetta.Debugger.debug("populateEntropyGrid: before density->entropy, new entropyGrid.highest = "+newEntropyGrid.getHighest()+", gridAvg="+gridAvg,1,this);
            entropyGrid.computeHighestLowest();
            Machinetta.Debugger.debug("populateEntropyGrid: after density->entropy, entropyGrid.highest = "+entropyGrid.getHighest(),1,this);
            
            entropyGrid.divide(entropyGrid.getHighest());
            
            Machinetta.Debugger.debug("populateEntropyGrid: after scaling by grid avg, entropyGrid.highest = "+entropyGrid.getHighest(),1,this);
            
            entropyGrid.negate();
            entropyGrid.add(1.0);
        }
        
        long timeEnd = System.currentTimeMillis();
        long timeElapsed = (timeEnd - timeStart);
        
        Machinetta.Debugger.debug("populateEntropyGrid: footprintSize="+footprintSize+", GridSize="+gridSize+", PARTICLES="+points.length+", elapsed="+timeElapsed,1,this);
    }
    
    public Point[] getParticlePoints() {
        Point[] points =null;
        synchronized(particlesLock) {
            points = new Point[particles.length];
            for(int loopi = 0; loopi < particles.length; loopi++) {
                points[loopi] = new Point(particles[loopi].x,particles[loopi].y);
            }
        }
        return points;
    }
    
    public void rebuild() {
        synchronized(rebuildEntropyGrid) {
            if(rebuildEntropyGrid) {
                long timeStart = System.currentTimeMillis();
                Point[] points = getParticlePoints();
                populateEntropyGrid(ENTROPY_FOOTPRINT_SIZE,(int)(mapWidth/50),points);
                rebuildEntropyGrid = false;
                long timeEnd = System.currentTimeMillis();
                long timeElapsed = (timeEnd - timeStart);
            }
        }
    }
    
    
    static private ProxyState state = new ProxyState();
    
    /**
     * Return the cost if a vehicle moves from (x1,y1,z1) at t1 to (x2,y2,z2) at t2
     */
    public double getCost(double x1, double y1, double z1, long t1, double x2, double y2, double z2, long t2) {
        double entropy = 0.0;
        double cost = 0.0;
        int dx = scaleFactor, dy = scaleFactor;
        
        if (x1 > x2) {
            double temp = x2;
            x2 = x1;
            x1 = temp;
        }
        
        if (y1 > y2) {
            double temp = y2;
            y2 = y1;
            y1 = temp;
        }
        

 	int gridx1 = entropyGrid.toGridX(x1);
 	int gridy1 = entropyGrid.toGridY(y1);
 	int gridx2 = entropyGrid.toGridX(x2);
 	int gridy2 = entropyGrid.toGridY(y2);
	if(!entropyGrid.insideGrid(gridx1, gridy1)
	   || !entropyGrid.insideGrid(gridx2, gridy2))
	    return Double.MAX_VALUE;

        double steps = 10;
        double ldx = (x2-x1)/steps;
        double ldy = (y2-y1)/steps;
        
        int px = -1;
        int py = -1;
        
        for (int i = 0; i < steps; i++) {
            x1 += ldx; y1 += ldy;
            int x = (int)Math.floor(x1/dx);
            int y = (int)Math.floor(y1/dy);
            int gridx = entropyGrid.toGridX(x);
            int gridy = entropyGrid.toGridY(y);
            entropy += entropyGrid.getValue(gridx, gridy);
        }
        // 'steps' is 10
        // entropy is now between 0 and (steps * 1.0), i.e. 0 and 10;

	//
	// i.e. entropy=10.0 is a high value, farther from the
	// average, hence LOW uncertainty, hence we DON'T want to go
	// there
	//
	// entropy=0.0 is a low value, closer to the average, hence
	// HIGH uncertainty, hence we WANT to go there
	//
	// so we want entropy=0.0 to result in cost = -50.0, and
	// entropy = 10.0 to result in cost = 50.0;
	double span = (1.0 * steps);
	double halfSpan = span/2;
	double scaleFactor = 100.0/steps;
	cost = (entropy - halfSpan) * scaleFactor;

        return cost;
    }
    
    /**
     * Return the cost if a vehicle moves from (x1,y1) at t1 to (x2,y2) at t2
     */
    public double getCost(double x1, double y1, long t1, double x2, double y2, long t2) {
        // This is where the entropy would need to be used to calculate a cost
        return getCost(x1,y1,0,t1,x2,y2,0,t2);
    }
    
    /**
     *
     * For dynamic cost maps, allows them to update.
     */
    public void timeElapsed(long t) {
        // Do nothing
    }
    
    /**
     * Which Rectangles are good to be in at time t.
     */
    public ArrayList<Rectangle> getGoodAreas(long t) {
        return null;
    }
    
    /**
     * Which Rectangles are bad to be in at time t.
     */
    public ArrayList<Rectangle> getBadAreas(long t) {
        return null;
    }
    
    /**
     *
     * Called when something updates state
     *
     *
     * @param b array of BeliefIDs that have changed.
     */
    public void stateChanged(BeliefID[] b) {
        
        int numReadings = 0;
        for (BeliefID id: b) {
            Belief bel = state.getBelief(id);
            if (bel instanceof RSSIReading) {
                numReadings++;
                // TODO: Wed Jul 26 14:47:30 EDT 2006 SRO
                //
                // Might change this to use offer(obj, timeout)
                // instead.
                try {
                    incomingReadingQueue.put((RSSIReading)bel);
                } catch (InterruptedException e) {
                    
                }
            }
        }
        if(numReadings > 0)
            Machinetta.Debugger.debug("Added "+numReadings+" RSSIReadings to incomingReadingQueue.",0,this);
    }
    
    public void run() {

	LinkedList<RSSIReading> beliefList = new LinkedList<RSSIReading>();

	while(true) {
	    beliefList.clear();
	    try {
		RSSIReading newReading = incomingReadingQueue.take();
		beliefList.add(newReading);
		incomingReadingQueue.drainTo(beliefList);	    
	    }
	    catch (InterruptedException e) {
		    
	    }

	    //	    if(beliefList.size() > 10) 
		Machinetta.Debugger.debug("Particle Filter sees "+beliefList.size()+" readings", 0, this);
	    int numReadings = beliefList.size();
	    long startTime = System.currentTimeMillis();

	    boolean added = false;
	    for (RSSIReading reading: beliefList) {
		addRSSIReading(reading);
		added = true;
		readingsSinceLastResample++;
		if(reading.locallySensed()) {
		    localReadingCounter++;
		    if(rand.nextDouble() < SHARING_PROB) {
			Machinetta.Debugger.debug("Sharing RSSI belief "+reading.getID()+" with other UAVs", 0, this);
			InformationAgent agent = new InformationAgent(reading, 6);
			MACoordination.addAgent(agent);
			// Let it act
			agent.stateChanged();
		    }
		}
		else {
		    remoteReadingCounter++;
		}
	    }

	    if (added) {
		updateParticles();
		if(USE_RANDOM_MOTION) {
		    addRandomMotionModel();
		    Machinetta.Debugger.debug("Adding random motion after "+readingsSinceLastResample+" readings since last resample", 1,this);
		}
		readingsSinceLastResample++;
		if(readingsSinceLastResample > RESAMPLE_EVERY_N_READINGS) {
		    resampleCounter++;
		    Machinetta.Debugger.debug("Resampling "+resampleCounter+" time, readings remote="+remoteReadingCounter+", local="+localReadingCounter+", since last resample="+readingsSinceLastResample,1,this);
		    readingsSinceLastResample = 0;
		    resampleParticles();
		    synchronized(rebuildEntropyGrid) {
			rebuildEntropyGrid = true;
		    }
		}
	    }
	    long endTime = System.currentTimeMillis();
	    long elapsed = endTime - startTime;
	    double avg = elapsed/numReadings;
	    Machinetta.Debugger.debug("Time to process "+numReadings+" readings = "+elapsed+" avg per reading="+fmt.format(avg),1,this);

	}

    }
    
}
