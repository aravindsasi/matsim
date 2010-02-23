package playground.mmoyo.analysis;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.utils.geometry.CoordUtils;
import playground.mmoyo.PTRouter.PTValues;
import playground.mmoyo.analysis.comp.PlanRouter;

/**finds connections with transfer point that are farther than the destination point **/ 
public class FindRoundaboutConnections {
	
	private Population createDetouredPlan (ScenarioImpl scenario){
		Population detouredPopulation = new PopulationImpl(scenario);

		PTValues.routerCalculator = 3;
		PTValues.distanceCoefficient =0.00;
		PTValues.timeCoefficient = 1.00;
		PTValues.transferPenalty = 60.0;
		new PlanRouter(scenario);

		for (Person person : scenario.getPopulation().getPersons().values() ){
			//TODO: fragment plan
			Plan plan =  person.getSelectedPlan();   //assuming we are using fragmented plans!!!!, otherwise it analyzes only the first leg
			
			List<Leg> legList = new ArrayList<Leg>();
			List<Activity> transitActList = new ArrayList<Activity>();

			List<PlanElement> elemList = plan.getPlanElements();
			Activity aAct = ((Activity)elemList.get(0));
			Activity bAct = ((Activity)elemList.get(elemList.size()-1));

			for (PlanElement pe : plan.getPlanElements()){
				if (pe instanceof Activity) {
					Activity act = (Activity)pe;
					if (!(act == aAct || act==bAct)){ 
						transitActList.add(act);
					}
				}else{
					
					Leg leg = (Leg)pe;
					leg.getTravelTime();
					System.out.println ("distance: " + leg.getRoute().getDistance());
					System.out.println ("time: " + leg.getTravelTime());
					
					legList.add((Leg)pe);
				}
			}
			double abDistance = CoordUtils.calcDistance(aAct.getCoord(), bAct.getCoord());

			//compare abDistance vs transfer points distances
			for (Activity trAct : transitActList ){
				double a_TransDistance = CoordUtils.calcDistance(aAct.getCoord() , trAct.getCoord());
				double b_TransDistance = CoordUtils.calcDistance(bAct.getCoord() , trAct.getCoord());
				if(a_TransDistance > abDistance || b_TransDistance > abDistance){
					if (!detouredPopulation.getPersons().keySet().contains(person.getId()))
						detouredPopulation.addPerson(person);
				}
			}
		}
		
		return detouredPopulation;
	}
	
	public static void main(String[] args) {
		String configFile = "../shared-svn/studies/countries/de/berlin-bvg09/ptManuel/comparison/BerlinBrandenburg/routed_1x_subset_xy2links_ptplansonly/fragmented/config/config_routedPlans_MoyoParameterized.xml";
		//if (args[0]!= null) configFile = args[0];
		
		ScenarioImpl scenario = new TransScenarioLoader().loadScenario(configFile);
		Population detouredPopulation = new FindRoundaboutConnections().createDetouredPlan(scenario);
		
		System.out.println("writing detoured population plan file in output folder..." );
		PopulationWriter popwriter = new PopulationWriter(detouredPopulation, scenario.getNetwork());
		popwriter.write("../playgrounds/mmoyo/output/detouredPopulation60.xml") ;
		System.out.println("done");
	}
	
}
