package br.com.kascosys.vulkanconnectv317.enums

enum class UserPermission {
    BASIC {
        override fun toBoolean(): Boolean {
            return false
        }
    },
    ADVANCED {
        override fun toBoolean(): Boolean {
            return true
        }
    };

    abstract fun toBoolean(): Boolean

    companion object {
        fun fromBoolean(value: Boolean): UserPermission {
            if(value) {
                return ADVANCED
            }

            return BASIC
        }
    }

}