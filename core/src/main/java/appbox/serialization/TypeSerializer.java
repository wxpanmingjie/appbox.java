package appbox.serialization;

import appbox.logging.Log;
import appbox.model.EntityModel;
import appbox.serialization.serializers.IntSerializer;
import appbox.serialization.serializers.StringSerializer;
import appbox.serialization.serializers.UserSerializer;

import java.util.HashMap;
import java.util.function.Supplier;

public abstract class TypeSerializer {

    private static final HashMap<Class<?>, TypeSerializer> _knownTypes    = new HashMap<>();
    private static final HashMap<Byte, TypeSerializer>     _sysKnownTypes = new HashMap<>();

    static {
        Log.debug("开始注册序列化器...");
        registerKnownType(IntSerializer.instance);
        registerKnownType(StringSerializer.instance);

        //----模型相关----
        registerKnownType(new UserSerializer(PayloadType.EntityModel, EntityModel.class, EntityModel::new));
    }

    /**
     * 注册已知类型的序列化器
     */
    public static void registerKnownType(TypeSerializer serializer) {
        _knownTypes.put(serializer.targetType, serializer);
        if (serializer.payloadType == PayloadType.ExtKnownType) {
            //TODO:
        }
        _sysKnownTypes.put(serializer.payloadType, serializer);
    }

    /**
     * 序列化时根据目标类型获取相应的序列化实现
     */
    public static TypeSerializer getSerializer(Class<?> type) {
        //TODO: 范型及容器类型的处理
        //TypeSerializer serializer = null;
        //var targetType = type;
        //if (type.getGenericInterfaces())

        return _knownTypes.get(type);
    }

    /**
     * 反序列化时根据PayloadType获取相应的系统已知类型的序列化实现
     */
    public static TypeSerializer getSerializer(byte payloadType) {
        return _sysKnownTypes.get(payloadType);
    }

    public final byte payloadType;

    public final Class<?> targetType;

    public final Supplier<Object> creator;

    protected TypeSerializer(byte payloadType, Class<?> targetType, Supplier<Object> creator) {
        this.payloadType = payloadType;
        this.targetType  = targetType;
        this.creator     = creator;
    }

    public abstract void write(BinSerializer bs, Object value) throws Exception;

    public abstract Object read(BinDeserializer bs, Object value) throws Exception;
}
