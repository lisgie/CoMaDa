package de.tum.in.net.WSNDataFramework.Modules.Web;

import java.util.ArrayList;
import java.util.List;

public class PastNodeState {
	private String _nodeID;
	private List<PastDatumState> _data = new ArrayList<PastDatumState>();
	

	public PastNodeState(String ID) {
		_nodeID = ID;
	}
	
	public String getNodeID() {
		return _nodeID;
	}
	
	public void addDatum(String datumID, String name, String type, String value, String unit) {
		PastDatumState datum = new PastDatumState(datumID, name, type, value, unit);
		_data.add(datum);
	}
	
	public List<String> getDatumIDs() {
		ArrayList<String> IDs = new ArrayList<String>();
		for(PastDatumState datum : _data) {
			IDs.add(datum.getID());
		}
		return IDs;
	}
	
	public PastDatumState getDatum(String datumID) {
		//ArrayList<String> datum = new ArrayList<String>();
		for(PastDatumState datum : _data) {
			if(datum.getID().equals(datumID)) {
				return datum;
			}
		}
		return null;
	}
	
	public List<PastDatumState> getAllData() {
		return _data;
	}
}
