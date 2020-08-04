package appbox.core.serialization;

import appbox.core.cache.ObjectPool;

public final class BinDeserializer {

    public static final ObjectPool<BinDeserializer> pool = new ObjectPool<>(BinDeserializer::new, null, 32);

    private BinDeserializer() {
    }

    private IInputStream _stream;

    public void reset(IInputStream stream) {
        _stream = stream;
    }

    public Object deserialize() throws Exception {
        var payloadType = _stream.readByte();
        if (payloadType == PayloadType.Null) return null;
        //TODO:对象引用

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
            return serializer.read(this);
        } else { //其他需要创建实例的类型
            //TODO:
            throw new Exception();
        }
    }

    public int readVariant() throws Exception {
        return _stream.readVariant();
    }

    public short readShort() throws Exception {
        return _stream.readShort();
    }

    public String readString() throws Exception {
        return _stream.readString();
    }
}
