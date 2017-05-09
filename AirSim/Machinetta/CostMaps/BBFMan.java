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
 * BBFMan.java
 *
 * Created on April 28, 2006, 9:51 AM
 *
 */

package AirSim.Machinetta.CostMaps;

import AirSim.Machinetta.UAVRI;
import AirSim.Machinetta.SimTime;
import AirSim.Machinetta.BeliefShare;
import AirSim.Machinetta.MiniWorldState;
import AirSim.Machinetta.Path3D;
import Machinetta.ConfigReader;
import Gui.StringUtils;
import AirSim.Environment.Assets.Sensors.EmitterModel;
import AirSim.Environment.Assets.Emitter;
import AirSim.Machinetta.Beliefs.RSSIReading;
import Machinetta.Coordination.MAC.InformationAgent;
import Machinetta.Coordination.MACoordination;
// import AirSim.Machinetta.RSSIReading;
import Machinetta.Debugger;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.ProxyState;
import Machinetta.State.StateChangeListener;
import Gui.IntGrid;
import Gui.DoubleGrid;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.text.DecimalFormat;
import java.awt.EventQueue;

/**
 *
 * @author pscerri
 */
public class BBFMan {
    private final static int MERGED_CHANNEL=-1;

    private final static DecimalFormat fmt = new DecimalFormat("0.000000");

    private HashSet<BeliefID> seenReadings = new HashSet<BeliefID>();
    private final static Debugger d = new Debugger();
    private int localReadingCounter = 0;
    private int remoteReadingCounter = 0;
    private SharingTrack localSharingTrack = new SharingTrack(100);
    private SharingTrack remoteSharingTrack = new SharingTrack(100);
    private SharingTrack overallSharingTrack = new SharingTrack(100);

    private double mapWidth;
    private double mapHeight;
    private MiniWorldState miniWorldState;

    private ArrayList<Point> emitters;
    private BBFTabPanel beliefPanel = null;
    private BBFTabPanel entropyPanel = null;

    // The size of the internal grid of probabilities for the binary
    // bayes filter.
    private int binaryBayesFilterGridSize;
    private double emitterModelScaleFactor;

//     private BinaryBayesFilter bbf = null;
//     private BinaryBayesFilter remoteBbf = null;
		    
    private HashMap<Integer,BinaryBayesFilter> bbfMap = new HashMap<Integer,BinaryBayesFilter>();
    public Integer[] getFilterChannels() { return bbfMap.keySet().toArray(new Integer[1]); }
    public BinaryBayesFilter getFilter(int channel) { return bbfMap.get(channel); }
    private HashMap<Integer,BinaryBayesFilter> remoteBbfMap = new HashMap<Integer,BinaryBayesFilter>();
    private HashMap<Integer,BinaryBayesDisplay> beliefDisplayMap = new HashMap<Integer,BinaryBayesDisplay>();
    private HashMap<Integer,BinaryBayesDisplay> entropyDisplayMap = new HashMap<Integer,BinaryBayesDisplay>();
    

    // @NOTE: mergedBBF isn't _really_ a merged version of the other
    // filters - it's really just the beliefs and entropy.
    private BinaryBayesFilter mergedBBF;
    private double[][] mergedBelief;
    private double[][] mergedEntropy;
    private boolean rebuildMerged=true;

    public ArrayList<Clust> clustList=null;

    private void rebuildMerged() {
	if(!rebuildMerged)
	    return;
	long startTime = System.currentTimeMillis();
	boolean first = true;
	for(BinaryBayesFilter bbf: bbfMap.values()) {
	    if(first) {
		first = false;
		mergedBBF.setBeliefs(bbf.getBeliefs());
	    }
	    else {
		mergedBBF.getHigherBeliefs(bbf);
	    }
	}
	long endTime = System.currentTimeMillis();
        d.debug(1, "rebuilding merged beliefs, elapsed time="+(endTime-startTime));
	rebuildMerged = false;
    }

    public void getEntropyGrid(DoubleGrid eGrid) { 
	rebuildMerged();
	mergedBBF.getEntropyGrid(eGrid); 
    }

    public double[][] getBeliefs() { 
	rebuildMerged();
	return mergedBBF.getBeliefs(); 
    }

    public void copyBeliefs(double[][] copy) {
	rebuildMerged();
	mergedBBF.copyBeliefs(copy);
    }

    public int getSize() { return binaryBayesFilterGridSize; } 


    private static int readingsQueueSize = 50;
    private static int readingsToShare = 10;

    private static Random rand = new Random();

    private BlockingQueue<RSSIReading> incomingReadingQueue = new LinkedBlockingQueue<RSSIReading>();
    private Thread myThread = null;

    private double[][] groundTruthBelief;
    
    public void setClustList(ArrayList<Clust> clustList) {
        this.clustList = clustList;
	
	BinaryBayesDisplay[] bdisplays=null;
	BinaryBayesDisplay[] edisplays=null;
	synchronized(beliefDisplayMap) {
	    bdisplays=beliefDisplayMap.values().toArray(new BinaryBayesDisplay[1]);
	}
	synchronized(entropyDisplayMap) {
	    edisplays=entropyDisplayMap.values().toArray(new BinaryBayesDisplay[1]);
	}

	final BinaryBayesDisplay[] bdisplaysF = bdisplays;
	final BinaryBayesDisplay[] edisplaysF = edisplays;
	final ArrayList<Clust> clustListF = clustList; 
        try {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
		    if(null != bdisplaysF) {
			for(int loopi = 0; loopi < bdisplaysF.length; loopi++) {
			    if(null != bdisplaysF[loopi]) 
				bdisplaysF[loopi].setClustList(clustListF);
			}
		    }
		    if(null != edisplaysF) {
			for(int loopi = 0; loopi < edisplaysF.length; loopi++) {
			    if(null != edisplaysF[loopi])
				edisplaysF[loopi].setClustList(clustListF);
			}
		    }
                }
            }
            );
        } catch(InterruptedException e) {
            System.err.println("Update of binary bayes panel was interrupted e="+e);
            e.printStackTrace();
        } catch(java.lang.reflect.InvocationTargetException e) {
            System.err.println("e="+e);
            e.printStackTrace();
        }
	
    }

    private void initGroundTruth() {
	groundTruthBelief = new double[binaryBayesFilterGridSize][binaryBayesFilterGridSize];
	
	for(int loopx = 0; loopx < binaryBayesFilterGridSize; loopx++) {
	    for(int loopy  = 0; loopy  < binaryBayesFilterGridSize; loopy++) {
		groundTruthBelief[loopx][loopy] = .000001;
	    }
	}

	for(Point emitterPoint: emitters) {
	    int beliefx = (int)(emitterPoint.x/UAVRI.BBF_GRID_SCALE_FACTOR);
	    int beliefy = (int)(emitterPoint.y/UAVRI.BBF_GRID_SCALE_FACTOR);
	    groundTruthBelief[beliefx][beliefy] = .99;
	}
	
    }
    /** Creates a new instance of BinaryBayesFilter */
    public BBFMan(double mapWidth, double mapHeight, MiniWorldState miniWorldState, ArrayList<Point> emitters, BBFTabPanel beliefPanel, BBFTabPanel entropyPanel, BinaryBayesFilter binaryBayesFilter) {

	this.mapWidth = mapWidth;
	this.mapHeight = mapHeight;
	this.miniWorldState = miniWorldState;
	this.emitters = emitters;
	this.beliefPanel = beliefPanel;
	this.entropyPanel = entropyPanel;
	
	binaryBayesFilterGridSize = (int)(mapWidth/UAVRI.BBF_GRID_SCALE_FACTOR);
	emitterModelScaleFactor = mapWidth/EmitterModel.ORIGINAL_SIZE_METERS;

	mergedBelief = new double[binaryBayesFilterGridSize][binaryBayesFilterGridSize];
	mergedEntropy = new double[binaryBayesFilterGridSize][binaryBayesFilterGridSize];
	mergedBBF = new BinaryBayesFilter(Emitter.CHANNEL_DEFAULT, binaryBayesFilterGridSize,
					  0, 
					  0, 
					  UAVRI.BBF_GRID_SCALE_FACTOR,
					  emitterModelScaleFactor,
					  UAVRI.BBF_SENSOR_MAX_RANGE_METERS,
					  UAVRI.BBF_SENSOR_MIN_RANGE_METERS,
					  UAVRI.BBF_UNIFORM_PRIOR,
					  UAVRI.BBF_DIFFUSE_INTERVAL_MS,
					  0,
					  false,
					  false);
	if(null != beliefPanel) {
	    double x = miniWorldState.getCurrx();
	    double y = miniWorldState.getCurry();
	    Path3D path = miniWorldState.getPath();
	    BinaryBayesDisplay bbfPanel = new BinaryBayesDisplay(x, y, mergedBBF, path, false, true, mapWidth,mapHeight);
	    synchronized(beliefDisplayMap) {
		beliefDisplayMap.put(MERGED_CHANNEL,bbfPanel);
	    }
	    beliefPanel.addPanel("merge", bbfPanel);
	}
	if(null != entropyPanel) {
	    double x = miniWorldState.getCurrx();
	    double y = miniWorldState.getCurry();
	    Path3D path = miniWorldState.getPath();
	    BinaryBayesDisplay bbfPanel = new BinaryBayesDisplay(x, y, mergedBBF, path, true, false, mapWidth,mapHeight);
	    synchronized(entropyDisplayMap) {
		entropyDisplayMap.put(MERGED_CHANNEL,bbfPanel);
	    }
	    entropyPanel.addPanel("merge", bbfPanel);
	}


	initGroundTruth();

// 	if(null == binaryBayesFilter) {
// 	    createBBF(Emitter.CHANNEL_DEFAULT);
// 	}
// 	else {
// 	    bbfMap.put(Emitter.CHANNEL_DEFAULT,binaryBayesFilter);
// 	}
        d.debug("Set mapWidth,mapHeight to "+mapWidth+", "+mapHeight,1,this);
    }

    private void createBBF(int channel) {
	d.debug("Creating binarybayesfilter for channel "+channel, 1, this);	

	BinaryBayesFilter defaultChannel = bbfMap.get(Emitter.CHANNEL_DEFAULT);

	// @TODO: HACK - setting diffuse delta to 0 - should really be
	// set in config files
	BinaryBayesFilter bbf;
	BinaryBayesFilter remoteBbf;
	bbf = new BinaryBayesFilter(channel, 
				    binaryBayesFilterGridSize,
				    0, 
				    0, 
				    UAVRI.BBF_GRID_SCALE_FACTOR,
				    emitterModelScaleFactor,
				    UAVRI.BBF_SENSOR_MAX_RANGE_METERS,
				    UAVRI.BBF_SENSOR_MIN_RANGE_METERS,
				    UAVRI.BBF_UNIFORM_PRIOR,
				    UAVRI.BBF_DIFFUSE_INTERVAL_MS,
				    0,
				    false,
				    false);
	bbf.initializeGTBeliefs(emitters);
	if(null != beliefPanel) {
	    double x = miniWorldState.getCurrx();
	    double y = miniWorldState.getCurry();
	    Path3D path = miniWorldState.getPath();
	    BinaryBayesDisplay bbfPanel = new BinaryBayesDisplay(x, y, bbf, path, false, true, mapWidth,mapHeight);
	    synchronized(beliefDisplayMap) {
		beliefDisplayMap.put(channel,bbfPanel);
	    }
	    beliefPanel.addPanel("Channel: "+channel, bbfPanel);
	}
	if(null != entropyPanel) {
	    double x = miniWorldState.getCurrx();
	    double y = miniWorldState.getCurry();
	    Path3D path = miniWorldState.getPath();
	    BinaryBayesDisplay bbfPanel = new BinaryBayesDisplay(x, y, bbf, path, true, false, mapWidth,mapHeight);
	    synchronized(entropyDisplayMap) {
		entropyDisplayMap.put(channel,bbfPanel);
	    }
	    entropyPanel.addPanel("Channel: "+channel, bbfPanel);
	}

	if(null != defaultChannel) {
	    bbf.setBeliefs(defaultChannel.getBeliefs());
	}

	bbfMap.put(channel,bbf);
	// @TODO: We really should break this out into some kind of
	// 'sharing' interface object - then this class just gets an
	// instance and calls that instance to decide if it shares.
	if(UAVRI.BBF_REMOTE_KLD_SHARING_ON) {
	    remoteBbf = new BinaryBayesFilter(channel,
					      binaryBayesFilterGridSize,
					      0,
					      0,
					      UAVRI.BBF_GRID_SCALE_FACTOR,
					      emitterModelScaleFactor,
					      UAVRI.BBF_SENSOR_MAX_RANGE_METERS,
					      UAVRI.BBF_SENSOR_MIN_RANGE_METERS,
					      UAVRI.BBF_UNIFORM_PRIOR,
					      UAVRI.BBF_DIFFUSE_INTERVAL_MS,
					      UAVRI.BBF_DIFFUSE_DELTA,
					      false,
					      false);
	    remoteBbf.initializeGTBeliefs(emitters);
	    remoteBbfMap.put(channel,remoteBbf);

	    if(null != defaultChannel) {
		remoteBbf.setBeliefs(defaultChannel.getBeliefs());
	    }

	}
    }
    
    private boolean singleReadingUpdate(BinaryBayesFilter bbf, BinaryBayesFilter remoteBbf, RSSIReading reading) {
	boolean added = false;
	    
	d.debug("simtime="+SimTime.getEstimatedTime()+" readingid="+reading.getID()+", reading="+reading.toString(), 0, this);
	if(seenReadings.contains(reading.getID())) {
	    d.debug("simtime="+SimTime.getEstimatedTime()+" readingid="+reading.getID()+" ttl "+reading.sharingTTL+" has been seen before (seenReadings.size = "+seenReadings.size()+" )", 0, this);
	    // If KLD sharing is on, send this reading out to someone else.
	    if(UAVRI.BBF_KLD_SHARING_ON) {
		if(reading.sharingTTL > 0) {
		    d.debug("simtime="+SimTime.getEstimatedTime()+" readingid="+reading.getID()+" Forwarding already seen reading "+reading.getID()+", not decrementing TTL, TTL= "+reading.sharingTTL, 0, this);
		    InformationAgent agent = new InformationAgent(reading, 1);
		    MACoordination.addAgent(agent);
		    agent.stateChanged();	    // Let it act
		}
		else {
		    d.debug("simtime="+SimTime.getEstimatedTime()+" readingid="+reading.getID()+" ignoring already seen reading, TTL <= 0",0,this);
		}
	    }
	    return false;
	}
	seenReadings.add(reading.getID());
		
	boolean shared = false;

	bbf.updateBinaryFilter(reading);
	if(UAVRI.BBF_REMOTE_KLD_SHARING_ON) {
	    remoteBbf.updateBinaryFilter(reading);
	}
		    
	added = true;
	if(reading.locallySensed()) {
	    localReadingCounter++;
	}
	else {
	    remoteReadingCounter++;
	}

	if(BinaryBayesFilter.BELIEF_SHARE_GTKLD_ON) {
	    // NOTE - we calculate and print KLDivergence between
	    // our bbf beliefs and ground truth, regardless of
	    // what sharing method we're using, to validate that
	    // our method 'works' - i.e. the kldivergence from
	    // ground truth goes down as time goes on, meaning our
	    // beliefs are closer to ground truth, both with
	    // random sharing and with kldivergence from old
	    // beliefs/new beliefs sharing.
	    double GTKLDivergence = bbf.getGTKLDSum();
	    int GTNumCells = bbf.getSize() * bbf.getSize();
	    double avgGTKLDivergence = GTKLDivergence/GTNumCells;
	    d.debug("simtime="+SimTime.getEstimatedTime()+" readingid="+reading.getID()+" GTKLDivergence= "+GTKLDivergence+", GTNumCells= "+GTNumCells+", avgGTKLDivergence= "+avgGTKLDivergence, 0, this);
	}

	if(UAVRI.BBF_REMOTE_KLD_SHARING_ON) {
	    int remoteNumCells = remoteBbf.getLastUpdateSize();
	    double remoteKLDivergence = remoteBbf.getBeliefKLDSum();
	    double avgRemoteKLDivergence = remoteKLDivergence/remoteNumCells;

	    int ttlIncrement = BeliefShare.calculateTTLIncrement(remoteKLDivergence, remoteNumCells);
		
	    d.debug("simtime="+SimTime.getEstimatedTime()+" readingid="+reading.getID()+" remoteKLDivergence= "+remoteKLDivergence+", remoteNumCells= "+remoteNumCells+", avgRemoteKLDivergence= "+avgRemoteKLDivergence+" ttlIncrement="+ttlIncrement, 0, this);

	    reading.sharingTTL--;
	    if(ttlIncrement > 0) 
		reading.sharingTTL++;
	    else
		reading.sharingTTL--;
	    if(reading.sharingTTL > 0) {
		shared = true;
		d.debug("simtime="+SimTime.getEstimatedTime()+" readingid="+reading.getID()+" KLD Remote Sharing TTLIncr= "+ttlIncrement+" sharingTTL="+reading.sharingTTL, 0, this);
		InformationAgent agent = new InformationAgent(reading, 1);
		MACoordination.addAgent(agent);
		agent.stateChanged();	    // Let it act
	    }
	    else {
		d.debug("simtime="+SimTime.getEstimatedTime()+" readingid="+reading.getID()+" NOT Sharing TTLincr= "+ttlIncrement+" sharingTTL="+reading.sharingTTL, 0, this);
	    }
	}

	if(UAVRI.BBF_KLD_SHARING_ON) {
	    int beliefNumCells2 = bbf.getLastUpdateSize();
	    double beliefKLDivergence2 = bbf.getBeliefKLDSum();
	    double avgBeliefKLDivergence2 = beliefKLDivergence2/beliefNumCells2;

	    int ttlIncrement = BeliefShare.calculateTTLIncrement(beliefKLDivergence2, beliefNumCells2);

	    d.debug("simtime="+SimTime.getEstimatedTime()+" readingid="+reading.getID()+" beliefKLDivergence2= "+beliefKLDivergence2+", beliefNumCells2= "+beliefNumCells2+", avgBeliefKLDivergence2= "+avgBeliefKLDivergence2+" ttlIncrement="+ttlIncrement, 0, this);

	    reading.sharingTTL--;
	    if(ttlIncrement > 0) 
		reading.sharingTTL++;
	    else
		reading.sharingTTL--;
	    if(reading.sharingTTL > 0) {
		shared = true;
		d.debug("simtime="+SimTime.getEstimatedTime()+" readingid="+reading.getID()+" KLD Sharing TTLIncr= "+ttlIncrement+" sharingTTL="+reading.sharingTTL, 0, this);
		InformationAgent agent = new InformationAgent(reading, 1);
		MACoordination.addAgent(agent);
		agent.stateChanged();	    // Let it act
	    }
	    else {
		d.debug("simtime="+SimTime.getEstimatedTime()+" readingid="+reading.getID()+" NOT Sharing TTLincr= "+ttlIncrement+" sharingTTL="+reading.sharingTTL, 0, this);
	    }
	}
	if(UAVRI.BBF_RANDOM_SHARING_ON) {
	    if(reading.locallySensed()) {
		if(rand.nextDouble() < UAVRI.BBF_RANDOM_SHARING_PROB) {
		    shared = true;
		    d.debug("simtime="+SimTime.getEstimatedTime()+" readingid="+reading.getID()+" Randomly Sharing TTL= "+UAVRI.BBF_RANDOM_SHARING_TTL, 0, this);
		    InformationAgent agent = new InformationAgent(reading, UAVRI.BBF_RANDOM_SHARING_TTL);
		    MACoordination.addAgent(agent);
		    agent.stateChanged();	    // Let it act
		}
	    }
	}

	if(reading.locallySensed()) {
	    localSharingTrack.add(shared);
	}
	else {
	    remoteSharingTrack.add(shared);
	}
	overallSharingTrack.add(shared);
		
	d.debug("simtime="+SimTime.getEstimatedTime()+" readingid="+reading.getID()
				  +" SHARINGSTATS "
				  +" overallCount= "+overallSharingTrack.getTotal()
				  +" %shared= "+overallSharingTrack.getTotalPercentTrue()
				  +" movingAvg("+overallSharingTrack.getSize()+")= "
				  +overallSharingTrack.getTrackPercentTrue()
				  +" localCount= "+localSharingTrack.getTotal()
				  +" %shared= "+localSharingTrack.getTotalPercentTrue()
				  +" movingAvg("+localSharingTrack.getSize()+")= "
				  +localSharingTrack.getTrackPercentTrue()
				  +" remoteCount= "+remoteSharingTrack.getTotal()
				  +" %shared= "+remoteSharingTrack.getTotalPercentTrue()
				  +" movingAvg("+remoteSharingTrack.getSize()+")= "
				  +remoteSharingTrack.getTrackPercentTrue(), 0, this);		
	return added;
    }

    public boolean update(LinkedList<RSSIReading> beliefList) {
	boolean added = false;

// 	d.debug(1,"received "+beliefList.size()+" readings");
// 	int counter=0;
// 	for (RSSIReading reading: beliefList) {
// 	    d.debug(1,"received reading "+counter+" on channel "+reading.channel+" reading="+reading);
// 	    counter++;
// 	}

	if(beliefList.size() > 10) 
	    d.debug("simtime="+SimTime.getEstimatedTime()+" we see "+beliefList.size()+" readings", 1, this);
	int numBeliefs = beliefList.size();
	long startTime = System.currentTimeMillis();

	for (RSSIReading reading: beliefList) {
	    BinaryBayesFilter bbf = bbfMap.get(reading.channel);
	    BinaryBayesFilter remoteBbf = remoteBbfMap.get(reading.channel);
	    if(null == bbf) {
		createBBF(reading.channel);
		bbf = bbfMap.get(reading.channel);
		remoteBbf = remoteBbfMap.get(reading.channel);
	    }
// 	    d.debug("Updating for reading on channel "+reading.channel+" reading="+reading, 1, this);	
	    if(singleReadingUpdate(bbf,remoteBbf,reading))
		added = true;
	}

	if(added)
	    rebuildMerged = true;
	if((null != beliefPanel) || (null != entropyPanel))
	    rebuildMerged();

	if(null != beliefPanel) 
	    beliefPanel.updateAllDisplays();
	if(null != entropyPanel) 
	    entropyPanel.updateAllDisplays();

	return added;
    }
    
    private class SharingTrack {
	double size;
	public double getSize() { return size; }
	double total = 0;
	public double getTotal() { return total; }
	double totalTrue = 0;
	public double getTotalTrue() { return totalTrue; }
	
	boolean track[];
	boolean full = false;
	int end = 0;

	public SharingTrack(int size) {
	    this.size = size;
	    track = new boolean[size];
	}

	public void add(boolean shared) {
	    track[end++] = shared;
	    if(end >= track.length) {
		end = 0;
		full = true;
	    }
	    total++;
	    if(shared) 
		totalTrue++;
	}

	public double getTotalPercentTrue() { 
	    return (totalTrue/total); 
	}

	// TODO: We easily could/should keep a running tally in add... 
	public double getTrackPercentTrue() {
	    double trackTrueTotal = 0;
	    double endLoop;
	    endLoop = full ? track.length : end; 
	    for(int loopi = 0; loopi < endLoop; loopi++) 
		if(track[loopi])
		    trackTrueTotal++;
	    return trackTrueTotal/endLoop;
	}
    }

}
