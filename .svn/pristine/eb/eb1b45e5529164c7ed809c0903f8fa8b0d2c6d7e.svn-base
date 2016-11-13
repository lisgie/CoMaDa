package de.tum.in.net.WSNDataFramework.Test.driver;

import java.util.Calendar;

import de.tum.in.net.WSNDataFramework.AggregationNode;
import de.tum.in.net.WSNDataFramework.Node;
import de.tum.in.net.WSNDataFramework.Topology;
import de.tum.in.net.WSNDataFramework.WSNDriver;
import de.tum.in.net.WSNDataFramework.CUSTOM.TUMI8AggregationNode;



public class TestWSNDriverModule extends WSNDriver {


	@Override
	public String getName() {
		return "Virtual-WSN-Driver";
	}

	@Override
	protected void _init() {
		_setRunning("up and running");
	}

	@Override
	protected void _run() {
		long startTime = Calendar.getInstance().getTimeInMillis()/1000;

		while (!Thread.interrupted()) {
			System.out.println("WSN-Test-Driver UPDATE");
			long curTime = Calendar.getInstance().getTimeInMillis()/1000;

//			Node node1 = this.app().wsn().node("V1101")!=null ? this.app().wsn().node("V1101") : new Node("V1101");
//			node1.data().update(new Node.Datum("t", "Sound (MTS300)", 181, ""));
//			node1.data().update(new Node.Datum("s", "Light (MTS300)", 135, ""));
//			node1.data().update(new Node.Datum("time", "NodeTime", curTime-startTime, "s"));
//
//			Node node2 = this.app().wsn().node("V1102")!=null ? this.app().wsn().node("V1102") : new Node("V1102");
//			node2.data().update(new Node.Datum("t", "Sound (MTS300)", 172, ""));
//			node2.data().update(new Node.Datum("s", "Temperature", Math.round((Math.random()*10+20)*100)/100, "°C"));
//			node2.data().update(new Node.Datum("time", "NodeTime", curTime-startTime, "s"));
//
//			Node node3 = this.app().wsn().node("V1103")!=null ? this.app().wsn().node("V1103") : new Node("V1103");
//			node3.data().update(new Node.Datum("t", "Humidity (Sensiron SHT11)", 37, "%"));
//			node3.data().update(new Node.Datum("s", "Light (LightTAOSTSL2550)", 154, ""));
//			node3.data().update(new Node.Datum("time", "NodeTime", curTime-startTime, "s"));
//
//			Node node4 = this.app().wsn().node("V1104")!=null ? this.app().wsn().node("V1104") : new Node("V1104");
//			node4.data().update(new Node.Datum("t", "Humidity (Sensiron SHT11)", 37, "%"));
//			node4.data().update(new Node.Datum("s", "Light (LightTAOSTSL2550)", 154, ""));
//			node4.data().update(new Node.Datum("time", "NodeTime", curTime-startTime, "s"));
//
//			Node node5 = this.app().wsn().node("V1105")!=null ? this.app().wsn().node("V1105") : new Node("V1105");
//			node5.data().update(new Node.Datum("t", "Sound (MTS300)", 172, ""));
//			node5.data().update(new Node.Datum("s", "Temperature", Math.round((Math.random()*30)*100)/100, "°C"));
//			node5.data().update(new Node.Datum("time", "NodeTime", curTime-startTime, "s"));
//
//			Node node6 = this.app().wsn().node("V1106")!=null ? this.app().wsn().node("V1106") : new Node("V1106");
//			node6.data().update(new Node.Datum("t", "Humidity (Sensiron SHT11)", 37, "%"));
//			node6.data().update(new Node.Datum("s", "Light (LightTAOSTSL2550)", 154, ""));
//			node6.data().update(new Node.Datum("time", "NodeTime", curTime-startTime, "s"));


//			AggregationNode node7 = this.app().wsn().node("V2203")!=null ? (AggregationNode)this.app().wsn().node("V2203") : new TUMI8AggregationNode("V2203");
//			node7.updateAggregatedNode(node1).updateAggregatedNode(node2).updateAggregatedNode(node6);
//			AggregationNode node8 = this.app().wsn().node("V2204")!=null ? (AggregationNode)this.app().wsn().node("V2204") : new TUMI8AggregationNode("V2204");
//			node8.updateAggregatedNode(node1).updateAggregatedNode(node4).updateAggregatedNode(node6);

			Topology topology = new Topology();
			/*topology.addLink(new WSNTopology.Link(new NodeID("V1101"), new NodeID("V2203"), 1.0));
			topology.addLink(new WSNTopology.Link(new NodeID("V1101"), new NodeID("V2204"), 1.0));
			topology.addLink(new WSNTopology.Link(new NodeID("V1102"), new NodeID("V2203"), 1.0));
			topology.addLink(new WSNTopology.Link(new NodeID("V1102"), new NodeID("V1106"), 1.0));
			topology.addLink(new WSNTopology.Link(new NodeID("V1103"), new NodeID("V2203"), 1.0));
			topology.addLink(new WSNTopology.Link(new NodeID("V1104"), new NodeID("V2204"), 1.0));
			topology.addLink(new WSNTopology.Link(new NodeID("V1104"), null, 1.0));
			topology.addLink(new WSNTopology.Link(new NodeID("V1105"), new NodeID("V2204"), 1.0));
			topology.addLink(new WSNTopology.Link(new NodeID("V1106"), new NodeID("V2203"), 1.0));
			topology.addLink(new WSNTopology.Link(new NodeID("V1106"), null, 1.0));
			topology.addLink(new WSNTopology.Link(new NodeID("V2203"), null, 1.0));
			topology.addLink(new WSNTopology.Link(new NodeID("V2204"), null, 1.0));*/

			try {
//				this.app().wsn().addNode(node1).addNode(node2).addNode(node3).addNode(node4).addNode(node5).addNode(node6).addNode(node7).addNode(node8);
				this.app().wsn().replaceTopology(topology);

				Thread.sleep(10000);
			} catch (Exception e) {
			}

			_setError("No WSN attached");
		}
	}

}