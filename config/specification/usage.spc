OBSERVER AUTOMATON UsageChecker

INITIAL STATE Init;

STATE USEFIRST Init :
	MATCH EXIT && CHECK( UsageAnalysisCPA, "") -> ERROR("Error");
	
END AUTOMATON

