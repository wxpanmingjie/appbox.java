package appbox.model.entity;

import appbox.data.PersistentState;
import appbox.model.EntityModel;
import appbox.serialization.BinDeserializer;
import appbox.serialization.BinSerializer;
import appbox.serialization.IBinSerializable;

/**
 * 系统存储及Sql存储的索引模型基类
 */
public abstract class IndexModelBase implements IBinSerializable {
    private EntityModel      _owner;
    private byte             _indexId;
    private String           _name;
    private boolean          _unique;
    private FieldWithOrder[] _fields;
    private short[]          _storingFields; //索引覆盖字段集合
    private PersistentState  _persistentState;

    public IndexModelBase(EntityModel owner, String name, boolean unique,
                          FieldWithOrder[] fields, short[] storingFields) {
        _owner         = owner;
        _name          = name;
        _unique        = unique;
        _fields        = fields;
        _storingFields = storingFields;
    }

    //region ====Properties====
    public PersistentState persistentState() {
        return _persistentState;
    }
    //endregion

    //region ====Design Methods====
    public void initIndexId(byte id) {
        if (_indexId == 0) {
            _indexId = id;
        }
    }

    public void acceptChanges() {
        _persistentState = PersistentState.Unchnaged;
    }

    public void markDeleted() {
        _persistentState = PersistentState.Deleted;
        _owner.onPropertyChanged();
        _owner.changeSchemaVersion();
    }
    //endregion

    //region ====Serialization====
    @Override
    public void writeTo(BinSerializer bs) throws Exception {
        //TODO:
    }

    @Override
    public void readFrom(BinDeserializer bs) throws Exception {
        //TODO:
    }
    //endregion
}