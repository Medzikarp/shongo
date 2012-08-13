package cz.cesnet.shongo.controller.api.xmlrpc;

import cz.cesnet.shongo.api.AtomicType;
import cz.cesnet.shongo.api.util.Converter;
import cz.cesnet.shongo.api.util.Options;
import cz.cesnet.shongo.fault.CommonFault;
import cz.cesnet.shongo.fault.FaultException;
import org.apache.xmlrpc.common.TypeConverter;
import org.apache.xmlrpc.common.TypeConverterFactoryImpl;

import java.util.Map;

/**
 * TypeConverterFactory that allows {@link AtomicType}, {@link StructType} and enums as method parameters
 * and return values.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class TypeConverterFactory extends TypeConverterFactoryImpl
{
    /**
     * Option whether store changes for object when it is converted map.
     */
    private Options options;

    /**
     * Converter for {@link AtomicType}.
     */
    private TypeConverter atomicTypeConverter = new AtomicTypeConverter(AtomicType.class);

    /**
     * Converter for {@link Map}.
     */
    private TypeConverter mapTypeConverter;

    /**
     * Constructor.
     *
     * @param options sets the {@link #options}
     */
    public TypeConverterFactory(Options options)
    {
        this.options = options;
        mapTypeConverter = new MapTypeConverter(options);
    }

    @Override
    public TypeConverter getTypeConverter(Class pClass)
    {
        if (pClass.isEnum()) {
            return EnumTypeConverter.getInstance(pClass);
        }
        else if (AtomicType.class.isAssignableFrom(pClass)) {
            return atomicTypeConverter;
        }
        else if (StructType.class.isAssignableFrom(pClass)) {
            return StructTypeConverter.getInstance(pClass, options);
        }
        else if (Map.class.isAssignableFrom(pClass)) {
            return mapTypeConverter;
        }
        return super.getTypeConverter(pClass);
    }

    /**
     * Converter for enum types.
     *
     * @author Martin Srom <martin.srom@cesnet.cz>
     */
    private static class EnumTypeConverter implements TypeConverter
    {
        private final Class clazz;

        EnumTypeConverter(Class pClass)
        {
            clazz = pClass;
        }

        @Override
        public boolean isConvertable(Object pObject)
        {
            return (pObject instanceof String) || clazz.isAssignableFrom(pObject.getClass());
        }

        @Override
        public Object convert(Object pObject)
        {
            if (pObject instanceof String) {
                String value = (String) pObject;
                try {
                    return Converter.convertStringToEnum(value, clazz);
                }
                catch (FaultException exception) {
                    throw new RuntimeException(exception);
                }
            }
            return pObject;
        }

        @Override
        public Object backConvert(Object result)
        {
            return result.toString();
        }

        public static EnumTypeConverter getInstance(Class pClass)
        {
            // TODO: Reuse instances for same class
            return new EnumTypeConverter(pClass);
        }
    }

    /**
     * Converter for atomic types.
     *
     * @author Martin Srom <martin.srom@cesnet.cz>
     */
    private static class AtomicTypeConverter implements TypeConverter
    {
        private final Class clazz;

        AtomicTypeConverter(Class pClass)
        {
            clazz = pClass;
        }

        @Override
        public boolean isConvertable(Object pObject)
        {
            return (pObject instanceof String) || clazz.isInstance(pObject);
        }

        @Override
        public Object convert(Object pObject)
        {
            if (pObject instanceof String) {
                String value = (String) pObject;
                AtomicType atomicType = null;
                try {
                    atomicType = (AtomicType) clazz.newInstance();
                }
                catch (java.lang.Exception exception) {
                    throw new RuntimeException(new FaultException(CommonFault.CLASS_CANNOT_BE_INSTANCED, clazz));
                }
                atomicType.fromString(value);
                return atomicType;
            }
            return pObject;
        }

        @Override
        public Object backConvert(Object result)
        {
            return result.toString();
        }
    }

    /**
     * ComplexType converter.
     *
     * @author Martin Srom <martin.srom@cesnet.cz>
     */
    private static class StructTypeConverter implements TypeConverter
    {
        /**
         * Option whether store changes for object when it is converted map.
         */
        private Options options;

        /**
         * Type of object.
         */
        private final Class type;

        /**
         * Constructor.
         *
         * @param options sets the {@link #options}
         */
        StructTypeConverter(Class type, Options options)
        {
            this.type = type;
            this.options = options;
        }

        @Override
        public boolean isConvertable(Object pObject)
        {
            return pObject == null || type.isAssignableFrom(pObject.getClass()) || pObject instanceof Map;
        }

        @Override
        public Object convert(Object pObject)
        {
            if (pObject instanceof Map) {
                try {
                    return Converter.convertMapToObject((Map) pObject, type, options);
                }
                catch (FaultException exception) {
                    throw new RuntimeException(exception);
                }
            }
            return pObject;
        }

        @Override
        public Object backConvert(Object pObject)
        {
            try {
                return Converter.convertObjectToMap(pObject, options);
            }
            catch (FaultException exception) {
                throw new RuntimeException(exception);
            }
        }

        public static StructTypeConverter getInstance(Class pClass, Options options)
        {
            // TODO: Reuse instances for same class
            return new StructTypeConverter(pClass, options);
        }
    }

    /**
     * Map converter.
     *
     * @author Martin Srom <martin.srom@cesnet.cz>
     */
    private static class MapTypeConverter implements TypeConverter
    {
        /**
         * Option whether store changes for object when it is converted map.
         */
        private Options options;

        /**
         * Constructor.
         *
         * @param options sets the {@link #options}
         */
        public MapTypeConverter(Options options)
        {
            this.options = options;
        }

        @Override
        public boolean isConvertable(Object pObject)
        {
            return pObject == null || Map.class.isAssignableFrom(pObject.getClass())
                    || StructType.class.isAssignableFrom(pObject.getClass());
        }

        @Override
        public Object convert(Object pObject)
        {
            if (pObject instanceof StructType) {
                try {
                    return Converter.convertObjectToMap(pObject, options);
                }
                catch (FaultException exception) {
                    throw new RuntimeException(exception);
                }
            }
            return pObject;
        }

        @Override
        public Object backConvert(Object pObject)
        {
            return pObject;
        }
    }
}
