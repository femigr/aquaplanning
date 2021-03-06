package edu.kit.aquaplanning.aquaplanning;

import java.io.File;
import java.io.IOException;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.Configuration.HeuristicType;
import edu.kit.aquaplanning.grounding.Grounder;
import edu.kit.aquaplanning.grounding.RelaxedPlanningGraphGrounder;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.model.lifted.PlanningProblem;
import edu.kit.aquaplanning.parsing.ProblemParser;
import edu.kit.aquaplanning.planners.ForwardSearchPlanner;
import edu.kit.aquaplanning.planners.Planner;
import edu.kit.aquaplanning.planners.SimpleParallelPlanner;
import edu.kit.aquaplanning.planners.SimpleSatPlanner;
import edu.kit.aquaplanning.planners.SearchStrategy.Mode;
import edu.kit.aquaplanning.validate.Validator;
import junit.framework.TestCase;

public class TestParallelPlanning extends TestCase {

	public void testSatPlanner() throws IOException {
		Configuration config = new Configuration();	
		config.searchTimeSeconds = 3;
		SimpleSatPlanner spp = new SimpleSatPlanner(config);
		spp.setIgnoreAtMostOneAction(true);
		testOnAll(spp);
	}
	
	public void testSatHeuristic() throws IOException {
		Configuration config = new Configuration();
		config.numThreads = 1;
		config.searchTimeSeconds = 3;
		config.searchStrategy = Mode.bestFirst;
		config.heuristic = HeuristicType.actionInterferenceRelaxation;
		ForwardSearchPlanner fsp = new ForwardSearchPlanner(config);
		testOnAll(fsp);
	}
	
	public void testParallelPlanner() throws IOException {
		Configuration config = new Configuration();
		config.numThreads = 8;
		config.searchTimeSeconds = 3;
		SimpleParallelPlanner spp = new SimpleParallelPlanner(config);
		testOnAll(spp);
	}

	private void testOnAll(Planner planner) throws IOException {
		File benchdir = new File("benchmarks");
		for (File domdir : benchdir.listFiles()) {
			String domain = domdir.getCanonicalPath() + "/domain.pddl";
			for (File f : domdir.listFiles()) {
				if (f.getName().startsWith("p") && f.getName().endsWith(".pddl")) {
					String problem = f.getCanonicalPath();
					//testBenchmark(domain, problem);
					testPlannerOnBenchmark(planner, domain, problem);
				}
			}
		}
	}
	
	private void testPlannerOnBenchmark(Planner planner, String domain, String problem) throws IOException {
		System.out.println("Testing planner on " + domain + ", " + problem);
		PlanningProblem pp = new ProblemParser().parse(domain, problem);
		Grounder grounder = new RelaxedPlanningGraphGrounder(new Configuration());
		GroundPlanningProblem gpp = grounder.ground(pp);
		Plan p = planner.findPlan(gpp);
		if (p != null) {
			System.out.println(p);
			System.out.println("Plan is valid:" + Validator.planIsValid(gpp, p));
		} else {
			System.out.println("TIMEOUT");
		}
	}

}
