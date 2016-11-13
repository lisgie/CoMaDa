package de.tum.in.net.WSNDataFramework.Modules.Web;

public class MyShutdownHook {
	private WSNSQLConnector _connector;
	
	public MyShutdownHook(WSNSQLConnector connector) {
		_connector = connector;
	}

	public void attachShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				_connector.cleanUp();
			}
		});
	}
}
