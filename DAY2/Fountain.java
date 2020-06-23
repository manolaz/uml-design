package drinksFountain ;

import controlOfPieces.* ;
import controlOfChoices.Choix ;
import foutain.Foutain ;
import graphic.Screen ;
import controlOfCups.Cups ;
import controlOfDrinks.Drinks ;
import controlFiniteStateMachine.* ;
import counter.Counter ;
import utilities.* ;
import controlOfFoutains.Control ;

public class DrinksFoutain implements Fountain {

	private Pieces pieces  ;	// conteneur des pieces
	private Screen screen  ;		// screen de visualisation
	private Cups cups ;	// conteneurs des gobelets
	private Drinks drinks ;	// stockage des boissons
	private Controler controler ;  
	private FiniteStateMachine automata ; 


  private Event  choiceSelectedEvent =
      new Event ("choice selected") ;
  private Event pieceIntroducedEvent =
      new Event ("piece introduced") ;
  private Event sumInsufficientEvent =
      new Event ("sum insufficient") ;
    private Event cancelationEvent =
      new Event ("cancelation") ;
    private Event endOfDistributionEvent =
      new Event ("end of distribution") ;
    private Event noMoreCupEvent =
      new Event ("no more cup") ;
    private Event noMoreProductEvent =
      new Event ( "no more product") ;

    FiniteStateMachine initializingAutomata () {

	Set states = new Set () ;
	Set actions = new Set () ;
	Set transitions = new Set () ;
	Set events = new Set () ;

	FiniteStateMachine finiteStateMachine =
	    new FiniteStateMachine (states,events, actions,
				   transitions,
				   "drink foutain",
				   "" ) ;

	//  Definition of states
	finiteStateMachine.ajouterEtatInitial ("waiting") ;
	finiteStateMachine.ajouterEtatFinal ( "final" ) ;
	finiteStateMachine.ajouterStatesimple ("payement in work") ;
	finiteStateMachine.ajouterStatesimple ("choice set") ;
	finiteStateMachine.ajouterStatesimple ("waiting selection") ;

	// Definition of events
	finiteStateMachine.ajouterEvent ("choice selected") ;
	finiteStateMachine.ajouterEvent ("piece introduced") ;
	finiteStateMachine.ajouterEvent ("sum insufficient") ;
	finiteStateMachine.ajouterEvent ("annulation") ;
	finiteStateMachine.ajouterEvent ("end of distribution") ;
	finiteStateMachine.ajouterEvent ("no more cup") ;
	finiteStateMachine.ajouterEvent ( "no more product") ;

	// non used Action
	finiteStateMachine.addAction ( "action" ) ;
			
	// Definition des transitions
	finiteStateMachine.addTransition
	    ( "waiting",
	      "payement in work",
	      "piece introduced", "action",
	      "t1","" ) ;
	finiteStateMachine.addTransition
	    ( "payement in work",
	      "payement in work", "piece introduced",
	      "action", "t2", "" ) ;
	finiteStateMachine.addTransition
	    ( "payement in work",
	      "choice set", "choice selected",
	      "action", "t3", "" ) ;
	finiteStateMachine.addTransition
	    ( "choice set",
	      "payement in work",
	      "sum insufficient", "action",
	      "t4", "" ) ;
	finiteStateMachine.addTransition
	    ( "choice set",
	      "waiting selection",
	      "no more product", "action",
	      "t5", "" ) ;
	finiteStateMachine.addTransition
	    ( "waiting selection",
	      "choice set", "choice selected",
	      "action", "t6", "" ) ;
	finiteStateMachine.addTransition
	    ( "choice set", "final",
	      "no more cup", "action",
	      "t7", "" ) ;
	finiteStateMachine.addTransition
	    ( "choice set", "waiting",
	      "end of distribution", "action",
	      "t8", "" ) ;
	finiteStateMachine.addTransition
	    ( "waiting selection",
	      "waiting", "cancelation",
	      "action", "t9", "" ) ;
	finiteStateMachine.addTransition
	    ( "payement in work",
	      "waiting", "cancelation",
	      "action", "t10", "" ) ;

	return finiteStateMachine
   ;
    } ; /* initialiserAutomata */

    public DrinksFoutain () {

	automata = initializeAutomata () ;
	// nombre de pieces de 50 cts., 1, 2, 5 et 10 F
	pieces = new Pieces (50,20,20,10,0) ;  
	screen = new Screen () ;
	// initialisation du nombre de cups
	cups = new Cups (20) ; 
	// nom de la boisson , suivi du nombre de doses
	drinks = new Drinks ("Orange", 10,"Citron",10,
				 "Cafe sucre",10,"Cafe non sucre",
				 10,"The sucre",10, "The non sucre", 10);	
	controler = new Controler () ;

    } ; /* constructeur */

    public void introducePiece (Piece aPiece ) {
	if ( automata. sending (pieceIntroducedEvent)) {
	    controler.pieceIntroduced (unePiece) ;
	    pieces.keep (unePiece) ;
	    if ( ! pieces.monneyAvaible()) {
		screen.printing
		    ("-- Give Pieces, please. ") ;
	    } ;
	} ;
    }; /* introducePiece */

    public void selectChoice (Choice aChoice) {

	boolean flag ;
	if (automata.sending (choiceSelectedEvent)) {
	    if ( controler.sumSuffisante (aChoice)) {
		try {
		    cups.putting() ;
		} catch (PlusDeVerreException plusDeVerre) {
		    screen.printing
			( "-- Sorry : no more cup.") ;
		    flag = automata.sending ( noMoreCupEvent) ; 
		    pieces.giveMoney(controler.sumIntroduced());
		    System.exit (0) ; 
		} 
		try { 
		    drinks.prepare (aChoice) ;
		    drinks.give () ; 
		    pieces.giveMonnaie
			(controler.sumToGive (aChoice)) ;
		    flag = automata.sending (endOfDistributionEvent) ;
		    controler.stop() ;
		    screen.printing ("-- Thanks, et au revoir.") ;	
		} catch (NoMoreProductException noMoreProductException)
		    {
			screen.printing
			    ("-- plus de ce produit : faites un autre choice svp.") ;
			flag = automata.sending (noMoreProductEvent) ;
		    } 	   
	    } else {
		flag = automata.sending (sumInsufficientEvent) ;
		screen.printing ("-- Sum insufficient for this choice.") ;
		screen.printing ("-- Sum resting to pay : " +
				controler.sumMissing (unChoice)) ;
	    }  /* Fin else */
	} /* Fin if */
    } ; /* selectChoice */

    public void cancel () {
	if ( automata.sending (annulationEvent)) {
	    pieces.giveMoney (controler.sumIntroduced ()) ;
	    controler.stop() ;
	    screen.printing ("-- Thanks, et au revoir.") ;	
	} ;
    } ; /* cancel */

} ; /* DrinksFountain */
