/**
 * This model demonstrates a use-case of the FIPA Contract Net interaction protocol. 
 * 		
 * One initiator sends a 'cfp' message to other participants.
 * All participants reply with a 'refuse' messages which end the interaction protocol.
 */

model cfp_cfp_1

global {
	int nbOfParticipants <- 5;
	
	init {
		create initiator;
		create participant number: nbOfParticipants;

		write 'Please step the simulation to observe the outcome in the console';
	}
}

species initiator skills: [fipa] { 
	
	reflex send_cfp_to_participants when: (time = 1) {
		//list<participant> participants <- list(participant);
		
		write '(Time ' + time + '): ' + name + ' sends a cfp message to all participants';
		do start_conversation with: [ receivers :: list(participant), protocol :: 'fipa-contract-net', performative :: 'cfp', content :: ['Go swimming'] ];
	}
	
	reflex receive_refuse_messages when: !empty(refuses) {
		write '(Time ' + time + '): ' + name + ' receives refuse messages';
		
		loop r over: refuses {
			write '\t' + name + ' receives a refuse message from ' + r.sender.name + ' with content ' + r.content ;
		}
	}
}

species participant skills: [fipa] {
	
	reflex receive_cfp_from_initiator when: !empty(cfps) {
		
		message proposalFromInitiator <- cfps[0];
		write '(Time ' + time + '): ' + name + ' receives a cfp message from ' + proposalFromInitiator.sender.name + ' and replies with a refuse message';
		do refuse (message :: proposalFromInitiator, content :: ['I am busy today'] );
		
	}
}

experiment test type: gui { 
	output {
		
	}
}