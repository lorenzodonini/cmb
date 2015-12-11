package report;

import core.Coord;
import core.DTNHost;
import core.SimClock;
import core.SimScenario;
import core.UpdateListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Example report that samples the whole simulation on regular intervals.
 *
 * @author teemuk
 */
public class SamplingReport
extends Report
implements UpdateListener {


  //==========================================================================//
  // Instance vars
  //==========================================================================//
  private double lastRecord = Double.MIN_VALUE;
  private int interval = 100;

  private Coord center;
  final List <Double> distancesFromOrigin = new ArrayList<>( 10000 );
  //==========================================================================//


  //==========================================================================//
  // Report
  //==========================================================================//
  public SamplingReport() {
    super();
    this.init();
  }

  @Override
  protected void init() {
    super.init();

    int worldX = SimScenario.getInstance().getWorldSizeX();
    int worldY = SimScenario.getInstance().getWorldSizeY();

    this.center = new Coord( worldX / 2, worldY / 2 );
  }

  @Override
  public void done() {
    super.write( "Mean: " + super.getAverage( this.distancesFromOrigin ) );
    super.write( "Variance: " + super.getVariance( this.distancesFromOrigin ) );

    super.done(); // Closes the report file
  }
  //==========================================================================//


  //==========================================================================//
  // UpdateListener
  //==========================================================================//
  @Override
  public void updated( final List <DTNHost> hosts ) {
    if ( SimClock.getTime() - lastRecord < interval ) return;
    lastRecord = SimClock.getTime();

    double totalDistances = 0;
    for ( final DTNHost host : hosts ) {
      totalDistances += this.center.distance( host.getLocation() );
    }
    this.distancesFromOrigin.add( totalDistances / hosts.size() );
  }
  //==========================================================================//
}
