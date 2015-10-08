/**
 *  sis
 *  Author: 
 *  Description: A compartmental SI model 
 */

model si

global { 
    int number_S <- 495;  // The number of susceptible
    int number_I <- 5 ;	// The number of infected
    float survivalProbability <- 1/(70*365) ; // The survival probability
	float beta <- 0.05 ; 	// The parameter Beta
	float nu <- 0.001 ;	// The parameter Nu
	int numberHosts <- number_S+number_I;
	bool local_infection <- true ;
	int neighbours_size <- 2 ;
	int nb_infected <- number_I;
	geometry shape <- square(50);
	init { 
		create Host number: number_S {
        	is_susceptible <- true;
        	is_infected <-  false;
            is_immune <-  false; 
            color <-  #green;
        }
        create Host number: number_I {
            is_susceptible <-  false; 
            is_infected <-  true;
            is_immune <-  false; 
            color <-  #red;  
       }
   }
   reflex compute_nb_infected {
   		nb_infected <- Host count (each.is_infected);
   }  
}


grid si_grid width: 50 height: 50 use_individual_shapes: false use_regular_agents: false frequency: 0{
	rgb color <- #black;
	list<si_grid> neighbours <- (self neighbors_at neighbours_size) ;       
}
species Host  {
	bool is_susceptible <- true;
	bool is_infected <- false;
    bool is_immune <- false;
    rgb color <- #green;
    int sic_count <- 0;
    si_grid myPlace;
    
    init {
    	myPlace <- one_of (si_grid as list);
    	location <- myPlace.location;
    }        
    reflex basic_move {
    	myPlace <- one_of (myPlace.neighbours) ;
        location <- myPlace.location;
    }
    
    reflex become_infected when: is_susceptible {
    	float rate <- 0.0;
    	if(local_infection) {
    		int nb_hosts <- 0;
    		int nb_hosts_infected <- 0;
    		loop hst over: ((myPlace.neighbours + myPlace) accumulate (Host overlapping each)) {
    			nb_hosts <- nb_hosts + 1;
    			if (hst.is_infected) {
    				nb_hosts_infected <- nb_hosts_infected + 1;
    			}
    		}
    		rate <- nb_hosts_infected / nb_hosts;
    	} else {
    		rate <- nb_infected / numberHosts;
    	}
    	if (flip(beta * rate)) {
        	is_susceptible <-  false;
            is_infected <-  true;
            is_immune <-  false;
            color <-  #red;    
        }
    }
    
    reflex shallDie when: flip(nu) {
		create species(self) {
			myPlace <- myself.myPlace ;
			location <- myself.location ; 
		}
       	do die;
    }
            
    aspect basic {
        draw circle(1) color: color; 
    }
}


experiment Simulation type: gui { 
 	parameter "Number of Susceptible" var: number_S ;// The number of susceptible
    parameter "Number of Infected" var: number_I ;	// The number of infected
    parameter "Survival Probability" var: survivalProbability ; // The survival probability
	parameter "Beta (S->I)" var:beta; 	// The parameter Beta
	parameter "Mortality" var:nu ;	// The parameter Nu
	parameter "Is the infection is computed locally?" var:local_infection ;
	parameter "Size of the neighbours" var:neighbours_size ;
	
 	output { 
	    display si_display {
	        grid si_grid lines: #black;
	        species Host aspect: basic;
	    }
	        
	    display chart refresh: every(10) {
			chart "Susceptible" type: series background: #lightgray style: exploded {
				data "susceptible" value: Host count (each.is_susceptible) color: #green;
				data "infected" value: Host count (each.is_infected) color: #red;
			}
		}
			
	}
}
