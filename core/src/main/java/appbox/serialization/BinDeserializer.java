package appbox.serialization;

import appbox.cache.ObjectPool;

public final class BinDeserializer {
    private static final ObjectPool<BinDeserializer> pool = new ObjectPool<>(BinDeserializer::new, 32);

    public static BinDeserializer rentFromPool(IInputStream stream) {
        var obj = pool.rent();
        obj._stream = stream;
        return obj;
    }

    public static void backToPool(BinDeserializer obj) {
        obj._stream = null;
        pool.back(obj);
    }

    private BinDeserializer() {
    }

    private IInputStream _stream;

    public Object deserialize() throws Exception {
        var payloadType = _stream.readByte();
        if (payloadType == PayloadType.Null) return null;
        else if (payloadType == PayloadType.BooleanTrue) return Boolean.TRUE;
        else if (payloadType == PayloadType.BooleanFalse) return Boolean.FALSE;
        else if (payloadType == PayloadType.ObjectRef) throw new Exception("TODO");

        TypeSerializer serializer = null;
        if (payloadType == PayloadType.ExtKnownType)
            throw new Exception("TODO");
        else
            serializer = TypeSerializer.getSerializer(payloadType);
        if (serializer == null)
            throw new Exception("待实现未知类型反序列化");

        //读取附加类型信息并创建实例
        if (serializer.creator == null
                && payloadType != PayloadType.Array //非数组类型
            /*&& serializer.genericTypeCount <= 0 //非范型类型*/) {
            return serializer.read(this, null);
        } else { //其他需要创建实例的类型
            Object result = null;
            //TODO: 先处理数组及范型类型
            result = serializer.creator.get();
            //TODO: 加入已序列化列表
            serializer.read(this, result);
            return result;
        }
    }

    public void skip(int size) throws Exception {
        _stream.skip(size);
    }

    public boolean readBool() throws Exception {
        return _stream.readBool();
    }

    public byte readByte() throws Exception {
        return _stream.readByte();
    }

    public short readShort() throws Exception {
        return _stream.readShort();
    }

    public int readInt() throws Exception {
        return _stream.readInt();
    }

    public long readLong() throws Exception {
        return _stream.readLong();
    }

    public int readVariant() throws Exception {
        return _stream.readVariant();
    }

    public int readNativeVariant() throws Exception {
        return _stream.readNativeVariant();
    }

    public String readString() throws Exception {
        return _stream.readString();
    }
}
