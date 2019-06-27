package it.unimib.disco.domain;

import java.util.Observable;
import java.util.logging.Logger;

public class Automobilista {

	private final static Logger _logger;	
	
	protected boolean loggingEnabled;
	
	protected Automobile automobile;
	protected Parcheggio parcheggio;
	protected Ticket ticket;
	protected Object ticketMonitor;
	
	static {
		_logger = Logger.getLogger(Automobilista.class.getName());
	}
	
	public Automobilista(Automobile automobile, Parcheggio parcheggio, boolean loggingEnabled) {
		
		this.loggingEnabled = loggingEnabled;
		this.automobile = automobile;
		this.parcheggio = parcheggio;
		this.ticket = null;
		this.ticketMonitor = new Object();
	}
	
	public Automobilista(Automobile automobile, Parcheggio parcheggio) {
		this(automobile, parcheggio, false);
	}
	
	private void onRitiro(Observable request, Object ticket) {
		
		this.ticket = (Ticket) ticket;
		this.automobile = null;
		
		synchronized (ticketMonitor) {
			ticketMonitor.notify();
		}
		
		if (loggingEnabled)
			_logger.info(String.format("[A] Ticket %s emesso", this.ticket.getUuid()));
	}
	
	private void onRestituzione(Observable request, Object automobile) {
		
		this.automobile = (Automobile) automobile;
		
		if (loggingEnabled)
			_logger.info(String.format("[A] Automobile prelevata con ticket %s", this.ticket.getUuid()));
	}
	
	public void consegna() {
		
		RitiroRequest rreq = new RitiroRequest(automobile);
		rreq.addObserver((o, t) -> onRitiro(o, t));
		parcheggio.ritira(rreq);
	}
	
	public void preleva() {
		
		RestituzioneRequest rreq = new RestituzioneRequest(ticket);
		rreq.addObserver((o, t) -> onRestituzione(o, t));
		parcheggio.restituisci(rreq);
	}

	public boolean isLoggingEnabled() {
		return loggingEnabled;
	}

	public void setLoggingEnabled(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}

	public Automobile getAutomobile() {
		return automobile;
	}

	public void setAutomobile(Automobile automobile) {
		this.automobile = automobile;
	}

	public Parcheggio getParcheggio() {
		return parcheggio;
	}

	public void setParcheggio(Parcheggio parcheggio) {
		this.parcheggio = parcheggio;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}
	
	public void waitOnTicket() throws InterruptedException {
		
		synchronized (ticketMonitor) {
			ticketMonitor.wait();
		}
	}
	
}
