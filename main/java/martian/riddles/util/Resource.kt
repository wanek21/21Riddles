package martian.riddles.util

/* Сообщения из ошибок записываются в message, НЕ в data */
data class Resource<out T>(
    val status: Status,
    val data: T?,
    var message: Int? // android resource string id (R.string)
) {

    companion object {

        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: Int, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }

    }
}
