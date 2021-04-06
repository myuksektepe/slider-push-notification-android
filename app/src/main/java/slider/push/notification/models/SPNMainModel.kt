package slider.push.notification.models

data class SPNMainModel(
    val call_to_action: String,
    val heading: String,
    val items: List<SPNItemModel>
)