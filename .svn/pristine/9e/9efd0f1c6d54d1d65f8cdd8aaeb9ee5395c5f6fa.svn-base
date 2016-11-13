package de.tum.in.net.WSNDataFramework.Modules.Cosm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class CosmAPI {

	/**
	 * @param APIKEY
	 */
	public CosmAPI(String username, String APIKEY) {
		this._username = username;
		this._apiKey = APIKEY;
	}

	/**
	 * gets current username.
	 */
	public String getUsername() {
		return _username;
	}

	/**
	 * gets current API key.
	 */
	public String getAPIKey() {
		return _apiKey;
	}

	/**
	 * gets feeds of current user.
	 * 
	 * @return
	 */
	public List<Feed> getFeeds() {

		List<Feed> feeds = new ArrayList<Feed>();

		try {
			JSONObject jsonObject = _GETapiCall("feeds?user="+_urlEncode(_username));

			JSONArray results=(JSONArray)jsonObject.get("results");
			for(Object feed: results){
				feeds.add(new Feed((JSONObject)feed));
			}
		} catch (Exception e) {
		}

		return feeds;
	}

	public Feed getFeed(String feedID) {
		try {
			JSONObject jsonObject = _GETapiCall("feeds/"+_urlEncode(feedID));

			return new Feed(jsonObject);
		} catch (Exception e) {
		}

		return null;
	}

	/**
	 * creates a new feed
	 * 
	 * @param title
	 * @return new feedID
	 */
	public String createFeed(String title) {
		try {
			Map<String,Object> jsonParams = new HashMap<String,Object>();
			jsonParams.put("title", title);
			jsonParams.put("version","1.0.0");
			jsonParams.put("datastreams", Arrays.asList(new String[0]));

			HttpResponse response = _POSTapiCall("feeds", jsonParams);
			Header[] headers = response.getAllHeaders();
			for (Header header: headers) {
				if (header.getName().toLowerCase().equals("location")) {
					return header.getValue().substring(header.getValue().lastIndexOf('/')+1);
				}
			}
		} catch (Exception e) {
		}

		return null;
	}

	/**
	 * delete a specific feed.
	 * CAUTION: cannot be undone.
	 * 
	 * @param feedID
	 */
	public void deleteFeed(String feedID) {
		try {
			_DELETEapiCall("feeds/"+_urlEncode(feedID));
		} catch (Exception e) {
		}
	}

	public void deleteDataStream(String streamID, String feedID) {
		try {
			_DELETEapiCall("feeds/"+_urlEncode(feedID)+"/datastreams/"+_urlEncode(_encodeDataStreamID(streamID)));
		} catch (Exception e) {
		}
	}

	/**
	 * creates a datastream for a specific feed
	 * 
	 * @param streamID
	 * @param feedID
	 */
	public void createDataStream(String streamID, String feedID) {
		this.createDataStream(streamID, feedID, null);
	}
	/**
	 * creates a datastream for a specific feed, passing along its unit-label
	 * 
	 * @param streamID
	 * @param feedID
	 * @param unit
	 */
	public void createDataStream(String streamID, String feedID, String unit) {
		try{
			Map<String,Object> jsonParams = new HashMap<String,Object>();
			jsonParams.put("version","1.0.0");


			Map<String,Object> streamEntry = new HashMap<String,Object>();
			streamEntry.put("id", _encodeDataStreamID(streamID));

			if (unit != null) {
				Map<String,String> unitEntry = new HashMap<String,String>();
				unitEntry.put("label", unit);
				streamEntry.put("unit", unitEntry);
			}

			jsonParams.put("datastreams", Arrays.asList(new Map[]{streamEntry}));

			_POSTapiCall("feeds/"+_urlEncode(feedID)+"/datastreams", jsonParams);
		} catch (Exception e) {
		}
	}

	/**
	 * updates a datastream, uploads given value
	 * 
	 * @param value
	 * @param streamID
	 * @param feedID
	 */
	public void updateStream(double value, String streamID, String feedID) {
		try {
			Map<String,Object> jsonParams = new HashMap<String,Object>();
			jsonParams.put("current_value", value);

			_PUTapiCall("feeds/"+_urlEncode(feedID)+"/datastreams/"+_urlEncode(_encodeDataStreamID(streamID)), jsonParams);
		} catch (Exception e) {

		}
	}


	/* protected helper */
	protected JSONObject _GETapiCall(String cmd) {
		try {
			HttpClient httpclient = new DefaultHttpClient();

			HttpGet request = new HttpGet(_apiURL + cmd);
			request.addHeader("X-ApiKey", _apiKey);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(request, responseHandler);

			return (JSONObject)JSONValue.parse(responseBody);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	protected HttpResponse _DELETEapiCall(String cmd) {
		try {
			HttpClient httpclient = new DefaultHttpClient();

			HttpDelete request = new HttpDelete(_apiURL + cmd);
			request.addHeader("X-ApiKey", _apiKey);

			HttpResponse response = httpclient.execute(request);
			return response;

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	/**
	 * @param cmd
	 * @param jsonParams will get encoded using JSONValue.toJSONString()
	 */
	protected HttpResponse _POSTapiCall(String cmd, Object jsonParams) {
		try {
			HttpClient httpclient = new DefaultHttpClient();


			HttpPost request = new HttpPost(_apiURL + cmd);

			request.addHeader("X-ApiKey", _apiKey);
			request.setEntity(new ByteArrayEntity(
					JSONValue.toJSONString(jsonParams).toString().getBytes()));

			HttpResponse response = httpclient.execute(request);
			return response;

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	/**
	 * @param cmd
	 * @param jsonParams will get encoded using JSONValue.toJSONString()
	 */
	protected HttpResponse _PUTapiCall(String cmd, Object jsonParams) {
		try {
			HttpClient httpclient = new DefaultHttpClient();


			HttpPut request = new HttpPut(_apiURL + cmd);

			request.addHeader("X-ApiKey", _apiKey);
			request.setEntity(new ByteArrayEntity(
					JSONValue.toJSONString(jsonParams).toString().getBytes()));

			HttpResponse response = httpclient.execute(request);
			return response;

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	protected String _urlEncode(String str) {
		try {
			return URLEncoder.encode(str, Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e) {
		}

		return null;
	}

	protected String _encodeDataStreamID(String streamID) {
		if (streamID==null) return null;

		return streamID.replaceAll("[\\W]", "");
	}

	/* protected member */
	protected static String _apiURL="http://api.cosm.com/v2/";
	protected String _username;
	protected String _apiKey;




	public static class Feed {
		public String id;
		public boolean isPrivate;
		public String feedJsonUrl;
		public String title;
		public String status;
		public String version;
		public String creator;
		public String location;
		public String updated;
		public List<String> tags;
		public List<DataStream> streams;

		public Feed(JSONObject cosmFeed) {
			this.id = cosmFeed.get("id")!=null ? cosmFeed.get("id").toString() : "";
			this.isPrivate = cosmFeed.get("private")!=null ? Boolean.parseBoolean(cosmFeed.get("private").toString()) : false;
			this.feedJsonUrl = cosmFeed.get("feed")!=null ? cosmFeed.get("feed").toString() : "";
			this.title = cosmFeed.get("title")!=null ? cosmFeed.get("title").toString() : "";
			this.status = cosmFeed.get("status")!=null ? cosmFeed.get("status").toString() : "";
			this.version = cosmFeed.get("version")!=null ? cosmFeed.get("version").toString() : "";
			this.creator = cosmFeed.get("creator")!=null ? cosmFeed.get("creator").toString() : "";
			this.location = cosmFeed.get("location")!=null ? cosmFeed.get("location").toString() : "";
			this.updated = cosmFeed.get("updated")!=null ? cosmFeed.get("updated").toString() : "";

			this.tags = new ArrayList<String>();
			JSONArray tags = (JSONArray) cosmFeed.get("tags");
			if (tags != null) {
				for (Object tag: tags) {
					this.tags.add(tag.toString());
				}
			}

			this.streams = new ArrayList<DataStream>();
			JSONArray streams = (JSONArray) cosmFeed.get("datastreams");
			if (streams != null) {
				for (Object stream: streams) {
					this.streams.add(new DataStream((JSONObject)stream));
				}
			}
		}

		public static class DataStream {
			public String id;
			public String currentValue;
			public String minValue;
			public String maxValue;
			public String updatedAt;
			public List<String> tags;

			public DataStream(JSONObject cosmStream) {
				this.id = cosmStream.get("id")!=null ? cosmStream.get("id").toString() : "";
				this.currentValue = cosmStream.get("current_value")!=null ? cosmStream.get("current_value").toString() : "";
				this.minValue = cosmStream.get("min_value")!=null ? cosmStream.get("min_value").toString() : "";
				this.maxValue = cosmStream.get("max_value")!=null ? cosmStream.get("max_value").toString() : "";
				this.updatedAt = cosmStream.get("at")!=null ? cosmStream.get("at").toString() : "";

				this.tags = new ArrayList<String>();
				JSONArray tags = (JSONArray) cosmStream.get("tags");
				if (tags != null) {
					for (Object tag: tags) {
						this.tags.add(tag.toString());
					}
				}
			}
		}
	}
}
