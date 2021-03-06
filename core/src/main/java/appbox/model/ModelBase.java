package appbox.model;

import appbox.data.PersistentState;
import appbox.serialization.BinDeserializer;
import appbox.serialization.BinSerializer;
import appbox.serialization.IBinSerializable;
import appbox.utils.IdUtil;

/**
 * 模型基类，实例分为设计时与运行时
 */
public abstract class ModelBase implements IBinSerializable {
    private long            _id;
    private String          _name;
    private String          _originalName;
    private boolean         _designMode;
    private int             _version;
    private PersistentState _persistentState;

    /**
     * only for Serialization
     */
    public ModelBase() {
    }

    public ModelBase(long id, String name) {
        _designMode      = true;
        _id              = id;
        _name            = name;
        _persistentState = PersistentState.Detached;
    }

    //region ====Properties====
    public ModelLayer modelLayer() {
        return ModelLayer.fromValue((byte) (_id & IdUtil.MODELID_LAYER_MASK));
    }

    public long id() {
        return _id;
    }

    public String name() {
        return _name;
    }

    public abstract ModelType modelType();

    public boolean designMode() {
        return _designMode;
    }

    public PersistentState persistentState() {
        return _persistentState;
    }
    //endregion

    //region ====Design Methods====
    public void checkDesignMode() throws RuntimeException {
        if (!designMode()) {
            throw new RuntimeException();
        }
    }

    public void onPropertyChanged() {
        if (_persistentState == PersistentState.Unchnaged) {
            _persistentState = PersistentState.Modified;
        }
    }

    public void acceptChanges() {
        if (_persistentState != PersistentState.Unchnaged) {
            if (_persistentState == PersistentState.Deleted) {
                _persistentState = PersistentState.Detached;
            } else {
                _persistentState = PersistentState.Unchnaged;
            }

            _originalName = null;
        }
    }
    //endregion

    //region ====Serialization====
    public static ModelBase makeModelByType(byte type) throws Exception {
        if (type == ModelType.Entity.value) {
            return new EntityModel();
        }
        throw new RuntimeException("Unknown model type: " + type);
    }

    @Override
    public void writeTo(BinSerializer bs) throws Exception {
        bs.writeLong(_id, 1);
        bs.writeString(_name, 2);
        bs.writeBool(_designMode, 3);

        if (_designMode) {
            bs.writeInt(_version, 4);
            bs.writeByte(_persistentState.value, 5);
            //TODO: folder
            if (_originalName != null) {
                bs.writeString(_originalName, 7);
            }
        } else if (modelType() == ModelType.Permission) {
            //TODO: folder
        }

        bs.finishWriteFields();
    }

    @Override
    public void readFrom(BinDeserializer bs) throws Exception {
        int propIndex;
        do {
            propIndex = bs.readVariant();
            switch (propIndex) {
                case 1:
                    _id = bs.readLong();
                    break;
                case 2:
                    _name = bs.readString();
                    break;
                case 3:
                    _designMode = bs.readBool();
                    break;
                case 4:
                    _version = bs.readInt();
                    break;
                case 5:
                    _persistentState = PersistentState.fromValue(bs.readByte());
                    break;
                case 7:
                    _originalName = bs.readString();
                    break;
                case 0:
                    break;
                default:
                    throw new RuntimeException("Unknown field id:" + propIndex);
            }
        } while (propIndex != 0);
    }
    //endregion
}