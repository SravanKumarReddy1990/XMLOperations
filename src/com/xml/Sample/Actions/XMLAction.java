package com.xml.Sample.Actions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.xml.Sample.XMLAnn.XMLColumn;
import com.xml.Sample.XMLAnn.XMLReport;

public class XMLAction {
	private Map<String, String> fieldLabelMap = new HashMap<String, String>();
	private Map<String, String> reportLabelMap = new HashMap<String, String>();
	private List<String> orderLabels = new ArrayList<String>();

	@SuppressWarnings("unchecked")
	public <T> List<T> readData(String classname, Document doc)
			throws Exception {
		Class clazz = Class.forName(classname);
		processAnnotations(clazz.newInstance());
		List<T> result = new ArrayList<T>();

		try {

			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName(getReportLabelMap()
					.get("report"));

			for (int j = 0; j < nodeList.getLength(); j++) {
				Node fstNode = nodeList.item(j);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					T one = (T) clazz.newInstance();
					result.add(one);
					for (int i = 0; i < getOrderLabels().size(); i++) {
						String labelName = getOrderLabels().get(i);
						String getter = getFieldLabelMap().get(labelName);
						String fieldName = getter.substring(3);
						fieldName = decapitalize(fieldName);
						Class<?> returnType = getGetterReturnClass(clazz,
								fieldName);
						Method method = constructMethod(clazz, fieldName);

						NodeList city_id = fstElmnt
								.getElementsByTagName(labelName);
						Element city_idElement = (Element) city_id.item(0);
						NodeList city_idNode = city_idElement.getChildNodes();

						if (returnType == Integer.class) {
							Integer val = 0;
							try {
								val = Integer
										.valueOf(((String) ((Node) city_idNode
												.item(0)).getNodeValue()));
							} catch (Exception e) {
								System.out.println(e + " inner loop");
							}
							method.invoke(one, val);
						} else if (returnType == String.class) {
							method.invoke(one, ((String) ((Node) city_idNode
									.item(0)).getNodeValue()));
						} else if (returnType == Long.class) {
							Long val = null;
							try {
								val = Long
										.parseLong(((String) ((Node) city_idNode
												.item(0)).getNodeValue()));
							} catch (Exception e) {
								System.out.println(e + " inner loop");
							}
							method.invoke(one, val);
						}
					}

				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return result;
	}

	public <T> String writtingData(List<T> data) throws Exception {
		// Class clazz = Class.forName(data.get(0));
		orderLabels = new ArrayList<String>();
		;
		processAnnotations(data.get(0));
		List<T> result = new ArrayList<T>();
		String s = "";
		String rep = getReportLabelMap().get("report");
		s += "<" + rep + ">";
		try {
			s += "\n";
			Class<? extends Object> classz = data.get(0).getClass();
			for (T t : data) {

				for (String label : getOrderLabels()) {
					String methodName = getFieldLabelMap().get(label);

					Method method = classz.getMethod(methodName);
					Object value = method.invoke(t, (Object[]) null);
					s += "<" + label + ">" + value + "</" + label + ">";
				}
				s += "\n";
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		s += "</" + rep + ">";
		return s;
	}

	private <T> void processAnnotations(T object) {
		Class<?> clazz = object.getClass();
		@SuppressWarnings("unused")
		XMLReport reportAnnotation = (XMLReport) clazz
				.getAnnotation(XMLReport.class);
		String rep = reportAnnotation.reportName();
		if (rep == null || rep.length() == 0) {
			rep = "NA";
		}
		getReportLabelMap().put("report", rep);

		for (Method method : clazz.getMethods()) {

			XMLColumn excelColumn = method.getAnnotation(XMLColumn.class);
			if ((excelColumn != null) && !excelColumn.ignore()) {
				getFieldLabelMap().put(excelColumn.label(), method.getName());
				getOrderLabels().add(excelColumn.label());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Method constructMethod(Class clazz, String fieldName)
			throws SecurityException, NoSuchMethodException {
		Class<?> fieldClass = getGetterReturnClass(clazz, fieldName);
		return clazz.getMethod("set" + capitalize(fieldName), fieldClass);
	}

	public Map<String, String> getFieldLabelMap() {
		return fieldLabelMap;
	}

	public void setFieldLabelMap(Map<String, String> fieldLabelMap) {
		this.fieldLabelMap = fieldLabelMap;
	}

	public Map<String, String> getReportLabelMap() {
		return reportLabelMap;
	}

	public void setReportLabelMap(Map<String, String> reportLabelMap) {
		this.reportLabelMap = reportLabelMap;
	}

	public String capitalize(String string) {
		String capital = string.substring(0, 1).toUpperCase();
		return capital + string.substring(1);
	}

	public List<String> getOrderLabels() {
		return orderLabels;
	}

	public String decapitalize(String string) {
		String capital = string.substring(0, 1).toLowerCase();
		return capital + string.substring(1);
	}

	private Class<?> getGetterReturnClass(Class<?> clazz, String fieldName) {
		String methodName = "get" + capitalize(fieldName);
		Class<?> returnType = null;
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName)) {
				returnType = method.getReturnType();
				break;
			}
		}
		return returnType;
	}
}
