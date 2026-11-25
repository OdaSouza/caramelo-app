package com.draxio.mapa_teste

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

// Repositório Central de Persistência de Dados Local para o Aplicativo Caramelo
object CarameloRepository {

    private const val PREFS_NAME = "caramelo_local_storage"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // -------------------------------------------------------------
    // 0. SESSÃO (sem servidor: só um sinalizador local)
    // -------------------------------------------------------------
    fun setLoggedIn(context: Context, loggedIn: Boolean) {
        getPrefs(context).edit { putBoolean("is_logged_in", loggedIn) }
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean("is_logged_in", false)
    }

    // Apaga todos os dados locais do usuário (perfil, histórico, bloqueios, motorista, tema)
    fun clearAllUserData(context: Context) {
        listOf(
            PREFS_NAME,
            "caramelo_theme_prefs",
            "secure_driver_prefs",
            "secure_vehicle_prefs"
        ).forEach { nome ->
            try {
                context.getSharedPreferences(nome, Context.MODE_PRIVATE).edit { clear() }
                context.deleteSharedPreferences(nome)
            } catch (_: Exception) {
                // segue para o próximo arquivo
            }
        }
    }

    // -------------------------------------------------------------
    // 1. PERSISTÊNCIA DE DADOS DO USUÁRIO & ENDEREÇO
    // -------------------------------------------------------------
    fun saveUserProfile(
        context: Context,
        name: String,
        email: String,
        logradouro: String,
        number: String,
        bairro: String,
        complement: String,
        cep: String,
        city: String,
        state: String
    ) {
        getPrefs(context).edit {
            putString("user_name", name)
            putString("user_email", email)
            putString("user_logradouro", logradouro)
            putString("user_number", number)
            putString("user_bairro", bairro)
            putString("user_complement", complement)
            putString("user_cep", cep)
            putString("user_city", city)
            putString("user_state", state)
        }
    }

    fun getUserName(context: Context): String = getPrefs(context).getString("user_name", "Passageiro") ?: "Passageiro"
    fun getUserEmail(context: Context): String = getPrefs(context).getString("user_email", "") ?: ""
    fun getUserCep(context: Context): String = getPrefs(context).getString("user_cep", "") ?: ""
    fun getUserLogradouro(context: Context): String = getPrefs(context).getString("user_logradouro", "") ?: ""
    fun getUserNumber(context: Context): String = getPrefs(context).getString("user_number", "") ?: ""
    fun getUserBairro(context: Context): String = getPrefs(context).getString("user_bairro", "") ?: ""
    fun getUserCity(context: Context): String = getPrefs(context).getString("user_city", "") ?: ""
    fun getUserState(context: Context): String = getPrefs(context).getString("user_state", "") ?: ""
    fun getUserComplement(context: Context): String = getPrefs(context).getString("user_complement", "") ?: ""

    // -------------------------------------------------------------
    // 2. PERSISTÊNCIA DE DADOS DO MOTORISTA PARCEIRO & CNH
    // -------------------------------------------------------------
    fun setDriverRegistered(context: Context, isRegistered: Boolean) {
        getPrefs(context).edit { putBoolean("is_driver_registered", isRegistered) }
    }

    fun isDriverRegistered(context: Context): Boolean {
        return getPrefs(context).getBoolean("is_driver_registered", false)
    }

    // -------------------------------------------------------------
    // 2b. VERIFICAÇÃO DE SEGURANÇA DO MOTORISTA (antecedentes, selfie, EAR)
    // Sem servidor ainda: o status é só local. A análise real é do backend.
    // -------------------------------------------------------------
    const val VERIFICATION_NOT_SENT = "nao_enviada"
    const val VERIFICATION_IN_REVIEW = "em_analise"
    const val VERIFICATION_APPROVED = "aprovada"

    fun setDriverVerificationStatus(context: Context, status: String) {
        getPrefs(context).edit { putString("driver_verification_status", status) }
    }

    fun getDriverVerificationStatus(context: Context): String {
        return getPrefs(context).getString("driver_verification_status", VERIFICATION_NOT_SENT) ?: VERIFICATION_NOT_SENT
    }

    // Grupo autodeclarado pelo motorista para o filtro de preferência (Geral, Mulher, Homem, LGBTQIA+)
    // Dado sensível (LGPD): fica em EncryptedSharedPreferences ("secure_driver_prefs")
    fun setDriverDeclaredGroup(context: Context, group: String) {
        try {
            securePrefs(context, "secure_driver_prefs").edit { putString("driver_declared_group", group) }
        } catch (_: Exception) {
            // sem criptografia disponível: não grava em texto puro
        }
    }

    fun getDriverDeclaredGroup(context: Context): String {
        return try {
            securePrefs(context, "secure_driver_prefs").getString("driver_declared_group", "Geral") ?: "Geral"
        } catch (_: Exception) {
            "Geral"
        }
    }

    // security-crypto foi descontinuado pelo AndroidX; migrar para DataStore + Tink quando houver backend
    @Suppress("DEPRECATION")
    private fun securePrefs(context: Context, name: String): SharedPreferences {
        val masterKey = androidx.security.crypto.MasterKey.Builder(context)
            .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
            .build()
        return androidx.security.crypto.EncryptedSharedPreferences.create(
            context,
            name,
            masterKey,
            androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // -------------------------------------------------------------
    // 3. PERSISTÊNCIA DE CONFIGURAÇÕES & PREFERÊNCIAS
    // -------------------------------------------------------------
    fun saveDefaultDriverPreference(context: Context, pref: String) {
        getPrefs(context).edit { putString("default_driver_pref", pref) }
    }

    fun getDefaultDriverPreference(context: Context): String {
        return getPrefs(context).getString("default_driver_pref", "LGBTQIA+") ?: "LGBTQIA+"
    }

    fun saveFixedPreferenceEnabled(context: Context, isFixed: Boolean) {
        getPrefs(context).edit { putBoolean("is_fixed_pref_enabled", isFixed) }
    }

    fun isFixedPreferenceEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean("is_fixed_pref_enabled", false)
    }

    // Interruptores das Configurações (padrão: ligados)
    fun setEnforceNoBlockedMatch(context: Context, enabled: Boolean) {
        getPrefs(context).edit { putBoolean("enforce_no_blocked_match", enabled) }
    }

    fun isEnforceNoBlockedMatch(context: Context): Boolean =
        getPrefs(context).getBoolean("enforce_no_blocked_match", true)

    fun setAutoBlockLowRating(context: Context, enabled: Boolean) {
        getPrefs(context).edit { putBoolean("auto_block_low_rating", enabled) }
    }

    fun isAutoBlockLowRating(context: Context): Boolean =
        getPrefs(context).getBoolean("auto_block_low_rating", true)

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit { putBoolean("notifications_enabled", enabled) }
    }

    fun isNotificationsEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean("notifications_enabled", true)

    fun setShareRouteEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit { putBoolean("share_route_enabled", enabled) }
    }

    fun isShareRouteEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean("share_route_enabled", true)

    fun savePriceIncrementStep(context: Context, step: String) {
        getPrefs(context).edit { putString("price_increment_step", step) }
    }

    fun getPriceIncrementStep(context: Context): String {
        return getPrefs(context).getString("price_increment_step", "R$ 2,00") ?: "R$ 2,00"
    }

    // -------------------------------------------------------------
    // 4. PERSISTÊNCIA DE MOTORISTAS BLOQUEADOS (JSON)
    // -------------------------------------------------------------
    fun saveBlockedDrivers(context: Context, blockedList: List<BlockedDriver>) {
        val jsonArray = JSONArray()
        blockedList.forEach { driver ->
            val obj = JSONObject().apply {
                put("id", driver.id)
                put("name", driver.name)
                put("carInfo", driver.carInfo)
                put("reason", driver.reason)
            }
            jsonArray.put(obj)
        }
        getPrefs(context).edit { putString("blocked_drivers_json", jsonArray.toString()) }
    }

    fun getBlockedDrivers(context: Context): List<BlockedDriver> {
        val jsonStr = getPrefs(context).getString("blocked_drivers_json", null) ?: return emptyList()
        return try {
            val list = mutableListOf<BlockedDriver>()
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    BlockedDriver(
                        id = obj.optString("id", ""),
                        name = obj.optString("name", ""),
                        carInfo = obj.optString("carInfo", ""),
                        reason = obj.optString("reason", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            listOf()
        }
    }

    // -------------------------------------------------------------
    // 5. PERSISTÊNCIA DE HISTÓRICO DE VIAGENS (JSON)
    // -------------------------------------------------------------
    fun saveTripHistory(context: Context, tripList: List<TripHistoryItem>) {
        val jsonArray = JSONArray()
        tripList.forEach { trip ->
            val obj = JSONObject().apply {
                put("date", trip.date)
                put("origin", trip.origin)
                put("destination", trip.destination)
                put("driverName", trip.driverName)
                put("driverRating", trip.driverRating)
                put("category", trip.category)
                put("driverType", trip.driverType)
                put("price", trip.price)
                put("rating", trip.rating)
                put("tip", trip.tip)
                put("payment", trip.payment)
                put("status", trip.status)
            }
            jsonArray.put(obj)
        }
        getPrefs(context).edit { putString("trip_history_json", jsonArray.toString()) }
    }

    fun getTripHistory(context: Context): List<TripHistoryItem> {
        val jsonStr = getPrefs(context).getString("trip_history_json", null) ?: return emptyList()
        return try {
            val list = mutableListOf<TripHistoryItem>()
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    TripHistoryItem(
                        date = obj.optString("date", ""),
                        origin = obj.optString("origin", ""),
                        destination = obj.optString("destination", ""),
                        driverName = obj.optString("driverName", ""),
                        driverRating = obj.optString("driverRating", ""),
                        category = obj.optString("category", ""),
                        driverType = obj.optString("driverType", ""),
                        price = obj.optString("price", ""),
                        // Campos novos: históricos antigos sem eles continuam lendo
                        rating = obj.optInt("rating", 0),
                        tip = obj.optString("tip", ""),
                        payment = obj.optString("payment", ""),
                        status = obj.optString("status", TRIP_DONE)
                    )
                )
            }
            list
        } catch (_: Exception) {
            listOf()
        }
    }

    // Acrescenta uma viagem no topo do histórico
    fun addTrip(context: Context, trip: TripHistoryItem) {
        val list = getTripHistory(context).toMutableList()
        list.add(0, trip)
        saveTripHistory(context, list)
    }

    const val TRIP_DONE = "Concluída"
    const val TRIP_CANCELED = "Cancelada"

    // Destinos recentes, sem repetição, a partir do histórico
    fun getRecentDestinations(context: Context, max: Int = 5): List<String> =
        getTripHistory(context).map { it.destination }.filter { it.isNotBlank() }.distinct().take(max)

    // -------------------------------------------------------------
    // 6. LUGARES SALVOS (Casa e Trabalho)
    // -------------------------------------------------------------
    fun saveHomeAddress(context: Context, address: String) {
        getPrefs(context).edit { putString("place_home", address) }
    }

    // Sem casa salva, usa o endereço do cadastro
    fun getHomeAddress(context: Context): String {
        val saved = getPrefs(context).getString("place_home", null)
        if (!saved.isNullOrBlank()) return saved
        val rua = getUserLogradouro(context).trim()
        val numero = getUserNumber(context).trim()
        if (rua.isEmpty() && numero.isEmpty()) return ""
        return if (numero.isEmpty()) rua else if (rua.isEmpty()) numero else "$rua, $numero"
    }

    fun saveWorkAddress(context: Context, address: String) {
        getPrefs(context).edit { putString("place_work", address) }
    }

    fun getWorkAddress(context: Context): String = getPrefs(context).getString("place_work", "") ?: ""

    // -------------------------------------------------------------
    // 7. CARTEIRA (simulada: nenhum dado de cartão é coletado)
    // -------------------------------------------------------------
    const val PAY_PIX = "Pix"
    const val PAY_CASH = "Dinheiro"
    const val PAY_CARD = "Cartão"

    fun setPaymentMethod(context: Context, method: String) {
        getPrefs(context).edit { putString("payment_method", method) }
    }

    fun getPaymentMethod(context: Context): String =
        getPrefs(context).getString("payment_method", PAY_PIX) ?: PAY_PIX

    // Cupom de demonstração: CARAMELO10 = 10% de desconto, até R$ 10
    const val DEMO_COUPON = "CARAMELO10"

    fun applyCoupon(context: Context, code: String): Boolean {
        val ok = code.trim().equals(DEMO_COUPON, ignoreCase = true)
        if (ok) getPrefs(context).edit { putString("active_coupon", DEMO_COUPON) }
        return ok
    }

    fun getActiveCoupon(context: Context): String? = getPrefs(context).getString("active_coupon", null)

    fun clearCoupon(context: Context) {
        getPrefs(context).edit { remove("active_coupon") }
    }

    // Desconto do cupom ativo sobre um valor (0 se não houver cupom)
    fun couponDiscount(context: Context, value: Float): Float =
        if (getActiveCoupon(context) == null) 0f else (value * 0.10f).coerceAtMost(10f)
}
