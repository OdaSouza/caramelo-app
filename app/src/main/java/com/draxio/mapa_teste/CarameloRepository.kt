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

    fun getUserName(context: Context): String = getPrefs(context).getString("user_name", "Marina Costa") ?: "Marina Costa"
    fun getUserEmail(context: Context): String = getPrefs(context).getString("user_email", "marina.costa@email.com") ?: "marina.costa@email.com"
    fun getUserCep(context: Context): String = getPrefs(context).getString("user_cep", "01310-100") ?: "01310-100"
    fun getUserLogradouro(context: Context): String = getPrefs(context).getString("user_logradouro", "Av. Paulista") ?: "Av. Paulista"
    fun getUserNumber(context: Context): String = getPrefs(context).getString("user_number", "1000") ?: "1000"
    fun getUserBairro(context: Context): String = getPrefs(context).getString("user_bairro", "Bela Vista") ?: "Bela Vista"
    fun getUserCity(context: Context): String = getPrefs(context).getString("user_city", "São Paulo") ?: "São Paulo"
    fun getUserState(context: Context): String = getPrefs(context).getString("user_state", "SP") ?: "SP"

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
        val jsonStr = getPrefs(context).getString("blocked_drivers_json", null) ?: return listOf(
            BlockedDriver("1", "João Pedro", "Honda Fit • Placa: XYZ-9876", "Direção imprudente"),
            BlockedDriver("2", "Marcos Oliveira", "Chevrolet Onix • Placa: KML-1234", "Atraso excessivo no embarque")
        )
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
            }
            jsonArray.put(obj)
        }
        getPrefs(context).edit { putString("trip_history_json", jsonArray.toString()) }
    }

    fun getTripHistory(context: Context): List<TripHistoryItem> {
        val jsonStr = getPrefs(context).getString("trip_history_json", null) ?: return listOf(
            TripHistoryItem("12 de Março, 14:30", "Localização atual", "Shopping Cidade Jardim", "Carlos Silva", "4.9 ★", "Caramelo Leve", "LGBTQIA+", "R$ 18,40"),
            TripHistoryItem("10 de Março, 09:15", "Alameda Santos, 1420", "Av. Faria Lima, 3400", "Fernanda Lima", "5.0 ★", "Caramelo Plus", "Mulher", "R$ 24,90"),
            TripHistoryItem("05 de Março, 20:00", "Parque Ibirapuera", "Rua Augusta, 500", "Lucas Almeida", "4.8 ★", "Caramelo Grupo", "Geral", "R$ 32,60"),
            TripHistoryItem("28 de Fevereiro, 11:45", "Aeroporto de Congonhas", "Alameda Santos, 1420", "Rafael Costa", "4.9 ★", "Caramelo Leve", "Homem", "R$ 38,00")
        )
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
                        price = obj.optString("price", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            listOf()
        }
    }
}
