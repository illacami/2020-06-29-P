package it.polito.tdp.PremierLeague.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	private PremierLeagueDAO dao;
	
	private Graph<Match, DefaultWeightedEdge> grafo;
	
	private List<Match> vertici;
	private List<Adiacenza> archi;
	
	private Map<Integer, Match> idMatch;
	
	private List<Adiacenza> best;
	
	public Model() {
		dao = new PremierLeagueDAO();
		idMatch = new HashMap<Integer,Match>();
		dao.listAllMatches(idMatch);
	}
	
	public List<Match> getVertici(int mese){
		vertici = new ArrayList<Match>();
		vertici = dao.getVertici(mese);
		return vertici;
	}
	
	public List<Adiacenza> getArchi(int minGiocati, int mese){
		archi = new ArrayList<Adiacenza>();
		archi = dao.getAdiacenze(minGiocati, mese);
		return archi;
	}
	
	public void creaGrafo(int minGiocati, int mese) {
		grafo = new SimpleWeightedGraph<Match, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		//aggiungo vertici
		Graphs.addAllVertices(grafo, this.getVertici(mese));
		
		//aggiungo archi
		this.getArchi(minGiocati, mese);
		for(Adiacenza a : archi) {
			Match m1 = a.getM1();
			Match m2 = a.getM2();
			if(grafo.containsVertex(m2) && grafo.containsVertex(m1))
				Graphs.addEdgeWithVertices(grafo, m1, m2, a.getPeso());
		}
		
		System.out.println("grafo creato\n# Vertici: "+grafo.vertexSet().size()+"\n# Archi: "+grafo.edgeSet().size());
	}
	
	public int getVerticiSize() {
		return grafo.vertexSet().size();
	}
	
	public int getEdgeSize() {
		return grafo.edgeSet().size();
	}
	
	public List<Adiacenza> connessioneMax(){
		
		double pesoMax = 0.0;
		for(DefaultWeightedEdge edge : grafo.edgeSet()){
			if(grafo.getEdgeWeight(edge)>pesoMax) {
				pesoMax = grafo.getEdgeWeight(edge);
			}
		}
		
		List<Adiacenza> archiTrovati = new ArrayList<Adiacenza>();
		
		for(DefaultWeightedEdge edge : grafo.edgeSet()) {
			if(grafo.getEdgeWeight(edge)>=pesoMax) {
				Match m1 = grafo.getEdgeSource(edge);
				Match m2 = grafo.getEdgeTarget(edge);
				int peso = (int)grafo.getEdgeWeight(edge);
				
				archiTrovati.add(new Adiacenza (idMatch.get(m1.getMatchID()), idMatch.get(m2.getMatchID()), peso));
			}
				
		}
		
		return archiTrovati;
	}
	
	public List<Adiacenza> camminoPesoMassimo(Match m1, Match m2){
		
		List<Adiacenza> parziale = new ArrayList<Adiacenza>();
		
		best = new ArrayList<Adiacenza>();
		
		cerca(m1, m2, parziale);
		
		return best;
	}
	
	public void cerca(Match mInizio, Match mFine, List<Adiacenza> parziale) {
		
		if(parziale.isEmpty()) {
			for(DefaultWeightedEdge edge : grafo.outgoingEdgesOf(mInizio)) {
				Match m1 = grafo.getEdgeSource(edge);
				Match m2 = grafo.getEdgeTarget(edge);
				int peso = (int)grafo.getEdgeWeight(edge);
				
				Adiacenza adiacenza = new Adiacenza(m1, m2, peso);
				
				parziale.add(adiacenza);
				cerca(mInizio, mFine, parziale);
				parziale.remove(parziale.size()-1);
			}
				
		}else {
		//caso terminale
		if(parziale.get(parziale.size()-1).getM2().equals(mFine)) {
			if(peso(parziale) > peso(best)) {
				best = new ArrayList<Adiacenza>(parziale);
				return;
			}
		}
		
		for(DefaultWeightedEdge edge : grafo.outgoingEdgesOf(parziale.get(parziale.size()-1).getM2())) {
			Match m1 = grafo.getEdgeSource(edge);
			Match m2 = grafo.getEdgeTarget(edge);
			int peso = (int)grafo.getEdgeWeight(edge);
			
			Adiacenza adiacenza = new Adiacenza(m1, m2, peso);
			
			if(this.aggiuntaAmmissibile(m1, m2) && !parziale.contains(adiacenza)) {
				parziale.add(adiacenza);
				cerca(mInizio,mFine,parziale);
				parziale.remove(parziale.size()-1);
			}
				
		}
		}
	}
	
	
	public double peso(List<Adiacenza> cammino) {
		
		if(cammino.isEmpty())
			return 0.0;
		
		double peso = 0.0;
		
		for(Adiacenza a : cammino) 
			peso += a.getPeso();
		
		return peso;
	}
	
	public boolean aggiuntaAmmissibile(Match m1, Match m2) {
		
		if(m1.getTeamHomeID().equals(m2.getTeamHomeID()) && m1.getTeamAwayID().equals(m2.getTeamAwayID())
				|| m1.getTeamHomeID().equals(m2.getTeamAwayID()) && m1.getTeamAwayID().equals(m2.getTeamHomeID()))
			return false;
		
		return true;
	}
	
	
	
	
	
	
}
