package br.com.kascosys.vulkanconnectv317.enums

enum class BrakeStatus {
    OPEN_START, OPEN_FINISH, CLOSE_START, CLOSE_FINISH, UNEXPECTED;

    companion object {
        fun nextStatus(
            lastCommandBit: Boolean,
            lastStatusBit: Boolean,
            nextCommandBit: Boolean,
            nextStatusBit: Boolean
        ): BrakeStatus {
            when {
                !lastCommandBit && !lastStatusBit -> {
                    if (nextCommandBit && !nextStatusBit) {
                        return OPEN_START
                    }
                }

                lastCommandBit && !lastStatusBit -> {
                    if (nextCommandBit && nextStatusBit) {
                        return OPEN_FINISH
                    }
                }

                lastCommandBit && lastStatusBit -> {
                    if (!nextCommandBit && nextStatusBit) {
                        return CLOSE_START
                    }
                }

                !lastCommandBit && lastStatusBit -> {
                    if (!nextCommandBit && !nextStatusBit) {
                        return CLOSE_FINISH
                    }
                }
            }

            return UNEXPECTED
        }
    }
}