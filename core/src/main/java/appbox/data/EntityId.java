package appbox.data;

import appbox.runtime.RuntimeContext;

import java.util.concurrent.atomic.AtomicInteger;

public final class EntityId {
    private static final AtomicInteger peerSeq = new AtomicInteger(0); //Peer流水号计数器

    //private long _data1;
    //private long _data2;
    private final byte[] _data = new byte[16];

    public EntityId() {
        var seq       = peerSeq.incrementAndGet() & 0xFFFF;
        var peerId    = RuntimeContext.peerId();
        var timestamp = System.currentTimeMillis() & 0xFFFFFFFFFFFFL; //保留48bit

        //流水号与PeerId, 大字节序
        _data[15] = (byte) (seq & 0xFF);
        _data[14] = (byte) (seq >>> 8);
        _data[13] = (byte) (peerId & 0xFF);
        _data[12] = (byte) (peerId >>> 8);
        //时间戳部分１，大字节序
        _data[11] = (byte) (timestamp & 0xFF);
        _data[10] = (byte) (timestamp >>> 8);
        _data[9]  = (byte) (timestamp >>> 16);
        _data[8]  = (byte) (timestamp >>> 24);
        //时间戳部分２，小字节序
        _data[7] = (byte) (timestamp >>> 40);
        _data[6] = (byte) (timestamp >>> 32);
    }

    /**
     * Epoch毫秒数
     */
    public long timestamp() {
        return Byte.toUnsignedLong(_data[11]) | Byte.toUnsignedLong(_data[10]) << 8
                | Byte.toUnsignedLong(_data[9]) << 16 | Byte.toUnsignedLong(_data[8]) << 24
                | Byte.toUnsignedLong(_data[7]) << 40 | Byte.toUnsignedLong(_data[6]) << 32;
    }

    public long raftGroupId() {
        var p1 = Byte.toUnsignedLong(_data[0]) | Byte.toUnsignedLong(_data[1]) << 8
                | Byte.toUnsignedLong(_data[2]) << 16 | Byte.toUnsignedLong(_data[3]) << 24;
        var p2 = Byte.toUnsignedLong(_data[4]) | Byte.toUnsignedLong(_data[5]) << 8;
        return (p1 << 12) | (p2 >>> 4);
    }

    public void initRaftGroupId(long raftGroupId) {
        //不需要判断是否已初始化，因为可能Schema变更后重试
        //RaftGroupId拆为32 + (12 + 4)
        //前32位
        int p1 = (int) (raftGroupId >>> 12);
        _data[0] = (byte) (p1 & 0xFF);
        _data[1] = (byte) (p1 >>> 8);
        _data[2] = (byte) (p1 >>> 16);
        _data[3] = (byte) (p1 >>> 24);
        //后12位　<< 4
        short p2 = (short) ((raftGroupId & 0xFFF) << 4);
        _data[4] = (byte) (p2 & 0xFF);
        _data[5] = (byte) (p2 >>> 8);
    }

    @Override
    public int hashCode() {
        //注意不计算RaftGroupId部分
        //TODO:暂简单实现
        int hash1 = 5381;
        int hash2 = hash1;

        int c = 0;
        for (int i = 6; i < 16; i++) {
            c     = _data[i];
            hash1 = ((hash1 << 5) + hash1) ^ c;
            if (i + 1 == 16) {
                break;
            }
            c     = _data[i + 1];
            hash2 = ((hash2 << 5) + hash2) ^ c;
            i++;
        }

        return hash1 + (hash2 * 1566083941);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EntityId)) {
            return false;
        }
        var t = (EntityId) obj;
        for (int i = 0; i < 16; i++) {
            if (t._data[i] != this._data[i]) {
                return false;
            }
        }
        return true;
    }
}
