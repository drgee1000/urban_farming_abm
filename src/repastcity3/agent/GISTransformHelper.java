package repastcity3.agent;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationNotFoundException;
import repast.simphony.space.gis.Geography;

public class GISTransformHelper {
	private MathTransform transform;
	private AttributeType type;

	public <T> GISTransformHelper(Class<T> clazz, URL shapefile) {
		ShapefileDataStore store = null;
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
			type = schema.getType(0);
			String name = type.getName().getLocalPart();
			System.out.println("Load shapefile format:" + name);

		} catch (IntrospectionException ex) {
//			msg.error("Error while introspecting class", ex);
		} catch (IOException e) {
//			msg.error(String.format("Error opening shapefile '%S'", shapefile), e);
		} finally {
			store.dispose();
		}
	}

	public void transform(Geography geography) throws FactoryException {
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
