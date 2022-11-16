package br.com.kascosys.vulkanconnectv317.models

data class PermissionData(var permission: Boolean = false) {
    fun get() = permission
    fun set(value: Boolean) {
        permission = value
    }

    fun match(varPermission: Boolean): Boolean {
        return permission or (permission.not() and varPermission.not())
    }

    companion object {
        val BASIC_PERMISSION = false
        val ADVANCED_PERMISSION = true
    }
}