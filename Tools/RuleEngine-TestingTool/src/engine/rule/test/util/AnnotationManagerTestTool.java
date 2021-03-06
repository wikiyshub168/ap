package engine.rule.test.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import engine.rule.model.BaseForm;
import engine.rule.model.ModelFieldOfDB;
import engine.rule.model.annotation.DBField;

public class AnnotationManagerTestTool {

	private static Map<Class<? extends BaseForm>, List<ModelFieldOfDB>> ModelFieldOfDBListMapping = new ConcurrentHashMap<Class<? extends BaseForm>, List<ModelFieldOfDB>>();

	private static Map<Class<? extends BaseForm>, Map<String, ModelFieldOfDB>> ModelFieldOfDBMapMapping = new ConcurrentHashMap<Class<? extends BaseForm>, Map<String, ModelFieldOfDB>>();

	private static Map<Class<? extends BaseForm>, List<Field>> WithOutDbFieldMapping = new ConcurrentHashMap<>();
	private static void _divideModelFieldOfDB(
			Class<? extends BaseForm> formClass) {
		List<ModelFieldOfDB> dbFieldsList = new ArrayList<ModelFieldOfDB>();
		Map<String, ModelFieldOfDB> dbFieldsMap = new HashMap<String, ModelFieldOfDB>();				
		ModelFieldOfDBListMapping.put(formClass, dbFieldsList);
		ModelFieldOfDBMapMapping.put(formClass, dbFieldsMap);
		
		List<Field> withDbField = new ArrayList<>();
		WithOutDbFieldMapping.put(formClass, withDbField);
		
		Field[] fields = formClass.getDeclaredFields();
		DBField dbField = null;
		for (Field field : fields) {
			// for jdk8
			// dbField = field.getDeclaredAnnotation(DBField.class);

			// for jdk7
			dbField = field.getAnnotation(DBField.class);

			if (dbField != null) {
				String name = dbField.fieldName();
				String adapter = dbField.bindAdapter();
				String variableType = dbField.variableType();

				// if not DerivativeVariable, put into List
				if (variableType.equals("")) {
					if (!name.equals("")) {
						dbFieldsList.add(new ModelFieldOfDB(dbField, field));
					} else if (!adapter.equals("")) {
						dbFieldsList.add(new ModelFieldOfDB(dbField, field,
								ModelFieldOfDB.DBMappingType.ADAPTER));
					}
				}
				// if DerivativeVariable, put into Map
				else {
					dbFieldsMap.put(variableType, new ModelFieldOfDB(dbField,
							field));
				}
			}else {
				withDbField.add(field);
			}
			
		}

	}

	public static List<Field> getWithoutFieldsList(
			Class<? extends BaseForm> formClass) {
		if (!WithOutDbFieldMapping.containsKey(formClass)) {
			_divideModelFieldOfDB(formClass);
		}
		return WithOutDbFieldMapping.get(formClass);
	}
	
	public static List<ModelFieldOfDB> getModelDBFieldsList(
			Class<? extends BaseForm> formClass) {
		if (!ModelFieldOfDBListMapping.containsKey(formClass)) {
			_divideModelFieldOfDB(formClass);
		}
		return ModelFieldOfDBListMapping.get(formClass);
	}

	public static Map<String, ModelFieldOfDB> getModelDBFieldsMap(
			Class<? extends BaseForm> formClass) {
		if (!ModelFieldOfDBMapMapping.containsKey(formClass)) {
			_divideModelFieldOfDB(formClass);
		}
		return ModelFieldOfDBMapMapping.get(formClass);
	}
}
