package igrek.todotree.mock;

import android.app.Activity;
import android.content.SharedPreferences;

import igrek.todotree.logger.Logs;
import igrek.todotree.services.preferences.Preferences;
import igrek.todotree.services.preferences.PropertyDefinition;

public class MockedPreferences extends Preferences {
	
	public MockedPreferences(Activity activity, Logs logger) {
		super(activity, logger);
	}
	
	@Override
	protected SharedPreferences createSharedPreferences(Activity activity) {
		return null;
	}
	
	@Override
	public void saveAll() {
	}
	
	@Override
	public <T> T getValue(PropertyDefinition propertyDefinition, Class<T> clazz) {
		if(clazz.equals(Boolean.class)){
			return (T) Boolean.FALSE;
		}
		return null;
	}
	
	@Override
	public void clear() {
	}
	
	@Override
	public boolean exists(String name) {
		return false;
	}
}
