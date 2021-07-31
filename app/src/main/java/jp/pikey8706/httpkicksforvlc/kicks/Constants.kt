package jp.pikey8706.httpkicksforvlc.kicks

class Constants {
    companion object {
        fun init() {
            KEY_CHANNELS_TS = arrayOf(
                    KEY_CHANNEL_TS_1,
                    KEY_CHANNEL_TS_2,
                    KEY_CHANNEL_TS_3,
                    KEY_CHANNEL_TS_4,
                    KEY_CHANNEL_TS_5,
                    KEY_CHANNEL_TS_6,
                    KEY_CHANNEL_TS_7,
                    KEY_CHANNEL_TS_8,
                    KEY_CHANNEL_TS_9,
                    KEY_CHANNEL_TS_10,
                    KEY_CHANNEL_TS_11,
                    KEY_CHANNEL_TS_12,
                    KEY_CHANNEL_TS_13,
                    KEY_CHANNEL_TS_14,
                    KEY_CHANNEL_TS_15,
                    KEY_CHANNEL_TS_16,
                    KEY_CHANNEL_TS_17,
                    KEY_CHANNEL_TS_18,
                    KEY_CHANNEL_TS_19,
                    KEY_CHANNEL_TS_20,
                    KEY_CHANNEL_TS_21,
                    KEY_CHANNEL_TS_22,
                    KEY_CHANNEL_TS_23,
                    KEY_CHANNEL_TS_24,
                    KEY_CHANNEL_TS_25,
                    KEY_CHANNEL_TS_26,
                    KEY_CHANNEL_TS_27,
                    KEY_CHANNEL_TS_28,
                    KEY_CHANNEL_TS_29,
                    KEY_CHANNEL_TS_30)
            KEY_CHANNELS_BS = arrayOf(
                    KEY_CHANNEL_BS_1,
                    KEY_CHANNEL_BS_2,
                    KEY_CHANNEL_BS_3,
                    KEY_CHANNEL_BS_4,
                    KEY_CHANNEL_BS_5,
                    KEY_CHANNEL_BS_6,
                    KEY_CHANNEL_BS_7,
                    KEY_CHANNEL_BS_8,
                    KEY_CHANNEL_BS_9,
                    KEY_CHANNEL_BS_10,
                    KEY_CHANNEL_BS_11,
                    KEY_CHANNEL_BS_12,
                    KEY_CHANNEL_BS_13,
                    KEY_CHANNEL_BS_14,
                    KEY_CHANNEL_BS_15,
                    KEY_CHANNEL_BS_16,
                    KEY_CHANNEL_BS_17,
                    KEY_CHANNEL_BS_18,
                    KEY_CHANNEL_BS_19,
                    KEY_CHANNEL_BS_20,
                    KEY_CHANNEL_BS_21,
                    KEY_CHANNEL_BS_22,
                    KEY_CHANNEL_BS_23,
                    KEY_CHANNEL_BS_24,
                    KEY_CHANNEL_BS_25,
                    KEY_CHANNEL_BS_26,
                    KEY_CHANNEL_BS_27,
                    KEY_CHANNEL_BS_28,
                    KEY_CHANNEL_BS_29,
                    KEY_CHANNEL_BS_30)
            KEY_HOSTS_TS = arrayOf(
                    KEY_HOST_TS_1,
                    KEY_HOST_TS_2,
                    KEY_HOST_TS_3,
                    KEY_HOST_TS_4
            )
            KEY_HOSTS_BS = arrayOf(
                    KEY_HOST_BS_1,
                    KEY_HOST_BS_2,
                    KEY_HOST_BS_3,
                    KEY_HOST_BS_4
            )
            KEY_HOST_NAMES_TS = arrayOf(
                    KEY_HOST_NAME_TS_1,
                    KEY_HOST_NAME_TS_2,
                    KEY_HOST_NAME_TS_3,
                    KEY_HOST_NAME_TS_4
            )
            KEY_HOST_NAMES_BS = arrayOf(
                    KEY_HOST_NAME_BS_1,
                    KEY_HOST_NAME_BS_2,
                    KEY_HOST_NAME_BS_3,
                    KEY_HOST_NAME_BS_4
            )
        }

        lateinit var KEY_HOST_NAMES_BS: Array<String>
        lateinit var KEY_HOST_NAMES_TS: Array<String>
        lateinit var KEY_HOSTS_BS: Array<String>
        lateinit var KEY_HOSTS_TS: Array<String>
        lateinit var KEY_CHANNELS_BS: Array<String>
        lateinit var KEY_CHANNELS_TS: Array<String>

        /**
         * Selected host TS/BS
         */
        const val KEY_SELECTED_HOST_TS = "selected_host_ts"
        const val KEY_SELECTED_HOST_BS = "selected_host_bs"

        /**
         * Edit host key
         */
        const val KEY_EDIT_HOST = "key_edit_host"
        const val KEY_EDIT_HOST_NAME = "key_edit_host_name"
        const val KEY_EDIT_HOST_VIEW_ID = "key_edit_host_view_id"

        /**
         * Host Name save keys
         */
        const val KEY_HOST_NAME_TS_1 = "key_host_name_ts_1"
        const val KEY_HOST_NAME_TS_2 = "key_host_name_ts_2"
        const val KEY_HOST_NAME_TS_3 = "key_host_name_ts_3"
        const val KEY_HOST_NAME_TS_4 = "key_host_name_ts_4"
        const val KEY_HOST_NAME_BS_1 = "key_host_name_bs_1"
        const val KEY_HOST_NAME_BS_2 = "key_host_name_bs_2"
        const val KEY_HOST_NAME_BS_3 = "key_host_name_bs_3"
        const val KEY_HOST_NAME_BS_4 = "key_host_name_bs_4"

        /**
         * Host save keys
         */
        const val KEY_HOST_TS_1 = "key_host_ts_1"
        const val KEY_HOST_TS_2 = "key_host_ts_2"
        const val KEY_HOST_TS_3 = "key_host_ts_3"
        const val KEY_HOST_TS_4 = "key_host_ts_4"
        const val KEY_HOST_BS_1 = "key_host_bs_1"
        const val KEY_HOST_BS_2 = "key_host_bs_2"
        const val KEY_HOST_BS_3 = "key_host_bs_3"
        const val KEY_HOST_BS_4 = "key_host_bs_4"

        const val PROTOCOL_HTTP = "http://"

        /**
         * Edit channel index and value
         */
        const val INDEX_EDIT_CHANNEL = "index_edit_channel"
        const val VALUE_EDIT_CHANNEL = "value_edit_channel"

        /**
         * TS channel name id key
         */
        const val KEY_CHANNEL_TS_1 = "key_channel_ts_1"
        const val KEY_CHANNEL_TS_2 = "key_channel_ts_2"
        const val KEY_CHANNEL_TS_3 = "key_channel_ts_3"
        const val KEY_CHANNEL_TS_4 = "key_channel_ts_4"
        const val KEY_CHANNEL_TS_5 = "key_channel_ts_5"
        const val KEY_CHANNEL_TS_6 = "key_channel_ts_6"
        const val KEY_CHANNEL_TS_7 = "key_channel_ts_7"
        const val KEY_CHANNEL_TS_8 = "key_channel_ts_8"
        const val KEY_CHANNEL_TS_9 = "key_channel_ts_9"
        const val KEY_CHANNEL_TS_10 = "key_channel_ts_10"
        const val KEY_CHANNEL_TS_11 = "key_channel_ts_11"
        const val KEY_CHANNEL_TS_12 = "key_channel_ts_12"
        const val KEY_CHANNEL_TS_13 = "key_channel_ts_13"
        const val KEY_CHANNEL_TS_14 = "key_channel_ts_14"
        const val KEY_CHANNEL_TS_15 = "key_channel_ts_15"
        const val KEY_CHANNEL_TS_16 = "key_channel_ts_16"
        const val KEY_CHANNEL_TS_17 = "key_channel_ts_17"
        const val KEY_CHANNEL_TS_18 = "key_channel_ts_18"
        const val KEY_CHANNEL_TS_19 = "key_channel_ts_19"
        const val KEY_CHANNEL_TS_20 = "key_channel_ts_20"
        const val KEY_CHANNEL_TS_21 = "key_channel_ts_21"
        const val KEY_CHANNEL_TS_22 = "key_channel_ts_22"
        const val KEY_CHANNEL_TS_23 = "key_channel_ts_23"
        const val KEY_CHANNEL_TS_24 = "key_channel_ts_24"
        const val KEY_CHANNEL_TS_25 = "key_channel_ts_25"
        const val KEY_CHANNEL_TS_26 = "key_channel_ts_26"
        const val KEY_CHANNEL_TS_27 = "key_channel_ts_27"
        const val KEY_CHANNEL_TS_28 = "key_channel_ts_28"
        const val KEY_CHANNEL_TS_29 = "key_channel_ts_29"
        const val KEY_CHANNEL_TS_30 = "key_channel_ts_30"

        /**
         * BS channel name id key
         */
        const val KEY_CHANNEL_BS_1 = "key_channel_bs_1"
        const val KEY_CHANNEL_BS_2 = "key_channel_bs_2"
        const val KEY_CHANNEL_BS_3 = "key_channel_bs_3"
        const val KEY_CHANNEL_BS_4 = "key_channel_bs_4"
        const val KEY_CHANNEL_BS_5 = "key_channel_bs_5"
        const val KEY_CHANNEL_BS_6 = "key_channel_bs_6"
        const val KEY_CHANNEL_BS_7 = "key_channel_bs_7"
        const val KEY_CHANNEL_BS_8 = "key_channel_bs_8"
        const val KEY_CHANNEL_BS_9 = "key_channel_bs_9"
        const val KEY_CHANNEL_BS_10 = "key_channel_bs_10"
        const val KEY_CHANNEL_BS_11 = "key_channel_bs_11"
        const val KEY_CHANNEL_BS_12 = "key_channel_bs_12"
        const val KEY_CHANNEL_BS_13 = "key_channel_bs_13"
        const val KEY_CHANNEL_BS_14 = "key_channel_bs_14"
        const val KEY_CHANNEL_BS_15 = "key_channel_bs_15"
        const val KEY_CHANNEL_BS_16 = "key_channel_bs_16"
        const val KEY_CHANNEL_BS_17 = "key_channel_bs_17"
        const val KEY_CHANNEL_BS_18 = "key_channel_bs_18"
        const val KEY_CHANNEL_BS_19 = "key_channel_bs_19"
        const val KEY_CHANNEL_BS_20 = "key_channel_bs_20"
        const val KEY_CHANNEL_BS_21 = "key_channel_bs_21"
        const val KEY_CHANNEL_BS_22 = "key_channel_bs_22"
        const val KEY_CHANNEL_BS_23 = "key_channel_bs_23"
        const val KEY_CHANNEL_BS_24 = "key_channel_bs_24"
        const val KEY_CHANNEL_BS_25 = "key_channel_bs_25"
        const val KEY_CHANNEL_BS_26 = "key_channel_bs_26"
        const val KEY_CHANNEL_BS_27 = "key_channel_bs_27"
        const val KEY_CHANNEL_BS_28 = "key_channel_bs_28"
        const val KEY_CHANNEL_BS_29 = "key_channel_bs_29"
        const val KEY_CHANNEL_BS_30 = "key_channel_bs_30"
    }
}