package bsm.insert.aiassignment.domain.common.snowflake

class Snowflake(
    private val nodeId: Long,
) {
    companion object {
        private const val NODE_ID_BITS = 10
        private const val SEQUENCE_BITS = 12

        private const val MAX_NODE_ID = (1L shl NODE_ID_BITS) - 1
        private const val MAX_SEQUENCE = (1L shl SEQUENCE_BITS) - 1

        // 2026-01-01T00:00:00Z
        private const val START_TIME_MILLIS = 1767225600000L
    }

    init {
        require(nodeId in 0..MAX_NODE_ID) {
            "nodeId must be between 0 and $MAX_NODE_ID"
        }
    }

    @Volatile
    private var lastTimestamp = -1L

    @Volatile
    private var sequence = 0L

    @Synchronized
    fun nextId(): Long {
        var currentTimestamp = System.currentTimeMillis()

        if (currentTimestamp < lastTimestamp) {
            throw IllegalStateException("Clock moved backwards")
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) and MAX_SEQUENCE
            if (sequence == 0L) {
                currentTimestamp = waitNextMillis(currentTimestamp)
            }
        } else {
            sequence = 0L
        }

        lastTimestamp = currentTimestamp

        return ((currentTimestamp - START_TIME_MILLIS) shl (NODE_ID_BITS + SEQUENCE_BITS)) or
                (nodeId shl SEQUENCE_BITS) or
                sequence
    }

    private fun waitNextMillis(timestamp: Long): Long {
        var ts = timestamp
        while (ts <= lastTimestamp) {
            ts = System.currentTimeMillis()
        }
        return ts
    }
}