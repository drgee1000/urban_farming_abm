package repastcity3.agent;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;
import simphony.util.messages.MessageCenter;

import com.vividsolutions.jts.geom.Geometry;

public class GISHelper {
	private MathTransform transform;
	private Geography geography;
	private Context context;
	private Class agentClass;

	private Iterator<SimpleFeature> featureIterator;

	public GISHelper(Class clazz, URL shapefile, Geography geography, Context context) {
		this.geography = geography;
		this.agentClass = clazz;
		this.context = context;

		ShapefileDataStore store = null;
		SimpleFeatureIterator iter = null;
		try {
			BeanInfo info = Introspector.getBeanInfo(clazz, Object.class);
			Map<String, Method> methodMap = new HashMap<String, Method>();
			PropertyDescriptor[] pds = info.getPropertyDescriptors();
			for (PropertyDescriptor pd : pds) {
				if (pd.getWriteMethod() != null) {
					methodMap.put(pd.getName().toLowerCase(), pd.getWriteMethod());
				}
			}

			store = new ShapefileDataStore(shapefile);
			SimpleFeatureType schema = store.getSchema(store.getTypeNames()[0]);

			// First attribute at index 0 is always the Geometry
			AttributeType type = schema.getType(0);
			String name = type.getName().getLocalPart();
			initTransform(geography, type);

			List<SimpleFeature> features = new ArrayList<SimpleFeature>();
			while (iter.hasNext()) {
				features.add(iter.next());
			}
			featureIterator = features.iterator();

		} catch (IntrospectionException ex) {
//			msg.error("Error while introspecting class", ex);
		} catch (IOException e) {
//			msg.error(String.format("Error opening shapefile '%S'", shapefile), e);
		} catch (FactoryException e) {
//			msg.error(String.format("Error creating transform between shapefile CRS and Geography CRS"), e);
		} finally {
			iter.close();
			store.dispose();
		}
	}

	private void initTransform(Geography geography, AttributeType type) throws FactoryException {
		GeometryType gType = (GeometryType) type;
		if (geography != null) {
			try {
				transform = ReferencingFactoryFinder.getCoordinateOperationFactory(null)
						.createOperation(gType.getCoordinateReferenceSystem(), geography.getCRS()).getMathTransform();

			} catch (OperationNotFoundException ex) {
				// bursa wolf params may be missing so try lenient.
				transform = CRS.findMathTransform(gType.getCoordinateReferenceSystem(), geography.getCRS(), true);
			}
		}
	}
}
