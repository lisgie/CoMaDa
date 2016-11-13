package de.tum.in.net.WSNDataFramework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class FileStorage {
	public FileStorage(File file) {
		_file = file;

		if (!_files.containsKey(file)) {
			this.reload();
		}
		_entries = _files.get(file);
	}
	public <T extends Serializable> T get(String key, Class<T> type) {
		try {
			return type.cast(_entries.get(key));
		} catch (Exception e) {
			return null;
		}
	}
	public FileStorage save(Serializable obj, String key) throws IOException {
		synchronized (_entries) {
			_entries.put(key, obj);

			FileOutputStream fos = new FileOutputStream(_file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(_entries);
			fos.close();
		}

		return this;
	}
	@SuppressWarnings("unchecked")
	public FileStorage reload() {
		synchronized (_files) {
			try {
				FileInputStream fos = new FileInputStream(_file);
				ObjectInputStream oos = new ObjectInputStream(fos);
				Object entries = oos.readObject();
				_files.put(_file, (Map<String,Object>)entries);
				fos.close();
			} catch (Exception e) {
				_files.put(_file,new HashMap<String,Object>());
			}
		}

		return this;
	}

	protected File _file=null;
	protected Map<String,Object> _entries=null;
	protected static Map<File,Map<String,Object>> _files=new HashMap<File,Map<String,Object>>();
}