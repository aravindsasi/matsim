/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 * 
 */
package org.matsim.contrib.noise;

import com.google.inject.multibindings.Multibinder;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.analysis.XYTRecord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.testcases.MatsimTestUtils;

/**
 * @author ikaddoura
 *
 */

public class NoiseOnlineExampleIT {
	private static final Logger log = Logger.getLogger( NoiseOnlineExampleIT.class ) ;
	
	@Rule
	public MatsimTestUtils testUtils = new MatsimTestUtils();

	@Test
	public final void test0(){

		String configFile = testUtils.getPackageInputDirectory() + "config.xml";
		Config config = ConfigUtils.loadConfig(configFile, new NoiseConfigGroup());
		config.controler().setLastIteration(1);
		config.controler().setOutputDirectory( testUtils.getOutputDirectory() );

		NoiseConfigGroup noiseParameters = (NoiseConfigGroup) config.getModule("noise");
		noiseParameters.setWriteOutputIteration(1);

		Scenario scenario = ScenarioUtils.loadScenario(config);
		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new NoiseModule());
		controler.run();

		String workingDirectory = controler.getConfig().controler().getOutputDirectory() + "/ITERS/it." + controler.getConfig().controler().getLastIteration() + "/immissions/";
		String receiverPointsFile = controler.getConfig().controler().getOutputDirectory() + "/receiverPoints/receiverPoints.csv";
		ProcessNoiseImmissions processNoiseImmissions = new ProcessNoiseImmissions(workingDirectory, receiverPointsFile, noiseParameters.getReceiverPointGap());
		processNoiseImmissions.run();
	}

	@Test
	public final void testNoiseListener(){

		String configFile = testUtils.getPackageInputDirectory() + "config.xml";
		Config config = ConfigUtils.loadConfig(configFile, new NoiseConfigGroup());
		config.controler().setLastIteration(1);
		config.controler().setOutputDirectory( testUtils.getOutputDirectory() );

		NoiseConfigGroup noiseParameters = (NoiseConfigGroup) config.getModule("noise");
		noiseParameters.setWriteOutputIteration(1);

		Scenario scenario = ScenarioUtils.loadScenario(config);
		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new NoiseModule());
		controler.addOverridingModule( new AbstractModule(){
			@Override public void install(){
				Multibinder<NoiseModule.NoiseListener> binder = Multibinder.newSetBinder( this.binder(), NoiseModule.NoiseListener.class ) ;
				binder.addBinding().toInstance( new NoiseModule.NoiseListener(){
					@Override public void newRecord( XYTRecord record ){
						log.warn( record ) ;
					}
				} );
			}
		} ) ;

		controler.run();

		String workingDirectory = controler.getConfig().controler().getOutputDirectory() + "/ITERS/it." + controler.getConfig().controler().getLastIteration() + "/immissions/";
		String receiverPointsFile = controler.getConfig().controler().getOutputDirectory() + "/receiverPoints/receiverPoints.csv";
		ProcessNoiseImmissions processNoiseImmissions = new ProcessNoiseImmissions(workingDirectory, receiverPointsFile, noiseParameters.getReceiverPointGap());
		processNoiseImmissions.run();
	}

}
