package sources

object ExternalSourceRegistry {

    private val mutableSources: MutableList<IExternalSource<*>> = mutableListOf()
    val sources: List<IExternalSource<*>>
        get() = mutableSources

    init {
        register(HttpCodeSource)
        register(XkcdColorSource)
    }

    fun register(s: IExternalSource<*>) {
        mutableSources.add(s)
    }

    fun unregister(s: IExternalSource<*>) {
        mutableSources.remove(s)
    }

}